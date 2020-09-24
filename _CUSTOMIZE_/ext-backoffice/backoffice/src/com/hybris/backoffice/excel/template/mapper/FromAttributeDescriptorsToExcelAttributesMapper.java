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
package com.hybris.backoffice.excel.template.mapper;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.util.Collection;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttributeDescriptorAttribute;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Allows to map Collection<{@link AttributeDescriptorModel}> to Collection<{@link ExcelAttributeDescriptorAttribute}>
 */
public class FromAttributeDescriptorsToExcelAttributesMapper
		implements ToExcelAttributesMapper<Collection<AttributeDescriptorModel>, ExcelAttributeDescriptorAttribute>
{

	private CommonI18NService commonI18NService;
	private Collection<ExcelFilter<ExcelAttributeDescriptorAttribute>> filters;

	@Override
	public Collection<ExcelAttributeDescriptorAttribute> apply(
			final Collection<AttributeDescriptorModel> attributeDescriptorModels)
	{
		return attributeDescriptorModels.stream() //
				.map(this::getExcelAttributes) //
				.flatMap(Collection::stream) //
				.filter(attribute -> filter(attribute, filters)) //
				.distinct() //
				.collect(Collectors.toSet());
	}

	protected Collection<ExcelAttributeDescriptorAttribute> getExcelAttributes(final AttributeDescriptorModel attributeDescriptor)
	{
		return attributeDescriptor.getLocalized() ? getLocalizedExcelAttributes(attributeDescriptor)
				: getUnlocalizedExcelAttributes(attributeDescriptor);
	}


	protected Collection<ExcelAttributeDescriptorAttribute> getLocalizedExcelAttributes(
			final AttributeDescriptorModel attributeDescriptor)
	{
		return commonI18NService.getAllLanguages().stream().filter(LanguageModel::getActive).map(LanguageModel::getIsocode)
				.map(isoCode -> new ExcelAttributeDescriptorAttribute(attributeDescriptor, isoCode)).collect(Collectors.toList());
	}


	protected Collection<ExcelAttributeDescriptorAttribute> getUnlocalizedExcelAttributes(
			final AttributeDescriptorModel attributeDescriptor)
	{
		return Lists.newArrayList(new ExcelAttributeDescriptorAttribute(attributeDescriptor));
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	// optional
	public void setFilters(final Collection<ExcelFilter<ExcelAttributeDescriptorAttribute>> filters)
	{
		this.filters = filters;
	}
}
