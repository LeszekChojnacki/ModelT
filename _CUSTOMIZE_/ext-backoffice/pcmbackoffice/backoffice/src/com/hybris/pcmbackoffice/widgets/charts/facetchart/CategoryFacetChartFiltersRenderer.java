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
package com.hybris.pcmbackoffice.widgets.charts.facetchart;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetSearchService;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.common.params.FacetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;

import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchService;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.SimpleSearchQueryData;


public class CategoryFacetChartFiltersRenderer implements FacetChartFiltersRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(CategoryFacetChartFiltersRenderer.class);

	private static final String FACET_NAME = "category";
	private static final String CATEGORY_SELECTED_VALUE = "categorySelectedValue";

	private static final String LABEL_CATEGORY = "solrchart.categoryfilterrenderer.category";
	private static final String FACET_ALL_VALUES = "-1";

	private static final String SCLASS_BUTTON_REMOVE_FILTER = "ye-text-button ye-delete-btn";

	private BackofficeFacetSearchService facetSearchService;
	private BiConsumer<String, Set<String>> facetSelectionChangeConsumer;
	private WidgetInstanceManager widgetInstanceManager;

	private int order = 200;

	@Override
	public void renderFilters(final WidgetInstanceManager widgetInstanceManager, final Div filterContainer,
			final BiConsumer<String, Set<String>> facetSelectionChange)
	{
		this.widgetInstanceManager = widgetInstanceManager;
		this.facetSelectionChangeConsumer = facetSelectionChange;

		appendCategoryLabel(filterContainer);
		appendRemoveFilterButton(filterContainer);
		appendChosenBox(filterContainer);
	}

	protected void appendRemoveFilterButton(final Div filterContainer)
	{
		final Button removeFilterButton = new Button();
		removeFilterButton.setClass(SCLASS_BUTTON_REMOVE_FILTER);
		removeFilterButton.addEventListener(Events.ON_CLICK, event -> removeFilters());
		removeFilterButton.setParent(filterContainer);
	}

	protected void appendCategoryLabel(final Div filterContainer)
	{
		final Label categoryLabel = new Label(Labels.getLabel(LABEL_CATEGORY));
		categoryLabel.setParent(filterContainer);
	}

	protected void appendChosenBox(final Div filterContainer)
	{
		final Chosenbox chosenbox = new Chosenbox();
		chosenbox.setModel(getCategories());
		chosenbox.addEventListener(Events.ON_SELECT,
				event -> onSelectCategory(((SelectEvent<Chosenbox, String>) event).getSelectedObjects()));

		final Collection<String> selectedValues = readSelectedValues();
		final Set<String> selectedCategories = getSelectedCategories((ListModelList) chosenbox.getModel(), selectedValues);
		if (selectedValues.size() != selectedCategories.size())
		{
			storeSelectedValues(selectedCategories);
			if (selectedCategories.isEmpty())
			{
				facetSelectionChangeConsumer.accept(FACET_NAME, selectedCategories);
				return;
			}
		}
		chosenbox.setSelectedObjects(selectedCategories);
		chosenbox.setParent(filterContainer);
	}

	protected ListModelList<String> getCategories()
	{
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.setMultiple(true);
		try
		{
			listModel.addAll(extractCategory().collect(Collectors.toList()));
		}
		catch (final FacetSearchException e)
		{
			LOG.warn("Can not load categories, did you index solr? ({})", e.getMessage());
		}

		return listModel;
	}

	protected Set<String> getSelectedCategories(final ListModelList listModel, final Collection<String> selectedValues)
	{
		return selectedValues.stream() //
				.filter(listModel::contains) //
				.collect(Collectors.toSet());
	}

	private Stream<String> extractCategory() throws FacetSearchException
	{
		final SearchQueryData conf = new SimpleSearchQueryData(ProductModel._TYPECODE);
		final SearchQuery query = facetSearchService.createBackofficeSolrSearchQuery(conf);
		query.addRawParam(FacetParams.FACET_LIMIT, FACET_ALL_VALUES);
		final SearchResult res = facetSearchService.search(query);

		final Facet facet = res.getFacet(FACET_NAME);
		if (facet == null)
		{
			return Stream.empty();
		}
		return Stream.of(facet) //
				.map(Facet::getFacetValues) //
				.flatMap(Collection::stream) //
				.map(FacetValue::getName);
	}

	protected void onSelectCategory(final Set<String> selectedCategories)
	{
		storeSelectedValues(selectedCategories);
		facetSelectionChangeConsumer.accept(FACET_NAME, selectedCategories);
	}

	protected Collection<String> readSelectedValues()
	{
		final Collection<String> selectedValues = widgetInstanceManager.getModel().getValue(CATEGORY_SELECTED_VALUE,
				Collection.class);
		return selectedValues == null ? Collections.<String> emptySet() : selectedValues;
	}

	protected void removeFilters()
	{
		storeSelectedValues(Collections.emptySet());
		facetSelectionChangeConsumer.accept(FACET_NAME, Collections.emptySet());
	}

	protected void storeSelectedValues(final Collection<String> selectedValues)
	{
		widgetInstanceManager.getModel().setValue(CATEGORY_SELECTED_VALUE, selectedValues);
	}

	public FacetSearchService getFacetSearchService()
	{
		return facetSearchService;
	}

	@Required
	public void setFacetSearchService(final BackofficeFacetSearchService facetSearchService)
	{
		this.facetSearchService = facetSearchService;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}
}
