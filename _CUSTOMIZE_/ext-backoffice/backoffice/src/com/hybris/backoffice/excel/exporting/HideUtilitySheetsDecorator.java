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

import de.hybris.platform.util.Config;

import java.util.Collection;

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Hides {@link ExcelTemplateConstants.UtilitySheet}s passed to decorator using {@link #setUtilitySheets(Collection)}.
 * The hidden level can be configured using {@value CONFIG_HIDDEN_SHEETS} - passing true won't allow to unhide hidden
 * sheet of excel file.
 */
public class HideUtilitySheetsDecorator implements ExcelExportWorkbookDecorator
{
	private static final String CONFIG_HIDDEN_SHEETS = "backoffice.excel.utility.sheets.hidden";
	private Collection<ExcelTemplateConstants.UtilitySheet> utilitySheets;

	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		utilitySheets.forEach(utilitySheet -> hideUtilitySheet(excelExportResult.getWorkbook(), utilitySheet.getSheetName()));
	}

	protected void hideUtilitySheet(final Workbook workbook, final String sheetName)
	{
		final int sheetIndex = workbook.getSheetIndex(sheetName);

		if (!workbook.isSheetHidden(sheetIndex) || workbook.getSheetAt(sheetIndex).isSelected())
		{
			activateFirstNonUtilitySheet(workbook);
			workbook.getSheetAt(sheetIndex).setSelected(false);
			workbook.setSheetVisibility(sheetIndex, getUtilitySheetHiddenLevel());
		}
	}

	protected SheetVisibility getUtilitySheetHiddenLevel()
	{
		return Config.getBoolean(CONFIG_HIDDEN_SHEETS, true) ? //
				SheetVisibility.VERY_HIDDEN : SheetVisibility.HIDDEN;
	}

	protected void activateFirstNonUtilitySheet(final Workbook workbook)
	{
		if (ExcelTemplateConstants.UtilitySheet.isUtilitySheet(utilitySheets,
				workbook.getSheetName(workbook.getActiveSheetIndex())))
		{
			for (int i = 0; i < workbook.getNumberOfSheets(); i++)
			{
				if (!ExcelTemplateConstants.UtilitySheet.isUtilitySheet(utilitySheets, workbook.getSheetName(i)))
				{
					workbook.setActiveSheet(i);
				}
			}
		}
	}

	@Override
	public int getOrder()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Required
	public void setUtilitySheets(final Collection<ExcelTemplateConstants.UtilitySheet> utilitySheets)
	{
		this.utilitySheets = utilitySheets;
	}
}
