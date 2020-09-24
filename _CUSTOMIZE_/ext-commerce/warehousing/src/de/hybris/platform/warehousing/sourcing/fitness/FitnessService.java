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
package de.hybris.platform.warehousing.sourcing.fitness;

import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;

import java.util.Collection;
import java.util.List;


/**
 * Defines the services related to fitness.
 */
public interface FitnessService
{
	/**
	 * Sorts a collection of sourcing locations by their evaluated fitness.
	 *
	 * @param sourcingContext
	 *           the sourcingContext containing sourcing locations to be sort by their calculated fitness
	 * @return sorted list of sourcing locations by fittest; never <tt>null</tt>
	 */
	List<SourcingLocation> sortByFitness(SourcingContext sourcingContext);

}
