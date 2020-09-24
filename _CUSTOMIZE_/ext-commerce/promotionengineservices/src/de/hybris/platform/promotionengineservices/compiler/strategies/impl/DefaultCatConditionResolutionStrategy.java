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
package de.hybris.platform.promotionengineservices.compiler.strategies.impl;

import de.hybris.platform.promotionengineservices.compiler.strategies.ConditionResolutionStrategy;
import de.hybris.platform.promotionengineservices.dao.PromotionSourceRuleDao;
import de.hybris.platform.promotionengineservices.model.CatForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.CombinedCatsForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedCatForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedProductForRuleModel;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruledefinitions.CollectionOperator;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.strategies.DroolsKIEBaseFinderStrategy;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.util.RAOConstants;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link ConditionResolutionStrategy} for y_qualifying_conditions condition. Values from parameter
 * "categories" are stored with the rule as {@link CatForPromotionSourceRuleModel}. Values from parameter
 * "excluded_categories" are stored as {@link ExcludedCatForRuleModel}. Values from parameter "excluded_products" are
 * stored as {@link ExcludedProductForRuleModel}.
 */
public class DefaultCatConditionResolutionStrategy implements ConditionResolutionStrategy
{
	private ModelService modelService;
	private PromotionSourceRuleDao promotionSourceRuleDao;
	private RulesModuleDao rulesModuleDao;
	private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;

	@Override
	public void getAndStoreParameterValues(final RuleConditionData condition, final PromotionSourceRuleModel rule,
			final RuleBasedPromotionModel ruleBasedPromotion)
	{
		final RuleParameterData categoriesOperatorParameter = condition.getParameters().get(RAOConstants.CATEGORIES_OPERATOR_PARAM);
		final RuleParameterData categoriesParameter = condition.getParameters().get(RAOConstants.CATEGORIES_PARAM);
		final RuleParameterData excludedCategoriesParameter = condition.getParameters().get(RAOConstants.EXCLUDED_CATEGORIES_PARAM);
		final RuleParameterData excludedProductsParameter = condition.getParameters().get(RAOConstants.EXCLUDED_PRODUCTS_PARAM);

		if (categoriesOperatorParameter == null || categoriesParameter == null)
		{
			return;
		}

		final List<String> categoryCodes = categoriesParameter.getValue();
		final CollectionOperator categoriesOperator = categoriesOperatorParameter.getValue();

		if (CollectionUtils.isNotEmpty(categoryCodes))
		{
			processCategoriesOperatorParameter(rule, ruleBasedPromotion, categoryCodes, categoriesOperator);

			// Excluded categories
			if (excludedCategoriesParameter != null)
			{
				final List<String> excludedCategoryCodes = excludedCategoriesParameter.getValue();
				if (CollectionUtils.isNotEmpty(excludedCategoryCodes))
				{
					createExcludedCatForRule(rule, excludedCategoryCodes);
				}
			}

			// Excluded products
			if (excludedProductsParameter != null)
			{
				final List<String> excludedProductCodes = excludedProductsParameter.getValue();
				if (CollectionUtils.isNotEmpty(excludedProductCodes))
				{
					createExcludedProductForRule(rule, excludedProductCodes);
				}
			}
		}
	}

	protected void processCategoriesOperatorParameter(final PromotionSourceRuleModel rule,
			final RuleBasedPromotionModel ruleBasedPromotion, final List<String> categoryCodes,
			final CollectionOperator categoriesOperator)
	{
		if (CollectionOperator.CONTAINS_ALL.equals(categoriesOperator))
		{
			// operator ALL for categories - product should belong to all categories from categoryCodes -> use CombinedCatsForRule
			final Integer conditionId = getNextConditionId(rule);
			for (final String categoryCode : categoryCodes)
			{
				final CombinedCatsForRuleModel combinedCatCodeForRule = getModelService().create(CombinedCatsForRuleModel.class);
				combinedCatCodeForRule.setCategoryCode(categoryCode);
				combinedCatCodeForRule.setRule(rule);
				combinedCatCodeForRule.setConditionId(conditionId);
				combinedCatCodeForRule.setPromotion(ruleBasedPromotion);
				getModelService().save(combinedCatCodeForRule);
			}
		}
		else
		{
			// if normal list of categories, with operator ANY, just insert them in CatForPromotionSourceRule
			for (final String categoryCode : categoryCodes)
			{
				final CatForPromotionSourceRuleModel catCodeForRule = getModelService().create(CatForPromotionSourceRuleModel.class);
				catCodeForRule.setCategoryCode(categoryCode);
				catCodeForRule.setRule(rule);
				catCodeForRule.setPromotion(ruleBasedPromotion);
				getModelService().save(catCodeForRule);
			}
		}
	}

