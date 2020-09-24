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
package de.hybris.platform.solrfacetsearch.solr.impl;

import de.hybris.platform.servicelayer.tenant.TenantService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.solr.IndexNameResolver;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link IndexNameResolver}.
 */
public class DefaultIndexNameResolver implements IndexNameResolver
{
	private static Pattern forbiddenCharacters = Pattern.compile("[^a-zA-Z0-9_\\\\-]+");

	private String separator;

	private TenantService tenantService;

	public String getSeparator()
	{
		return separator;
	}

	@Required
	public void setSeparator(final String separator)
	{
		this.separator = separator;
	}

	public TenantService getTenantService()
	{
		return tenantService;
	}

	@Required
	public void setTenantService(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	@Override
	public String resolve(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType, final String qualifier)
	{
		final StringBuilder result = new StringBuilder();

		// tenant part
		result.append(tenantService.getCurrentTenantId());

		// facet search configuration part
		result.append(separator).append(
				indexedType.getIndexNameFromConfig() != null ? indexedType.getIndexNameFromConfig() : facetSearchConfig.getName());

		// indexed type part
		result.append(separator).append(indexedType.getIndexName() != null ? indexedType.getIndexName() : indexedType.getCode());

		// qualifier part (optional)
		if (StringUtils.isNotBlank(qualifier))
		{
			result.append(separator).append(qualifier);
		}

		// remove forbidden characters
		return forbiddenCharacters.matcher(result.toString()).replaceAll("");
	}
}
