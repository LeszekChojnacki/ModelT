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
package de.hybris.platform.ruleengine.versioning;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


/**
 * The historical content creator interface
 */
@FunctionalInterface
public interface  RuleModelHistoricalContentCreator
{

	/**
	 * creates the historical version of the model
	 *
	 * @param ruleEngineEntity
	 *           - the model to be versioned
	 * @param context
	 *           - the InterceptorContext instance, where the versioned copy is registered
	 */
	void createHistoricalVersion(AbstractRuleEngineRuleModel ruleEngineEntity, InterceptorContext context);

}
