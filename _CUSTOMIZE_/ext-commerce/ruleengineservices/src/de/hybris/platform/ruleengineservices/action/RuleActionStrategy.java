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
package de.hybris.platform.ruleengineservices.action;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;

import java.util.List;


/**
 * Defines a strategy that encapsulates the logic of a rule action.
 */
public interface RuleActionStrategy<T extends ItemModel>
{
	/**
	 * Applies the action described by the given {@link AbstractRuleActionRAO}.
	 *
	 * @param action
	 *           the action to apply
	 * @return list of {@link ItemModel} that are affected by the application of this action.
	 */
	List<T> apply(AbstractRuleActionRAO action);

	/**
	 * Returns this strategy's unique Id
	 *
	 * @return the strategy's Id
	 */
	String getStrategyId();

	/**
	 * Revokes the Promotion Action that was applied.
	 *
	 * @param action
	 *           the action to be undone
	 */
	void undo(ItemModel action);
}
