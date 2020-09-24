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

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;

import java.util.Optional;
import java.util.Set;


/**
 * Strategy for performing operations on search configurations.
 */
public interface AsSearchConfigurationStrategy<P extends AbstractAsSearchProfileModel, C extends AbstractAsSearchConfigurationModel>
{
	/**
	 * Returns the search configuration for a specific context and search profile.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	Optional<C> getForContext(AsSearchProfileContext context, P searchProfile);

	/**
	 * Returns the search configuration for a specific context and search profile. If the search configuration does not
	 * yet exist a new one is created.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	C getOrCreateForContext(AsSearchProfileContext context, P searchProfile);

	/**
	 * Returns search configuration related information for a specific context and search profile.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	AsSearchConfigurationInfoData getInfoForContext(AsSearchProfileContext context, P searchProfile);

	/**
	 * Gets the qualifiers for the search profile
	 *
	 * @return qualifiers for the search profile
	 */
	Set<String> getQualifiers(P searchProfile);
}
