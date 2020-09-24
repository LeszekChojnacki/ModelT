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
package com.hybris.backoffice.excel.template.populator.typesheet;

import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.AttributeNameFormatter;
import com.hybris.backoffice.excel.template.CollectionFormatter;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.template.populator.DefaultExcelAttributeContext;
import com.hybris.backoffice.excel.translators.ExcelTranslatorRegistry;


public class TypeSystemRowFactory
{
	private CommonI18NService commonI18NService;
	private ExcelFilter<AttributeDescriptorModel> excelUniqueFilter;
	private ExcelTranslatorRegistry excelTranslatorRegistry;
	private CollectionFormatter collectionFormatter;
	private AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter;

	public TypeSystemRow create(final @Nonnull AttributeDescriptorModel attributeDescriptor)
	{
		final String attributeDisplayName = getAttributeDisplayName(attributeDescriptor);
		final TypeSystemRow created = new TypeSystemRow();
		created.setTypeCode(collectionFormatter.formatToString(attributeDescriptor.getEnclosingType().getCode()));
		created.setTypeName(attributeDescriptor.getEnclosingType().getName());
		created.setAttrQualifier(attributeDescriptor.getQualifier());
		created.setAttrName(attributeDescriptor.getName());
		created.setAttrOptional(attributeDescriptor.getOptional());
		created.setAttrTypeCode(attributeDescriptor.getAttributeType().getCode());
		created.setAttrTypeItemType(attributeDescriptor.getDeclaringEnclosingType().getCode());
		created.setAttrLocalized(attributeDescriptor.getLocalized());
		created.setAttrLocLang(getAttrLocLang(attributeDescriptor.getLocalized()));
		created.setAttrDisplayName(attributeDisplayName);
		created.setAttrUnique(excelUniqueFilter.test(attributeDescriptor));
		created.setAttrReferenceFormat(getReferenceFormat(attributeDescriptor));
		return created;
	}

	private String getAttributeDisplayName(final AttributeDescriptorModel attributeDescriptor)
	{
		final Collection<String> attributeNames = commonI18NService.getAllLanguages().stream() //
				.filter(LanguageModel::getActive) //
				.map(C2LItemModel::getIsocode) //
				.map(lang -> attributeNameFormatter.format(DefaultExcelAttributeContext
						.ofExcelAttribute(new ExcelAttributeDescriptorAttribute(attributeDescriptor, lang)))) //
				.collect(Collectors.toList());
		return collectionFormatter.formatToString(attributeNames);
	}

	private String getAttrLocLang(final boolean localized)
	{
		if (!localized)
		{
			return StringUtils.EMPTY;
		}
		final List<String> isoCodes = commonI18NService.getAllLanguages().stream() //
				.filter(LanguageModel::getActive) //
				.map(C2LItemModel::getIsocode) //
				.collect(Collectors.toList());
		return collectionFormatter.formatToString(isoCodes);
	}

	private String getReferenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		return excelTranslatorRegistry.getTranslator(attributeDescriptor) //
				.map(excelValueTranslator -> excelValueTranslator.referenceFormat(attributeDescriptor)) //
				.orElse(StringUtils.EMPTY);
	}


	public TypeSystemRow merge(final @Nonnull TypeSystemRow first, final @Nonnull TypeSystemRow second)
	{
		final TypeSystemRow merged = new TypeSystemRow();
		BeanUtils.copyProperties(first, merged);
		if (!first.getTypeCode().contains(second.getTypeCode()))
		{
			merged.setTypeCode(String.join(",", first.getTypeCode(), second.getTypeCode()));
		}
		return merged;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	@Required
	public void setExcelUniqueFilter(final ExcelFilter<AttributeDescriptorModel> excelUniqueFilter)
	{
		this.excelUniqueFilter = excelUniqueFilter;
	}

	@Required
	public void setExcelTranslatorRegistry(final ExcelTranslatorRegistry excelTranslatorRegistry)
	{
		this.excelTranslatorRegistry = excelTranslatorRegistry;
	}

	@Required
	public void setCollectionFormatter(final CollectionFormatter collectionFormatter)
	{
		this.collectionFormatter = collectionFormatter;
	}

	@Required
	public void setAttributeNameFormatter(final AttributeNameFormatter<ExcelAttributeDescriptorAttribute> attributeNameFormatter)
	{
		this.attributeNameFormatter = attributeNameFormatter;
	}
}
