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
package de.hybris.platform.promotionengineservices.dao.impl;

import static java.util.Collections.singletonList;

import de.hybris.platform.promotionengineservices.dao.PromotionSourceRuleDao;
import de.hybris.platform.promotionengineservices.model.CatForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.CombinedCatsForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedCatForRuleModel;
import de.hybris.platform.promotionengineservices.model.ExcludedProductForRuleModel;
import de.hybris.platform.promotionengineservices.model.ProductForPromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;


public class DefaultPromotionSourceRuleDao extends AbstractItemDao implements PromotionSourceRuleDao
{
	protected static final String FILTER_BY_RULE_AND_MODULE_NAME = " AND EXISTS ({{ SELECT {dr." + DroolsRuleModel.PROMOTION
			+ "} FROM {" + RuleBasedPromotionModel._TYPECODE + " as rbp JOIN " + DroolsRuleModel._TYPECODE + " as dr ON {dr."
			+ DroolsRuleModel.PK + "} = {rbp." + RuleBasedPromotionModel.RULE + "} JOIN " + DroolsKIEBaseModel._TYPECODE
			+ " as kb ON {kb." + DroolsKIEBaseModel.PK + "} = {dr." + DroolsRuleModel.KIEBASE + "} JOIN "
			+ AbstractRuleModel._TYPECODE + " as rule ON {rule." + AbstractRuleModel.PK + "} = {dr." + DroolsRuleModel.SOURCERULE
			+ "} } " + "WHERE {kb." + DroolsKIEBaseModel.NAME + "} = ?name AND {rule." + AbstractRuleModel.CODE + "} = ?ruleCode}})";

	protected static final String GET_ALL_PRODUCTS_FOR_RULE = "SELECT {pfp." + ProductForPromotionSourceRuleModel.PK + "} " // NOSONAR
			+ "FROM {" + ProductForPromotionSourceRuleModel._TYPECODE + " as pfp JOIN " + AbstractRuleModel._TYPECODE
			+ " as r ON {pfp." + ProductForPromotionSourceRuleModel.RULE + "} = {r." + AbstractRuleModel.PK + "} } WHERE {r."
			+ AbstractRuleModel.CODE + "} = ?ruleCode"; // NOSONAR

	protected static final String GET_ALL_PRODUCTS_FOR_RULE_AND_MODULE = GET_ALL_PRODUCTS_FOR_RULE
			+ FILTER_BY_RULE_AND_MODULE_NAME;

	protected static final String GET_ALL_CATEGORIES_FOR_RULE =
			"SELECT {cfp." + CatForPromotionSourceRuleModel.PK + "} " + "FROM {" // NOSONAR
					+ CatForPromotionSourceRuleModel._TYPECODE + " as cfp JOIN " + AbstractRuleModel._TYPECODE + " as r ON {cfp."
					+ CatForPromotionSourceRuleModel.RULE + "} = {r." + AbstractRuleModel.PK + "} } WHERE {r." + AbstractRuleModel.CODE
					+ "} = ?ruleCode"; // NOSONAR

	protected static final String GET_ALL_CATEGORIES_FOR_RULE_AND_MODULE = GET_ALL_CATEGORIES_FOR_RULE
			+ FILTER_BY_RULE_AND_MODULE_NAME;

	protected static final String GET_ALL_EXCLUDED_CATEGORIES_FOR_RULE = "SELECT {cfp." + ExcludedCatForRuleModel.PK + "} " // NOSONAR
			+ "FROM {" + ExcludedCatForRuleModel._TYPECODE + " as cfp JOIN " + AbstractRuleModel._TYPECODE + " as r ON {cfp."
			+ ExcludedCatForRuleModel.RULE + "} = {r." + AbstractRuleModel.PK + "} } WHERE {r." + AbstractRuleModel.CODE
			+ "} = ?ruleCode"; // NOSONAR

	protected static final String GET_ALL_EXCLUDED_CATEGORIES_FOR_RULE_AND_MODULE = GET_ALL_EXCLUDED_CATEGORIES_FOR_RULE
			+ FILTER_BY_RULE_AND_MODULE_NAME;

