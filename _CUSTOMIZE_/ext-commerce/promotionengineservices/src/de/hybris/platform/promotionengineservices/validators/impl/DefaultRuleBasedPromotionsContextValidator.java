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
package de.hybris.platform.promotionengineservices.validators.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotionengineservices.validators.RuleBasedPromotionsContextValidator;
import de.hybris.platform.ruleengine.dao.CatalogVersionToRuleEngineContextMappingDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.CatalogVersionToRuleEngineContextMappingModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default(Drools specific) implementation for the RuleBasedPromotionsContextValidator.
 */
public class DefaultRuleBasedPromotionsContextValidator implements RuleBasedPromotionsContextValidator
{
	private CatalogVersionToRuleEngineContextMappingDao catalogVersionToRuleEngineContextMappingDao;

	@Override
	public boolean isApplicable(final RuleBasedPromotionModel ruleBasedPromotion, final CatalogVersionModel catalogVersion,
			final RuleType ruleType)
	{
		checkArgument(nonNull(ruleBasedPromotion), "The provided ruleBasedPromotion cannot be NULL here");
		checkArgument(nonNull(catalogVersion), "The provided catalogVersion cannot be NULL here");
		checkArgument(nonNull(ruleType), "The provided ruleType cannot be NULL here");

		if(!(ruleBasedPromotion.getRule() instanceof DroolsRuleModel))
		{
			return false;
		}
		final DroolsRuleModel rule = (DroolsRuleModel) ruleBasedPromotion.getRule();
		if (isNotLinkedWithDroolsRule(ruleBasedPromotion) || isOutdated(ruleBasedPromotion) || isNull(rule.getKieBase()))
		{
			return false;
		}

		final Collection<CatalogVersionToRuleEngineContextMappingModel> mappings = getCatalogVersionToRuleEngineContextMappingDao()
				.findMappingsByCatalogVersion(Lists.newArrayList(catalogVersion), ruleType);

		final Set<DroolsRuleEngineContextModel> droolsRuleEngineContexts = mappings.stream()
				.map(CatalogVersionToRuleEngineContextMappingModel::getContext).filter(DroolsRuleEngineContextModel.class::isInstance)
				.map(DroolsRuleEngineContextModel.class::cast).collect(Collectors.toSet());

		final DroolsKIEBaseModel kieBase = rule.getKieBase();
		return droolsRuleEngineContexts.stream().filter(isValidKieSessionPredicate()).map(rec -> rec.getKieSession().getKieBase())
				.anyMatch(kieBase::equals);
	}

	protected boolean isOutdated(final RuleBasedPromotionModel ruleBasedPromotion)
	{
		return isNotTrue(ruleBasedPromotion.getRule().getCurrentVersion());
	}

	protected boolean isNotLinkedWithDroolsRule(final RuleBasedPromotionModel ruleBasedPromotion)
	{
		return !(ruleBasedPromotion.getRule() instanceof DroolsRuleModel);
	}

	protected Predicate<DroolsRuleEngineContextModel> isValidKieSessionPredicate()
	{
		return (DroolsRuleEngineContextModel rec) -> nonNull(rec.getKieSession());
	}

	protected CatalogVersionToRuleEngineContextMappingDao getCatalogVersionToRuleEngineContextMappingDao()
	{
		return catalogVersionToRuleEngineContextMappingDao;
	}

	@Required
	public void setCatalogVersionToRuleEngineContextMappingDao(
			final CatalogVersionToRuleEngineContextMappingDao catalogVersionToRuleEngineContextMappingDao)
	{
		this.catalogVersionToRuleEngineContextMappingDao = catalogVersionToRuleEngineContextMappingDao;
	}
}
