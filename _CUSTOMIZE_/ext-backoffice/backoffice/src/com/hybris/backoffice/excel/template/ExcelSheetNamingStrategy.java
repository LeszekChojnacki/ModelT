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
package com.hybris.backoffice.excel.template;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excel has restriction on sheet name length. The name cannot exceed 31 chars.
 * {@link ExcelSheetNamingStrategy} tries to truncate typed to 31 chars.
 */
public interface ExcelSheetNamingStrategy
{
	/**
	 * Generates sheet name which has max 31 chars.
	 * @param workbook
	 * @param typeCode
	 * @return generated sheet name
	 */
	String generateName(final Workbook workbook, final String typeCode);
}
