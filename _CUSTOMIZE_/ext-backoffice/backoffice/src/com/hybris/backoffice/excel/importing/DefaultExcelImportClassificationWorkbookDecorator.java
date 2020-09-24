/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.backoffice.excel.importing;

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.classification.ClassificationSystemService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.classification.ExcelClassificationAttributeFactory;
import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.importing.data.ClassificationTypeSystemRow;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


public class DefaultExcelImportClassificationWorkbookDecorator extends AbstractExcelImportWorkbookDecorator
{


	private ExcelClassificationTypeSystemService excelClassificationTypeSystemService;
	private ClassificationSystemService classificationSystemService;
	private ExcelClassificationAttributeFactory excelClassificationAttributeFactory;


	@Override
	protected Collection<ExcelAttribute> getExcelAttributes(final Sheet sheet)
	{
		final Collection<ExcelAttribute> attributes = new ArrayList<>();
		final ExcelClassificationTypeSystemService.ExcelClassificationTypeSystem typeSystem = getExcelClassificationTypeSystemService()
				.loadTypeSystem(sheet.getWorkbook());
		final Map<String, ClassificationSystemVersionModel> versionCache = new HashMap<>();
		final Map<String, ClassificationClassModel> classCache = new HashMap<>();
		final Row headerRow = sheet.getRow(ExcelTemplateConstants.HEADER_ROW_INDEX);
		for (int columnIndex = headerRow.getFirstCellNum(); columnIndex <= headerRow.getLastCellNum(); columnIndex++)
		{
			final String header = getExcelCellService().getCellValue(headerRow.getCell(columnIndex));
			final Optional<ClassificationTypeSystemRow> row = typeSystem.findRow(header);
			if (row.isPresent())
			{
				final ClassificationTypeSystemRow classificationTypeSystemRow = row.get();
				final ClassificationSystemVersionModel classificationSystemVersionModel = getClassificationSystemVersionModel(
						versionCache, classificationTypeSystemRow);
				final ClassificationClassModel classificationClassModel = getClassificationClassModel(classCache,
						classificationTypeSystemRow, classificationSystemVersionModel);
				final List<ClassAttributeAssignmentModel> assignments = classificationClassModel
						.getDeclaredClassificationAttributeAssignments();
				if (CollectionUtils.isNotEmpty(assignments))
				{
					assignments.stream()
							.filter(assignment -> classificationTypeSystemRow.getClassificationAttribute()
									.equals(assignment.getClassificationAttribute().getCode()))
							.findFirst().ifPresent(assignment -> attributes.add(
									excelClassificationAttributeFactory.create(assignment, classificationTypeSystemRow.getIsoCode())));
				}
			}
		}
		return attributes;
	}

	private ClassificationSystemVersionModel getClassificationSystemVersionModel(
			final Map<String, ClassificationSystemVersionModel> versionCache,
			final ClassificationTypeSystemRow classificationTypeSystemRow)
	{
		final String versionKey = String.format("%s:%s", classificationTypeSystemRow.getClassificationSystem(),
				classificationTypeSystemRow.getClassificationVersion());
		return versionCache.computeIfAbsent(versionKey,
				key -> getClassificationSystemService().getSystemVersion(classificationTypeSystemRow.getClassificationSystem(),
						classificationTypeSystemRow.getClassificationVersion()));
	}

	private ClassificationClassModel getClassificationClassModel(final Map<String, ClassificationClassModel> classCache,
			final ClassificationTypeSystemRow classificationTypeSystemRow,
			final ClassificationSystemVersionModel classificationSystemVersionModel)
	{
		final String classKey = String.format("%s:%s:%s", classificationTypeSystemRow.getClassificationSystem(),
				classificationTypeSystemRow.getClassificationVersion(), classificationTypeSystemRow.getClassificationClass());
		return classCache.computeIfAbsent(classKey, key -> getClassificationSystemService()
				.getClassForCode(classificationSystemVersionModel, classificationTypeSystemRow.getClassificationClass()));
	}

	public ClassificationSystemService getClassificationSystemService()
	{
		return classificationSystemService;
	}

	@Required
	public void setClassificationSystemService(final ClassificationSystemService classificationSystemService)
	{
		this.classificationSystemService = classificationSystemService;
	}

	@Required
	public void setExcelClassificationAttributeFactory(
			final ExcelClassificationAttributeFactory excelClassificationAttributeFactory)
	{
		this.excelClassificationAttributeFactory = excelClassificationAttributeFactory;
	}

	public ExcelClassificationTypeSystemService getExcelClassificationTypeSystemService()
	{
		return excelClassificationTypeSystemService;
	}

	@Required
	public void setExcelClassificationTypeSystemService(
			final ExcelClassificationTypeSystemService excelClassificationTypeSystemService)
	{
		this.excelClassificationTypeSystemService = excelClassificationTypeSystemService;
	}
}
