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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.strategies.AsUidGenerator;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.type.TypeService;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default Adaptive Search implementation of model cloning context.
 */
public class DefaultAsModelCloningContext implements ModelCloningContext
{
	private TypeService typeService;
	private AsUidGenerator asUidGenerator;

	@Override
	public boolean skipAttribute(final Object model, final String qualifier)
	{
		return false;
	}

	@Override
	public boolean treatAsPartOf(final Object model, final String qualifier)
	{
		if (!(model instanceof ItemModel))
		{
			return false;
		}

		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(model.getClass());
		final AttributeDescriptorModel attributeDescriptor = typeService.getAttributeDescriptor(composedType, qualifier);

		return BooleanUtils.isTrue(attributeDescriptor.getPartOf()) && BooleanUtils.isTrue(attributeDescriptor.getWritable());
	}

	@Override
	public boolean usePresetValue(final Object model, final String qualifier)
	{
		return model instanceof AbstractAsConfigurationModel && StringUtils.equals(AbstractAsConfigurationModel.UID, qualifier);
	}

	@Override
	public Object getPresetValue(final Object model, final String qualifier)
	{
		return asUidGenerator.generateUid();
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public AsUidGenerator getAsUidGenerator()
	{
		return asUidGenerator;
	}

	@Required
	public void setAsUidGenerator(final AsUidGenerator asUidGenerator)
	{
		this.asUidGenerator = asUidGenerator;
	}
}
