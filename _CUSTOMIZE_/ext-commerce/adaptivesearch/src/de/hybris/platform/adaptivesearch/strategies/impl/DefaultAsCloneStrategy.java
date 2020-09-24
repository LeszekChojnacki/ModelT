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

import de.hybris.platform.adaptivesearch.strategies.AsCloneStrategy;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.model.ModelService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the clone strategy.
 */
public class DefaultAsCloneStrategy implements AsCloneStrategy
{
	private ModelService modelService;
	private ModelCloningContext modelCloningContext;

	@Override
	public <T extends ItemModel> T clone(final T objectToClone)
	{
		return modelService.clone(objectToClone, modelCloningContext);
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

	public ModelCloningContext getModelCloningContext()
	{
		return modelCloningContext;
	}

	@Required
	public void setModelCloningContext(final ModelCloningContext modelCloningContext)
	{
		this.modelCloningContext = modelCloningContext;
	}
}
