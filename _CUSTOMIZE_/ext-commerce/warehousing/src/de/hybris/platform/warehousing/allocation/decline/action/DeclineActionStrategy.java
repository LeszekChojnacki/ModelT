/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.allocation.decline.action;

import de.hybris.platform.warehousing.data.allocation.DeclineEntry;

import java.util.Collection;


/**
 * Defines decline action strategies
 */
public interface DeclineActionStrategy
{
	/**
	 * Action to be performed based on the given {@link DeclineEntry}, based on the selected {@link de.hybris.platform.warehousing.enums.DeclineReason}
	 *
	 * @param declineEntry
	 * 		the {@link DeclineEntry} to be declined
	 */
	void execute(final DeclineEntry declineEntry);

	/**
	 * Action to be performed on the given collection of {@link DeclineEntry}, based on the selected {@link de.hybris.platform.warehousing.enums.DeclineReason}
	 *
	 * @param declineEntries
	 * 		the collection of {@link DeclineEntry} to be declined
	 */
	void execute(final Collection<DeclineEntry> declineEntries);
}
