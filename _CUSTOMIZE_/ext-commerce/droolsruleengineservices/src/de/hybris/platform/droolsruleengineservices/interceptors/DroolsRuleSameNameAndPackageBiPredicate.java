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
package de.hybris.platform.droolsruleengineservices.interceptors;

import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.util.function.BiPredicate;

import org.apache.commons.lang.builder.EqualsBuilder;


/**
 * Encapsulates the test whether both given DroolsRuleModel objects have the same ruleName and rulePackage (while not
 * being the same object or having the same code)
 */
public class DroolsRuleSameNameAndPackageBiPredicate implements BiPredicate<DroolsRuleModel, DroolsRuleModel>
{

	@Override
	public boolean test(final DroolsRuleModel rule1, final DroolsRuleModel rule2)
	{
		return !new EqualsBuilder()
					 				.append(rule1, rule2)
					 				.isEquals() &&
					 (new EqualsBuilder()
								  .append(rule1.getCode(), rule2.getCode())
								  .append(rule1.getVersion(), rule2.getVersion())
								  .append(rule1.getCurrentVersion(), rule2.getCurrentVersion())
								  .isEquals() ||
								  new EqualsBuilder()
												.append(rule1.getUuid(), rule2.getUuid())
												.append(rule1.getRulePackage(), rule2.getRulePackage())
												.append(rule1.getVersion(), rule2.getVersion())
												.append(rule1.getCurrentVersion(), rule2.getCurrentVersion())
												.isEquals());
	}

}
