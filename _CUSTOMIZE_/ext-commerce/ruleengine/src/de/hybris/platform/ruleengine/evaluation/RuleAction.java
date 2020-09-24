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
package de.hybris.platform.ruleengine.evaluation;

import java.util.Collection;


public interface RuleAction
{
	/**
	 * Inserts the given facts using the given context.
	 *
	 * @param engineContext
	 *           the context
	 * @param facts
	 *           the facts to insert
	 */
	void insertFacts(Object engineContext, Object... facts);

	/**
	 * Inserts the facts given in collection using the given context.
	 *
	 * @param engineContext
	 *           the context
	 * @param facts
	 *           the Collection of facts to insert
	 */
	void insertFacts(Object engineContext, Collection facts);

	/**
	 * Updates the given facts using the given context.
	 *
	 * @param engineContext
	 *           the context
	 * @param facts
	 *           the facts to update
	 */
	void updateFacts(Object engineContext, Object... facts);
}
