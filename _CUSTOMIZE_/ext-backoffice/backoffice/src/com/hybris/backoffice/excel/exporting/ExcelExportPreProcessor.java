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

import com.hybris.backoffice.excel.data.ExcelExportParams;


/**
 * Backoffice Excel mechanism dedicated to pre-processing exporting process -- just before exporting process. It can
 * modify the input of export.
 */
public interface ExcelExportPreProcessor
{
	/**
	 * Allows to modify input for export {@link ExcelExportParams} by adding new content or removing it.
	 *
	 * @param excelExportParams
	 *           - object that will be used at the beginning of export process
	 */
	@Nonnull
	ExcelExportParams process(@Nonnull ExcelExportParams excelExportParams);
}
