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

import javax.annotation.Nonnull;

import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.ExcelExportParams;


/**
 * Excel decorator used by {@link ExcelExportPreProcessor} mechanism. Each decorator can modify the input of export
 * process.
 */
public interface ExcelExportParamsDecorator extends Ordered
{
	/**
	 * Decorates export input {@link ExcelExportParams} object.
	 *
	 * @param excelExportParams
	 *           (depending on the order of decorator) an input for the Excel export process or the result of previous
	 *           decorator.
	 * @see #getOrder()
	 */
	@Nonnull
	ExcelExportParams decorate(@Nonnull ExcelExportParams excelExportParams);

	@Override
	int getOrder();
}
