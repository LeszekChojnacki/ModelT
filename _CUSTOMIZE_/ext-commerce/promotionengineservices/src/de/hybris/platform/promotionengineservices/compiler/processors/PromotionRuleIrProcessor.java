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
package de.hybris.platform.promotionengineservices.compiler.processors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.BooleanUtils.isFalse;

import de.hybris.platform.promotionengineservices.constants.PromotionEngineServicesConstants;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotions.PromotionsService;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessor;
import de.hybris.platform.ruleengineservices.compiler.RuleIrTypeCondition;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rao.WebsiteGroupRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.util.SharedParametersProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Maps;


/**
 * Adds required conditions to the intermediate representation.
 */
public class PromotionRuleIrProcessor implements RuleIrProcessor
{
	public static final String WEBSITE_GROUP_RAO_ID_ATTRIBUTE = "id";
	protected static final String ORDER_CONSUMED_RAO_CART_ATTRIBUTE = "cart";
	protected static final String AVAILABLE_QUANTITY_PARAM = "availableQuantity";
	protected static final String CART_RAO_CURRENCY_ATTRIBUTE = "currencyIsoCode";
	protected static final String CART_RAO_TOTAL_ATTRIBUTE = "total";

	private PromotionsService promotionsService;
	private SharedParametersProvider sharedParametersProvider;
	private boolean websiteGroupGenerationEnabled;

	@Override
	public void process(final RuleCompilerContext context, final RuleIr ruleIr)
	{
		final AbstractRuleModel sourceRule = context.getRule();

		if (sourceRule instanceof PromotionSourceRuleModel)
		{
			// add condition for cart

			final String cartRaoVariable = context.generateVariable(CartRAO.class);

			final RuleIrTypeCondition irCartCondition = new RuleIrTypeCondition();
			irCartCondition.setVariable(cartRaoVariable);

			final List<RuleIrCondition> conditions = ruleIr.getConditions();
			ruleIr.getConditions().add(irCartCondition);

			// add condition for rule engine result

			final String resultRaoVariable = context.generateVariable(RuleEngineResultRAO.class);

			final RuleIrTypeCondition irResultCondition = new RuleIrTypeCondition();
			irResultCondition.setVariable(resultRaoVariable);

			ruleIr.getConditions().add(irResultCondition);

			// add condition for website
			if (isWebsiteGroupGenerationEnabled())
			{
				PromotionGroupModel website = ((PromotionSourceRuleModel) sourceRule).getWebsite();

				if (website == null)
				{
					website = promotionsService.getPromotionGroup(PromotionEngineServicesConstants.DEFAULT_PROMOTION_GROUP_IDENTIFIER);
				}

				if (website == null)
				{
					throw new RuleCompilerException("Website associated with the promotion cannot be null or empty.");
				}

				final String websiteGroupRaoVariable = context.generateVariable(WebsiteGroupRAO.class);

				final RuleIrAttributeCondition irWebsiteGroupCondition = new RuleIrAttributeCondition();
				irWebsiteGroupCondition.setVariable(websiteGroupRaoVariable);
				irWebsiteGroupCondition.setAttribute(WEBSITE_GROUP_RAO_ID_ATTRIBUTE);
				irWebsiteGroupCondition.setOperator(RuleIrAttributeOperator.EQUAL);
				irWebsiteGroupCondition.setValue(website.getIdentifier());

				conditions.add(irWebsiteGroupCondition);
			}

			// add action parameters for cart
			final Map<String, Object> sharedParameters = getSharedParameters(context.getRuleConditions());

			if (!sharedParameters.isEmpty())
			{
				ruleIr.getActions()
						.stream()
						.filter(action -> (action instanceof RuleIrExecutableAction))
						.forEach(
								action -> {
									final Map<String, Object> actionParameters = Maps.newHashMap(((RuleIrExecutableAction) action)
											.getActionParameters());
									actionParameters.putAll(sharedParameters);
									((RuleIrExecutableAction) action).setActionParameters(actionParameters);
								});
			}
		}
	}

	protected Map<String, Object> getSharedParameters(final List<RuleConditionData> ruleConditionDataList)
	{
		final Map<String, Object> result = new HashMap<>();
		for (final RuleConditionData ruleConditionData : ruleConditionDataList)
		{
			final Map<String, RuleParameterData> parameters = ruleConditionData.getParameters();
			for (final String parameterName : sharedParametersProvider.getAll())
			{
				if (parameters.containsKey(parameterName))
				{
					final RuleParameterData ruleParameterData = parameters.get(parameterName);
					result.put(parameterName, ruleParameterData.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	protected boolean isRuleNonStackable(final RuleCompilerContext context)
	{
		final AbstractRuleModel rule = context.getRule();
		if (nonNull(rule))
		{
			return isFalse(rule.getStackable());
		}
		return Boolean.TRUE.booleanValue();
	}

	public PromotionsService getPromotionsService()
	{
		return promotionsService;
	}

	@Required
	public void setPromotionsService(final PromotionsService promotionsService)
	{
		this.promotionsService = promotionsService;
	}

	@Required
	public void setSharedParametersProvider(final SharedParametersProvider sharedParametersProvider)
	{
		this.sharedParametersProvider = sharedParametersProvider;
	}

	/**
	 * @deprecated added during patch release for performance optimization. From 1905 onwards this configuration will be
	 *             handled via the SwitchService (new in 1905)
	 */
	@Deprecated
	protected boolean isWebsiteGroupGenerationEnabled()
	{
		return websiteGroupGenerationEnabled;
	}

	/**
	 * @deprecated added during patch release for performance optimization. From 1905 onwards this configuration will be
	 *             handled via the SwitchService (new in 1905)
	 */
	@Deprecated
	@Required
	public void setWebsiteGroupGenerationEnabled(final boolean websiteGroupGenerationEnabled)
	{
		this.websiteGroupGenerationEnabled = websiteGroupGenerationEnabled;
	}
}
