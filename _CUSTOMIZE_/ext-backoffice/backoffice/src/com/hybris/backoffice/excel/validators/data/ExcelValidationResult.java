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
package com.hybris.backoffice.excel.validators.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class represent validation result. One validation result can contain many validation messages.
 */
public class ExcelValidationResult implements Serializable, Comparable<ExcelValidationResult>
{

	public static final ExcelValidationResult SUCCESS = new ExcelValidationResult(new ArrayList<>());

	/**
	 * Validation header. This validation message is used as a title for validation result group.
	 */
	private ValidationMessage header;

	/**
	 * List of validation messages.
	 */
	private List<ValidationMessage> validationErrors;

	/**
	 * Indicates whether validation result is connected with workbook validation. If list of {@link ExcelValidationResult}
	 * contains any results with workbookValidationResult=true then validation of
	 * {@link com.hybris.backoffice.excel.importing.AbstractExcelImportWorkbookDecorator} won't be invoked.
	 */
	private boolean workbookValidationResult;

	public ExcelValidationResult(final ValidationMessage header, final List<ValidationMessage> validationErrors)
	{
		this.header = header;
		this.validationErrors = validationErrors;
	}

	public ExcelValidationResult(final List<ValidationMessage> validationErrors)
	{
		this.validationErrors = validationErrors;
	}

	public ExcelValidationResult(final ValidationMessage validationError)
	{
		this.validationErrors = new ArrayList<>();
		validationErrors.add(validationError);
	}

	public void addValidationError(final ValidationMessage validationMessage)
	{
		validationErrors.add(validationMessage);
	}

	public boolean hasErrors()
	{
		return !validationErrors.isEmpty();
	}

	public ValidationMessage getHeader()
	{
		return header;
	}

	public void setHeader(final ValidationMessage header)
	{
		this.header = header;
	}

	public List<ValidationMessage> getValidationErrors()
	{
		return validationErrors;
	}

	public void setValidationErrors(final List<ValidationMessage> validationErrors)
	{
		this.validationErrors = validationErrors;
	}

	public boolean isWorkbookValidationResult()
	{
		return workbookValidationResult;
	}

	public void setWorkbookValidationResult(final boolean workbookValidationResult)
	{
		this.workbookValidationResult = workbookValidationResult;
	}

	@Override
	public int compareTo(final ExcelValidationResult another)
	{
		return header.compareTo(another.getHeader());
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}

		final ExcelValidationResult that = (ExcelValidationResult) o;

		if (header != null ? !header.equals(that.header) : (that.header != null))
		{
			return false;
		}
		return validationErrors != null ? validationErrors.equals(that.validationErrors) : (that.validationErrors == null);
	}

	@Override
	public int hashCode()
	{
		int result = header != null ? header.hashCode() : 0;
		result = 31 * result + (validationErrors != null ? validationErrors.hashCode() : 0);
		return result;
	}
}
