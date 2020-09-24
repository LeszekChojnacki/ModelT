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
package de.hybris.platform.promotionengineservices.dao;

import de.hybris.platform.promotionengineservices.model.CatForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.CombinedCatsForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedCatForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedProductForRuleModel;
import de.hybris.platform.promotionengineservices.model.ProductForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Data Access Object for looking up items related to {@link PromotionSourceRuleModel}.
 */
public interface PromotionSourceRuleDao
{

	/**
	 * Get all ProductForPromotionSourceRuleModels for given promotion source rule and module name.
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get products for
	 * @param baseName
	 * 		KIE base name
	 * @return list of {@link ProductForPromotionSourceRuleModel} for given rule
	 */
	List<ProductForPromotionSourceRuleModel> findAllProductForPromotionSourceRule(PromotionSourceRuleModel rule,
			String baseName);

	/**
	 * Get all CatForPromotionSourceRuleModels for given promotion source rule and module name
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get categories for
	 * @param baseName
	 * 		KIE base name
	 * @return list of {@link CatForPromotionSourceRuleModel} for given rule
	 */
	List<CatForPromotionSourceRuleModel> findAllCatForPromotionSourceRule(PromotionSourceRuleModel rule, String baseName);

	/**
	 * Get all ExcludedCatForRuleModels for given promotion source rule.
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get excluded categories for
	 * @param baseName
	 * 		KIE base name
	 * @return list of {@link ExcludedCatForRuleModel} for given rule
	 */
	List<ExcludedCatForRuleModel> findAllExcludedCatForPromotionSourceRule(PromotionSourceRuleModel rule, String baseName);

	/**
	 * Get all ExcludedProductForRuleModel for given promotion source rule.
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get excluded products for
	 * @param baseName
	 * 		KIE base name
	 * @return list of {@link ExcludedProductForRuleModel} for given rule
	 */
	List<ExcludedProductForRuleModel> findAllExcludedProductForPromotionSourceRule(PromotionSourceRuleModel rule,
			String baseName);

	/**
	 * Get all CombinedCatsForRuleModels for given promotion source rule.
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get excluded categories for
	 * @param baseName
	 * 		KIE base name
	 * @return list of {@link CombinedCatsForRuleModel} for given rule
	 */
	List<CombinedCatsForRuleModel> findAllCombinedCatsForRule(PromotionSourceRuleModel rule, String baseName);

	/**
	 * Finds RuleBasedPromotionModels for given product code and category codes (but will exclude all rules for which the
	 * corresponding {@link PromotionSourceRuleModel#getExcludeFromStorefrontDisplay()} is set to {@code true} OR current
	 * date is out of {@link PromotionSourceRuleModel#getStartDate()}, {@link PromotionSourceRuleModel#getEndDate()} date
	 * range OR the rule status {@link PromotionSourceRuleModel#getStatus()} is not PUBLISHED).
	 *
	 * @param promotionGroups
	 * 		collection of promotion groups to get promotion rules for
	 * @param productCode
	 * 		product code to get related source promotion rules
	 * @param categoryCodes
	 * 		category codes to get related source promotion rules
	 * @return List of {@link RuleBasedPromotionModel}
	 */
	List<RuleBasedPromotionModel> findPromotions(Collection<PromotionGroupModel> promotionGroups, String productCode,
			Set<String> categoryCodes);

	/**
	 * Get max conditionId in CombinedCatsForRule for one rule.
	 *
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to get max condition id for
	 * @return Integer representing maximal condition id
	 */
	Integer findLastConditionIdForRule(PromotionSourceRuleModel rule);
}
