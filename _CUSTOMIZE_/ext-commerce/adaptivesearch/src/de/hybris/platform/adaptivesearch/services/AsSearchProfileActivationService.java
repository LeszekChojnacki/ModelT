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
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;

import java.util.List;
import java.util.Optional;


/**
 * Service used for activation of search profiles.
 */
public interface AsSearchProfileActivationService
{
	/**
	 * Sets the current active search profiles in the session.
	 *
	 * @param searchProfiles
	 *           - the active search profiles
	 */
	void setCurrentSearchProfiles(final List<AbstractAsSearchProfileModel> searchProfiles);

	/**
	 * Returns the current active search profiles in the session. If there are no current active search profiles, an
	 * empty {@link Optional} is returned.
	 *
	 * @return the active search profiles
	 */
	Optional<List<AbstractAsSearchProfileModel>> getCurrentSearchProfiles();

	/**
	 * Clears the current active search profiles in the session.
	 */
	void clearCurrentSearchProfiles();

	/**
	 * Returns the active search profile groups for a given context.
	 *
	 * @param context
	 *           - the search profile context
	 *
	 * @return the active search profile group
	 */
	List<AsSearchProfileActivationGroup> getSearchProfileActivationGroupsForContext(AsSearchProfileContext context);
}
