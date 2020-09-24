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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;

import java.util.List;
import java.util.Map;


/**
 * Registry used for discovering mappings associated with search profiles. This includes, for example, mappings for
 * load, calculation and activation strategies.
 */
public interface AsSearchProfileRegistry
{
	/**
	 * Returns the search profile mapping for a specific search profile.
	 *
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search profile mapping
	 */
	AsSearchProfileMapping getSearchProfileMapping(AbstractAsSearchProfileModel searchProfile);

	/**
	 * Returns all the search profile mappings.
	 *
	 * @return the search profile mappings
	 */
	Map<String, AsSearchProfileMapping> getSearchProfileMappings();

	/**
	 * Returns all the search profile activation mappings.
	 *
	 * @return the search profile activation mappings
	 */
	List<AsSearchProfileActivationMapping> getSearchProfileActivationMappings();
}
