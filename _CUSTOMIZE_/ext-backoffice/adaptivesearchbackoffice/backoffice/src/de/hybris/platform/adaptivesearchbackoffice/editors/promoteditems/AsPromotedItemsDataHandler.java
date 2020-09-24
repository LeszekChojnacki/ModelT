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
package de.hybris.platform.adaptivesearchbackoffice.editors.promoteditems;

import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchbackoffice.data.PromotedItemEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandler;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.DataHandler;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.ModelLoadingException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.labels.LabelService;


/**
 * Implementation of {@link DataHandler} for the promoted items.
 */
public class AsPromotedItemsDataHandler extends AbstractDataHandler<PromotedItemEditorData, AsPromotedItemModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(AsPromotedItemsDataHandler.class);

	private ModelService modelService;
	private LabelService labelService;

	@Override
	public String getTypeCode()
	{
		return AsPromotedItemModel._TYPECODE;
	}

	@Override
	protected PromotedItemEditorData createEditorData()
	{
		final PromotedItemEditorData editorData = new PromotedItemEditorData();
		editorData.setValid(true);
		return editorData;
	}

	@Override
	protected void loadDataFromSearchResult(final Map<Object, PromotedItemEditorData> mapping, final SearchResultData searchResult,
			final Map<String, Object> parameters)
	{
		if (searchResult == null || searchResult.getAsSearchResult() == null)
		{
			return;
		}

		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		final AsSearchProfileResult searchProfileResult = asSearchResult.getSearchProfileResult();

		if (searchProfileResult != null && MapUtils.isNotEmpty(searchProfileResult.getPromotedItems()))
		{
			final AbstractAsSearchProfileModel searchProfile = (AbstractAsSearchProfileModel) parameters.get(SEARCH_PROFILE_PARAM);

			for (final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItemHolder : ((MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>>) searchProfileResult
					.getPromotedItems()).orderedValues())
			{
				final AsPromotedItem promotedItem = promotedItemHolder.getConfiguration();
				final PromotedItemEditorData editorData = getOrCreateEditorData(mapping, promotedItem.getUid());
				convertFromSearchProfileResult(promotedItemHolder, editorData, searchProfile);
			}
		}
	}

	@Override
	protected void loadDataFromInitialValue(final Map<Object, PromotedItemEditorData> mapping,
			final Collection<AsPromotedItemModel> initialValue, final Map<String, Object> parameters)
	{
		if (CollectionUtils.isNotEmpty(initialValue))
		{
			for (final AsPromotedItemModel promotedItem : initialValue)
			{
				final PromotedItemEditorData editorData = getOrCreateEditorData(mapping, promotedItem.getUid());
				convertFromModel(promotedItem, editorData);
			}
		}
	}

	protected void convertFromSearchProfileResult(
			final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> source,
			final PromotedItemEditorData target, final AbstractAsSearchProfileModel searchProfile)
	{
		final AsPromotedItem promotedItem = source.getConfiguration();
		final PK itemPk = promotedItem.getItemPk();
		ItemModel item = null;

		try
		{
			item = modelService.get(itemPk);
		}
		catch (final ModelLoadingException e)
		{
			LOG.warn("Failed to load promoted item", e);
		}

		target.setUid(promotedItem.getUid());
		target.setLabel(labelService.getObjectLabel(item));
		target.setItemPk(itemPk);
		target.setBoostItemConfiguration(promotedItem);
		target.setFromSearchProfile(isConfigurationFromSearchProfile(source.getConfiguration(), searchProfile));

		final AbstractAsBoostItemConfiguration replacedConfiguration = CollectionUtils
				.isNotEmpty(source.getReplacedConfigurations()) ? source.getReplacedConfigurations().get(0) : null;
		target.setOverride(replacedConfiguration != null);
		target.setOverrideFromSearchProfile(isConfigurationFromSearchProfile(replacedConfiguration, searchProfile));
	}

	protected boolean isConfigurationFromSearchProfile(final AbstractAsBoostItemConfiguration configuration,
			final AbstractAsSearchProfileModel searchProfile)
	{
		if (configuration == null || searchProfile == null)
		{
			return false;
		}

		return StringUtils.equals(searchProfile.getCode(), configuration.getSearchProfileCode());
	}

	protected void convertFromModel(final AsPromotedItemModel source, final PromotedItemEditorData target)
	{
		// generates uid for new items
		if (StringUtils.isBlank(source.getUid()))
		{
			source.setUid(getAsUidGenerator().generateUid());
		}

		final ItemModel item = source.getItem();

		target.setUid(source.getUid());
		target.setLabel(labelService.getObjectLabel(item));
		target.setValid(getAsConfigurationService().isValid(source));
		target.setItemPk(item == null ? null : item.getPk());
		target.setModel(source);
		target.setFromSearchProfile(true);
		target.setFromSearchConfiguration(true);
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
