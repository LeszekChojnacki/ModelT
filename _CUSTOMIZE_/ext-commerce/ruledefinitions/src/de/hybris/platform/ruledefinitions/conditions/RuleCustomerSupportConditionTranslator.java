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
package de.hybris.platform.ruledefinitions.conditions;

import static de.hybris.platform.ruledefinitions.conditions.builders.IrConditions.empty;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeConditionBuilder.newAttributeConditionFor;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.rao.CustomerSupportRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;


/**
 * Creates the intermediate representation of the CustomerSupportRAO.customerEmulationActive condition
 */
public class RuleCustomerSupportConditionTranslator extends AbstractRuleConditionTranslator
{
	protected static final String ASSISTED_SERVICE_SESSION_ACTIVE_PARAM = "value";
	protected static final String CUSTOMER_SUPPORT_RAO_CUSTOMER_EMULATION_ACTIVE_ATTRIBUTE = "customerEmulationActive";

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
				 final RuleConditionDefinitionData conditionDefinition)
	{
		final RuleParameterData assistedServiceSessionActiveParameter = condition.getParameters().get(
					 ASSISTED_SERVICE_SESSION_ACTIVE_PARAM);
		if (verifyAllPresent(assistedServiceSessionActiveParameter))
		{
			final Boolean assistedServiceSessionActive = assistedServiceSessionActiveParameter.getValue();
			if (verifyAllPresent(assistedServiceSessionActive))
			{
				return newAttributeConditionFor(context.generateVariable(CustomerSupportRAO.class))
							 .withAttribute(CUSTOMER_SUPPORT_RAO_CUSTOMER_EMULATION_ACTIVE_ATTRIBUTE)
							 .withOperator(RuleIrAttributeOperator.EQUAL)
							 .withValue(assistedServiceSessionActive)
							 .build();
			}
		}
		return empty();
	}
}
