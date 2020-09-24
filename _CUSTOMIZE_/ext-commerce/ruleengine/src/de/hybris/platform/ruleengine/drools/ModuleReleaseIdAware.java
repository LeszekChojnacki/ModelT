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

import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;


/**
 * Interface for deployed module relese id retrieval 
 */
public interface ModuleReleaseIdAware
{

	/**
	 * Given the {@link RuleEvaluationContext} retrieves the {@link ReleaseId} of the deployed {@link KieContainer}
	 *
	 * @param context
	 * 			 instance of {@link RuleEvaluationContext}
	 * @return instance of {@link ReleaseId} of the currently deployed version of {@link KieContainer}
	 */
	ReleaseId getDeployedKieModuleReleaseId(RuleEvaluationContext context);

}
