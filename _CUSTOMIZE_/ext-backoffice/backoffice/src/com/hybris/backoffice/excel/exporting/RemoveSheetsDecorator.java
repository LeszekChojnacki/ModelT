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

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Decorator which removes given utility sheets before the workbook is downloaded by user. This decorator has almost the
 * lowest precedence, therefore sheets are removed just before sending workbook into response.
 */
public class RemoveSheetsDecorator implements ExcelExportWorkbookDecorator
{
	private Collection<ExcelTemplateConstants.UtilitySheet> sheetsToRemove;

	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		final Workbook workbook = excelExportResult.getWorkbook();
		sheetsToRemove.forEach(sheetToRemove -> {
			final int sheetIndex = workbook.getSheetIndex(sheetToRemove.getSheetName());
			workbook.removeSheetAt(sheetIndex);
		});
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE - 10;
	}

	@Required
	public void setSheetsToRemove(final Collection<ExcelTemplateConstants.UtilitySheet> sheetsToRemove)
	{
		this.sheetsToRemove = sheetsToRemove;
	}
}
