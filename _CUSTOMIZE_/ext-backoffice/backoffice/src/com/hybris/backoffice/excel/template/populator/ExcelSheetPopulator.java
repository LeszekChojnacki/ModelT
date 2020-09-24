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
package com.hybris.backoffice.excel.template.populator;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelExportResult;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Populates sheet with additional information about exported objects, like type or classification. Most of the
 * implementation use {@link ExcelFilter}s to limit populated information in cases the user has no access to given type
 * or attribute is not readable.
 *
 * @see ExcelFilter
 */
@FunctionalInterface
public interface ExcelSheetPopulator
{
	/**
	 * Populates a sheet with values retrieved from {@link ExcelExportResult}
	 *
	 * @param excelExportResult
	 *           a pojo which contains list of selected items, list of selected and available attributes etc.
	 */
	void populate(@Nonnull ExcelExportResult excelExportResult);
}