	protected void createExcludedProductForRule(final PromotionSourceRuleModel rule, final List<String> excludedProductCodes)
	{
		for (final String excludedProductCode : excludedProductCodes)
		{
			final ExcludedProductForRuleModel excludedProductForRule = getModelService().create(ExcludedProductForRuleModel.class);
			excludedProductForRule.setProductCode(excludedProductCode);
			excludedProductForRule.setRule(rule);
			getModelService().save(excludedProductForRule);
		}
	}

	protected void createExcludedCatForRule(final PromotionSourceRuleModel rule, final List<String> excludedCategoryCodes)
	{
		for (final String excludedCategoryCode : excludedCategoryCodes)
		{
			final ExcludedCatForRuleModel excludedCatForRule = getModelService().create(ExcludedCatForRuleModel.class);
			excludedCatForRule.setCategoryCode(excludedCategoryCode);
			excludedCatForRule.setRule(rule);
			getModelService().save(excludedCatForRule);
		}
	}

	@Override
	public void cleanStoredParameterValues(final RuleCompilerContext context)
	{
		final PromotionSourceRuleModel rule = (PromotionSourceRuleModel) context.getRule();

		final String kieBaseName = getKIEBaseName(context.getModuleName());

		final List<CatForPromotionSourceRuleModel> catForPromotionModels = getPromotionSourceRuleDao()
				.findAllCatForPromotionSourceRule(rule, kieBaseName);
		getModelService().removeAll(catForPromotionModels);

		final List<ExcludedCatForRuleModel> excludedCatForRule = getPromotionSourceRuleDao()
				.findAllExcludedCatForPromotionSourceRule(rule, kieBaseName);
		getModelService().removeAll(excludedCatForRule);

		final List<ExcludedProductForRuleModel> excludedProductForRule = getPromotionSourceRuleDao()
				.findAllExcludedProductForPromotionSourceRule(rule, kieBaseName);
		getModelService().removeAll(excludedProductForRule);

		final List<CombinedCatsForRuleModel> combinedCatsForRule = getPromotionSourceRuleDao()
				.findAllCombinedCatsForRule(rule, kieBaseName);
		getModelService().removeAll(combinedCatsForRule);
	}

	protected Integer getNextConditionId(final PromotionSourceRuleModel rule)
	{
		final Integer lastConditionId = getPromotionSourceRuleDao().findLastConditionIdForRule(rule);
		if (lastConditionId == null)
		{
			return Integer.valueOf(1);
		}
		return Integer.valueOf(lastConditionId.intValue() + 1);
	}

	protected String getKIEBaseName(final String moduleName)
	{
		final DroolsKIEModuleModel droolsKIEModule = getRulesModuleDao().findByName(moduleName);
		final DroolsKIEBaseModel kieBaseForKIEModule = getDroolsKIEBaseFinderStrategy().getKIEBaseForKIEModule(droolsKIEModule);
		return kieBaseForKIEModule.getName();
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

	protected PromotionSourceRuleDao getPromotionSourceRuleDao()
	{
		return promotionSourceRuleDao;
	}

	@Required
	public void setPromotionSourceRuleDao(final PromotionSourceRuleDao promotionSourceRuleDao)
	{
		this.promotionSourceRuleDao = promotionSourceRuleDao;
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected DroolsKIEBaseFinderStrategy getDroolsKIEBaseFinderStrategy()
	{
		return droolsKIEBaseFinderStrategy;
	}

	@Required
	public void setDroolsKIEBaseFinderStrategy(final DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy)
	{
		this.droolsKIEBaseFinderStrategy = droolsKIEBaseFinderStrategy;
	}
}



