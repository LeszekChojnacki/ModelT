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
package com.hybris.backoffice.excel.template;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.populator.ExcelAttributeContext;
import com.hybris.backoffice.excel.template.populator.appender.ExcelMarkAppender;


/**
 * Formats {@link ExcelAttribute} to value displayed in excel sheet's header row
 * ({@value com.hybris.backoffice.excel.template.ExcelTemplateConstants#HEADER_ROW_INDEX} index)
 */
public class DisplayNameAttributeNameFormatter implements AttributeNameFormatter<ExcelAttributeDescriptorAttribute>
{
	private CommonI18NService commonI18NService;
	private TypeService typeService;
	private List<ExcelMarkAppender<ExcelAttributeDescriptorAttribute>> appenders = Collections.emptyList();

	/**
	 * Returns displayed header name based on attribute's metadata
	 *
	 * @param excelAttributeContext
	 *           context which contains excelAttribute
	 * @return displayed header name based
	 */
	@Override
	public String format(@Nonnull final ExcelAttributeContext<ExcelAttributeDescriptorAttribute> excelAttributeContext)
	{
		final String isoCode = excelAttributeContext.getAttribute("isoCode", String.class);

		return Optional.of(excelAttributeContext.getExcelAttribute(ExcelAttributeDescriptorAttribute.class)) //
				.map(attr -> getAttributeDisplayName(attr, isoCode)) //
				.orElse(StringUtils.EMPTY);
	}

	protected String getAttributeDisplayName(final ExcelAttributeDescriptorAttribute excelAttributeDescriptorAttribute,
			final String workbookIsoCode)
	{
		final AttributeDescriptorModel attributeDescriptor = excelAttributeDescriptorAttribute.getAttributeDescriptorModel();
		final String langIsoCode = excelAttributeDescriptorAttribute.getIsoCode();

		final Supplier<String> currentLang = () -> StringUtils.isNotEmpty(langIsoCode) ? langIsoCode
				: commonI18NService.getCurrentLanguage().getIsocode();

		final boolean isNameUnique = typeService.getAttributeDescriptorsForType(attributeDescriptor.getEnclosingType()).stream()
				.map(this::getAttributeDescriptorName)
				.filter(attrName -> StringUtils.equals(getAttributeDescriptorName(attributeDescriptor), attrName))//
				.count() == 1;

		String value = new StringBuilder()//
				.append(StringUtils.trim(getAttributeDescriptorName(attributeDescriptor, workbookIsoCode)))//
				.append(isNameUnique ? "" : String.format("[%s]", attributeDescriptor.getQualifier()))//
				.append(attributeDescriptor.getLocalized() ? String.format("[%s]", currentLang.get()) : StringUtils.EMPTY)//
				.toString();

		for (final ExcelMarkAppender<ExcelAttributeDescriptorAttribute> appender : appenders)
		{
			value = appender.apply(value, excelAttributeDescriptorAttribute);
		}
		return value;
	}

	protected String getAttributeDescriptorName(final AttributeDescriptorModel attributeDescriptor)
	{
		return getAttributeDescriptorName(attributeDescriptor, null);
	}

	protected String getAttributeDescriptorName(final AttributeDescriptorModel attributeDescriptor, final String isoCode)
	{
		if (StringUtils.isNotEmpty(isoCode))
		{
			final Locale locale = commonI18NService.getLocaleForIsoCode(isoCode);
			if (StringUtils.isNotEmpty(attributeDescriptor.getName(locale)))
			{
				return attributeDescriptor.getName(locale);
			}
		}

		return StringUtils.isNotEmpty(attributeDescriptor.getName()) ? attributeDescriptor.getName()
				: attributeDescriptor.getQualifier();
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	// optional
	public void setAppenders(final List<ExcelMarkAppender<ExcelAttributeDescriptorAttribute>> appenders)
	{
		if (appenders != null)
		{
			OrderComparator.sort(appenders);
		}
		this.appenders = ListUtils.emptyIfNull(appenders);
	}
}
