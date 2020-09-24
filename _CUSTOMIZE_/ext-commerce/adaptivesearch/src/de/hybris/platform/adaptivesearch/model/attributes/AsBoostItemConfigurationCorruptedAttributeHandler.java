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
package de.hybris.platform.adaptivesearch.model.attributes;

import de.hybris.platform.adaptivesearch.model.AbstractAsBoostItemConfigurationModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import org.springframework.beans.factory.annotation.Required;


/**
 * Handler for corrupted attribute of {@link AbstractAsBoostItemConfigurationModel}.
 */
public class AsBoostItemConfigurationCorruptedAttributeHandler
		implements DynamicAttributeHandler<Boolean, AbstractAsBoostItemConfigurationModel>
{
	private ModelService modelService;

	@Override
	public Boolean get(final AbstractAsBoostItemConfigurationModel model)
	{
		if (modelService.isNew(model))
		{
			return Boolean.FALSE;
		}

		return model.getItem() == null;
	}

	@Override
	public void set(final AbstractAsBoostItemConfigurationModel model, final Boolean value)
	{
		throw new UnsupportedOperationException("Write is not a valid operation for this dynamic attribute");
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
