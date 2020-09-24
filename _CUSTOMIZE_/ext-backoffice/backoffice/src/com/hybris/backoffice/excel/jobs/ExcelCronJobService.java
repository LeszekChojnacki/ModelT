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
package com.hybris.backoffice.excel.jobs;

import com.hybris.backoffice.model.ExcelImportCronJobModel;


/**
 * Cron job service responsible for uploading excel file as a media and creating and running cron job for importing
 * excel data.
 */
public interface ExcelCronJobService
{

	/**
	 * Uploads excel file as a media, creates cron job and runs it.
	 * 
	 * @param excelFile
	 *           - object represents excel file
	 * @param referencedContentFile
	 *           - object represents content zip file
	 * @return {@link ExcelImportCronJobModel}
	 */
	ExcelImportCronJobModel createImportJob(final FileContent excelFile, final FileContent referencedContentFile);
}
