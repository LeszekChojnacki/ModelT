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
package com.hybris.backoffice.excel.importing;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Workbook;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Service responsible for generating {@link Impex} based on excel file represented by {@link Workbook} object. In order
 * to generating impex script, we can pass the result of {#importData} to {@link ImpexConverter#convert(Impex)}.
 */
public interface ExcelImportService
{

	/**
	 * Transforms excel workbook into {@link Impex} object. It takes into account all sheets included in excel file.
	 *
	 * @param workbook
	 *           {@link Workbook} object which represents excel file
	 * @return generated {@link Impex} object which represents data from the workbook
	 */
	Impex convertToImpex(final Workbook workbook);

	/**
	 * Validates a workbook.
	 * 
	 * @param workbook
	 *           workbook to validate.
	 * @return list of validation results.
	 * @deprecated since 6.7 please use {@link #validate(Workbook, Set)}
	 */
	@Deprecated
	List<ExcelValidationResult> validate(final Workbook workbook);

	/**
	 * Validates a workbook.
	 *
	 * @param workbook
	 *           workbook to validate.
	 * @param mediaContentEntries
	 *           set of entries names inside media content attached to the excel file. Null if media is not attached.
	 * @return list of validation results.
	 */
	default List<ExcelValidationResult> validate(final Workbook workbook, @Nullable final Set<String> mediaContentEntries)
	{
		return validate(workbook);
	}

}