	protected static final String GET_ALL_EXCLUDED_PRODUCTS_FOR_RULE =
			"SELECT {pfp." + ExcludedProductForRuleModel.PK + "} " // NOSONAR
					+ "FROM {" + ExcludedProductForRuleModel._TYPECODE + " as pfp JOIN " + AbstractRuleModel._TYPECODE
					+ " as r ON {pfp." + ExcludedProductForRuleModel.RULE + "} = {r." + AbstractRuleModel.PK + "} } WHERE {r."
					+ AbstractRuleModel.CODE // NOSONAR
					+ "} = ?ruleCode"; // NOSONAR

	protected static final String GET_ALL_EXCLUDED_PRODUCTS_FOR_RULE_AND_MODULE = GET_ALL_EXCLUDED_PRODUCTS_FOR_RULE
			+ FILTER_BY_RULE_AND_MODULE_NAME;

	protected static final String GET_ALL_COMBINED_CATS_FOR_RULE =
			"SELECT {cfp." + CombinedCatsForRuleModel.PK + "} " + "FROM {" // NOSONAR
					+ CombinedCatsForRuleModel._TYPECODE + " as cfp JOIN " + AbstractRuleModel._TYPECODE + " as r ON {cfp."
					+ CombinedCatsForRuleModel.RULE + "} = {r." + AbstractRuleModel.PK + "} } WHERE {r." + AbstractRuleModel.CODE
					+ "} = ?ruleCode"; // NOSONAR

	protected static final String GET_ALL_COMBINED_CATS_FOR_RULE_AND_MODULE = GET_ALL_COMBINED_CATS_FOR_RULE
			+ FILTER_BY_RULE_AND_MODULE_NAME;

	protected static final String GET_LAST_CONDITIONID_FOR_RULE = "SELECT max({" + CombinedCatsForRuleModel.CONDITIONID
			+ "}) FROM {" + CombinedCatsForRuleModel._TYPECODE + "} WHERE {rule} = ?rule";

	protected static final String FIND_PROMOTIONS_SELECT = "SELECT {rel.promotion} as pr " + "FROM {" // NOSONAR
			+ PromotionSourceRuleModel._TYPECODE + " as r}, ";

	protected static final String FIND_PROMOTIONS_WHERE1 = "WHERE {rel.rule} = {r.pk} ";

	protected static final String FIND_PROMOTIONS_WHERE2 = "AND {r.status} =?statusPublished AND {r.excludeFromStorefrontDisplay} !=?excludeFromStorefrontDisplay AND {r.website} IN (?promotionGroups) ";

	protected static final String FIND_PROMOTIONS_WITHIN_START_END_DATES = "AND ( {r.startDate} <= ?nowDate OR {r.startDate} IS NULL) AND ( {r.endDate} >= ?nextMinuteDate OR {r.endDate} IS NULL) ";

	protected static final String FIND_PUBLISHED_PROMOTIONS = "AND {r.status} IN (?publishedPromotionStatuses) ";

	protected static final String FIND_PROMOTIONS_PROD_SELECT = "{" + ProductForPromotionSourceRuleModel._TYPECODE + " as rel} ";

	protected static final String FIND_PROMOTIONS_PROD_WHERE = "AND {rel.productCode} = ?productCode ";

	protected static final String FIND_PROMOTIONS_CAT_SELECT = "{" + CatForPromotionSourceRuleModel._TYPECODE + " as rel} ";

	protected static final String FIND_PROMOTIONS_CAT_WHERE = "AND {rel.categoryCode} IN (?categoryCodes) ";

