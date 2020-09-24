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
package com.hybris.backoffice.excel.exporting;

import de.hybris.platform.core.model.ItemModel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.data.ExcelExportResult;


/**
 * Default implementation of {@link ExcelExportWorkbookDecorator} supports classification attributes. The decorator uses
 * {@link com.hybris.backoffice.excel.translators.ExcelAttributeTranslatorRegistry} with collection of
 * {@link com.hybris.backoffice.excel.translators.ExcelAttributeTranslator}. This class filters out additional
 * attributes and uses just {@link ExcelClassificationAttribute}
 */
public class DefaultExcelExportClassificationWorkbookDecorator extends AbstractExcelExportWorkbookDecorator
{
	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		final Workbook workbook = excelExportResult.getWorkbook();

		final Collection<ExcelClassificationAttribute> selectedAdditionalAttributes = extractClassificationAttributes(
				excelExportResult.getSelectedAdditionalAttributes());
		final Collection<ItemModel> selectedItems = excelExportResult.getSelectedItems();
		decorate(workbook, selectedAdditionalAttributes, selectedItems);
	}

	private static List<ExcelClassificationAttribute> extractClassificationAttributes(final Collection<ExcelAttribute> attributes)
	{
		return attributes //
				.stream() //
				.filter(ExcelClassificationAttribute.class::isInstance) //
				.map(ExcelClassificationAttribute.class::cast) //
				.collect(Collectors.toList());
	}

	@Override
	public int getOrder()
	{
		return 0;
	}
}
