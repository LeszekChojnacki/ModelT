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

import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.strategies.AsFacetsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Base class for implementations of {@link AsFacetsMergeStrategy}.
 */
public abstract class AbstractAsFacetsMergeStrategy implements AsFacetsMergeStrategy
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

	protected <T extends AbstractAsFacetConfiguration> AsConfigurationHolder<T, AbstractAsFacetConfiguration> cloneConfigurationHolder(
			final AsConfigurationHolder<T, AbstractAsFacetConfiguration> configurationHolder)
	{
		return asSearchProfileResultFactory.cloneConfigurationHolder(configurationHolder);
	}

	protected <T1 extends AbstractAsFacetConfiguration, T2 extends AbstractAsFacetConfiguration> void updateReplacedConfigurations(
			final AsConfigurationHolder<T1, AbstractAsFacetConfiguration> configurationHolder,
			final AsConfigurationHolder<T2, AbstractAsFacetConfiguration> replacedConfigurationHolder)
	{
		if (replacedConfigurationHolder != null)
		{
			final List<AbstractAsFacetConfiguration> replacedConfigurations = configurationHolder.getReplacedConfigurations();
			replacedConfigurations.add(replacedConfigurationHolder.getConfiguration());

			if (CollectionUtils.isNotEmpty(replacedConfigurationHolder.getReplacedConfigurations()))
			{
				replacedConfigurations.addAll(replacedConfigurationHolder.getReplacedConfigurations());
			}
		}
	}
}