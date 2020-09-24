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
package de.hybris.platform.ruleengineservices.rule.evaluation;

import de.hybris.platform.ruleengineservices.rao.AbstractActionedRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;


/**
 * DisplayMessageRAOAction fires a message as a Rule Action.
 */
public interface DisplayMessageRAOAction
{
	/**
	 * Fires a message as a Rule Action. The {@code ruleContext} object can be used to enhance the returned
	 * DisplayMessageRAO.
	 *
	 * @param appliedToObject
	 *           object to which the message is applied to (Cart, OrderEntry, Product, etc..)
	 * @param result
	 *           the result rao
	 * @param ruleContext
	 *           an optional context object that can be used to provide additional information for the returned
	 *           DisplayMessageRAO
	 */
	DisplayMessageRAO fireMessage(AbstractActionedRAO appliedToObject, RuleEngineResultRAO result, Object ruleContext);
}
