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
package de.hybris.platform.ruleengineservices.versioning;

import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;


/**
 * The historical content creator interface
 */
public interface RuleModelHistoricalContentCreator
{

	/**
	 * creates the historical version of the model
	 *
	 * @param sourceRule
	 *           - the model to be versioned
	 * @param context
	 *           - the InterceptorContext instance, where the versioned copy is registered
	 */
	void createHistoricalVersion(SourceRuleModel sourceRule, InterceptorContext context) throws InterceptorException;


}
