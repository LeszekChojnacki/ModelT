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
package com.hybris.backoffice.solrsearch.daos.impl;


import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.jalo.config.SolrFacetSearchConfig;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.daos.SolrFacetSearchConfigDAO;
import com.hybris.backoffice.solrsearch.jalo.BackofficeIndexedTypeToSolrFacetSearchConfig;
import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;


public class DefaultSolrFacetSearchConfigDAO implements SolrFacetSearchConfigDAO
{
	protected static final String FIND_SEARCH_CFG_FOR_TYPES_QUERY = String.format(
			"select {b:%s} from {%s as b} where {b:%s} in (?%s)", BackofficeIndexedTypeToSolrFacetSearchConfigModel.PK,
			BackofficeIndexedTypeToSolrFacetSearchConfigModel._TYPECODE,
			BackofficeIndexedTypeToSolrFacetSearchConfigModel.INDEXEDTYPE,
			BackofficeIndexedTypeToSolrFacetSearchConfigModel.INDEXEDTYPE);

	protected static final String FIND_SEARCH_CFG_FOR_NAME_QUERY = String.format(
			"SELECT {bfc:%s} FROM {%s AS bfc LEFT JOIN %s AS fc ON {bfc:%s} = {fc:%s} } WHERE { fc:%s } = ?%s",
			BackofficeIndexedTypeToSolrFacetSearchConfigModel.PK, BackofficeIndexedTypeToSolrFacetSearchConfigModel._TYPECODE,
			BackofficeIndexedTypeToSolrFacetSearchConfig.SOLRFACETSEARCHCONFIG,
			BackofficeIndexedTypeToSolrFacetSearchConfig.SOLRFACETSEARCHCONFIG, SolrFacetSearchConfig.PK, SolrFacetSearchConfig.NAME,
			SolrFacetSearchConfig.NAME);

	protected static final String FIND_ALL_SEARCH_CFG_QUERY = String.format("select {b:%s} from {%s as b}",
			BackofficeIndexedTypeToSolrFacetSearchConfigModel.PK, BackofficeIndexedTypeToSolrFacetSearchConfigModel._TYPECODE);


	private FlexibleSearchService flexibleSearchService;

	private TypeService typeService;

	@Override
	public List<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findSearchConfigurationsForTypes(
			final List<ComposedTypeModel> types)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_SEARCH_CFG_FOR_TYPES_QUERY);
		query.addQueryParameter(BackofficeIndexedTypeToSolrFacetSearchConfigModel.INDEXEDTYPE, types);
		return flexibleSearchService.<BackofficeIndexedTypeToSolrFacetSearchConfigModel> search(query).getResult();
	}

	@Override
	public List<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findSearchConfigurationsForName(
			final String facetSearchConfigName)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_SEARCH_CFG_FOR_NAME_QUERY);
		query.addQueryParameter(SolrFacetSearchConfig.NAME, facetSearchConfigName);
		return flexibleSearchService.<BackofficeIndexedTypeToSolrFacetSearchConfigModel> search(query).getResult();
	}

	@Override
	public List<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findAllSearchConfigs()
	{
		if (isSolrFacetSearchConfigModelCreated())
		{
			final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ALL_SEARCH_CFG_QUERY);
			return flexibleSearchService.<BackofficeIndexedTypeToSolrFacetSearchConfigModel> search(query).getResult();
		}

		return Collections.emptyList();
	}

	private boolean isSolrFacetSearchConfigModelCreated()
	{
		try
		{
			typeService.getComposedTypeForClass(BackofficeIndexedTypeToSolrFacetSearchConfigModel.class);
		}
		catch (final UnknownIdentifierException exception)
		{
			return false;
		}

		return true;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

}
