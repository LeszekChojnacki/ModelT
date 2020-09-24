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

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelExportParams;


/**
 * Default {@link ExcelExportPreProcessor} that uses decorators injected via {@link #setDecorators(List)}. The
 * decorators are used by order parameter. The results of all decorators are being aggregated meaning the output of a
 * previous decorator will be an input for the next decorator.
 *
 * @see ExcelExportPreProcessor
 * @see org.springframework.core.Ordered
 */
public class DefaultExcelExportPreProcessor implements ExcelExportPreProcessor
{
	private List<ExcelExportParamsDecorator> decorators = new LinkedList<>();

	@Override
	public @Nonnull ExcelExportParams process(final @Nonnull ExcelExportParams excelExportParams)
	{
		ExcelExportParams accumulatedParams = excelExportParams;
		for (final ExcelExportParamsDecorator decorator : decorators)
		{
			accumulatedParams = decorator.decorate(accumulatedParams);
		}
		return accumulatedParams;
	}

	// optional
	public void setDecorators(final List<ExcelExportParamsDecorator> decorators)
	{
		this.decorators = decorators;
		OrderComparator.sort(this.decorators);
	}

	public List<ExcelExportParamsDecorator> getDecorators()
	{
		return decorators;
	}
}
