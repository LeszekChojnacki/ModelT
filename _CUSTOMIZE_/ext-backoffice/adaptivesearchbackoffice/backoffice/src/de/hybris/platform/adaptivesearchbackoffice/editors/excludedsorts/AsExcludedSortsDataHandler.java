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
package de.hybris.platform.adaptivesearchbackoffice.editors.excludedsorts;

import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedSort;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedSortModel;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchbackoffice.data.AbstractSortConfigurationEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.ExcludedSortEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchContextData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.ListModelList;


public class AsExcludedSortsDataHandler extends AbstractDataHandler<ExcludedSortEditorData, AsExcludedSortModel>
{
	private I18NService i18NService;

	@Override
	public String getTypeCode()
	{
		return AsExcludedSortModel._TYPECODE;
	}

	@Override
	protected ExcludedSortEditorData createEditorData()
	{
		final ExcludedSortEditorData editorData = new ExcludedSortEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, ExcludedSortEditorData> mapping, final SearchResultData searchResult,
			final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && MapUtils.isNotEmpty(searchProfileResult.getExcludedSorts()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsExcludedSort, AbstractAsSortConfiguration> excludedSortHolder : ((MergeMap<String, AsConfigurationHolder<AsExcludedSort, AbstractAsSortConfiguration>>) searchProfileResult
					.getExcludedSorts()).orderedValues())
			{
				final AsExcludedSort excludedSort = excludedSortHolder.getConfiguration();
				final ExcludedSortEditorData editorData = getOrCreateEditorData(mapping, excludedSort.getCode());
				convertFromSearchProfileResult(excludedSortHolder, editorData, searchProfile);
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, ExcludedSortEditorData> mapping,
			final Collection<AsExcludedSortModel> initialValue, final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsExcludedSortModel excludedSort : initialValue)
			{
				final ExcludedSortEditorData editorData = getOrCreateEditorData(mapping, excludedSort.getCode());
				convertFromModel(excludedSort, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(final AsConfigurationHolder<AsExcludedSort, AbstractAsSortConfiguration> source,
			final ExcludedSortEditorData target, final AbstractAsSearchProfileModel searchProfile)
	{
		final AsExcludedSort excludedSort = source.getConfiguration();

		target.setUid(excludedSort.getUid());
		target.setCode(excludedSort.getCode());
		target.setName(excludedSort.getName());
		target.setPriority(excludedSort.getPriority());
		target.setSortConfiguration(excludedSort);
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

	protected void convertFromModel(final AsExcludedSortModel source, final ExcludedSortEditorData target)
	{
		// generates uid for new items
		if (StringUtils.isBlank(source.getUid()))
		{
			source.setUid(getAsUidGenerator().generateUid());
		}

		target.setUid(source.getUid());
		target.setValid(getAsConfigurationService().isValid(source));
		target.setCode(source.getCode());
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
	protected void postLoadData(final Collection<AsExcludedSortModel> initialValue, final SearchResultData searchResult,
			final Map<String, Object> parameters, final ListModelList<ExcludedSortEditorData> data)
	{
		if (data != null && searchResult != null)
		{
			localizeSorts(searchResult.getSearchContext(), data.getInnerList());
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
