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
import org.kie.api.runtime.KieSession;


/**
 * helper methods interface for KieSessions
 */
public interface KieSessionHelper<T> extends ModuleReleaseIdAware
{

	/**
	 * given the {@link RuleEvaluationContext} and currently active instance of {@link KieContainer}, initializes the {@link
	 * T} kie session for evaluation of rules
	 *
	 * @param kieSessionClass
	 * 		implementation class of the Kie session ({@link KieSession} or {@link org.kie.api.runtime.StatelessKieSession})
	 * @param context
	 * 		instance of {@link RuleEvaluationContext}
	 * @param kieContainer
	 * 		currently active instance of {@link KieContainer}
	 * @return initialized instance of {@link KieSession}
	 */
	T initializeSession(Class<T> kieSessionClass, RuleEvaluationContext context, KieContainer kieContainer);

	/**
	 * Shuts down the kie session pool (if enabled and existing) for the given rule module and version. This method is
	 * invoked after a new rule module has been published successfully.
	 *
	 * @param moduleName
	 *           the rule module name
	 * @param version
	 *           the rule module version
	 */
	default void shutdownKieSessionPools(final String moduleName, final String version)
	{
		// default implementation does nothing, the method is made default just for backwards compatibility
	}
}
