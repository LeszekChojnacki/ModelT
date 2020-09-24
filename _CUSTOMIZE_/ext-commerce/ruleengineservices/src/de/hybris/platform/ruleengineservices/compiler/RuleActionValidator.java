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

import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;


/**
 * Implementations of this interface are responsible for validating a rule action. Information on compilation problems
 * is held in context {@link RuleCompilerContext}.
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
public interface RuleActionValidator
{
	/**
	 * Validates a rule action.
	 *
	 * @param context
	 *           - the compiler context
	 * @param action
	 *           - the action
	 * @param actionDefinition
	 *           - the action definition
	 */
	void validate(RuleCompilerContext context, RuleActionData action, RuleActionDefinitionData actionDefinition);
}
