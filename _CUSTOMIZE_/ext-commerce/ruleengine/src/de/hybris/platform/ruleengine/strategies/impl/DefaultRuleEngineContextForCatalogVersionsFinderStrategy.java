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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.dao.CatalogVersionToRuleEngineContextMappingDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.CatalogVersionToRuleEngineContextMappingModel;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextForCatalogVersionsFinderStrategy;


/**
 * Default implementation for the RuleEngineContextForCatalogVersionsFinderStrategy.
 */
@SuppressWarnings("unchecked")
public class DefaultRuleEngineContextForCatalogVersionsFinderStrategy implements RuleEngineContextForCatalogVersionsFinderStrategy
{
	private CatalogVersionToRuleEngineContextMappingDao catalogVersionToRuleEngineContextMappingDao;

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractRuleEngineContextModel> List<T> findRuleEngineContexts(
			final Collection<CatalogVersionModel> catalogVersions, final RuleType ruleType)

	{
		final Collection<CatalogVersionToRuleEngineContextMappingModel> mappings = getCatalogVersionToRuleEngineContextMappingDao()
				.findMappingsByCatalogVersion(catalogVersions, ruleType);

		if (isEmpty(mappings))
		{
			return emptyList();
		}

		return mappings.stream().map(m -> (T) m.getContext()).collect(toList());
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
