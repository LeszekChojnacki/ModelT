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
package de.hybris.platform.adaptivesearchbackoffice.editors.promotedfacets;

import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.enums.AsFacetType;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetModel;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.PromotedFacetEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandler;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.ListModelList;


public class AsPromotedFacetsDataHandler extends AbstractDataHandler<PromotedFacetEditorData, AsPromotedFacetModel>
{
	private AsFacetUtils asFacetUtils;

	@Override
	public String getTypeCode()
	{
		return AsPromotedFacetModel._TYPECODE;
	}

	@Override
	protected PromotedFacetEditorData createEditorData()
	{
		final PromotedFacetEditorData editorData = new PromotedFacetEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, PromotedFacetEditorData> mapping,
			final SearchResultData searchResult, final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && MapUtils.isNotEmpty(searchProfileResult.getPromotedFacets()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration> promotedFacetHolder : ((MergeMap<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>>) searchProfileResult
					.getPromotedFacets()).orderedValues())
			{
				final AsPromotedFacet promotedFacet = promotedFacetHolder.getConfiguration();
				final PromotedFacetEditorData editorData = getOrCreateEditorData(mapping, promotedFacet.getIndexProperty());
				convertFromSearchProfileResult(promotedFacetHolder, editorData, searchProfile);
			}
		}

		// if it is in the search result it should also be in the search profile result
		if (CollectionUtils.isNotEmpty(asSearchResult.getFacets()))
		{
			for (final AsFacetData facet : asSearchResult.getFacets())
			{
				final PromotedFacetEditorData editorData = mapping.get(facet.getIndexProperty());
				if (editorData != null)
				{
					convertFromSearchResult(facet, editorData);
				}
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, PromotedFacetEditorData> mapping,
			final Collection<AsPromotedFacetModel> initialValue, final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsPromotedFacetModel promotedFacet : initialValue)
			{
				final PromotedFacetEditorData editorData = getOrCreateEditorData(mapping, promotedFacet.getIndexProperty());
				convertFromModel(promotedFacet, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(
			final AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration> source, final PromotedFacetEditorData target,
			final AbstractAsSearchProfileModel searchProfile)
	{
		final AsPromotedFacet promotedFacet = source.getConfiguration();
		final String indexProperty = promotedFacet.getIndexProperty();

		target.setUid(promotedFacet.getUid());
		target.setLabel(indexProperty);
		target.setIndexProperty(indexProperty);
		target.setPriority(promotedFacet.getPriority());
		target.setMultiselect(promotedFacet.getFacetType() == AsFacetType.MULTISELECT_AND
				|| promotedFacet.getFacetType() == AsFacetType.MULTISELECT_OR);
		target.setFacetConfiguration(promotedFacet);
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

	protected void convertFromSearchResult(final AsFacetData source, final PromotedFacetEditorData target)
	{
		final boolean inSearchResult = CollectionUtils.isNotEmpty(source.getValues())
				|| CollectionUtils.isNotEmpty(source.getSelectedValues());

		target.setInSearchResult(inSearchResult);
		target.setOpen(asFacetUtils.isOpen(source));
		target.setFacet(source);

		if (CollectionUtils.isNotEmpty(source.getSelectedValues()))
		{
			target.setFacetFiltersCount(source.getSelectedValues().size());
		}
	}

	protected void convertFromModel(final AsPromotedFacetModel source, final PromotedFacetEditorData target)
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
	protected void postLoadData(final Collection<AsPromotedFacetModel> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters, final ListModelList<PromotedFacetEditorData> data)
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
