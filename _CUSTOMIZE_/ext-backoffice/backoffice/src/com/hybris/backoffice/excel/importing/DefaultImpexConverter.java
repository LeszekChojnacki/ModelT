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

import de.hybris.platform.util.CSVConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImpexForType;
import com.hybris.backoffice.excel.data.ImpexHeaderValue;


/**
 * Default service responsible for generating impex script based on {@link Impex} object.
 */
public class DefaultImpexConverter implements ImpexConverter
{

	public static final String IMPEX_OPERATION_TYPE = "INSERT_UPDATE ";
	public static final String DEFAULT_FIELD_SEPARATOR = String.valueOf(CSVConstants.HYBRIS_FIELD_SEPARATOR);
	public static final String DEFAULT_LINE_SEPARATOR = String.valueOf(CSVConstants.HYBRIS_LINE_SEPARATOR);

	@Override
	public String convert(final Impex impex)
	{
		final StringBuilder sb = new StringBuilder();

		for (final ImpexForType impexForType : impex.getImpexes())
		{
			sb.append(prepareImpexHeader(impexForType));
			sb.append(prepareImpexRows(impexForType));
			sb.append(DEFAULT_LINE_SEPARATOR).append(DEFAULT_LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Generates String which contains many lines represent rows for impex scripts. Lines which does not contain all unique
	 * attributes are omitted.
	 *
	 * @param impexForType
	 *           {@link ImpexForType}
	 * @return multi-lines String represents rows of impex scripts
	 */
	protected String prepareImpexRows(final ImpexForType impexForType)
	{
		final Set<String> rows = new HashSet<>();
		for (final Integer index : impexForType.getImpexTable().rowKeySet())
		{
			final StringBuilder sb = new StringBuilder(DEFAULT_FIELD_SEPARATOR);
			final Map<ImpexHeaderValue, Object> row = impexForType.getImpexTable().row(index);
			if (areUniqueAttributesPopulated(row))
			{
				impexForType.getImpexTable().columnKeySet()
						.forEach(header -> sb.append(getValue(row.get(header))).append(DEFAULT_FIELD_SEPARATOR));
				rows.add(sb.toString());
			}
		}
		return String.join(DEFAULT_LINE_SEPARATOR, rows);
	}

	/**
	 * Returns escaped string value. If value contains semicolon, coma, new lines then value is wrapped in double quotes. If
	 * the value is null then empty string will be returned.
	 *
	 * @param value
	 *           which should be converted to string and escaped if needed
	 * @return escaped value
	 */
	protected String getValue(final Object value)
	{
		if (value != null)
		{
			final String valueAsString = value.toString();
			if (valueAsString.contains(DEFAULT_FIELD_SEPARATOR))
			{
				final String escapedValue = StringEscapeUtils.escapeCsv(valueAsString);
				return !escapedValue.startsWith("\"") ? String.format("\"%s\"", escapedValue) : escapedValue;
			}
			return StringEscapeUtils.escapeCsv(valueAsString);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Returns true when all values indicated as unique have not empty value.
	 * 
	 * @param row
	 *           maps contains {@link ImpexHeaderValue} as keys and {@link Object} as values.
	 * @return true when all values indicated as unique have not empty value.
	 */
	protected boolean areUniqueAttributesPopulated(final Map<ImpexHeaderValue, Object> row)
	{
		for (final Map.Entry<ImpexHeaderValue, Object> entry : row.entrySet())
		{
			if (entry.getKey().isUnique() && entry.getKey().isMandatory() && isEmpty(entry))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean isEmpty(final Map.Entry<ImpexHeaderValue, Object> entry)
	{
		return entry.getValue() != null && StringUtils.isBlank(entry.getValue().toString());
	}

	/**
	 * Prepares first row of impex script for given type. Example result: INSERT_UPDATE
	 * Product;code[unique=true];name[lang=en];
	 *
	 * @param impexForType
	 *           {@link ImpexForType}
	 * @return first row of impex script for given type.
	 */
	protected String prepareImpexHeader(final ImpexForType impexForType)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(IMPEX_OPERATION_TYPE).append(impexForType.getTypeCode()).append(DEFAULT_FIELD_SEPARATOR);
		impexForType.getImpexTable().columnKeySet()
				.forEach(attr -> sb.append(prepareHeaderAttribute(attr)).append(DEFAULT_FIELD_SEPARATOR));
		sb.append(DEFAULT_LINE_SEPARATOR);
		return sb.toString();
	}

	/**
	 * Prepares single header value of impex. Example outputs: code[unique=true], name[unique=true, lang=en]
	 * 
	 * @param headerAttribute
	 *           {@link ImpexHeaderValue} consists of header name, language and indicator whether attribute is unique
	 * @return represents single header value
	 */
	protected String prepareHeaderAttribute(final ImpexHeaderValue headerAttribute)
	{
		final List<String> headerAttributes = new ArrayList<>();
		if (headerAttribute.isUnique())
		{
			headerAttributes.add("unique=true");
		}
		if (StringUtils.isNotBlank(headerAttribute.getLang()))
		{
			headerAttributes.add(String.format("lang=%s", headerAttribute.getLang()));
		}
		if (StringUtils.isNotBlank(headerAttribute.getDateFormat()))
		{
			headerAttributes.add(String.format("dateformat=%s", headerAttribute.getDateFormat()));
		}
		if (StringUtils.isNotBlank(headerAttribute.getTranslator()))
		{
			headerAttributes.add(String.format("translator=%s", headerAttribute.getTranslator()));
		}
		return headerAttributes.isEmpty() ? headerAttribute.getName()
				: String.format("%s[%s]", headerAttribute.getName(), String.join(",", headerAttributes));
	}

}