	protected static final String FIND_PROMOTIONS_COMB_CAT_QUERY = "SELECT promotion as pr " + "FROM ({{ "
			+ "SELECT {cc1.rule} as r1, {cc1.promotion} as promotion, {cc1.conditionId} as c1, count({cc1.categoryCode}) as count_cat1 "
			+ "FROM {" + PromotionSourceRuleModel._TYPECODE + " as r}, {" + CombinedCatsForRuleModel._TYPECODE + " as cc1} "// NOSONAR
			+ "WHERE {cc1.categoryCode} in (?categoryCodes) " + "AND {r.pk} = {cc1.rule} " + FIND_PROMOTIONS_WHERE2
			+ FIND_PROMOTIONS_WITHIN_START_END_DATES + "GROUP BY {cc1.rule}, {cc1.promotion}, {cc1.conditionId} " + "}}) a "
			+ "WHERE EXISTS " + "({{ " + "SELECT {cc2.rule}, {cc2.promotion}, {cc2.conditionId}, count({cc2.categoryCode}) "
			+ "FROM {" + PromotionSourceRuleModel._TYPECODE + " as r}, {" + CombinedCatsForRuleModel._TYPECODE + " as cc2} "// NOSONAR
			+ "WHERE {cc2.rule} = r1 " + "AND {cc2.conditionId} = c1 " + "AND {r.pk} = {cc2.rule} " + FIND_PROMOTIONS_WHERE2
			+ FIND_PROMOTIONS_WITHIN_START_END_DATES + "GROUP BY {cc2.rule}, {cc2.promotion}, {cc2.conditionId} "
			+ "HAVING count({cc2.categoryCode}) = count_cat1 " + "}}) " + "AND NOT EXISTS ({{ SELECT 1 FROM {"
			+ ExcludedProductForRuleModel._TYPECODE + "} WHERE {rule} = r1 AND {productCode} = ?productCode }}) "
			+ "AND NOT EXISTS ({{ SELECT 1 FROM {" + ExcludedCatForRuleModel._TYPECODE
			+ "} WHERE {rule} = r1 AND {categoryCode} IN (?categoryCodes) }}) ";

	//exclude rules where product or it's categories are excluded
	protected static final String EXCLUDE_PRODUCTS_WHERE = "AND NOT EXISTS ({{ SELECT 1 FROM {ExcludedProductForRule} WHERE {rule} = {rel.rule} AND {productCode} = ?productCode }}) ";
	protected static final String EXCLUDE_CAT_WHERE = "AND NOT EXISTS ({{ SELECT 1 FROM {ExcludedCatForRule} WHERE {rule} = {rel.rule} AND {categoryCode} IN (?categoryCodes) }}) ";
	protected static final String EXCLUDE_NULL_PRODUCT_PROMOTIONS = "AND {rel.promotion} IS NOT NULL ";
	protected static final String EXCLUDE_NULL_RESULTS_PROMOTIONS = "WHERE x.pr IS NOT NULL ";
	protected static final String FIND_PROMOTIONS_PROD_QUERY = FIND_PROMOTIONS_SELECT + FIND_PROMOTIONS_PROD_SELECT
			+ FIND_PROMOTIONS_WHERE1 + FIND_PROMOTIONS_WHERE2 + FIND_PROMOTIONS_PROD_WHERE + FIND_PROMOTIONS_WITHIN_START_END_DATES
			+ FIND_PUBLISHED_PROMOTIONS + EXCLUDE_NULL_PRODUCT_PROMOTIONS;
	protected static final String FIND_PROMOTIONS_CAT_QUERY = FIND_PROMOTIONS_SELECT + FIND_PROMOTIONS_CAT_SELECT
			+ FIND_PROMOTIONS_WHERE1 + FIND_PROMOTIONS_WHERE2 + FIND_PROMOTIONS_CAT_WHERE + FIND_PROMOTIONS_WITHIN_START_END_DATES
			+ EXCLUDE_PRODUCTS_WHERE + EXCLUDE_CAT_WHERE;

	protected static final String FIND_PROMOTIONS_UNION1 = "SELECT x.pr FROM ( {{";
	protected static final String FIND_PROMOTIONS_UNION2 = "}} UNION {{";
	protected static final String FIND_PROMOTIONS_UNION3 = "}}) x " + EXCLUDE_NULL_RESULTS_PROMOTIONS;

	protected static final String RULE_CODE_PARAM = "ruleCode";

	@Override
	public List<ProductForPromotionSourceRuleModel> findAllProductForPromotionSourceRule(final PromotionSourceRuleModel rule,
			final String baseName)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(RULE_CODE_PARAM, rule.getCode());
		params.put(DroolsKIEBaseModel.NAME, baseName);

