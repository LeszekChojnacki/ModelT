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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsBoostRulesMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsBoostItemsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsBoostRulesMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsFacetsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategyFactory;

import java.util.Map;

import de.hybris.platform.adaptivesearch.strategies.AsSortsMergeStrategy;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsMergeStrategyFactory}
 */
public class DefaultAsMergeStrategyFactory implements AsMergeStrategyFactory
{
	private Map<AsFacetsMergeMode, AsFacetsMergeStrategy> facetsMergeModeMapping;
	private Map<AsBoostItemsMergeMode, AsBoostItemsMergeStrategy> boostItemsMergeModeMapping;
	private Map<AsBoostRulesMergeMode, AsBoostRulesMergeStrategy> boostRulesMergeModeMapping;
	private Map<AsSortsMergeMode, AsSortsMergeStrategy> sortsMergeModeMapping;

	@Override
	public AsFacetsMergeStrategy getFacetsMergeStrategy(final AsFacetsMergeMode mergeMode)
	{
		return facetsMergeModeMapping.get(mergeMode);
	}

	@Override
	public AsBoostItemsMergeStrategy getBoostItemsMergeStrategy(final AsBoostItemsMergeMode mergeMode)
	{
		return boostItemsMergeModeMapping.get(mergeMode);
	}

	@Override
	public AsBoostRulesMergeStrategy getBoostRulesMergeStrategy(final AsBoostRulesMergeMode mergeMode)
	{
		return boostRulesMergeModeMapping.get(mergeMode);
	}

	@Override
	public AsSortsMergeStrategy getSortsMergeStrategy(AsSortsMergeMode mergeMode)
	{
		return sortsMergeModeMapping.get(mergeMode);
	}

	public Map<AsFacetsMergeMode, AsFacetsMergeStrategy> getFacetsMergeModeMapping()
	{
		return facetsMergeModeMapping;
	}

	@Required
	public void setFacetsMergeModeMapping(final Map<AsFacetsMergeMode, AsFacetsMergeStrategy> facetsMergeModeMapping)
	{
		this.facetsMergeModeMapping = facetsMergeModeMapping;
	}

	public Map<AsBoostItemsMergeMode, AsBoostItemsMergeStrategy> getBoostItemsMergeModeMapping()
	{
		return boostItemsMergeModeMapping;
	}

	@Required
	public void setBoostItemsMergeModeMapping(final Map<AsBoostItemsMergeMode, AsBoostItemsMergeStrategy> resultMergeModeMapping)
	{
		this.boostItemsMergeModeMapping = resultMergeModeMapping;
	}

	public Map<AsBoostRulesMergeMode, AsBoostRulesMergeStrategy> getBoostRulesMergeModeMapping()
	{
		return boostRulesMergeModeMapping;
	}

	@Required
	public void setBoostRulesMergeModeMapping(final Map<AsBoostRulesMergeMode, AsBoostRulesMergeStrategy> boostMergeModeMapping)
	{
		this.boostRulesMergeModeMapping = boostMergeModeMapping;
	}

	public Map<AsSortsMergeMode, AsSortsMergeStrategy> getSortsMergeModeMapping()
	{
		return sortsMergeModeMapping;
	}

	@Required
	public void setSortsMergeModeMapping(Map<AsSortsMergeMode, AsSortsMergeStrategy> sortsMergeModeMapping)
	{
		this.sortsMergeModeMapping = sortsMergeModeMapping;
	}
}