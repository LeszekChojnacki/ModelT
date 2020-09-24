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
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;

import java.util.Collections;
import java.util.List;


/**
 * Implementations of this interface can activate search profiles.
 */
public interface AsSearchProfileActivationStrategy
{
	/**
	 * Returns a list of active search profiles.
	 *
	 * @param context
	 *           - the search profile context
	 *
	 * @return the active search profiles
	 */
	default List<AbstractAsSearchProfileModel> getActiveSearchProfiles(AsSearchProfileContext context)
	{
		return Collections.emptyList();
	}

	/**
	 * Returns a list of active search profile groups.
	 *
	 * @param context
	 *           - the search profile context
	 *
	 * @return the active search profiles
	 */
	default AsSearchProfileActivationGroup getSearchProfileActivationGroup(final AsSearchProfileContext context)
	{
		final List<AbstractAsSearchProfileModel> activeSearchProfiles = getActiveSearchProfiles(context);

		final AsSearchProfileActivationGroup group = new AsSearchProfileActivationGroup();
		group.setSearchProfiles(activeSearchProfiles);

		return group;
	}
}
