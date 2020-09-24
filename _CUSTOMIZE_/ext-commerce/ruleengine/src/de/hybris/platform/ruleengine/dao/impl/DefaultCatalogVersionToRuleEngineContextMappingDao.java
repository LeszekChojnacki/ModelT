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

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.dao.CatalogVersionToRuleEngineContextMappingDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.CatalogVersionToRuleEngineContextMappingModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Default implementation of the CatalogVersionToRuleEngineContextMappingDao interface
 */
public class DefaultCatalogVersionToRuleEngineContextMappingDao extends AbstractItemDao
		implements CatalogVersionToRuleEngineContextMappingDao
{

	private static final String SELECT = "SELECT {" + CatalogVersionToRuleEngineContextMappingModel.PK + "} from {"
			+ CatalogVersionToRuleEngineContextMappingModel._TYPECODE + "} ";
	private static final String WHERE_CATALOG_VERSION_IN_TEMPLATE = "WHERE {"
			+ CatalogVersionToRuleEngineContextMappingModel.CATALOGVERSION + "} IN (?catalogVersions)";
	private static final String WHERE_CONTEXT_IN_TEMPLATE = "WHERE {"
			+ CatalogVersionToRuleEngineContextMappingModel.CONTEXT + "} IN (?contexts)";

	@Override
	public Collection<CatalogVersionToRuleEngineContextMappingModel> findMappingsByCatalogVersion(
			final Collection<CatalogVersionModel> catalogVersions, final RuleType ruleType)
	{
		if (isEmpty(catalogVersions))
		{
			return Collections.emptyList();
		}
		final String queryString = SELECT + WHERE_CATALOG_VERSION_IN_TEMPLATE;
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("catalogVersions", catalogVersions);

		List<CatalogVersionToRuleEngineContextMappingModel> result = getFlexibleSearchService()
				.<CatalogVersionToRuleEngineContextMappingModel>search(query).getResult();

		if (nonNull(ruleType))
		{
			result = result.stream().filter(m -> filterMappingByRuleType(m.getContext(), ruleType)).collect(Collectors.toList());
		}
		return result;
	}

	@Override
	public Collection<CatalogVersionToRuleEngineContextMappingModel> findByContext(
			final Collection<AbstractRuleEngineContextModel> contexts)
	{
		if (isEmpty(contexts))
		{
			return Collections.emptyList();
		}
		final String queryString = SELECT + WHERE_CONTEXT_IN_TEMPLATE;
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("contexts", contexts);

		return getFlexibleSearchService()
				.<CatalogVersionToRuleEngineContextMappingModel>search(query).getResult();
	}

	protected boolean filterMappingByRuleType(final AbstractRuleEngineContextModel abstractContext, final RuleType ruleType)
	{
		if (abstractContext instanceof DroolsRuleEngineContextModel)
		{
			final DroolsRuleEngineContextModel context = (DroolsRuleEngineContextModel) abstractContext;
			return context.getKieSession().getKieBase() != null && //
					context.getKieSession().getKieBase().getKieModule() != null && //
					ruleType.equals(context.getKieSession().getKieBase().getKieModule().getRuleType());
		}
		else
		{
			return false;
		}
	}
}
