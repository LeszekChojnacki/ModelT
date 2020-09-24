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

import de.hybris.platform.validation.enums.Severity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.hybris.backoffice.excel.template.ExcelTemplateConstants;


/**
 * Class which represent validation message. The validation message should consist of messageKey which is key for
 * localized message, list of params for localized message and metadata which can store any information.
 */
public class ValidationMessage implements Serializable, Comparable<ValidationMessage>
{

	private Severity severity;
	private final String messageKey;
	private final Serializable[] params;
	private final transient Map<String, Object> metadata;

	public ValidationMessage(final String messageKey, final Serializable... params)
	{
		this.messageKey = messageKey;
		this.params = params;
		this.metadata = new HashMap<>();
		this.severity = Severity.ERROR;
	}

	public ValidationMessage(final String messageKey)
	{
		this.messageKey = messageKey;
		this.params = new Serializable[0];
		this.metadata = new HashMap<>();
		this.severity = Severity.ERROR;
	}

	public ValidationMessage(final String messageKey, final Severity severity)
	{
		this(messageKey);
		this.severity = severity;
	}

	public ValidationMessage(final String messageKey, final Severity severity, final Serializable... params)
	{
		this(messageKey, params);
		this.severity = severity;
	}

	public String getMessageKey()
	{
		return messageKey;
	}

	public Serializable[] getParams()
	{
		return params;
	}


	public void addMetadata(final String key, final Object value)
	{
		metadata.put(key, value);
	}

	public void addMetadataIfAbsent(final String key, final Object value)
	{
		metadata.putIfAbsent(key, value);
	}

	public Object getMetadata(final String key)
	{
		return metadata.get(key);
	}

	public boolean containsMetadata(final String key)
	{
		return metadata.containsKey(key);
	}

	public Severity getSeverity()
	{
		return severity;
	}

	@Override
	public int compareTo(final ValidationMessage another)
	{
		final Integer messageKeyComparision = compareMessageKeys(another);
		if (messageKeyComparision != 0)
		{
			return messageKeyComparision;
		}

		final int sheetNameComparision = compareSheetNames(another);
		if (sheetNameComparision != 0)
		{
			return sheetNameComparision;
		}

		return compareRows(another);
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

		final ValidationMessage that = (ValidationMessage) o;

		if (messageKey != null ? !messageKey.equals(that.messageKey) : (that.messageKey != null))
		{
			return false;
		}
		return Arrays.equals(params, that.params) && (metadata != null ? metadata.equals(that.metadata) : (that.metadata == null));
	}

	@Override
	public int hashCode()
	{
		int result = messageKey != null ? messageKey.hashCode() : 0;
		result = 31 * result + Arrays.hashCode(params);
		result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
		return result;
	}

	private int compareMessageKeys(final ValidationMessage another)
	{
		if (messageKey == null)
		{
			return 1;
		}

		if (another.messageKey == null)
		{
			return -1;
		}

		return messageKey.compareTo(another.messageKey);
	}

	private int compareSheetNames(final ValidationMessage another)
	{
		if (!containsMetadata((ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY)))
		{
			return 1;
		}
		if (!another.containsMetadata((ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY)))
		{
			return -1;
		}
		final String firstSheetName = (String) getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY);
		final String secondSheetName = (String) another
				.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.SHEET_NAME_KEY);

		return firstSheetName.compareTo(secondSheetName);
	}

	private int compareRows(final ValidationMessage another)
	{
		if (!containsMetadata((ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY)))
		{
			return 1;
		}
		if (!another.containsMetadata((ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY)))
		{
			return -1;
		}
		final Integer firstRowIndex = (Integer) getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY);
		final Integer secondRowIndex = (Integer) another
				.getMetadata(ExcelTemplateConstants.ValidationMessageMetadata.ROW_INDEX_KEY);

		return firstRowIndex.compareTo(secondRowIndex);
	}
}
