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
package de.hybris.platform.adaptivesearchbackoffice.editors.excludedfacets;

import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacet;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.enums.AsFacetType;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetModel;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.ExcludedFacetEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandler;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.DataHandler;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.ListModelList;


/**
 * Implementation of {@link DataHandler} for the excluded facets.
 */
public class AsExcludedFacetsDataHandler extends AbstractDataHandler<ExcludedFacetEditorData, AsExcludedFacetModel>
{
	private AsFacetUtils asFacetUtils;

	@Override
	public String getTypeCode()
	{
		return AsExcludedFacetModel._TYPECODE;
	}

	@Override
	protected ExcludedFacetEditorData createEditorData()
	{
		final ExcludedFacetEditorData editorData = new ExcludedFacetEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, ExcludedFacetEditorData> mapping,
			final SearchResultData searchResult, final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && MapUtils.isNotEmpty(searchProfileResult.getExcludedFacets()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration> excludedFacetHolder : ((MergeMap<String, AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration>>) searchProfileResult
					.getExcludedFacets()).orderedValues())
			{
				final AsExcludedFacet excludedFacet = excludedFacetHolder.getConfiguration();
				final ExcludedFacetEditorData editorData = getOrCreateEditorData(mapping, excludedFacet.getIndexProperty());
				convertFromSearchProfileResult(excludedFacetHolder, editorData, searchProfile);
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, ExcludedFacetEditorData> mapping,
			final Collection<AsExcludedFacetModel> initialValue, final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsExcludedFacetModel excludedFacet : initialValue)
			{
				final ExcludedFacetEditorData editorData = getOrCreateEditorData(mapping, excludedFacet.getIndexProperty());
				convertFromModel(excludedFacet, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(
			final AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration> source, final ExcludedFacetEditorData target,
			final AbstractAsSearchProfileModel searchProfile)
	{
		final AsExcludedFacet excludedFacet = source.getConfiguration();
		final String indexProperty = excludedFacet.getIndexProperty();

		target.setUid(excludedFacet.getUid());
		target.setLabel(indexProperty);
		target.setIndexProperty(indexProperty);
		target.setPriority(excludedFacet.getPriority());
		target.setMultiselect(excludedFacet.getFacetType() == AsFacetType.MULTISELECT_AND
				|| excludedFacet.getFacetType() == AsFacetType.MULTISELECT_OR);
		target.setFacetConfiguration(excludedFacet);
		target.setFromSearchProfile(isConfigurationFromSearchProfile(source.getConfiguration(), searchProfile));

		final AbstractAsFacetConfiguration replacedConfiguration = CollectionUtils.isNotEmpty(source.getReplacedConfigurations())
				? source.getReplacedConfigurations().get(0)
				: null;
		target.setOverride(replacedConfiguration != null);
		target.setOverrideFromSearchProfile(isConfigurationFromSearchProfile(replacedConfiguration, searchProfile));
	}

	protected boolean isConfigurationFromSearchProfile(final AbstractAsFacetConfiguration configuration,
			final AbstractAsSearchProfileModel searchProfile)
	{
		if (configuration == null || searchProfile == null)
		{
			return false;
		}

		return StringUtils.equals(searchProfile.getCode(), configuration.getSearchProfileCode());
	}

	protected void convertFromModel(final AsExcludedFacetModel source, final ExcludedFacetEditorData target)
	{
		// generates uid for new items
		if (StringUtils.isBlank(source.getUid()))
		{
			source.setUid(getAsUidGenerator().generateUid());
		}

		final String indexProperty = source.getIndexProperty();

		target.setUid(source.getUid());
		target.setLabel(indexProperty);
		target.setValid(getAsConfigurationService().isValid(source));
		target.setIndexProperty(indexProperty);
		target.setPriority(source.getPriority());
		target.setModel(source);
		target.setFromSearchProfile(true);
		target.setFromSearchConfiguration(true);
	}

	@Override
	protected void postLoadData(final Collection<AsExcludedFacetModel> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters, final ListModelList<ExcludedFacetEditorData> data)
	{
		if (searchResult != null && data != null)
		{
			// populates the label attribute
			asFacetUtils.localizeFacets(searchResult.getNavigationContext(), searchResult.getSearchContext(), data.getInnerList());
		}
	}

	public AsFacetUtils getAsFacetUtils()
	{
		return asFacetUtils;
	}

	@Required
	public void setAsFacetUtils(final AsFacetUtils asFacetUtils)
	{
		this.asFacetUtils = asFacetUtils;
	}
}
