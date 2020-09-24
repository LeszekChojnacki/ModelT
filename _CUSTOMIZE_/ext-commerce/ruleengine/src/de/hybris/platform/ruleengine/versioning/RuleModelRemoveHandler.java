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
 * The rule model validator interface
 */
@FunctionalInterface
public interface RuleModelRemoveHandler
{

	/**
	 * Introduces additional logic on rule engine rule model removal against the persistence context
	 *
	 * @param rule
	 *           - the AbstractRuleEngineRuleModel entity to be validated
	 * @param context
	 *           - the interceptor context to validate the content with
	 *
	 */
	void handleOnRemove(final AbstractRuleEngineRuleModel rule, final InterceptorContext context);

}
