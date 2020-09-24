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
package de.hybris.platform.ruleengineservices.rule.interceptors;

import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import java.util.Objects;
import java.util.UUID;


/**
 * Prepare interceptor for AbstractRuleModel.
 */
public class RulePrepareInterceptor implements PrepareInterceptor<AbstractRuleModel>
{
	@Override
	public void onPrepare(final AbstractRuleModel model, final InterceptorContext context)
	{
		generateUuid(model, context);
	}

	protected void generateUuid(final AbstractRuleModel model, final InterceptorContext context)
	{
		if (context.isNew(model) && Objects.isNull(model.getUuid()))
		{
			final UUID uuid = UUID.randomUUID();
			model.setUuid(uuid.toString());
		}
	}

}
