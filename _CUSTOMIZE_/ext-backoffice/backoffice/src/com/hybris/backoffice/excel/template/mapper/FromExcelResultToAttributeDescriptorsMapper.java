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
package com.hybris.backoffice.excel.template.mapper;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.template.sheet.ExcelSheetService;


/**
 * Allows to map {@link ExcelExportResult} to Collection<{@link AttributeDescriptorModel}>
 */
public class FromExcelResultToAttributeDescriptorsMapper implements ToAttributeDescriptorsMapper<ExcelExportResult>
{
	private ExcelMapper<String, AttributeDescriptorModel> mapper;
	private ExcelSheetService excelSheetService;
	private Collection<ExcelFilter<AttributeDescriptorModel>> filters;

	@Override
	public Collection<AttributeDescriptorModel> apply(final ExcelExportResult excelExportResult)
	{
		final Predicate<String> isNotUtilitySheet = sheetName -> Stream.of(ExcelTemplateConstants.UtilitySheet.values()) //
				.map(ExcelTemplateConstants.UtilitySheet::getSheetName) //
				.noneMatch(utilitySheetName -> StringUtils.equals(utilitySheetName, sheetName));

		return IntStream //
				.range(0, excelExportResult.getWorkbook().getNumberOfSheets()) //
				.mapToObj(excelExportResult.getWorkbook()::getSheetName) //
				.filter(isNotUtilitySheet) //
				.map(sheetName -> excelSheetService.findTypeCodeForSheetName(excelExportResult.getWorkbook(), sheetName)) //
				.map(mapper)//
				.flatMap(Collection::stream) //
				.distinct() //
				.filter(attribute -> filter(attribute, filters)) //
				.collect(Collectors.toList());
	}


	@Required
	public void setExcelSheetService(final ExcelSheetService excelSheetService)
	{
		this.excelSheetService = excelSheetService;
	}

	@Required
	public void setMapper(final ExcelMapper<String, AttributeDescriptorModel> mapper)
	{
		this.mapper = mapper;
	}

	// optional
	public void setFilters(final Collection<ExcelFilter<AttributeDescriptorModel>> filters)
	{
		this.filters = filters;
	}
}
