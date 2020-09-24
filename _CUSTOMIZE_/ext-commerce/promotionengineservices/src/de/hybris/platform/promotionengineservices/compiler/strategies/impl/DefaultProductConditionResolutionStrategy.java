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
import de.hybris.platform.promotionengineservices.model.ProductForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
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
 * Implementation of {@link ConditionResolutionStrategy} for y_qualifying_products condition. Values from "products"
 * parameter are stored with the rule as {@link ProductForPromotionSourceRuleModel}.
 */
public class DefaultProductConditionResolutionStrategy implements ConditionResolutionStrategy
{
	private ModelService modelService;
	private PromotionSourceRuleDao promotionSourceRuleDao;
	private RulesModuleDao rulesModuleDao;
	private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;

	@Override
	public void getAndStoreParameterValues(final RuleConditionData condition, final PromotionSourceRuleModel rule,
			final RuleBasedPromotionModel ruleBasedPromotion)
	{
		final RuleParameterData productsParameter = condition.getParameters().get(RAOConstants.PRODUCTS_PARAM);
		if (productsParameter == null)
		{
			return;
		}

		final List<String> productCodes = productsParameter.getValue();
		if (CollectionUtils.isNotEmpty(productCodes))
		{
			for (final String productCode : productCodes)
			{
				final ProductForPromotionSourceRuleModel productCodeForRule = getModelService()
						.create(ProductForPromotionSourceRuleModel.class);
				productCodeForRule.setProductCode(productCode);
				productCodeForRule.setRule(rule);
				productCodeForRule.setPromotion(ruleBasedPromotion);
				getModelService().save(productCodeForRule);
			}
		}
	}

	@Override
	public void cleanStoredParameterValues(final RuleCompilerContext context)
	{
		final List<ProductForPromotionSourceRuleModel> productForPromotionModels = getPromotionSourceRuleDao()
				.findAllProductForPromotionSourceRule((PromotionSourceRuleModel) context.getRule(),
						getKIEBaseName(context.getModuleName()));

		getModelService().removeAll(productForPromotionModels);
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
