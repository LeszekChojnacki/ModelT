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
package de.hybris.platform.warehousing.sourcing.strategy;

import de.hybris.platform.warehousing.data.sourcing.SourcingContext;

/**
 * Strategy to apply for the sourcing process.
 */
public interface SourcingStrategy
{
	/**
	 * Determine which order entries/order item quantities should be sourced from which sourcing locations.
	 *
	 * @param sourcingContext
	 *           - the sourcing context; cannot be <tt>null</tt>
	 */
	public void source(final SourcingContext sourcingContext);

	/**
	 * Indicates whether this sourcing strategy will allow other sourcing strategies to attempt to source the same order
	 * entries.
	 *
	 * @return <tt>true</tt> if the strategy is terminal; <tt>false</tt> otherwise
	 */
	Boolean isTerminal();
}
