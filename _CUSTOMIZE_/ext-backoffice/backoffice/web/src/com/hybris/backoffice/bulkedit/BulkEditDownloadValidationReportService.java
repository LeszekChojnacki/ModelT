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
package com.hybris.backoffice.bulkedit;

import java.util.List;


/**
 * Service which allows to download validation report file
 */
public interface BulkEditDownloadValidationReportService
{
	/**
	 * Triggers downloading of report file.
	 *
	 * @param validationResults
	 *           source of the content of the generated file.
	 */
	void downloadReport(final List<ValidationResult> validationResults);
}
