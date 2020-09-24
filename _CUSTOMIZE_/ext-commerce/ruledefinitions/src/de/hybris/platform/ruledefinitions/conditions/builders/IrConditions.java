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
package de.hybris.platform.ruledefinitions.conditions.builders;

import de.hybris.platform.ruleengineservices.compiler.RuleIrEmptyCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrFalseCondition;


public class IrConditions
{

	private IrConditions()
	{
		// empty
	}

	public static RuleIrFalseCondition newIrRuleFalseCondition()
	{
		return new RuleIrFalseCondition();
	}

	public static RuleIrEmptyCondition empty()
	{
		return new RuleIrEmptyCondition();
	}
}
