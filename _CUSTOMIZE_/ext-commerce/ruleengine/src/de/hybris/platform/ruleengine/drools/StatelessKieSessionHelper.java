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
package de.hybris.platform.ruleengine.drools;

import de.hybris.platform.ruleengine.RuleEvaluationContext;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;


/**
 * helper methods interface for StatelessKieSession
 * @deprecated since 6.6. Will be replaced by {@link de.hybris.platform.ruleengine.drools.KieSessionHelper}
 */
@Deprecated
public interface StatelessKieSessionHelper extends ModuleReleaseIdAware
{
	
	/**
	 * given the {@link RuleEvaluationContext} and currently active instance of {@link KieContainer}, initializes the {@link
	 * StatelessKieSession} for evaluation of rules
	 *
	 * @param context
	 * 			 instance of {@link RuleEvaluationContext}
	 * @param kieContainer
	 * 			 currently active instance of {@link KieContainer}
	 * @return initialized instance of {@link StatelessKieSession}
	 */
	StatelessKieSession initializeSession(RuleEvaluationContext context, KieContainer kieContainer);
	

}
