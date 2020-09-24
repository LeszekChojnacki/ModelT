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
package de.hybris.platform.promotionengineservices.compiler.listeners;

import de.hybris.platform.promotionengineservices.compiler.strategies.ConditionResolutionStrategy;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerListener;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rao.WebsiteGroupRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link RuleCompilerListener} that adds variables that are required for promotions. It parses and
 * stores parameter values for specific conditions of the rule, like product and category codes, so that they can be
 * used later.
 *
 */
public class PromotionRuleCompilerListener implements RuleCompilerListener
{
	private Map<String, ConditionResolutionStrategy> conditionResolutionStrategies;
	private ModelService modelService;
	private EngineRuleDao engineRuleDao;

	@Override
	public void beforeCompile(final RuleCompilerContext context)
	{
		if (context.getRule() instanceof PromotionSourceRuleModel)
		{
			context.generateVariable(CartRAO.class);
			context.generateVariable(RuleEngineResultRAO.class);
			context.generateVariable(WebsiteGroupRAO.class);

			//clean previously stored parameter values for this rule
			cleanStoredParameterValues(context);
		}
	}
	
	/**
	 * Cleans previously stored parameter values for given context for all conditions.
	 * 
	 * @param context
	 *           {@link RuleCompilerContext} to clean the parameter values for.
	 */
	protected void cleanStoredParameterValues(final RuleCompilerContext context)
	{
		if (MapUtils.isNotEmpty(getConditionResolutionStrategies()))
		{
			for (final ConditionResolutionStrategy strategy : getConditionResolutionStrategies().values())
			{
				strategy.cleanStoredParameterValues(context);
			}
		}
	}

	@Override
	public void afterCompile(final RuleCompilerContext context)
	{
		if (context.getRule() instanceof PromotionSourceRuleModel && MapUtils.isNotEmpty(getConditionResolutionStrategies()))
		{
			extractAndStoreParamValues((PromotionSourceRuleModel) context.getRule(), context.getModuleName(),
					context.getRuleConditions());
		}
	}

	/**
	 * Parse rule parameters of specific types (like list of products or categories) and store them in the database.
	 *
	 * @param rule
	 *           rule to get and store parameter values for
	 * @param conditions
	 *           rule conditions
	 */
	protected void extractAndStoreParamValues(final PromotionSourceRuleModel rule, final String moduleName,
			final List<RuleConditionData> conditions)
	{
		if (CollectionUtils.isNotEmpty(conditions))
		{
			final AbstractRuleEngineRuleModel engineRule = getEngineRuleDao().getRuleByCode(rule.getCode(), moduleName);
			for (final RuleConditionData condition : conditions)
			{
				final ConditionResolutionStrategy strategy = getConditionResolutionStrategies().get(condition.getDefinitionId());
				if (strategy != null)
				{
					strategy.getAndStoreParameterValues(condition, rule, engineRule.getPromotion());
				}
			}
		}
	}

	@Override
	public void afterCompileError(final RuleCompilerContext context)
	{
		// NOOP
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected Map<String, ConditionResolutionStrategy> getConditionResolutionStrategies()
	{
		return conditionResolutionStrategies;
	}

	@Required
	public void setConditionResolutionStrategies(final Map<String, ConditionResolutionStrategy> conditionResolutionStrategies)
	{
		this.conditionResolutionStrategies = conditionResolutionStrategies;
	}
}
