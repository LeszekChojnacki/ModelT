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

/**
 * Mapping for strategies that are specific to a search profile type.
 */
public interface AsSearchProfileMapping
{
	/**
	 * Returns the search profile load strategy.
	 *
	 * @return the search profile load strategy
	 */
	AsSearchProfileLoadStrategy getLoadStrategy();

	/**
	 * Returns the search profile calculation strategy.
	 *
	 * @return the search profile calculation strategy
	 */
	AsSearchProfileCalculationStrategy getCalculationStrategy();

	/**
	 * Returns the search configuration strategy.
	 *
	 * @return the search configuration strategy
	 */
	AsSearchConfigurationStrategy getSearchConfigurationStrategy();

}
