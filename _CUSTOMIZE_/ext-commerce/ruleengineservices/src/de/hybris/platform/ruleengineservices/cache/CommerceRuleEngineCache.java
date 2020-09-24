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
package de.hybris.platform.ruleengineservices.cache;

import de.hybris.platform.ruleengine.cache.RuleEngineCache;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;

import java.util.Collection;


/**
 * CommerceRuleEngineCache extends the RuleEngineCache by allowing for caching of fact templates
 */
public interface CommerceRuleEngineCache extends RuleEngineCache
{

	/**
	 * returns the facts registered for the given kieBase.
	 *
	 * @param kieBase
	 * @return a collection of facts derived from the cached fact templates
	 */
	Collection<Object> getCachedFacts(DroolsKIEBaseModel kieBase);
}
