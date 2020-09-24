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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.springframework.core.OrderComparator;
import org.zkoss.zul.Div;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class DefaultFacetChartFiltersRenderer
{
	private List<FacetChartFiltersRenderer> renderers = new ArrayList<>();

	private final OrderComparator orderComparator = new OrderComparator();

	public void renderFilters(final WidgetInstanceManager widgetInstanceManager, final Div filterContainer,
			final BiConsumer<String, Set<String>> facetSelectionChange)
	{
		filterContainer.getChildren().clear();

		renderers.forEach(renderer -> renderer.renderFilters(widgetInstanceManager, filterContainer, facetSelectionChange));
	}

	public List<FacetChartFiltersRenderer> getRenderers()
	{
		return renderers;
	}

	public void setRenderers(final List<FacetChartFiltersRenderer> renderers)
	{
		this.renderers = renderers;
	}

	public void loadRenderers(final List<String> renderers)
	{
		loadRenderersInternal(renderers);
		renderers.sort(orderComparator);
	}

	private void loadRenderersInternal(final List<String> renderersToLoad)
	{
		for (final String name : renderersToLoad)
		{
			final FacetChartFiltersRenderer renderer = BackofficeSpringUtil.getBean(name, FacetChartFiltersRenderer.class);
			renderers.add(renderer);
		}
	}
}
