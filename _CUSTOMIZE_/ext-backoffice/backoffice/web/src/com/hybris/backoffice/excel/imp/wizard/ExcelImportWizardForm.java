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

import java.util.HashSet;
import java.util.Set;

import com.hybris.cockpitng.editor.defaultfileupload.FileUploadResult;


/**
 * Pojo for excel import
 */
public class ExcelImportWizardForm
{
	private FileUploadResult excelFile;
	private FileUploadResult zipFile;

	private Set<FileUploadResult> fileUploadResult = new HashSet<>();


	public FileUploadResult getExcelFile()
	{
		return excelFile;
	}

	public void setExcelFile(final FileUploadResult excelFile)
	{
		this.excelFile = excelFile;
	}

	public FileUploadResult getZipFile()
	{
		return zipFile;
	}

	public void setZipFile(final FileUploadResult zipFile)
	{
		this.zipFile = zipFile;
	}

	/**
	 * @deprecated since 6.7 use separate methods for zip and excel file
	 */
	@Deprecated
	public Set<FileUploadResult> getFileUploadResult()
	{
		return fileUploadResult;
	}

	/**
	 * @deprecated since 6.7 use separate methods for zip {@link #getZipFile()} and excel file {@link #getExcelFile()}
	 */
	@Deprecated
	public void setFileUploadResult(final Set<FileUploadResult> fileUploadResult)
	{
		this.fileUploadResult = fileUploadResult;
	}

}
