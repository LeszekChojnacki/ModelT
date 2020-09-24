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

import java.util.List;

import org.zkoss.chart.Point;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.util.YTestTools;


public class FacetChartBottomPanelRenderer
{
	private static final String SCLASS_CHART_ONE_POINT_CONTAINER = "yw-solrfacetchart-bottompanel-onepoint";
	private static final String SCLASS_CHART_INFO_LABEL = "yw-solrfacetchart-bottompanel-onepoint-label";
	private static final String SCLASS_CHART_INFO_VALUE = "yw-solrfacetchart-bottompanel-onepoint-value";
	private static final String SCLASS_CHART_COLOR_LABEL = "yw-solrfacetchart-bottompanel-onepoint-color-label";

	public void render(final Div parent, final List<Point> points, final EventListener<? super Event> onClickListener)
	{
		parent.getChildren().clear();
		points.forEach(p -> renderPoint(p, parent, onClickListener));
	}

	private static void renderPoint(final Point pointData, final Div parent, final EventListener<? super Event> onClickListener)
	{
		final Div onePointContainer = new Div();
		onePointContainer.setSclass(SCLASS_CHART_ONE_POINT_CONTAINER);

		final Label colorLabel = new Label();
		colorLabel.setSclass(SCLASS_CHART_COLOR_LABEL);
		if (pointData.getColor() != null)
		{
			colorLabel.setStyle("background-color: " + pointData.getColor().stringValue());
		}

		final Label infoLabel = new Label(pointData.getName());
		infoLabel.setSclass(SCLASS_CHART_INFO_LABEL);

		final Label valueLabel = new Label(pointData.getY().toString());
		valueLabel.setSclass(SCLASS_CHART_INFO_VALUE);

		onePointContainer.getChildren().add(colorLabel);
		onePointContainer.getChildren().add(infoLabel);
		onePointContainer.getChildren().add(valueLabel);
		onePointContainer.setParent(parent);

		YTestTools.modifyYTestId(onePointContainer, String.format("solr-chart-data-point_%s", pointData.getName()));

		onePointContainer.addEventListener(Events.ON_CLICK, event -> {
			final Event myEvent = new Event(FacetChartController.EVENT_ON_POINT_CLICK, onePointContainer, pointData);
			onClickListener.onEvent(myEvent);

		});
	}
}
