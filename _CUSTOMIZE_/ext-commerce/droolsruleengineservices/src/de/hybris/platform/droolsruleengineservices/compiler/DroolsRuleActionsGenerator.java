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
package de.hybris.platform.droolsruleengineservices.compiler;

public interface DroolsRuleActionsGenerator
{
	/**
	 * Generates the actions for the Drools rule engine.
	 *
	 * @param context
	 *           - the drools rule generator context
	 * @param indentation
	 *           - the indentation
	 *
	 * @return actions
	 */
	String generateActions(final DroolsRuleGeneratorContext context, String indentation);
}
