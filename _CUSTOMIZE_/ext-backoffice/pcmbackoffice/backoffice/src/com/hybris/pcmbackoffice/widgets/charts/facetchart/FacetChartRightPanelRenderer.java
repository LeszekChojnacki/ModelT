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

import org.zkoss.zul.Div;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.search.data.pageable.FullTextSearchPageable;


public interface FacetChartRightPanelRenderer
{
	void render(final Div parent, final WidgetInstanceManager widgetInstanceManager,
			final FullTextSearchPageable fullTextSearchPagable);
}
