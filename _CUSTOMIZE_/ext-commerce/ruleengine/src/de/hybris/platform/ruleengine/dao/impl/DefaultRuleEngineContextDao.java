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
package de.hybris.platform.ruleengine.dao.impl;

import de.hybris.platform.ruleengine.dao.RuleEngineContextDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;


/**
 * Provides a default dao implementation for rule context objects
 */
public class DefaultRuleEngineContextDao extends AbstractItemDao implements RuleEngineContextDao
{

	private static final String GET_CONTEXT_BY_NAME = "select {" + AbstractRuleEngineContextModel.PK + "} from {"
			+ AbstractRuleEngineContextModel._TYPECODE + "} where {" + AbstractRuleEngineContextModel.NAME + "} = ?name";

	private static final String GET_CONTEXT_BY_RULES_MODULE = "select {" + DroolsRuleEngineContextModel.PK + "} from {"
			+ DroolsRuleEngineContextModel._TYPECODE + " as ctx JOIN " + DroolsKIESessionModel._TYPECODE + " as s ON {ctx."
			+ DroolsRuleEngineContextModel.KIESESSION + "} = {s." + DroolsKIESessionModel.PK + "}" + " JOIN "
			+ DroolsKIEBaseModel._TYPECODE + " as kb ON {s." + DroolsKIESessionModel.KIEBASE + "} = {kb." + DroolsKIEBaseModel.PK
			+ "}} where {kb." + DroolsKIEBaseModel.KIEMODULE + "} = ?module";

	@Override
	public AbstractRuleEngineContextModel findRuleEngineContextByName(final String name)
	{
		final Map queryParams = ImmutableMap.of("name", name);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_CONTEXT_BY_NAME, queryParams);
		return getFlexibleSearchService().searchUnique(query);
	}

	@Override
	public <T extends AbstractRuleEngineContextModel> List<T> findRuleEngineContextByRulesModule(
			final AbstractRulesModuleModel rulesModule)
	{
		final Map queryParams = ImmutableMap.of("module", rulesModule);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_CONTEXT_BY_RULES_MODULE, queryParams);
		return getFlexibleSearchService().<T>search(query).getResult();
	}
}
