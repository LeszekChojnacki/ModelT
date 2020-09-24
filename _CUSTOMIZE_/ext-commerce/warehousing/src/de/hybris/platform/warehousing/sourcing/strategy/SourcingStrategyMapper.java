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
 * A mapper is used to map a sourcing context to a sourcing strategy.
 *
 * @see SourcingContext
 * @see SourcingStrategy
 */
public interface SourcingStrategyMapper
{
	/**
	 * Get the sourcing strategy associate with this matcher.
	 *
	 * @return the sourcing strategy or <tt>null</tt> if none is set
	 */
	SourcingStrategy getStrategy();

	/**
	 * Determines if this mapper produces a match for the given sourcing context.
	 *
	 * @param context
	 *           - the sourcing context
	 * @return <tt>true</tt> if it is a match; <tt>false</tt> otherwise
	 */
	Boolean isMatch(SourcingContext context);

}
