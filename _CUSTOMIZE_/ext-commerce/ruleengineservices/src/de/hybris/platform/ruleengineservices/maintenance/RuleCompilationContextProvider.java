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
package de.hybris.platform.ruleengineservices.maintenance;

/**
 * Rule Compilation context provider
 */
public interface RuleCompilationContextProvider
{
	/**
	 * get {@link RuleCompilationContext}
	 *
	 * @return instance of {@link RuleCompilationContext}
	 */
	RuleCompilationContext getRuleCompilationContext();

}