		final SearchResult<ProductForPromotionSourceRuleModel> searchResult = getFlexibleSearchService()
				.search(GET_ALL_PRODUCTS_FOR_RULE_AND_MODULE, params);
		return searchResult.getResult();
	}

	@Override
	public List<CatForPromotionSourceRuleModel> findAllCatForPromotionSourceRule(final PromotionSourceRuleModel rule,
			final String baseName)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(RULE_CODE_PARAM, rule.getCode());
		params.put(DroolsKIEBaseModel.NAME, baseName);

		final SearchResult<CatForPromotionSourceRuleModel> searchResult = getFlexibleSearchService()
				.search(GET_ALL_CATEGORIES_FOR_RULE_AND_MODULE, params);
		return searchResult.getResult();
	}

	@Override
	public List<ExcludedCatForRuleModel> findAllExcludedCatForPromotionSourceRule(final PromotionSourceRuleModel rule,
			final String baseName)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(RULE_CODE_PARAM, rule.getCode());
		params.put(DroolsKIEBaseModel.NAME, baseName);

		final SearchResult<ExcludedCatForRuleModel> searchResult = getFlexibleSearchService()
				.search(GET_ALL_EXCLUDED_CATEGORIES_FOR_RULE_AND_MODULE, params);
		return searchResult.getResult();
	}

	@Override
	public List<CombinedCatsForRuleModel> findAllCombinedCatsForRule(final PromotionSourceRuleModel rule, final String baseName)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(RULE_CODE_PARAM, rule.getCode());
		params.put(DroolsKIEBaseModel.NAME, baseName);

		final SearchResult<CombinedCatsForRuleModel> searchResult = getFlexibleSearchService()
				.search(GET_ALL_COMBINED_CATS_FOR_RULE_AND_MODULE, params);
		return searchResult.getResult();
	}

	@Override
	public List<ExcludedProductForRuleModel> findAllExcludedProductForPromotionSourceRule(final PromotionSourceRuleModel rule,
			final String baseName)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put(RULE_CODE_PARAM, rule.getCode());
		params.put(DroolsKIEBaseModel.NAME, baseName);

		final SearchResult<ExcludedProductForRuleModel> searchResult = getFlexibleSearchService()
				.search(GET_ALL_EXCLUDED_PRODUCTS_FOR_RULE_AND_MODULE, params);
		return searchResult.getResult();
	}

	@Override
	public List<RuleBasedPromotionModel> findPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final String productCode, final Set<String> categoryCodes)
	{
		return findPromotions(promotionGroups, productCode, categoryCodes, new Date());
	}

	@Override
	public Integer findLastConditionIdForRule(final PromotionSourceRuleModel rule)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("rule", rule);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_LAST_CONDITIONID_FOR_RULE, params);
		query.setResultClassList(singletonList(Integer.class));

		final SearchResult<Integer> searchResult = getFlexibleSearchService().search(query);
		return searchResult.getResult().get(0);
	}

	protected List<RuleBasedPromotionModel> findPromotions(final Collection<PromotionGroupModel> promotionGroups,
			final String productCode, final Set<String> categoryCodes, final Date currentDate)
	{
		final Map<String, Object> params = new HashMap<>();
		params.put("productCode", productCode);
		params.put("statusPublished", RuleStatus.PUBLISHED);
		params.put("promotionGroups", promotionGroups);
		params.put("publishedPromotionStatuses", new HashSet<>(singletonList(RuleStatus.PUBLISHED)));
		params.put("nowDate", Date.from(currentDate.toInstant().truncatedTo(ChronoUnit.MINUTES)));
		params.put("nextMinuteDate", Date.from(currentDate.toInstant().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES)));
		// excluded flag is negated in flexible search in a "where flag is not TRUE" manner
		// (i.e. WHERE excludedFromStorefrontDisplay != ?excludeFromStorefrontDisplay)
		// as item attribute was added in a later release with default value FALSE, but we need to
		// account for NULL value as FALSE
		params.put("excludeFromStorefrontDisplay", Boolean.TRUE);

		final StringBuilder stringBuilder = new StringBuilder(200);
		stringBuilder.append(FIND_PROMOTIONS_PROD_QUERY);

		if (CollectionUtils.isNotEmpty(categoryCodes))
		{
			params.put("categoryCodes", categoryCodes);

			stringBuilder.insert(0, FIND_PROMOTIONS_UNION1);
			stringBuilder.append(FIND_PROMOTIONS_UNION2);
			stringBuilder.append(FIND_PROMOTIONS_CAT_QUERY);
			stringBuilder.append(FIND_PROMOTIONS_UNION2);
			stringBuilder.append(FIND_PROMOTIONS_COMB_CAT_QUERY);
			stringBuilder.append(FIND_PROMOTIONS_UNION3);
		}

		final SearchResult<RuleBasedPromotionModel> searchResult = getFlexibleSearchService().search(stringBuilder.toString(),
				params);

		return searchResult.getResult();
	}
}
