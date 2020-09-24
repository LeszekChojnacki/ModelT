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

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.mapper.ExcelMapper;
import com.hybris.backoffice.excel.template.populator.ExcelSheetPopulator;


/**
 * Implementation of {@link ExcelExportWorkbookDecorator}, supports classification attributes. The decorator prepares
 * data before delegating population process of Header Prompt hidden sheet.
 */
public class HeaderPromptWorkbookDecorator implements ExcelExportWorkbookDecorator
{

	private ExcelMapper<ExcelExportResult, ExcelAttributeDescriptorAttribute> mapper;
	private ExcelSheetPopulator headerPromptPopulator;

	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		final Collection<ExcelAttribute> attributes = CollectionUtils.union(excelExportResult.getAvailableAdditionalAttributes(),
				mapper.apply(excelExportResult));

		final ExcelExportResult result = new ExcelExportResult(excelExportResult.getWorkbook(),
				excelExportResult.getSelectedItems(), excelExportResult.getSelectedAttributes(),
				excelExportResult.getSelectedAdditionalAttributes(), attributes);

		getHeaderPromptPopulator().populate(result);
	}

	@Override
	public int getOrder()
	{
		return 1000;
	}

	public ExcelSheetPopulator getHeaderPromptPopulator()
	{
		return headerPromptPopulator;
	}

	@Required
	public void setHeaderPromptPopulator(final ExcelSheetPopulator headerPromptPopulator)
	{
		this.headerPromptPopulator = headerPromptPopulator;
	}

	public ExcelMapper<ExcelExportResult, ExcelAttributeDescriptorAttribute> getMapper()
	{
		return mapper;
	}

	@Required
	public void setMapper(final ExcelMapper<ExcelExportResult, ExcelAttributeDescriptorAttribute> mapper)
	{
		this.mapper = mapper;
	}
}
