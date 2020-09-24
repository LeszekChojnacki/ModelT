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
package de.hybris.platform.adaptivesearch.strategies;

import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;


/**
 * Provides methods for creating search profile result related objects.
 */
public interface AsSearchProfileResultFactory
{
	/**
	 * Creates a new instance of {@link AsSearchProfileResult}.
	 *
	 * @return the new instance
	 */
	AsSearchProfileResult createResult();

	/**
	 * Creates a new instance of {@link AsSearchProfileResult} from {@link AbstractAsConfigurableSearchConfigurationModel}.
	 *
	 * @param searchConfiguration
	 *           - the search configuration
	 *
	 * @return the new instance
	 */
	AsSearchProfileResult createResultFromSearchConfiguration(AsConfigurableSearchConfiguration searchConfiguration);

	/**
	 * Creates a new instance of {@link AsConfigurationHolder}.
	 *
	 * @param <T>
	 *           - the type of the configuration
	 * @param <R>
	 *           - the type of the replaced configuration
	 *
	 * @param configuration
	 *           - the configuration
	 *
	 * @return the new instance
	 */
	<T, R> AsConfigurationHolder<T, R> createConfigurationHolder(T configuration);

	/**
	 * Creates a new instance of {@link AsConfigurationHolder}.
	 *
	 * @param <T>
	 *           - the type of the configuration
	 * @param <R>
	 *           - the type of the replaced configuration
	 *
	 * @param configuration
	 *           - the configuration
	 * @param data
	 *           - additional data to be stored together with the configuration holder
	 *
	 * @return the new instance
	 */
	<T, R> AsConfigurationHolder<T, R> createConfigurationHolder(T configuration, Object data);

	/**
	 * Clones an instance of {@link AsConfigurationHolder}.
	 *
	 * @param <T>
	 *           - the type of the configuration
	 * @param <R>
	 *           - the type of the replaced configuration
	 *
	 * @param configurationHolder
	 *           - the instance of {@link AsConfigurationHolder} to clone
	 *
	 * @return the new instance
	 */
	<T, R> AsConfigurationHolder<T, R> cloneConfigurationHolder(AsConfigurationHolder<T, R> configurationHolder);
}
