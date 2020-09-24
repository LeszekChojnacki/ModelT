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

import de.hybris.platform.adaptivesearch.daos.AsSearchConfigurationDao;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchConfigurationStrategy;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.model.ModelService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for implementations of {@link AsSearchConfigurationStrategy}.
 */
public abstract class AbstractAsSearchConfigurationStrategy<P extends AbstractAsSearchProfileModel, C extends AbstractAsSearchConfigurationModel>
		implements AsSearchConfigurationStrategy<P, C>
{
	private ModelService modelService;
	private L10NService l10nService;
	private AsSearchConfigurationDao asSearchConfigurationDao;

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public L10NService getL10nService()
	{
		return l10nService;
	}

	@Required
	public void setL10nService(final L10NService l10nService)
	{
		this.l10nService = l10nService;
	}

	public AsSearchConfigurationDao getAsSearchConfigurationDao()
	{
		return asSearchConfigurationDao;
	}

	@Required
	public void setAsSearchConfigurationDao(final AsSearchConfigurationDao asSearchConfigurationDao)
	{
		this.asSearchConfigurationDao = asSearchConfigurationDao;
	}
}
