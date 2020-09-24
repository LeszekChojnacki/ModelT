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

import java.util.Set;
import java.util.function.BiConsumer;

import org.springframework.core.Ordered;
import org.zkoss.zul.Div;

import com.hybris.cockpitng.engine.WidgetInstanceManager;


public interface FacetChartFiltersRenderer extends Ordered
{
	void renderFilters(final WidgetInstanceManager widgetInstanceManager, final Div filterContainer,
			final BiConsumer<String, Set<String>> facetSelectionChange);
}
