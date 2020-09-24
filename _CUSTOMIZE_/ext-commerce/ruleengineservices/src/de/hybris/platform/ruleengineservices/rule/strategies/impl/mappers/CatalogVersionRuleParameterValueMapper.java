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

import de.hybris.platform.catalog.daos.CatalogVersionDao;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Performs mapping between {@link CatalogVersionModel} and a String representation of its instance.
 */
public class CatalogVersionRuleParameterValueMapper implements RuleParameterValueMapper<CatalogVersionModel>
{
	private CatalogVersionDao catalogVersionDao;

	private String delimiter;

	@Override
	public String toString(final CatalogVersionModel catalog)
	{
		ServicesUtil.validateParameterNotNull(catalog, "Object cannot be null");
		return catalog.getCatalog().getId() + getDelimiter() + catalog.getVersion();
	}

	@Override
	public CatalogVersionModel fromString(final String value)
	{
		ServicesUtil.validateParameterNotNull(value, "String value cannot be null");
		Preconditions.checkArgument(value.contains(getDelimiter()),
				"Invalid format of the CatalogVersionModel string representation");
		final String[] parts = value.split(getDelimiter());
		final Collection<CatalogVersionModel> catalogVersions = getCatalogVersionDao().findCatalogVersions(parts[0], parts[1]);
		if (CollectionUtils.isEmpty(catalogVersions))
		{
			throw new RuleParameterValueMapperException("Cannot find Catalog Version with the code: " + value);
		}

		return catalogVersions.iterator().next();
	}

	protected CatalogVersionDao getCatalogVersionDao()
	{
		return catalogVersionDao;
	}

	@Required
	public void setCatalogVersionDao(final CatalogVersionDao catalogVersionDao)
	{
		this.catalogVersionDao = catalogVersionDao;
	}

	protected String getDelimiter()
	{
		return delimiter;
	}

	@Required
	public void setDelimiter(final String delimiter)
	{
		this.delimiter = delimiter;
	}
}
