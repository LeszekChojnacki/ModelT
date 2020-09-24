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
package de.hybris.platform.adaptivesearch.converters.populators;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;

import de.hybris.platform.adaptivesearch.converters.AsItemConfigurationConverterContext;
import de.hybris.platform.adaptivesearch.converters.AsSearchConfigurationConverterContext;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacet;
import de.hybris.platform.adaptivesearch.data.AsExcludedItem;
import de.hybris.platform.adaptivesearch.data.AsExcludedSort;
import de.hybris.platform.adaptivesearch.data.AsFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsPromotedSort;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedItemModel;
import de.hybris.platform.adaptivesearch.model.AsExcludedSortModel;
import de.hybris.platform.adaptivesearch.model.AsFacetModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearch.model.AsPromotedSortModel;
import de.hybris.platform.adaptivesearch.model.AsSortModel;
import de.hybris.platform.adaptivesearch.util.ContextAwareConverter;
import de.hybris.platform.adaptivesearch.util.ContextAwarePopulator;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populates {@link AsConfigurableSearchConfiguration} from {@link AbstractAsConfigurableSearchConfigurationModel}.
 */
public class AsConfigurableSearchConfigurationPopulator implements
		ContextAwarePopulator<AbstractAsConfigurableSearchConfigurationModel, AsConfigurableSearchConfiguration, AsSearchConfigurationConverterContext>
{
	private ModelService modelService;

	private ContextAwareConverter<AsPromotedFacetModel, AsPromotedFacet, AsItemConfigurationConverterContext> asPromotedFacetConverter;
	private ContextAwareConverter<AsFacetModel, AsFacet, AsItemConfigurationConverterContext> asFacetConverter;
	private ContextAwareConverter<AsExcludedFacetModel, AsExcludedFacet, AsItemConfigurationConverterContext> asExcludedFacetConverter;
	private ContextAwareConverter<AsPromotedItemModel, AsPromotedItem, AsItemConfigurationConverterContext> asPromotedItemConverter;
	private ContextAwareConverter<AsExcludedItemModel, AsExcludedItem, AsItemConfigurationConverterContext> asExcludedItemConverter;
	private ContextAwareConverter<AsBoostRuleModel, AsBoostRule, AsItemConfigurationConverterContext> asBoostRuleConverter;
	private ContextAwareConverter<AsPromotedSortModel, AsPromotedSort, AsItemConfigurationConverterContext> asPromotedSortConverter;
	private ContextAwareConverter<AsSortModel, AsSort, AsItemConfigurationConverterContext> asSortConverter;
	private ContextAwareConverter<AsExcludedSortModel, AsExcludedSort, AsItemConfigurationConverterContext> asExcludedSortConverter;

	@Override
	public void populate(final AbstractAsConfigurableSearchConfigurationModel source,
			final AsConfigurableSearchConfiguration target, final AsSearchConfigurationConverterContext context)
	{
		final AsItemConfigurationConverterContext childContext = new AsItemConfigurationConverterContext();
		childContext.setSearchProfileCode(context.getSearchProfileCode());
		childContext.setSearchConfigurationUid(source.getUid());

		target.setFacetsMergeMode(source.getFacetsMergeMode());
		target.setPromotedFacets(convertAll(asPromotedFacetConverter, source.getPromotedFacets(), childContext));
		target.setFacets(convertAll(asFacetConverter, source.getFacets(), childContext));
		target.setExcludedFacets(convertAll(asExcludedFacetConverter, source.getExcludedFacets(), childContext));
		target.setBoostItemsMergeMode(source.getBoostItemsMergeMode());
		target.setPromotedItems(convertAll(asPromotedItemConverter, source.getPromotedItems(), childContext));
		target.setExcludedItems(convertAll(asExcludedItemConverter, source.getExcludedItems(), childContext));
		target.setBoostRulesMergeMode(source.getBoostRulesMergeMode());
		target.setBoostRules(convertAll(asBoostRuleConverter, source.getBoostRules(), childContext));
		target.setSortsMergeMode(source.getSortsMergeMode());
		target.setPromotedSorts(convertAll(asPromotedSortConverter, source.getPromotedSorts(), childContext));
		target.setSorts(convertAll(asSortConverter, source.getSorts(), childContext));
		target.setExcludedSorts(convertAll(asExcludedSortConverter, source.getExcludedSorts(), childContext));
	}

	public static <S extends AbstractAsConfigurationModel, T, C> List<T> convertAll(final ContextAwareConverter<S, T, C> converter,
			final List<? extends S> sources, final C childContext)
	{
		return sources.stream().filter(configuration -> !configuration.isCorrupted())
				.map(source -> converter.convert(source, childContext)).collect(Collectors.toList());
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

	public ContextAwareConverter<AsPromotedFacetModel, AsPromotedFacet, AsItemConfigurationConverterContext> getAsPromotedFacetConverter()
	{
		return asPromotedFacetConverter;
	}

	@Required
	public void setAsPromotedFacetConverter(
			final ContextAwareConverter<AsPromotedFacetModel, AsPromotedFacet, AsItemConfigurationConverterContext> asPromotedFacetConverter)
	{
		this.asPromotedFacetConverter = asPromotedFacetConverter;
	}

	public ContextAwareConverter<AsFacetModel, AsFacet, AsItemConfigurationConverterContext> getAsFacetConverter()
	{
		return asFacetConverter;
	}

	@Required
	public void setAsFacetConverter(
			final ContextAwareConverter<AsFacetModel, AsFacet, AsItemConfigurationConverterContext> asFacetConverter)
	{
		this.asFacetConverter = asFacetConverter;
	}

	public ContextAwareConverter<AsExcludedFacetModel, AsExcludedFacet, AsItemConfigurationConverterContext> getAsExcludedFacetConverter()
	{
		return asExcludedFacetConverter;
	}

	@Required
	public void setAsExcludedFacetConverter(
			final ContextAwareConverter<AsExcludedFacetModel, AsExcludedFacet, AsItemConfigurationConverterContext> asExcludedFacetConverter)
	{
		this.asExcludedFacetConverter = asExcludedFacetConverter;
	}

	public ContextAwareConverter<AsPromotedItemModel, AsPromotedItem, AsItemConfigurationConverterContext> getAsPromotedItemConverter()
	{
		return asPromotedItemConverter;
	}

	@Required
	public void setAsPromotedItemConverter(
			final ContextAwareConverter<AsPromotedItemModel, AsPromotedItem, AsItemConfigurationConverterContext> asPromotedItemConverter)
	{
		this.asPromotedItemConverter = asPromotedItemConverter;
	}

	public ContextAwareConverter<AsExcludedItemModel, AsExcludedItem, AsItemConfigurationConverterContext> getAsExcludedItemConverter()
	{
		return asExcludedItemConverter;
	}

	@Required
	public void setAsExcludedItemConverter(
			final ContextAwareConverter<AsExcludedItemModel, AsExcludedItem, AsItemConfigurationConverterContext> asExcludedItemConverter)
	{
		this.asExcludedItemConverter = asExcludedItemConverter;
	}

	public ContextAwareConverter<AsBoostRuleModel, AsBoostRule, AsItemConfigurationConverterContext> getAsBoostRuleConverter()
	{
		return asBoostRuleConverter;
	}

	@Required
	public void setAsBoostRuleConverter(
			final ContextAwareConverter<AsBoostRuleModel, AsBoostRule, AsItemConfigurationConverterContext> asBoostRuleConverter)
	{
		this.asBoostRuleConverter = asBoostRuleConverter;
	}

	public ContextAwareConverter<AsPromotedSortModel, AsPromotedSort, AsItemConfigurationConverterContext> getAsPromotedSortConverter()
	{
		return asPromotedSortConverter;
	}

	@Required
	public void setAsPromotedSortConverter(
			final ContextAwareConverter<AsPromotedSortModel, AsPromotedSort, AsItemConfigurationConverterContext> asPromotedSortConverter)
	{
		this.asPromotedSortConverter = asPromotedSortConverter;
	}

	public ContextAwareConverter<AsSortModel, AsSort, AsItemConfigurationConverterContext> getAsSortConverter()
	{
		return asSortConverter;
	}

	@Required
	public void setAsSortConverter(
			final ContextAwareConverter<AsSortModel, AsSort, AsItemConfigurationConverterContext> asSortConverter)
	{
		this.asSortConverter = asSortConverter;
	}

	public ContextAwareConverter<AsExcludedSortModel, AsExcludedSort, AsItemConfigurationConverterContext> getAsExcludedSortConverter()
	{
		return asExcludedSortConverter;
	}

	@Required
	public void setAsExcludedSortConverter(
			final ContextAwareConverter<AsExcludedSortModel, AsExcludedSort, AsItemConfigurationConverterContext> asExcludedSortConverter)
	{
		this.asExcludedSortConverter = asExcludedSortConverter;
	}
}
