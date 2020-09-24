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

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.function.BiPredicate;


/**
 * Validate interceptor for DroolsKIEBase. Validates that the KIEBase has a corresponding KIEModule set and that no two
 * rules within the base have the same name and package.
 */
public class DroolsKIEBaseValidateInterceptor implements ValidateInterceptor<DroolsKIEBaseModel>
{

	// the test predicate to check if two rules have the same name and package
	private BiPredicate<DroolsRuleModel, DroolsRuleModel> sameNameAndPackageBiPredicate;

	@Override
	public void onValidate(final DroolsKIEBaseModel base, final InterceptorContext context) throws InterceptorException
	{
		if (base.getKieModule() == null)
		{
			throw new InterceptorException("DroolsKIEBase: " + base.getName() + " must have DroolsKIEModule set!");
		}

		if (CollectionUtils.isEmpty(base.getRules()))
		{
			return;
		}
		for (final DroolsRuleModel rule1 : base.getRules())
		{
			for (final DroolsRuleModel rule2 : base.getRules())
			{
				if (getSameNameAndPackageBiPredicate().test(rule1, rule2))
				{
					throw new InterceptorException("cannot add DroolsRule with codes:" + rule1.getCode() + ", " + rule2.getCode()
							+ " to DroolsKIEBase:" + base.getName() + "! Both rules have the same name and package declaration.");
				}
			}
		}
	}

	protected BiPredicate<DroolsRuleModel, DroolsRuleModel> getSameNameAndPackageBiPredicate()
	{
		return sameNameAndPackageBiPredicate;
	}

	@Required
	public void setSameNameAndPackageBiPredicate(final BiPredicate<DroolsRuleModel, DroolsRuleModel> sameNameAndPackageBiPredicate)
	{
		this.sameNameAndPackageBiPredicate = sameNameAndPackageBiPredicate;
	}



}
