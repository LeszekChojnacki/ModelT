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
package de.hybris.platform.adaptivesearchsolr.strategies.impl;

import de.hybris.platform.adaptivesearch.enums.AsBoostOperator;
import de.hybris.platform.adaptivesearch.enums.AsBoostType;
import de.hybris.platform.adaptivesearch.enums.AsFacetType;
import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsTypeMappingRegistry;
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.search.BoostField.BoostType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.QueryOperator;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.InitializingBean;


/**
 * Bidirectional adaptive search and solr type mappings.
 */
public class DefaultSolrAsTypeMappingRegistry implements SolrAsTypeMappingRegistry, InitializingBean
{
	private final BidiMap<AsFacetType, FacetType> facetTypeMapping = new DualHashBidiMap<>();
	private final BidiMap<AsBoostOperator, QueryOperator> boostOperatorMapping = new DualHashBidiMap<>();
	private final BidiMap<AsBoostType, BoostType> boostTypeMapping = new DualHashBidiMap<>();

	@Override
	public void afterPropertiesSet()
	{
		populateFacetTypeMapping();
		populateBoostOperatorMapping();
		populateBoostTypeMapping();
	}

	protected void populateFacetTypeMapping()
	{
		facetTypeMapping.put(AsFacetType.REFINE, FacetType.REFINE);
		facetTypeMapping.put(AsFacetType.MULTISELECT_OR, FacetType.MULTISELECTOR);
		facetTypeMapping.put(AsFacetType.MULTISELECT_AND, FacetType.MULTISELECTAND);
	}

	protected void populateBoostOperatorMapping()
	{
		boostOperatorMapping.put(AsBoostOperator.EQUAL, QueryOperator.EQUAL_TO);
		boostOperatorMapping.put(AsBoostOperator.GREATER_THAN, QueryOperator.GREATER_THAN);
		boostOperatorMapping.put(AsBoostOperator.GREATER_THAN_OR_EQUAL, QueryOperator.GREATER_THAN_OR_EQUAL_TO);
		boostOperatorMapping.put(AsBoostOperator.LESS_THAN, QueryOperator.LESS_THAN);
		boostOperatorMapping.put(AsBoostOperator.LESS_THAN_OR_EQUAL, QueryOperator.LESS_THAN_OR_EQUAL_TO);
		boostOperatorMapping.put(AsBoostOperator.MATCH, QueryOperator.MATCHES);
	}

	protected void populateBoostTypeMapping()
	{
		boostTypeMapping.put(AsBoostType.ADDITIVE, BoostType.ADDITIVE);
		boostTypeMapping.put(AsBoostType.MULTIPLICATIVE, BoostType.MULTIPLICATIVE);
	}

	protected BidiMap<AsFacetType, FacetType> getFacetTypeMapping()
	{
		return facetTypeMapping;
	}

	protected BidiMap<AsBoostOperator, QueryOperator> getBoostOperatorMapping()
	{
		return boostOperatorMapping;
	}

	protected BidiMap<AsBoostType, BoostType> getBoostTypeMapping()
	{
		return boostTypeMapping;
	}

	@Override
	public FacetType toFacetType(final AsFacetType asFacetType)
	{
		if (asFacetType == null)
		{
			return null;
		}

		return facetTypeMapping.get(asFacetType);
	}

	@Override
	public AsFacetType toAsFacetType(final FacetType facetType)
	{
		if (facetType == null)
		{
			return null;
		}

		return facetTypeMapping.inverseBidiMap().get(facetType);
	}

	@Override
	public QueryOperator toQueryOperator(final AsBoostOperator asBoostOperator)
	{
		if (asBoostOperator == null)
		{
			return null;
		}

		return boostOperatorMapping.get(asBoostOperator);
	}

	@Override
	public AsBoostOperator toAsBoostOperator(final QueryOperator boostOperator)
	{
		if (boostOperator == null)
		{
			return null;
		}

		return boostOperatorMapping.inverseBidiMap().get(boostOperator);
	}

	@Override
	public BoostType toBoostType(final AsBoostType asBoostType)
	{
		if (asBoostType == null)
		{
			return null;
		}

		return boostTypeMapping.get(asBoostType);
	}

	@Override
	public AsBoostType toAsBoostType(final BoostType boostType)
	{
		if (boostType == null)
		{
			return null;
		}

		return boostTypeMapping.inverseBidiMap().get(boostType);
	}
}
