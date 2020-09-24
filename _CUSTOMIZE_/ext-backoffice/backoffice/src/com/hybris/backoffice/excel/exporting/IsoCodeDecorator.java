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

import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.workbook.ExcelWorkbookService;


/**
 * Decorator puts to the {@link org.apache.poi.ss.usermodel.Workbook} information about the language of the exported
 * data
 */
public class IsoCodeDecorator implements ExcelExportWorkbookDecorator
{
	private int order = 10_000;
	private CommonI18NService commonI18NService;
	private ExcelWorkbookService excelWorkbookService;

	@Override
	public void decorate(final ExcelExportResult excelExportResult)
	{
		excelWorkbookService.addProperty(excelExportResult.getWorkbook(), "isoCode",
				commonI18NService.getCurrentLanguage().getIsocode());
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setExcelWorkbookService(final ExcelWorkbookService excelWorkbookService)
	{
		this.excelWorkbookService = excelWorkbookService;
	}

	// optional
	public void setOrder(final int order)
	{
		this.order = order;
	}
}
