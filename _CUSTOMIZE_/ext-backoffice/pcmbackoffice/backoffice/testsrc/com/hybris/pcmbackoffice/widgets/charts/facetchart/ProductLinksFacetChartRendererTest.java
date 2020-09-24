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

import static com.hybris.pcmbackoffice.widgets.charts.facetchart.ProductLinksFacetChartRenderer.MODEL_FILTERS_COUNTER;
import static com.hybris.pcmbackoffice.widgets.charts.facetchart.ProductLinksFacetChartRenderer.SCSS_ADD_NEW_PRODUCT_BUTTON;
import static com.hybris.pcmbackoffice.widgets.charts.facetchart.ProductLinksFacetChartRenderer.SCSS_GO_TO_ALL_PRODUCTS_BUTTON;
import static com.hybris.pcmbackoffice.widgets.charts.facetchart.ProductLinksFacetChartRenderer.SOCKET_ADD_NEW_PRODUCT;
import static com.hybris.pcmbackoffice.widgets.charts.facetchart.ProductLinksFacetChartRenderer.SOCKET_GO_TO_ALL_PRODUCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.search.data.pageable.FullTextSearchPageable;
import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class ProductLinksFacetChartRendererTest
{
	@InjectMocks
	private ProductLinksFacetChartRenderer productLinksSolrChartRenderer;
	private WidgetInstanceManager widgetInstanceManager;
	@Mock
	private FullTextSearchPageable fullTextSearchPageable;

	@Before
	public void setUp()
	{
		CockpitTestUtil.mockZkEnvironment();
		widgetInstanceManager = CockpitTestUtil.mockWidgetInstanceManager();
	}

	@Test
	public void shouldRenderPanel()
	{
		final Div parent = new Div();
		when(fullTextSearchPageable.getTotalCount()).thenReturn(123);

		productLinksSolrChartRenderer.render(parent, widgetInstanceManager, fullTextSearchPageable);

		final Optional<Component> labelWithTotalCount = CockpitTestUtil.find(parent, c -> isLabelWithText(c, "123"));
		assertThat(labelWithTotalCount).isPresent();

		final Optional<Component> goToAllButton = CockpitTestUtil.find(parent,
				c -> isButtonWithCssClass(c, SCSS_GO_TO_ALL_PRODUCTS_BUTTON));
		assertThat(goToAllButton).isPresent();

		final Optional<Component> addNewButton = CockpitTestUtil.find(parent,
				c -> isButtonWithCssClass(c, SCSS_ADD_NEW_PRODUCT_BUTTON));
		assertThat(addNewButton).isPresent();
	}

	@Test
	public void shouldGoToAllProducts()
	{
		final Div parent = new Div();
		when(fullTextSearchPageable.getTotalCount()).thenReturn(123);
		productLinksSolrChartRenderer.render(parent, widgetInstanceManager, fullTextSearchPageable);

		final Optional<Component> goToAllButton = CockpitTestUtil.find(parent,
				c -> isButtonWithCssClass(c, SCSS_GO_TO_ALL_PRODUCTS_BUTTON));
		assertThat(goToAllButton).isPresent();

		//when
		executeEvent(goToAllButton.get(), Events.ON_CLICK);

		//then
		verify(widgetInstanceManager).sendOutput(eq(SOCKET_GO_TO_ALL_PRODUCT), argThat(new ArgumentMatcher<Object>()
		{
			@Override
			public boolean matches(final Object argument)
			{
				return argument instanceof AdvancedSearchData;
			}
		}));
	}

	@Test
	public void shouldOpenWizardToAddNewProductProducts()
	{
		final Div parent = new Div();
		when(fullTextSearchPageable.getTotalCount()).thenReturn(123);
		productLinksSolrChartRenderer.render(parent, widgetInstanceManager, fullTextSearchPageable);

		final Optional<Component> goToAllButton = CockpitTestUtil.find(parent,
				c -> isButtonWithCssClass(c, SCSS_ADD_NEW_PRODUCT_BUTTON));
		assertThat(goToAllButton).isPresent();

		//when
		executeEvent(goToAllButton.get(), Events.ON_CLICK);

		//then
		verify(widgetInstanceManager).sendOutput(eq(SOCKET_ADD_NEW_PRODUCT), any());
	}

	@Test
	public void shouldRenderLabelTotalProductsFiltered()
	{
		final Div parent = new Div();
		when(widgetInstanceManager.getModel().getValue(MODEL_FILTERS_COUNTER, Integer.class)).thenReturn(1);

		productLinksSolrChartRenderer.render(parent, widgetInstanceManager, fullTextSearchPageable);

		final Optional<Component> labelWithTotalProductFiltered = CockpitTestUtil.find(parent, c -> isLabelWithText(c, "null*"));
		assertThat(labelWithTotalProductFiltered).isPresent();
	}

	private boolean isButtonWithCssClass(final Component c, final String s)
	{
		return c instanceof A && s.equals(((A) c).getSclass());
	}

	private boolean isLabelWithText(final Component component, final String label)
	{
		return component instanceof Label && label.equals(((Label) component).getValue());
	}

	private void executeEvent(final Component component, final String eventName)
	{
		executeEvent(component, new Event(eventName));
	}

	private void executeEvent(final Component component, final Event event)
	{
		final Iterable<EventListener<? extends Event>> events = component.getEventListeners(event.getName());
		events.forEach(listener -> {
			try
			{
				((EventListener) listener).onEvent(event);
			}
			catch (final Exception e)
			{
				fail(e.getMessage());
			}
		});
	}
}
