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
package de.hybris.platform.adaptivesearchbackoffice.editors.sorts;

import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.data.AsSortData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSortModel;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractSortConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.data.SortEditorData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandler;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.ListModelList;


public class AsSortsDataHandler extends AbstractDataHandler<SortEditorData, AsSortModel>
{
	private I18NService i18NService;

	@Override
	public String getTypeCode()
	{
		return AsSortModel._TYPECODE;
	}

	@Override
	protected SortEditorData createEditorData()
	{
		final SortEditorData editorData = new SortEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, SortEditorData> mapping, final SearchResultData searchResult,
			final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && MapUtils.isNotEmpty(searchProfileResult.getSorts()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> sortHolder : ((MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>>) searchProfileResult
					.getSorts()).orderedValues())
			{
				final AsSort sort = sortHolder.getConfiguration();
				final SortEditorData editorData = getOrCreateEditorData(mapping, sort.getCode());
				convertFromSearchProfileResult(sortHolder, editorData, searchProfile);
			}
		}

		// if it is in the search result it should also be in the search profile result
		if (CollectionUtils.isNotEmpty(asSearchResult.getAvailableSorts()))
		{
			for (final AsSortData sort : asSearchResult.getAvailableSorts())
			{
				final SortEditorData editorData = mapping.get(sort.getCode());
				if (editorData != null)
				{
					convertFromSearchResult(sort, editorData);
				}
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, SortEditorData> mapping, final Collection<AsSortModel> initialValue,
			final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsSortModel sort : initialValue)
			{
				final SortEditorData editorData = getOrCreateEditorData(mapping, sort.getCode());
				convertFromModel(sort, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(final AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> source,
			final SortEditorData target, final AbstractAsSearchProfileModel searchProfile)
	{
		final AsSort sort = source.getConfiguration();

		target.setUid(sort.getUid());
		target.setCode(sort.getCode());
		target.setName(sort.getName());
		target.setPriority(sort.getPriority());
		target.setSortConfiguration(sort);
		target.setFromSearchProfile(isConfigurationFromSearchProfile(source.getConfiguration(), searchProfile));

		final AbstractAsSortConfiguration replacedConfiguration = CollectionUtils.isNotEmpty(source.getReplacedConfigurations())
				? source.getReplacedConfigurations().get(0)
				: null;
		target.setOverride(replacedConfiguration != null);
		target.setOverrideFromSearchProfile(isConfigurationFromSearchProfile(replacedConfiguration, searchProfile));
	}

	protected boolean isConfigurationFromSearchProfile(final AbstractAsSortConfiguration configuration,
			final AbstractAsSearchProfileModel searchProfile)
	{
		if (configuration == null || searchProfile == null)
		{
			return false;
		}

		return StringUtils.equals(searchProfile.getCode(), configuration.getSearchProfileCode());
	}

	protected void convertFromSearchResult(final AsSortData source, final SortEditorData target)
	{
		target.setInSearchResult(true);
	}

	protected void convertFromModel(final AsSortModel source, final SortEditorData target)
	{
		// generates uid for new items
		if (StringUtils.isBlank(source.getUid()))
		{
			source.setUid(getAsUidGenerator().generateUid());
		}

		target.setUid(source.getUid());
		target.setCode(source.getCode());
		target.setValid(getAsConfigurationService().isValid(source));
		target.setName(buildNameLocalizationMap(source));
		target.setPriority(source.getPriority());
		target.setModel(source);
		target.setFromSearchProfile(true);
		target.setFromSearchConfiguration(true);
	}

	protected Map<String, String> buildNameLocalizationMap(final AbstractAsSortConfigurationModel source)
	{
		final Set<Locale> supportedLocales = i18NService.getSupportedLocales();

		return supportedLocales.stream().filter(locale -> StringUtils.isNotBlank(source.getName(locale)))
				.collect(Collectors.toMap(Locale::toString, locale -> source.getName(locale)));
	}

	@Override
	protected void postLoadData(final Collection<AsSortModel> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters, final ListModelList<SortEditorData> data)
	{
		if (data != null)
		{
			if (searchResult != null)
			{
				localizeSorts(searchResult.getSearchContext(), data.getInnerList());
			}

			updateRankAttributes(data.getInnerList());
		}
	}

	protected void localizeSorts(final SearchContextData searchContext,
			final List<? extends AbstractSortConfigurationEditorData> sorts)
	{
		final String language = searchContext != null ? searchContext.getLanguage() : null;

		for (final AbstractSortConfigurationEditorData sort : sorts)
		{
			final String label = sort.getName().get(language);

			if (StringUtils.isNotBlank(label))
			{
				sort.setLabel(label);
			}
			else
			{
				sort.setLabel("[" + sort.getCode() + "]");
			}
		}
	}

	protected void updateRankAttributes(final List<? extends AbstractSortConfigurationEditorData> sorts)
	{
		AbstractSortConfigurationEditorData previousSort = null;

		for (final AbstractSortConfigurationEditorData sort : sorts)
		{
			if (previousSort != null && previousSort.getModel() != null && sort.getModel() != null
					&& ObjectUtils.equals(previousSort.getPriority(), sort.getPriority()))
			{
				previousSort.setRankDownAllowed(true);
				sort.setRankUpAllowed(true);
			}

			previousSort = sort;
		}
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
