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
package com.hybris.backoffice.excel.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;


/**
 * Represents parsed parameters for given cell. The object consists of type code for given sheet, isoCode for localized
 * field, original cell value and list of parameters.
 */
public class ImportParameters implements Serializable
{

	public static final String RAW_VALUE = "rawValue";
	public static final String MULTIVALUE_SEPARATOR = ",";

	/**
	 * Type code of given impex;
	 */
	private final String typeCode;

	/**
	 * Iso code for localized field.
	 */
	private final String isoCode;

	/**
	 * Original cell value.
	 */
	private final Serializable cellValue;

	/**
	 * Impex reference to currently row which is currently processing.
	 */
	private final String entryRef;

	/**
	 * List of parameters.
	 */
	private final List<Map<String, String>> parameters;

	private final String formatError;

	public ImportParameters(final String typeCode, final String isoCode, final Serializable cellValue, final String entryRef,
			final String formatError)
	{
		this.typeCode = typeCode;
		this.isoCode = isoCode;
		this.cellValue = cellValue;
		this.entryRef = entryRef;
		this.formatError = formatError;
		this.parameters = Collections.emptyList();
	}

	public ImportParameters(final String typeCode, final String isoCode, final Serializable cellValue, final String entryRef,
			final List<Map<String, String>> parameters)
	{
		this.typeCode = typeCode;
		this.isoCode = isoCode;
		this.cellValue = cellValue;
		this.entryRef = entryRef;
		this.parameters = parameters;
		this.formatError = null;
	}

	public String getTypeCode()
	{
		return typeCode;
	}

	public String getIsoCode()
	{
		return isoCode;
	}

	public String getEntryRef()
	{
		return entryRef;
	}

	public Serializable getCellValue()
	{
		return cellValue;
	}

	/**
	 * @return list of maps with import parameters for multivalues field.
	 */
	public List<Map<String, String>> getMultiValueParameters()
	{
		return parameters;
	}

	/**
	 * @return first found map on the list or new map if list is empty.
	 */
	public Map<String, String> getSingleValueParameters()
	{
		return !parameters.isEmpty() ? parameters.get(0) : new HashedMap();
	}

	/**
	 * @return true if cell value is null or blank
	 */
	public boolean isCellValueBlank()
	{
		return cellValue == null || StringUtils.isBlank(cellValue.toString());
	}

	/**
	 * @return true if cell value is not null and not blank
	 */
	public boolean isCellValueNotBlank()
	{
		return !isCellValueBlank();
	}

	public boolean hasFormatErrors()
	{
		return formatError != null;
	}

	public String getFormatError()
	{
		return formatError;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final ImportParameters that = (ImportParameters) o;

		if ((typeCode != null) ? !typeCode.equals(that.typeCode) : (that.typeCode != null))
		{
			return false;
		}
		if ((isoCode != null) ? !isoCode.equals(that.isoCode) : (that.isoCode != null))
		{
			return false;
		}
		if ((cellValue != null) ? !cellValue.equals(that.cellValue) : (that.cellValue != null))
		{
			return false;
		}
		if ((entryRef != null) ? !entryRef.equals(that.entryRef) : (that.entryRef != null))
		{
			return false;
		}
		if ((parameters != null) ? !parameters.equals(that.parameters) : (that.parameters != null))
		{
			return false;
		}
		return (formatError != null) ? formatError.equals(that.formatError) : (that.formatError == null);
	}

	@Override
	public int hashCode()
	{
		int result = typeCode != null ? typeCode.hashCode() : 0;
		result = 31 * result + (isoCode != null ? isoCode.hashCode() : 0);
		result = 31 * result + (cellValue != null ? cellValue.hashCode() : 0);
		result = 31 * result + (entryRef != null ? entryRef.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		result = 31 * result + (formatError != null ? formatError.hashCode() : 0);
		return result;
	}
}
