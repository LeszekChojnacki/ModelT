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
package de.hybris.platform.ruleengineservices.compiler;

import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;


/**
 * Implementations of this interface are responsible for validating a rule condition. Information on compilation
 * problems is held in context {@link RuleCompilerContext}.
 *
 * <p>
 * If a compilation error occurs, creating and adding it to a context should normally use the following pattern:
 *
 * <pre>
 * final RuleCompilerProblem ruleCompilerProblem = ruleCompilerProblemFactory.createProblem(Severity.ERROR, errorMessage);
 * context.addProblem(ruleCompilerProblem);
 * </pre>
 *
 */
public interface RuleConditionValidator
{
	/**
	 * Validates a rule condition.
	 *
	 * @param context
	 *           - the compiler context
	 * @param condition
	 *           - the condition
	 * @param conditionDefinition
	 *           - the condition definition
	 */
	void validate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData conditionDefinition);
}
