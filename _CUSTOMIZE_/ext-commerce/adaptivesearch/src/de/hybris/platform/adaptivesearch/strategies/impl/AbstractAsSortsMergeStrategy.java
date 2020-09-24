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

import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;
import de.hybris.platform.adaptivesearch.strategies.AsSortsMergeStrategy;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for implementations of {@link AsSortsMergeStrategy}.
 */
public abstract class AbstractAsSortsMergeStrategy implements AsSortsMergeStrategy
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;

	public AsSearchProfileResultFactory getAsSearchProfileResultFactory()
	{
		return asSearchProfileResultFactory;
	}

	@Required
	public void setAsSearchProfileResultFactory(final AsSearchProfileResultFactory asSearchProfileResultFactory)
	{
		this.asSearchProfileResultFactory = asSearchProfileResultFactory;
	}

	protected <T extends AbstractAsSortConfiguration> AsConfigurationHolder<T, AbstractAsSortConfiguration> cloneConfigurationHolder(
			final AsConfigurationHolder<T, AbstractAsSortConfiguration> configurationHolder)
	{
		return asSearchProfileResultFactory.cloneConfigurationHolder(configurationHolder);
	}

	protected <T1 extends AbstractAsSortConfiguration, T2 extends AbstractAsSortConfiguration> void updateReplacedConfigurations(
			final AsConfigurationHolder<T1, AbstractAsSortConfiguration> configurationHolder,
			final AsConfigurationHolder<T2, AbstractAsSortConfiguration> replacedConfigurationHolder)
	{
		if (replacedConfigurationHolder != null)
		{
			final List<AbstractAsSortConfiguration> replacedConfigurations = configurationHolder.getReplacedConfigurations();
			replacedConfigurations.add(replacedConfigurationHolder.getConfiguration());

			if (CollectionUtils.isNotEmpty(replacedConfigurationHolder.getReplacedConfigurations()))
			{
				replacedConfigurations.addAll(replacedConfigurationHolder.getReplacedConfigurations());
			}
		}
	}
}