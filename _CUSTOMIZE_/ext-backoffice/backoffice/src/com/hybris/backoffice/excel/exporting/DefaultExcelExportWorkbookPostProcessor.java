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

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelExportResult;


/**
 * Default implementation of {@link ExcelExportWorkbookPostProcessor}. It is aggregator for
 * {@link ExcelExportWorkbookDecorator}s.
 */
public class DefaultExcelExportWorkbookPostProcessor implements ExcelExportWorkbookPostProcessor
{

	private List<ExcelExportWorkbookDecorator> decorators;

	/**
	 * Delegates decorating process to collection of {@link ExcelExportWorkbookDecorator}s which can be set using
	 * {@link #setDecorators(List)}
	 *
	 * @param excelExportResult
	 *           - object represents exporting result. The object consists of exported
	 *           {@link org.apache.poi.ss.usermodel.Workbook}, selected attributes, additional attributes and exported
	 */
	@Override
	public void process(final ExcelExportResult excelExportResult)
	{
		decorators.forEach(decorator -> decorator.decorate(excelExportResult));
	}

	public List<ExcelExportWorkbookDecorator> getDecorators()
	{
		return decorators;
	}

	@Required
	public void setDecorators(final List<ExcelExportWorkbookDecorator> decorators)
	{
		if (decorators != null)
		{
			OrderComparator.sort(decorators);
		}
		this.decorators = decorators;
	}
}
