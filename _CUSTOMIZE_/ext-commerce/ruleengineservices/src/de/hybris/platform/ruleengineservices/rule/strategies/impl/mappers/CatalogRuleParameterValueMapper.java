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
package de.hybris.platform.ruleengineservices.rule.strategies.impl.mappers;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import org.springframework.beans.factory.annotation.Required;



/**
 * Performs mapping between {@link CatalogModel} and a String representation of its instance.
 */
public class CatalogRuleParameterValueMapper implements RuleParameterValueMapper<CatalogModel>
{
	private CatalogService catalogService;

	@Override
	public String toString(final CatalogModel catalog)
	{
		ServicesUtil.validateParameterNotNull(catalog, "Object cannot be null");
		return catalog.getId();
	}

	@Override
	public CatalogModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		final CatalogModel catalog = getCatalogService().getCatalogForId(value);

		if (catalog == null)
		{
			throw new RuleParameterValueMapperException("Cannot find Catalog with the id: " + value);
		}

		return catalog;
	}

	protected CatalogService getCatalogService()
	{
		return catalogService;
	}

	@Required
	public void setCatalogService(final CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}
}
