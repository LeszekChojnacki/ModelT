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

import java.util.Collection;
import java.util.List;


/**
 * Service to get sourcing strategies to be used in different contexts.
 * 
 * @see SourcingStrategy
 * @see SourcingStrategyMapper
 */
public interface SourcingStrategyService
{
	/**
	 * Get the sourcing strategies according to a given sourcing context and sourcing mappers.
	 *
	 * @param context
	 *           - the sourcing context
	 * @param mappers
	 *           - collection of sourcing strategy mappers
	 * @return ordered list of sourcing strategies; never <tt>null</tt>
	 */
	List<SourcingStrategy> getStrategies(SourcingContext context, Collection<SourcingStrategyMapper> mappers);

	/**
	 * Get the default sourcing strategies.
	 *
	 * @return ordered list of sourcing strategies; never <tt>null</tt>
	 */
	List<SourcingStrategy> getDefaultStrategies();
}
