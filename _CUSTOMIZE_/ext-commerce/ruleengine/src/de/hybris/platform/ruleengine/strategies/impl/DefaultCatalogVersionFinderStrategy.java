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
package de.hybris.platform.ruleengine.strategies.impl;

import static java.util.stream.Collectors.toList;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.dao.CatalogVersionToRuleEngineContextMappingDao;
import de.hybris.platform.ruleengine.dao.RuleEngineContextDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.CatalogVersionToRuleEngineContextMappingModel;
import de.hybris.platform.ruleengine.strategies.CatalogVersionFinderStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the {@link CatalogVersionFinderStrategy}
 */
public class DefaultCatalogVersionFinderStrategy implements CatalogVersionFinderStrategy
{

	private CatalogVersionToRuleEngineContextMappingDao catalogVersionToRuleEngineContextMappingDao;
	private RuleEngineContextDao ruleEngineContextDao;

	@Override
	public List<CatalogVersionModel> findCatalogVersionsByRulesModule(final AbstractRulesModuleModel rulesModule)
	{
		final List<AbstractRuleEngineContextModel> ruleEngineContextList = getRuleEngineContextDao()
				.findRuleEngineContextByRulesModule(rulesModule);

		if (CollectionUtils.isNotEmpty(ruleEngineContextList))
		{
			final Collection<CatalogVersionToRuleEngineContextMappingModel> catalogVersionsToRuleEngineMappings = getCatalogVersionToRuleEngineContextMappingDao()
					.findByContext(ruleEngineContextList);

			return catalogVersionsToRuleEngineMappings.stream()
					.map(CatalogVersionToRuleEngineContextMappingModel::getCatalogVersion).filter(Objects::nonNull).distinct()
					.collect(toList());
		}
		return Collections.emptyList();
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

	protected RuleEngineContextDao getRuleEngineContextDao()
	{
		return ruleEngineContextDao;
	}

	@Required
	public void setRuleEngineContextDao(final RuleEngineContextDao ruleEngineContextDao)
	{
		this.ruleEngineContextDao = ruleEngineContextDao;
	}
}
