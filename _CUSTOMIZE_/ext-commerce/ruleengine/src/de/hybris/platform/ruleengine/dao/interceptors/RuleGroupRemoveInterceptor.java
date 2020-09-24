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
package de.hybris.platform.ruleengine.dao.interceptors;


import de.hybris.platform.ruleengineservices.model.RuleGroupModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;

import org.springframework.beans.factory.annotation.Required;


/**
 * The remove interceptor implementation for RuleGroupModel items
 */
public class RuleGroupRemoveInterceptor implements RemoveInterceptor<RuleGroupModel>
{
	private L10NService l10NService;

	@Override
	public void onRemove(final RuleGroupModel ruleGroup, final InterceptorContext ctx) throws InterceptorException
	{
		if (!ruleGroup.getRules().isEmpty())
		{
			throw new InterceptorException(getL10NService().getLocalizedString("error.rulegroup.cantremovehasrules", new Object[]
			{ ruleGroup.getCode() }));
		}
	}

	protected L10NService getL10NService()
	{
		return l10NService;
	}

	@Required
	public void setL10NService(final L10NService l10nService)
	{
		l10NService = l10nService;
	}
}
