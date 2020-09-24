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

import org.apache.logging.log4j.util.Strings;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchMode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.cockpitng.search.data.pageable.FullTextSearchPageable;



public class ProductLinksFacetChartRenderer implements FacetChartRightPanelRenderer
{
	protected static final String SOCKET_GO_TO_ALL_PRODUCT = "goToAllProducts";
	protected static final String SOCKET_ADD_NEW_PRODUCT = "addNewProduct";
	protected static final String MODEL_FILTERS_COUNTER = "filtersCounter";

	protected static final String SCSS_TOTAL_PRODUCT_CONTAINER = "yw-solrfacetchart-rightpanel-container";
	protected static final String SCSS_TOTAL_PRODUCT_COUNT = "yw-solrfacetchart-rightpanel-total-product-count";
	protected static final String SCSS_TOTAL_PRODUCT_COUNT_LABEL = "yw-solrfacetchart-rightpanel-total-product-count-label";
	protected static final String SCSS_GO_TO_ALL_PRODUCTS_BUTTON = "yw-solrfacetchart-rightpanel-go-to-all-products-button";
	protected static final String SCSS_ADD_NEW_PRODUCT_BUTTON = "yw-solrfacetchart-rightpanel-add-new-product-button";

	private static final String LABEL_BROWSE_ALL_PRODUCTS = "solrchart.productlinks.browase_all_product";
	private static final String LABEL_ADD_NEW_PRODUCT = "solrchart.productlinks.add_new_product";
	private static final String LABEL_TOTAL_PRODUCTS = "solrchart.productlinks.total_products";

	public void render(final Div parent, final WidgetInstanceManager widgetInstanceManager,
			final FullTextSearchPageable fullTextSearchPagable)
	{
		parent.getChildren().clear();

		final Div container = new Div();
		container.setParent(parent);
		container.setSclass(SCSS_TOTAL_PRODUCT_CONTAINER);

		final Label totalProductCount = new Label(fullTextSearchPagable.getTotalCount() + "");
		totalProductCount.setParent(container);
		totalProductCount.setSclass(SCSS_TOTAL_PRODUCT_COUNT);

		final Label totalProductCountLabel = new Label(getTotalProductCountLabelValue(widgetInstanceManager));
		totalProductCountLabel.setParent(container);
		totalProductCountLabel.setSclass(SCSS_TOTAL_PRODUCT_COUNT_LABEL);

		final A goToAllProducts = new A(Labels.getLabel(LABEL_BROWSE_ALL_PRODUCTS));
		goToAllProducts.setParent(container);
		goToAllProducts.addEventListener(Events.ON_CLICK, event -> goToAllProductsClick(widgetInstanceManager));
		goToAllProducts.setSclass(SCSS_GO_TO_ALL_PRODUCTS_BUTTON);

		final A addNewProduct = new A(Labels.getLabel(LABEL_ADD_NEW_PRODUCT));
		addNewProduct.setParent(container);
		addNewProduct.addEventListener(Events.ON_CLICK, event -> addNewProduct(widgetInstanceManager));
		addNewProduct.setSclass(SCSS_ADD_NEW_PRODUCT_BUTTON);
	}

	private void addNewProduct(final WidgetInstanceManager widgetInstanceManager)
	{
		widgetInstanceManager.sendOutput(SOCKET_ADD_NEW_PRODUCT, ProductModel._TYPECODE);
	}

	private void goToAllProductsClick(final WidgetInstanceManager widgetInstanceManager)
	{
		final AdvancedSearchData queryData = createEmptySearchData();
		widgetInstanceManager.sendOutput(SOCKET_GO_TO_ALL_PRODUCT, queryData);
	}

	private AdvancedSearchData createEmptySearchData()
	{
		final AdvancedSearchData queryData = new AdvancedSearchData();
		queryData.setTypeCode(ProductModel._TYPECODE);
		queryData.setSearchQueryText(Strings.EMPTY);
		queryData.setTokenizable(true);
		queryData.setAdvancedSearchMode(AdvancedSearchMode.SIMPLE);
		queryData.setIncludeSubtypes(true);
		queryData.setGlobalOperator(ValueComparisonOperator.OR);
		return queryData;
	}

	private boolean isAnyFacetSelected(final WidgetInstanceManager widgetInstanceManager)
	{
		final Integer numberOfFilters = widgetInstanceManager.getModel().getValue(MODEL_FILTERS_COUNTER, Integer.class);
		return numberOfFilters != null && numberOfFilters > 0;
	}

	private String getTotalProductCountLabelValue(final WidgetInstanceManager widgetInstanceManager)
	{
		final boolean isSelectedFacets = isAnyFacetSelected(widgetInstanceManager);
		final String totalProductCountLabelValue = Labels.getLabel(LABEL_TOTAL_PRODUCTS);
		return isSelectedFacets ? totalProductCountLabelValue + "*" : totalProductCountLabelValue;
	}
}
