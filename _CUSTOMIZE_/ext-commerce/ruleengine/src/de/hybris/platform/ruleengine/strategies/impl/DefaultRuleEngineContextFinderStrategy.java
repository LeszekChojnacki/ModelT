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

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengine.dao.RuleEngineContextDao;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextFinderStrategy;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextForCatalogVersionsFinderStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for the RuleEngineContextFinderStrategy.
 */
public class DefaultRuleEngineContextFinderStrategy implements RuleEngineContextFinderStrategy
{

	private static final String ILLEGAL_STATUS_MESSAGE_CATALOG_RULE_TYPE = "Cannot determine unique rule engine context for rule evaluation: more than one rule engine context %s found for catalog versions %s and rule type [%s]";
	private static final String ILLEGAL_STATUS_MESSAGE_RULE_MODULE = "Cannot determine unique rule engine context for rule evaluation: the derived RuleEngineContext is not unique %s for rule module [%s]";
	private static final String ILLEGAL_STATUS_MESSAGE_BY_RULE_TYPE = "Cannot determine unique rule engine context for rule evaluation: More than one Rules Modules (%s) found for type %s";

	private RulesModuleDao rulesModuleDao;
	private RuleEngineContextDao ruleEngineContextDao;
	private CatalogVersionService catalogVersionService;
	private RuleEngineContextForCatalogVersionsFinderStrategy ruleEngineContextForCatalogVersionsFinderStrategy;

	@Override
	public <T extends AbstractRuleEngineContextModel> Optional<T> findRuleEngineContext(final RuleType ruleType)
	{
		final List<AbstractRulesModuleModel> rulesModules = getRulesModuleDao().findActiveRulesModulesByRuleType(ruleType);

		if (isEmpty(rulesModules))
		{
			return empty();
		}

		if (rulesModules.size() != 1)
		{
			throw new IllegalStateException(String.format(ILLEGAL_STATUS_MESSAGE_BY_RULE_TYPE,
					rulesModules.stream().map(AbstractRulesModuleModel::getName).collect(Collectors.joining(", ")),
					ruleType.getCode()));
		}

		final List<T> ruleEngineContextByRulesModule = getRuleEngineContextDao()
				.findRuleEngineContextByRulesModule(rulesModules.get(0));
		final List<String> ruleEngineContextNames = ruleEngineContextByRulesModule.stream()
				.map(AbstractRuleEngineContextModel::getName).distinct().collect(toList());
		if (ruleEngineContextNames.size() > 1)
		{
			throw new IllegalStateException(
					String.format(ILLEGAL_STATUS_MESSAGE_RULE_MODULE, ruleEngineContextNames, rulesModules.get(0)));
		}
		return ruleEngineContextByRulesModule.stream().findFirst();

	}

	@Override
	public <T extends AbstractRuleEngineContextModel, O extends AbstractOrderModel> Optional<T> findRuleEngineContext(
			final O order, final RuleType ruleType)
	{
		Collection<CatalogVersionModel> catalogVersions = getCatalogVersionsForProducts(getProductsForOrder(order));
		if (isEmpty(catalogVersions))
		{
			catalogVersions = getAvailableCatalogVersions();
		}
		return getRuleEngineContextForCatalogVersions(catalogVersions, ruleType);
	}

	@Override
	public <T extends AbstractRuleEngineContextModel> Optional<T> findRuleEngineContext(final ProductModel product,
			final RuleType ruleType)
	{
		Collection<CatalogVersionModel> catalogVersions = getCatalogVersionsForProducts(Collections.singletonList(product));
		if (isEmpty(catalogVersions))
		{
			catalogVersions = getAvailableCatalogVersions();
		}
		return getRuleEngineContextForCatalogVersions(catalogVersions, ruleType);
	}

	@Override
	public <T extends AbstractRuleEngineContextModel> Optional<T> getRuleEngineContextForCatalogVersions(
			final Collection<CatalogVersionModel> catalogVersions, final RuleType ruleType)
	{
		final List<T> ruleEngineContexts = getRuleEngineContextForCatalogVersionsFinderStrategy()
				.findRuleEngineContexts(catalogVersions, ruleType);

		if (isNotEmpty(ruleEngineContexts))
		{
			final List<String> ruleEngineContextNames = ruleEngineContexts.stream().map(AbstractRuleEngineContextModel::getName)
					.distinct().collect(toList());
			if (ruleEngineContextNames.size() > 1)
			{
				throw new IllegalStateException(
						String.format(ILLEGAL_STATUS_MESSAGE_CATALOG_RULE_TYPE, ruleEngineContextNames, catalogVersions.stream().map(
								this::catalogVersionToString).collect(toList()), ruleType));
			}
			return ruleEngineContexts.stream().findFirst();
		}

		// and finally go with fallback to 6.3
		return findRuleEngineContext(ruleType);
	}

	protected String catalogVersionToString(final CatalogVersionModel catalogVersion)
	{
		final CatalogModel catalog = catalogVersion.getCatalog();
		return catalog.getName() + ":" + catalog.getVersion();
	}

	protected Collection<CatalogVersionModel> getCatalogVersionsForProducts(final Collection<ProductModel> products)
	{
		if (isNotEmpty(products))
		{
			return products.stream().map(ProductModel::getCatalogVersion).filter(Objects::nonNull).distinct().collect(toList());
		}
		return Collections.emptyList();
	}

	protected Collection<CatalogVersionModel> getAvailableCatalogVersions()
	{
		final Collection<CatalogVersionModel> sessionCatalogVersions = getCatalogVersionService().getSessionCatalogVersions();
		if (isNotEmpty(sessionCatalogVersions))
		{
			return sessionCatalogVersions.stream().distinct().collect(toList());
		}
		return Collections.emptyList();
	}

	protected <T extends AbstractOrderModel> Collection<ProductModel> getProductsForOrder(final T order)
	{
		final List<AbstractOrderEntryModel> orderEntries = order.getEntries();
		if (isNotEmpty(orderEntries))
		{
			return orderEntries.stream().map(AbstractOrderEntryModel::getProduct).filter(Objects::nonNull).collect(toSet());
		}
		return Collections.emptyList();
	}

	protected CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	protected RuleEngineContextForCatalogVersionsFinderStrategy getRuleEngineContextForCatalogVersionsFinderStrategy()
	{
		return ruleEngineContextForCatalogVersionsFinderStrategy;
	}

	@Required
	public void setRuleEngineContextForCatalogVersionsFinderStrategy(
			final RuleEngineContextForCatalogVersionsFinderStrategy ruleEngineContextForCatalogVersionsFinderStrategy)
	{
		this.ruleEngineContextForCatalogVersionsFinderStrategy = ruleEngineContextForCatalogVersionsFinderStrategy;
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
