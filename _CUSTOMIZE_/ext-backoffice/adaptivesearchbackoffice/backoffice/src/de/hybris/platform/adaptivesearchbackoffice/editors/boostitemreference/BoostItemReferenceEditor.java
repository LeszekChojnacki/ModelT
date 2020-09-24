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
package de.hybris.platform.adaptivesearchbackoffice.editors.boostitemreference;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;

import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.model.AbstractAsBoostItemConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedItemModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRuntimeException;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.editor.defaultreferenceeditor.DefaultReferenceEditor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;


public class BoostItemReferenceEditor<T> extends DefaultReferenceEditor<T>
{
	@Resource
	private ModelService modelService;

	@Resource
	private AsSearchProviderFactory asSearchProviderFactory;

	@Override
	public void render(final Component parent, final EditorContext<T> context, final EditorListener<T> listener)
	{
		final AbstractAsBoostItemConfigurationModel parentObject = (AbstractAsBoostItemConfigurationModel) context.getParameters()
				.get(PARENT_OBJECT);

		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = modelService.getAttributeValue(parentObject,
				SEARCH_CONFIGURATION_ATTRIBUTE);

		final AbstractAsSearchProfileModel searchProfile = modelService.getAttributeValue(searchConfiguration,
				SEARCH_PROFILE_ATTRIBUTE);

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final Optional<AsIndexTypeData> indexTypeData = searchProvider.getIndexTypeForCode(searchProfile.getIndexType());

		if (!indexTypeData.isPresent())
		{
			throw new EditorRuntimeException("Index property not found: " + searchProfile.getIndexType());
		}

		context.setParameter("restrictToType", indexTypeData.get().getItemType());

		if (indexTypeData.get().isCatalogVersionAware())
		{
			context.setParameter("referenceSearchCondition_catalogVersion", searchProfile.getCatalogVersion());
		}

		populateContextWithPKCondition(context, searchConfiguration);

		super.render(parent, context, listener);
	}

	protected void populateContextWithPKCondition(final EditorContext<T> context,
			final AbstractAsConfigurableSearchConfigurationModel searchConfiguration)
	{
		final List<PK> pks = resolveConditionValue(searchConfiguration);

		if (CollectionUtils.isNotEmpty(pks))
		{
			context.setParameter("referenceSearchCondition_pk_doesNotContain",
					"{{" + pks.stream().map(pk -> pk.getLongValueAsString() + "L").collect(Collectors.joining(",")) + "}}");
		}
	}

	protected List<PK> resolveConditionValue(final AbstractAsConfigurableSearchConfigurationModel searchConfiguration)
	{
		final List<AsPromotedItemModel> promotedItems = searchConfiguration.getPromotedItems() == null ? Collections.emptyList()
				: searchConfiguration.getPromotedItems();
		final List<AsExcludedItemModel> excludedItems = searchConfiguration.getExcludedItems() == null ? Collections.emptyList()
				: searchConfiguration.getExcludedItems();

		return Stream.concat(promotedItems.stream(), excludedItems.stream()).filter(boostItem -> boostItem.getItem() != null)
				.map(boostItem -> boostItem.getItem().getPk()).collect(Collectors.toList());
	}
}
