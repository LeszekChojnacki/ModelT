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
/**
 *
 */
package de.hybris.platform.ruleengineservices.rule.dao.impl;

import static com.google.common.collect.ImmutableMap.of;

import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengineservices.model.RuleGroupModel;
import de.hybris.platform.ruleengineservices.model.RuleToEngineRuleTypeMappingModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleGroupDao;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;


/**
 * Default implementation of {@link RuleGroupDao}
 */
public class DefaultRuleGroupDao extends AbstractItemDao implements RuleGroupDao
{
	private static final String GET_ALL_RULE_GROUPS_EXCEPTING = "select {" + RuleGroupModel.PK + "} from {"
			+ RuleGroupModel._TYPECODE + "} where {" + RuleGroupModel.PK + "} not in ( ?groupIds )";

	private static final String GET_ALL_REFERRED_RULE_GROUPS = "select distinct {" + RuleGroupModel.PK + "} from {"
			+ RuleGroupModel._TYPECODE + " as rg join " + SourceRuleModel._TYPECODE + " as sr on {sr." + SourceRuleModel.RULEGROUP
			+ "} = {rg.pk}}";

	protected static final String GET_RULE_GROUP_FOR_ENGINE_RULE_TYPE = "select distinct {" + RuleGroupModel.PK + "} from {"
			+ RuleGroupModel._TYPECODE + " as rg join " + SourceRuleModel._TYPECODE + " as sr on {sr." + SourceRuleModel.RULEGROUP
			+ "} = {rg." + RuleGroupModel.PK + "} join " + RuleToEngineRuleTypeMappingModel._TYPECODE + " as r2re on {r2re."
			+ RuleToEngineRuleTypeMappingModel.RULETYPE + "} = {sr." + SourceRuleModel.ITEMTYPE + "}} where {r2re."
			+ RuleToEngineRuleTypeMappingModel.ENGINERULETYPE + "} = ?engineRuleType";

	protected static final String GET_ALL_RULE_GROUPS_QUERY = "select {" + RuleGroupModel.PK + "} from {" // NOSONAR
			+ RuleGroupModel._TYPECODE + "}";

	protected static final String GET_RULE_GROUP_BY_CODE = " where {" + RuleGroupModel.CODE + "} = ?code"; // NOSONAR

	@Override
	public Optional<RuleGroupModel> findRuleGroupByCode(final String code)
	{
		if (StringUtils.isEmpty(code))
		{
			return Optional.empty();
		}
		else
		{
			final Map queryParams = of("code", code);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULE_GROUPS_QUERY + GET_RULE_GROUP_BY_CODE,
					queryParams);

			final SearchResult<RuleGroupModel> search = getFlexibleSearchService().search(query);
			final List<RuleGroupModel> rules = search.getResult();
			return rules.stream().findFirst();
		}
	}

	@Override
	public List<RuleGroupModel> findRuleGroupOfType(final RuleType engineRuleType)
	{
		final Map queryParams = of("engineRuleType", engineRuleType);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RULE_GROUP_FOR_ENGINE_RULE_TYPE, queryParams);
		final SearchResult<RuleGroupModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	protected List<RuleGroupModel> findAllReferredRuleGroups()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_REFERRED_RULE_GROUPS);
		final SearchResult<RuleGroupModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}

	@Override
	public List<RuleGroupModel> findAllNotReferredRuleGroups()
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_ALL_RULE_GROUPS_EXCEPTING,
				Collections.singletonMap("groupIds", findAllReferredRuleGroups()));
		final SearchResult<RuleGroupModel> search = getFlexibleSearchService().search(query);
		return search.getResult();
	}
}
