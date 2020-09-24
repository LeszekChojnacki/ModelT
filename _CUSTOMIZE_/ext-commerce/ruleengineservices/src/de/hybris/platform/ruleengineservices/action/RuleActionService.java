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
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;

import java.util.List;


/**
 * Processes Rule Actions.
 *
 */
public interface RuleActionService
{
	/**
	 * Applies Actions of Rule Engine Result in a way specific to an Action instance that can change current state of
	 * related to the Action object entities (Order, OrderEntries etc.).
	 *
	 * @param ruleEngineResultRAO
	 *           contains Actions
	 * @return list of {@link ItemModel} (or it subclasses) as a result of the Actions application.
	 */
	List<ItemModel> applyAllActions(RuleEngineResultRAO ruleEngineResultRAO);
}
