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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.chart.Charts;
import org.zkoss.chart.ChartsEvent;
import org.zkoss.chart.ChartsEvents;
import org.zkoss.chart.Color;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;

import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchMode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.facades.search.FieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.FullTextSearchData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.pageable.FullTextSearchPageable;
import com.hybris.cockpitng.testing.annotation.InextensibleMethod;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class FacetChartController extends DefaultWidgetController
{
	protected static final String EVENT_ON_POINT_CLICK = "onPointClick";
	protected static final String COMPONENT_SHOW_FILTERS_BUTTON_ID = "showFilters";
	protected static final String SELECTED_FACETS = "selectedFacets";
	protected static final String SOCKET_IN_PAGEABLE = "fullTextSearchPageable";
	protected static final String SOCKET_IN_INIT_SEARCH = "initSearch";
	protected static final String SOCKET_OUT_FACETS = "selectedFacets";
	protected static final String SOCKET_OUT_INITIAL_SEARCH_DATA = "initialSearchData";
	private static final Logger LOG = LoggerFactory.getLogger(FacetChartController.class);
	private static final String TYPE_CODE_PRODUCT = "Product";
	private static final String MODEL_CURRENT_FACET = "currentFacet";
	protected static final String MODEL_FILTERS_COUNTER = "filtersCounter";

	private static final String SETTING_BOTTOM_PANEL_RENDERER = "bottomPanelRenderer";
	private static final String SETTING_FILTER_PANEL_RENDERER = "filterPanelRenderer";
	private static final Object SETTING_FACET_CHOOSER_RENDERER = "facetChooserRenderer";
	private static final String SETTING_RIGHT_PANEL_RENDERER = "rightPanelRenderer";
	private static final String SETTING_EXPORT_ENABLED = "exportEnabled";
	private static final String SETTING_FILTER_PANEL_RENDERERS = "filterPanelRendererNames";
	private static final String SETTING_CHART_DECORATOR_NAME = "chartDecorator";
	private static final String SETTING_FACETS_NAMES = "facetsNames";
	private static final String SETTING_CHART_TITLE_KEY = "chartTitleKey";

	private static final String SCLASS_FILTERS_COUNTER = "yw-solrfacetchart-toppanel-filters-counter";

	@Wire
	private Charts charts;
	@Wire
	private Div bottomPanel;
	@Wire
	private Button showFilters;
	@Wire
	private Popup filtersPopup;
	@Wire
	private Div filtersContainer;
	@Wire
	private Div facetChooser;
	@Wire
	private Div rightPanel;
	@Wire
	private Label chartTitle;
	@Wire
	private Label filtersCounterLabel;

	@WireVariable
	private transient FacetChartDataExtractor facetChartDataExtractor;

	private transient FacetChartBottomPanelRenderer facetChartBottomPanelRenderer;
	private transient DefaultFacetChartFiltersRenderer facetChartFiltersRenderer;
	private transient FacetChartFacetChooserRenderer facetChartFacetChooserRenderer;
	private transient FacetChartRightPanelRenderer rightPanelRenderer;
	private transient FacetChartComposer facetChartComposer;

	private transient FullTextSearchData fullTextSearchData;
	private transient Object originalQuery;
	private transient FullTextSearchPageable fullTextSearchPagable;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

		initBeans();

		setChartTitle();

		composeChart(charts);

		initCurrentFacet();

		renderFilters();

		initializeFiltersCounterLabel();
	}

	protected void initBeans()
	{
		facetChartBottomPanelRenderer = BackofficeSpringUtil.getBean(getBottomPanelRendererName(),
				FacetChartBottomPanelRenderer.class);
		facetChartFiltersRenderer = BackofficeSpringUtil.getBean(getFilterPanelRendererName(),
				DefaultFacetChartFiltersRenderer.class);
		facetChartFiltersRenderer.loadRenderers(getFilterRendererNames());
		facetChartFacetChooserRenderer = BackofficeSpringUtil.getBean(getFacetChooserRendererName(),
				FacetChartFacetChooserRenderer.class);
		facetChartComposer = BackofficeSpringUtil.getBean(getChartComposerName(), FacetChartComposer.class);
		rightPanelRenderer = BackofficeSpringUtil.getBean(getRightPanelRendererName(), FacetChartRightPanelRenderer.class);
	}

	protected void setChartTitle()
	{
		final String chartTitleKey = getWidgetSettings().getString(SETTING_CHART_TITLE_KEY);
		if (chartTitleKey != null)
		{
			getChartTitle().setValue(Labels.getLabel(chartTitleKey));
		}
	}

	protected void initCurrentFacet()
	{
		if (getCurrentFacet() == null)
		{
			if (getFacetNames() == null || getFacetNames().isEmpty())
			{
				LOG.warn("No facet is set, add at least one facet to configuration");
				storeCurrentFacet(StringUtils.EMPTY);
				return;
			}
			storeCurrentFacet(getFacetNames().get(0));
		}
	}

	@SocketEvent(socketId = SOCKET_IN_INIT_SEARCH)
	public void initSearch()
	{
		final AdvancedSearchData advancedSearchData = createAdvancedSearchData();
		applySelectedFacets(advancedSearchData);

		sendOutput(SOCKET_OUT_INITIAL_SEARCH_DATA, advancedSearchData);
	}

	protected AdvancedSearchData createAdvancedSearchData()
	{
		final AdvancedSearchData advancedSearchData = new AdvancedSearchData();
		advancedSearchData.setTypeCode(TYPE_CODE_PRODUCT);
		advancedSearchData.setIncludeSubtypes(true);
		advancedSearchData.setGlobalOperator(ValueComparisonOperator.OR);
		advancedSearchData.setAdvancedSearchMode(AdvancedSearchMode.SIMPLE);
		advancedSearchData.setSearchQueryText(StringUtils.EMPTY);
		advancedSearchData.setSelectedFacets(new HashMap<>());

		return advancedSearchData;
	}

	@InextensibleMethod
	private void applySelectedFacets(final AdvancedSearchData advancedSearchData)
	{
		@SuppressWarnings("unchecked")
		final Map<String, Set<String>> facets = getValue(SELECTED_FACETS, Map.class);
		if (facets != null && !facets.isEmpty())
		{
			advancedSearchData.setSelectedFacets(facets);
		}
	}

	@SocketEvent(socketId = SOCKET_IN_PAGEABLE)
	public void onInput(final FullTextSearchPageable fullTextSearchPagable)
	{
		if (fullTextSearchPagable == null)
		{
			return;
		}
		executeSearch(fullTextSearchPagable);
		this.fullTextSearchPagable = fullTextSearchPagable;

		fullTextSearchData = fullTextSearchPagable.getFullTextSearchData();
		if (fullTextSearchData == null)
		{
			return;
		}
		originalQuery = fullTextSearchData.getContext().getAttribute(FieldSearchFacadeStrategy.CONTEXT_ORIGINAL_QUERY);
		render();
		renderFacetChooser();
	}

	protected void executeSearch(final FullTextSearchPageable fullTextSearchPagable)
	{
		fullTextSearchPagable.getCurrentPage();
		fullTextSearchPagable.onPageLoaded();
	}

	protected void render()
	{
		if (fullTextSearchData == null)
		{
			LOG.warn("No data to show");
			return;
		}
		getCharts().getSeries().remove();
		getCharts().getChildren().clear();

		final Series series = getCharts().getSeries();

		final List<Point> points = facetChartDataExtractor.getPoints(fullTextSearchData, getCurrentFacet());//
		points.forEach(series::addPoint);
		composeSeries(series);

		assignColorsToPoint(points);

		renderFilters();
		renderBottomPanel(points);
		renderRightPanel(fullTextSearchPagable);
	}

	protected void composeSeries(final Series series)
	{
		if (facetChartComposer != null)
		{
			series.setName(getValue(MODEL_CURRENT_FACET, String.class));
			facetChartComposer.composeSeries(series);
		}
	}

	protected void composeChart(final Charts charts)
	{
		charts.addEventListener(ChartsEvents.ON_PLOT_CLICK, this::handleClickOnPoint);
		charts.getExporting().setEnabled(isExportEnabled());
		if (facetChartComposer != null)
		{
			facetChartComposer.composeChart(charts);
		}
	}

	protected void assignColorsToPoint(final List<Point> points)
	{
		int i = 0;
		final List<Color> colors = charts.getColors();
		for (final Point p : points)
		{
			if (colors.size() <= i)
			{
				i = 0;
			}
			p.setColor(colors.get(i));
			i++;
		}
	}

	protected void renderBottomPanel(final List<Point> points)
	{
		if (facetChartBottomPanelRenderer != null)
		{
			facetChartBottomPanelRenderer.render(bottomPanel, points, this::handleClickOnPoint);
		}
	}

	protected void renderFilters()
	{
		if (facetChartFiltersRenderer != null)
		{
			facetChartFiltersRenderer.renderFilters(getWidgetInstanceManager(), filtersContainer, this::applyFacetSelection);
		}
	}

	protected void renderFacetChooser()
	{
		if (facetChartFacetChooserRenderer != null)
		{
			facetChartFacetChooserRenderer.render( //
					facetChooser, //
					getWidgetInstanceManager(), //
					this::onFacetChange, //
					convertToFacets(getFacetNames()) //
			);
		}
	}

	protected List<FacetData> convertToFacets(final List<String> facetNames)
	{
		final List<FacetData> result = new LinkedList<>();

		fullTextSearchData //
				.getFacets() //
				.forEach(facet -> facetNames //
						.stream() //
						.filter(facetName -> facetName.equals(facet.getName())) //
						.forEach(filtered -> result.add(facet)) //
		);

		return result;
	}

	protected void renderRightPanel(final FullTextSearchPageable fullTextSearchPagable)
	{
		if (rightPanelRenderer != null)
		{
			rightPanelRenderer.render(rightPanel, getWidgetInstanceManager(), fullTextSearchPagable);
		}
	}

	protected void onFacetChange(final String facetName)
	{
		storeCurrentFacet(facetName);
		render();
	}

	protected void handleClickOnPoint(final Event event)
	{
		final Optional<Point> point = extractEventData(event);

		if (originalQuery instanceof AdvancedSearchData && point.isPresent())
		{
			applyFacetSelection((AdvancedSearchData) originalQuery, point.get());
		}
	}

	protected Optional<Point> extractEventData(final Event event)
	{
		Point point = null;
		if (event instanceof ChartsEvent)
		{
			point = ((ChartsEvent) event).getPoint();
		}
		else if (EVENT_ON_POINT_CLICK.equals(event.getName()))
		{
			point = (Point) event.getData();
		}
		return Optional.ofNullable(point);
	}

	protected void applyFacetSelection(final AdvancedSearchData query, final Point point)
	{
		final AdvancedSearchData queryData = createCopyAdvancedSearchData(query);
		queryData.setTokenizable(true);

		if (queryData.getSelectedFacets() != null)
		{
			final Map<String, Set<String>> selectedFacets = new HashMap<>(queryData.getSelectedFacets());
			selectedFacets.put(getCurrentFacet(), Collections.singleton(point.getName()));
			queryData.setSelectedFacets(selectedFacets);
		}

		queryData.setAdvancedSearchMode(AdvancedSearchMode.SIMPLE);
		sendOutput(SOCKET_OUT_FACETS, queryData);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = COMPONENT_SHOW_FILTERS_BUTTON_ID)
	public void showFilters()
	{
		filtersPopup.open(showFilters, "after_end");
	}

	protected void applyFacetSelection(final String facetName, final Set<String> selectedFacets)
	{
		Validate.notNull("Selected facets can not be null", selectedFacets);

		final Map<String, Set<String>> alreadySelected = calculateSelectedFacets(facetName, selectedFacets);

		updateFiltersCounter(alreadySelected);
		setValue(SELECTED_FACETS, alreadySelected);

		if (originalQuery == null)
		{
			return;
		}

		final AdvancedSearchData queryData = createCopyAdvancedSearchData((AdvancedSearchData) originalQuery);
		queryData.setTokenizable(true);

		queryData.setSelectedFacets(alreadySelected);

		queryData.setAdvancedSearchMode(AdvancedSearchMode.SIMPLE);
		sendOutput(SOCKET_OUT_INITIAL_SEARCH_DATA, queryData);
	}

	@InextensibleMethod
	private Map<String, Set<String>> calculateSelectedFacets(final String facetName, final Set<String> selectedFacets)
	{
		final Map<String, Set<String>> alreadySelected = getSelectedFacetsCopy();
		if (selectedFacets.isEmpty())
		{
			alreadySelected.remove(facetName);
		}
		else
		{
			alreadySelected.put(facetName, selectedFacets);
		}
		return alreadySelected;
	}

	@InextensibleMethod
	private Map<String, Set<String>> getSelectedFacetsCopy()
	{
		final Map<String, Set<String>> alreadySelected = getValue(SELECTED_FACETS, Map.class);
		final HashMap<String, Set<String>> newFilters;
		if (alreadySelected == null)
		{
			newFilters = new HashMap<>();
		}
		else
		{
			newFilters = new HashMap<>(alreadySelected);
		}
		return newFilters;
	}

	protected AdvancedSearchData createCopyAdvancedSearchData(final AdvancedSearchData data)
	{
		return new AdvancedSearchData(data);
	}

	protected List<String> getFacetNames()
	{
		final String facetsFromSettings = getWidgetInstanceManager().getWidgetSettings().getString(SETTING_FACETS_NAMES);
		if (StringUtils.isBlank(facetsFromSettings))
		{
			return Collections.emptyList();
		}
		return Arrays.asList(facetsFromSettings.split(","));
	}

	protected void initializeFiltersCounterLabel()
	{
		getFiltersCounterLabel().setSclass(SCLASS_FILTERS_COUNTER);
		final Integer numberOfFilters = getModel().getValue(MODEL_FILTERS_COUNTER, Integer.class);

		if (numberOfFilters == null)
		{
			setFiltersCounterLabelValue(0);
		}
		else
		{
			setFiltersCounterLabelValue(numberOfFilters);
		}
	}

	protected void setFiltersCounterLabelValue(final int numberOfFilters)
	{
		final String numberOfFiltersAsString = String.valueOf(numberOfFilters);
		getFiltersCounterLabel().setValue(numberOfFiltersAsString);
	}

	protected void saveFiltersCounterModelValue(final int numberOfFilters)
	{
		getModel().setValue(MODEL_FILTERS_COUNTER, Integer.valueOf(numberOfFilters));
	}

	protected void updateFiltersCounter(final Map<String, Set<String>> alreadySelected)
	{
		final int numberOfFilters = alreadySelected.values().stream().mapToInt(Set::size).sum();
		setFiltersCounterLabelValue(numberOfFilters);
		saveFiltersCounterModelValue(numberOfFilters);
	}

	protected String getBottomPanelRendererName()
	{
		return getWidgetInstanceManager().getWidgetSettings().getString(SETTING_BOTTOM_PANEL_RENDERER);
	}

	protected String getFilterPanelRendererName()
	{
		return getWidgetInstanceManager().getWidgetSettings().getString(SETTING_FILTER_PANEL_RENDERER);
	}

	protected String getFacetChooserRendererName()
	{
		return getWidgetInstanceManager().getWidgetSettings().getString(SETTING_FACET_CHOOSER_RENDERER);
	}

	protected String getChartComposerName()
	{
		return getWidgetInstanceManager().getWidgetSettings().getString(SETTING_CHART_DECORATOR_NAME);
	}

	protected boolean isExportEnabled()
	{
		return getWidgetInstanceManager().getWidgetSettings().getBoolean(SETTING_EXPORT_ENABLED);
	}

	protected List<String> getFilterRendererNames()
	{
		final String rendererNames = getWidgetInstanceManager().getWidgetSettings().getString(SETTING_FILTER_PANEL_RENDERERS);
		if (StringUtils.isBlank(rendererNames))
		{
			return Collections.emptyList();
		}
		return Arrays.asList(rendererNames.split(","));
	}

	protected String getRightPanelRendererName()
	{
		return getWidgetInstanceManager().getWidgetSettings().getString(SETTING_RIGHT_PANEL_RENDERER);
	}

	protected String getCurrentFacet()
	{
		return getValue(MODEL_CURRENT_FACET, String.class);
	}

	protected void storeCurrentFacet(final String curentFacet)
	{
		setValue(MODEL_CURRENT_FACET, curentFacet);
	}

	protected Charts getCharts()
	{
		return this.charts;
	}

	protected Div getBottomPanel()
	{
		return bottomPanel;
	}

	protected void setFacetChartDataExtractor(final FacetChartDataExtractor facetChartDataExtractor)
	{
		this.facetChartDataExtractor = facetChartDataExtractor;
	}

	protected void setFacetChartBottomPanelRenderer(final FacetChartBottomPanelRenderer facetChartBottomPanelRenderer)
	{
		this.facetChartBottomPanelRenderer = facetChartBottomPanelRenderer;
	}

	protected Button getShowFilters()
	{
		return showFilters;
	}

	protected Popup getFiltersPopup()
	{
		return filtersPopup;
	}

	protected Div getFiltersContainer()
	{
		return filtersContainer;
	}

	protected Div getFacetChooser()
	{
		return facetChooser;
	}

	protected void setFacetChooser(final Div facetChooser)
	{
		this.facetChooser = facetChooser;
	}

	protected Div getRightPanel()
	{
		return rightPanel;
	}

	protected Label getChartTitle()
	{
		return chartTitle;
	}

	protected Label getFiltersCounterLabel()
	{
		return filtersCounterLabel;
	}
}
