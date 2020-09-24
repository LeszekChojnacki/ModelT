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
package de.hybris.platform.adaptivesearch.services;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;

import java.util.List;


/**
 * Service that provides calculation functionality for search profiles.
 */
public interface AsSearchProfileCalculationService
{
	/**
	 * Creates a new instance of {@link AsSearchProfileResult}.
	 *
	 * @param context
	 *           - the search profile context
	 *
	 * @return the new instance
	 *
	 * @since 6.6
	 */
	AsSearchProfileResult createResult(AsSearchProfileContext context);

	/**
	 * Creates a new instance of {@link AsConfigurationHolder}.
	 *
	 * @param <T>
	 *           - the type of the configuration
	 * @param <R>
	 *           - the type of the replaced configuration
	 *
	 * @param context
	 *           - the search profile context
	 * @param configuration
	 *           - the configuration
	 *
	 * @return the new instance
	 *
	 * @since 6.6
	 */
	<T, R> AsConfigurationHolder<T, R> createConfigurationHolder(AsSearchProfileContext context, T configuration);

	/**
	 * Creates a new instance of {@link AsConfigurationHolder}.
	 *
	 * @param <T>
	 *           - the type of the configuration
	 * @param <R>
	 *           - the type of the replaced configuration
	 *
	 * @param context
	 *           - the search profile context
	 * @param configuration
	 *           - the configuration
	 * @param data
	 *           - additional data to be stored together with the configuration holder
	 *
	 * @return the new instance
	 *
	 * @since 6.6
	 */
	<T, R> AsConfigurationHolder<T, R> createConfigurationHolder(AsSearchProfileContext context, T configuration, Object data);

	/**
	 * Calculates a search profile result based on a list of search profiles.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfiles
	 *           - the search profiles
	 *
	 * @return the search profile result
	 */
	AsSearchProfileResult calculate(AsSearchProfileContext context, List<AbstractAsSearchProfileModel> searchProfiles);

	/**
	 * Calculates a search profile result based on an exiting result and a list of search profiles.
	 *
	 * @param context
	 *           - the search profile context
	 * @param result
	 *           - the result
	 * @param searchProfiles
	 *           - the search profiles
	 *
	 * @return the search profile result
	 *
	 * @since 6.6
	 */
	AsSearchProfileResult calculate(AsSearchProfileContext context, AsSearchProfileResult result,
			List<AbstractAsSearchProfileModel> searchProfiles);

	/**
	 * Calculates a search profile result based on an exiting result and a list of search profiles.
	 *
	 * @param context
	 *           - the search profile context
	 * @param groups
	 *           - the search profile groups
	 *
	 * @return the search profile result
	 *
	 * @since 6.6
	 */
	AsSearchProfileResult calculateGroups(AsSearchProfileContext context, List<AsSearchProfileActivationGroup> groups);

	/**
	 * Calculates a search profile result based on an exiting result and a list of search profiles.
	 *
	 * @param context
	 *           - the search profile context
	 * @param result
	 *           - the result
	 * @param groups
	 *           - the search profile groups
	 *
	 * @return the search profile result
	 *
	 * @since 6.6
	 */
	AsSearchProfileResult calculateGroups(AsSearchProfileContext context, AsSearchProfileResult result, List<AsSearchProfileActivationGroup> groups);
}
