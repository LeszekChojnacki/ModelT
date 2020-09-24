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
package de.hybris.platform.ruleengine.dynamic;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.strategies.CatalogVersionFinderStrategy;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;


/**
 * Dynamic attribute handler for {@link AbstractRulesModuleModel#getCatalogVersions()} that returns catalog versions linked to it
 * {@link CatalogVersionFinderStrategy#findCatalogVersionsByRulesModule(AbstractRulesModuleModel)}
 */
public class RuleModuleCatalogVersionAttributeHandler implements DynamicAttributeHandler<Collection<CatalogVersionModel>, AbstractRulesModuleModel>
{
	private CatalogVersionFinderStrategy catalogVersionFinderStrategy;

	@Override
	public Collection<CatalogVersionModel> get(final AbstractRulesModuleModel rulesModule)
	{
		return getCatalogVersionFinderStrategy().findCatalogVersionsByRulesModule(rulesModule);
	}

	@Override
	public void set(final AbstractRulesModuleModel rulesModule, final Collection<CatalogVersionModel> catalogVersions)
	{
		throw new UnsupportedOperationException("AbstractRulesModuleModel.catalogVersions is readonly attribute");
	}

	protected CatalogVersionFinderStrategy getCatalogVersionFinderStrategy()
	{
		return catalogVersionFinderStrategy;
	}

	@Required
	public void setCatalogVersionFinderStrategy(
			final CatalogVersionFinderStrategy catalogVersionFinderStrategy)
	{
		this.catalogVersionFinderStrategy = catalogVersionFinderStrategy;
	}
}
