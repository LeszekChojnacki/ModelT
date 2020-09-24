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
package de.hybris.platform.ruleengine.versioning.impl;

import static java.util.Objects.requireNonNull;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.versioning.HistoricalRuleContentProvider;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;


public abstract class AbstractHistoricalRuleContentProvider implements HistoricalRuleContentProvider
{

	protected <T extends Object> T getOriginal(final AbstractRuleEngineRuleModel droolsRule, final InterceptorContext context,
			final String attributeQualifier)
	{
		if (context.isModified(droolsRule, attributeQualifier))
		{
			final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(droolsRule);
			return modelContext.getOriginalValue(attributeQualifier);
		}
		final ModelService modelService = requireNonNull(context.getModelService());
		return modelService.getAttributeValue(droolsRule, attributeQualifier);
	}


}
