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
package com.hybris.backoffice.excel.imp.wizard;

import java.util.Collection;

import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;


/**
 * Service which allows to download validation report file
 */
public interface ExcelDownloadReportService
{
	/**
	 * Triggers downloading of report file.
	 *
	 * @param excelValidationResults
	 *           source of the content of the generated file.
	 */
	void downloadReport(final Collection<ExcelValidationResult> excelValidationResults);
}
