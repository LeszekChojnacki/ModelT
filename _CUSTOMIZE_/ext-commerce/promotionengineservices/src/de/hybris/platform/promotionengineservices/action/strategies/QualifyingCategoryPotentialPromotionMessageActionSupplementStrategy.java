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
package de.hybris.platform.promotionengineservices.action.strategies;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.ActionSupplementStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * The class supplements {@link DisplayMessageRAO} with dynamically-evaluated data for Qualifying Category Products
 * condition.
 *
 */
public class QualifyingCategoryPotentialPromotionMessageActionSupplementStrategy implements ActionSupplementStrategy
{
	protected static final String CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER = "qualifying_categorizied_products_quantity";
	protected static final String CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER_UUID = CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER
			+ UUID_SUFFIX;
	protected static final String CATEGORIES_PARAMETER = "qualifying_categories";
	protected static final String CATEGORIES_PARAMETER_UUID = CATEGORIES_PARAMETER + UUID_SUFFIX;

	@Override
	public boolean isActionProperToHandle(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		return isNotEmpty(context.getParameters()) && actionRao instanceof DisplayMessageRAO
				&& isMessageForQualifiedCategory(context.getParameters());
	}

	@Override
	public void postProcessAction(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		Preconditions.checkArgument(isActionProperToHandle(actionRao, context), "The strategy is not proper to handle the action.");

		final DisplayMessageRAO displayMessageRAO = (DisplayMessageRAO) actionRao;
		if (displayMessageRAO.getParameters() == null)
		{
			displayMessageRAO.setParameters(new HashMap<String, Object>());
		}

		final Integer targetItemQuantity = (Integer) context.getParameter(CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER);
		final List<String> conditionProductCategory = (List<String>) context.getParameter(CATEGORIES_PARAMETER);
		final CartRAO cartRao = context.getCartRao();
		final int actualItemQuantity = cartRao.getEntries().stream()
				.filter(e -> e.getProduct().getCategories().stream().anyMatch(c -> conditionProductCategory.contains(c.getCode())))
				.mapToInt(e -> e.getQuantity()).sum();
		if (targetItemQuantity.intValue() > actualItemQuantity)
		{
			displayMessageRAO.getParameters().put(context.getParameter(CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER_UUID).toString(),
					Integer.valueOf(targetItemQuantity.intValue() - actualItemQuantity));
		}
	}

	protected boolean isMessageForQualifiedCategory(final Map<String, Object> parameters)
	{
		return hasNotNullParameter(parameters, CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER)
				&& parameters.containsKey(CATEGORIZIED_PRODUCTS_QUANTITY_PARAMETER_UUID)
				&& hasNotNullParameter(parameters, CATEGORIES_PARAMETER) && parameters.containsKey(CATEGORIES_PARAMETER_UUID);
	}

	protected boolean hasNotNullParameter(final Map<String, Object> parameters, final String paramName)
	{
		return parameters.containsKey(paramName) && parameters.get(paramName) != null;
	}
}
