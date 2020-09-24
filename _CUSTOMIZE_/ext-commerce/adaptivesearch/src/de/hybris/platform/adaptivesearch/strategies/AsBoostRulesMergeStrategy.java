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

import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;


/**
 * Strategy for merging boost rules.
 */
public interface AsBoostRulesMergeStrategy
{
	/**
	 * Merges the boost rules from the source result into the target result.
	 *
	 * @param source
	 *           - the source result
	 * @param target
	 *           - the target result
	 */
	void mergeBoostRules(AsSearchProfileResult source, AsSearchProfileResult target);
}
