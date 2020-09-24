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
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.BooleanUtils;


public class ExcelAttributeDescriptorAttribute implements ExcelAttribute
{

	private final AttributeDescriptorModel attributeDescriptorModel;
	private final String isoCode;

	public ExcelAttributeDescriptorAttribute(@Nonnull final AttributeDescriptorModel attributeDescriptorModel,
			@Nullable final String isoCode)
	{
		this.attributeDescriptorModel = attributeDescriptorModel;
		this.isoCode = isoCode;
	}

	public ExcelAttributeDescriptorAttribute(@Nonnull final AttributeDescriptorModel attributeDescriptorModel)
	{
		this.attributeDescriptorModel = attributeDescriptorModel;
		this.isoCode = null;
	}

	@Override
	public String getName()
	{
		return attributeDescriptorModel.getName();
	}

	@Override
	public boolean isLocalized()
	{
		return BooleanUtils.isTrue(attributeDescriptorModel.getLocalized());
	}

	@Override
	public String getIsoCode()
	{
		return isoCode;
	}

	@Override
	public String getQualifier()
	{
		return attributeDescriptorModel.getQualifier();
	}

	@Override
	public boolean isMandatory()
	{
		return BooleanUtils.isFalse(attributeDescriptorModel.getOptional());
	}

	@Override
	public String getType()
	{
		final String type = attributeDescriptorModel instanceof RelationDescriptorModel ? getTypeOfRelationElement()
				: getTypeOfNonRelationElement();
		return isLocalized() ? type.substring("localized:".length()) : type;
	}

	private String getTypeOfRelationElement()
	{
		final RelationDescriptorModel relationDescriptorModel = (RelationDescriptorModel) attributeDescriptorModel;
		if (BooleanUtils.isTrue(relationDescriptorModel.getIsSource()))
		{
			return relationDescriptorModel.getRelationType().getTargetType().getCode();
		}
		return relationDescriptorModel.getRelationType().getSourceType().getCode();
	}

	private String getTypeOfNonRelationElement()
	{
		if (attributeDescriptorModel.getAttributeType() instanceof CollectionTypeModel)
		{
			return getTypeOfCollectionElement();
		}
		return attributeDescriptorModel.getAttributeType().getCode();
	}

	private String getTypeOfCollectionElement()
	{
		return ((CollectionTypeModel) attributeDescriptorModel.getAttributeType()).getElementType().getCode();
	}

	@Override
	public boolean isMultiValue()
	{
		return attributeDescriptorModel instanceof RelationDescriptorModel
				|| BooleanUtils.isTrue(attributeDescriptorModel.getPartOf());
	}

	public AttributeDescriptorModel getAttributeDescriptorModel()
	{
		return attributeDescriptorModel;
	}
}
