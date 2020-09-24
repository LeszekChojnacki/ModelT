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

import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.excel.util.ExcelUtils;


/**
 * Represents selected attribute which should be exported/imported. The object consists of isoCode for localized field,
 * getReferenceFormat for reference, defaultValues provided by user in excel sheet and attribute descriptor for given
 * attribute.
 */
public class SelectedAttribute implements Comparable<SelectedAttribute>
{
	/**
	 * @deprecated since 1808. Use
	 *             {@link com.hybris.backoffice.excel.template.ExcelTemplateConstants#REFERENCE_PATTERN_SEPARATOR} instead.
	 */
	@Deprecated
	public static final String REFERENCE_PATTERN_SEPARATOR = ":";
	/**
	 * Information about language for localized field.
	 */
	private String isoCode;

	/**
	 * Expected format for references. For example, catalog:version
	 */
	private String referenceFormat;

	/**
	 * Default values provided by user in the third row of sheet.
	 */
	private String defaultValues;

	/**
	 * Attribute descriptor for given selected attribute.
	 */
	private AttributeDescriptorModel attributeDescriptor;

	public SelectedAttribute()
	{
	}

	public SelectedAttribute(final AttributeDescriptorModel attributeDescriptor)
	{
		this(null, attributeDescriptor);
	}

	public SelectedAttribute(final String isoCode, final AttributeDescriptorModel attributeDescriptor)
	{
		this(isoCode, null, null, attributeDescriptor);
	}

	public SelectedAttribute(final String isoCode, final String referenceFormat, final String defaultValues,
			final AttributeDescriptorModel attributeDescriptor)
	{
		this.isoCode = isoCode;
		this.referenceFormat = referenceFormat;
		this.defaultValues = defaultValues;
		this.attributeDescriptor = attributeDescriptor;
	}

	/**
	 * Parses referencePattern cell (which is located in the second row) and default values cell (which is located in the
	 * third row) and creates map where key is equals to reference key and value is equals to value provided in the third
	 * row. For example, for the following reference cell's value: catalog:version and default value cell: Defailt:Online
	 * the following map will be returned: {{key: catalog, value: Default}, {key: version, value: Online}}
	 *
	 * @deprecated since 1808. Use
	 *             {@link com.hybris.backoffice.excel.importing.parser.DefaultImportParameterParser#parseDefaultValues(String, String)}
	 *             instead
	 * @return Map of default values. If default values are not provided then only keys will be returned.
	 */
	@Deprecated
	public Map<String, String> findDefaultValues()
	{
		final Map<String, String> defaultValuesMap = new LinkedHashMap<>();
		final String rawDefaultValues = defaultValues != null ? defaultValues : "";

		if (StringUtils.isBlank(referenceFormat))
		{
			return defaultValuesMap;
		}
		final String[] referenceFormatTokens = ExcelUtils.extractExcelCellTokens(referenceFormat);
		final String[] defaultValuesTokens = ExcelUtils.extractExcelCellTokens(rawDefaultValues);

		for (int i = 0; i < referenceFormatTokens.length; i++)
		{
			final String referenceFormatToken = referenceFormatTokens[i];
			final String defaultValueToken = defaultValuesTokens.length > i && StringUtils.isNotBlank(defaultValuesTokens[i])
					? defaultValuesTokens[i]
					: null;
			defaultValuesMap.put(referenceFormatToken, defaultValueToken);
		}
		return defaultValuesMap;
	}

	public String getIsoCode()
	{
		return isoCode;
	}

	public void setIsoCode(final String isoCode)
	{
		this.isoCode = isoCode;
	}

	public String getReferenceFormat()
	{
		return referenceFormat;
	}

	public void setReferenceFormat(final String referenceFormat)
	{
		this.referenceFormat = referenceFormat;
	}

	public String getDefaultValues()
	{
		return defaultValues;
	}

	public void setDefaultValues(final String defaultValues)
	{
		this.defaultValues = defaultValues;
	}

	public AttributeDescriptorModel getAttributeDescriptor()
	{
		return attributeDescriptor;
	}

	public void setAttributeDescriptor(final AttributeDescriptorModel attributeDescriptor)
	{
		this.attributeDescriptor = attributeDescriptor;
	}

	public boolean isRequired(final String currentLanguageIsoCode)
	{
		return (isRequiredWithoutDefaultValue() || isUnique())
				&& isNotLocalizedOrLocalizedForCurrentLanguage(currentLanguageIsoCode);
	}

	private boolean isNotLocalizedOrLocalizedForCurrentLanguage(final String currentLanguageIsoCode)
	{
		return !isLocalized() || StringUtils.equals(getIsoCode(), currentLanguageIsoCode);
	}

	private boolean isUnique()
	{
		return BooleanUtils.isTrue(this.attributeDescriptor.getUnique()
				|| this.attributeDescriptor.getEnclosingType().getUniqueKeyAttributes().contains(attributeDescriptor));
	}

	private boolean isRequiredWithoutDefaultValue()
	{
		return BooleanUtils.isFalse(this.attributeDescriptor.getOptional()) && this.attributeDescriptor.getDefaultValue() == null;
	}

	public String getName()
	{
		return StringUtils.isNotEmpty(this.attributeDescriptor.getName()) ? this.attributeDescriptor.getName() : getQualifier();
	}

	public String getQualifier()
	{
		return this.attributeDescriptor.getQualifier();
	}

	public boolean isLocalized()
	{
		return BooleanUtils.isTrue(this.attributeDescriptor.getLocalized());
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

		final SelectedAttribute that = (SelectedAttribute) o;

		if ((isoCode != null) ? !isoCode.equals(that.isoCode) : (that.isoCode != null))
		{
			return false;
		}
		if ((referenceFormat != null) ? !referenceFormat.equals(that.referenceFormat) : (that.referenceFormat != null))
		{
			return false;
		}
		if ((defaultValues != null) ? !defaultValues.equals(that.defaultValues) : (that.defaultValues != null))
		{
			return false;
		}
		if (attributeDescriptor == null || that.getAttributeDescriptor() == null)
		{
			return false;
		}
		return ObjectUtils.equals(that.getAttributeDescriptor().getQualifier(), attributeDescriptor.getQualifier());
	}

	@Override
	public int hashCode()
	{
		int result = isoCode != null ? isoCode.hashCode() : 0;
		result = 31 * result + (referenceFormat != null ? referenceFormat.hashCode() : 0);
		result = 31 * result + (defaultValues != null ? defaultValues.hashCode() : 0);
		result = 31 * result + (attributeDescriptor != null
				? (attributeDescriptor.getQualifier() != null ? attributeDescriptor.getQualifier().hashCode() : 0)
				: 0);
		return result;
	}

	@Override
	public int compareTo(final SelectedAttribute o)
	{
		return this.getName().compareTo(o.getName());
	}
}
