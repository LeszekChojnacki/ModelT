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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.hybris.cockpitng.search.data.facet.FacetData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.testing.util.CockpitTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class FacetChartFacetChooserRendererTest
{
	private static final String FACET_NAME = "facetName";

	private FacetChartFacetChooserRenderer facetChartFacetChooserRenderer;
	private WidgetInstanceManager widgetInstanceManager;

	@Spy
	private Div parent;
	@Mock
	private Consumer<String> onFacetSelectionChangeConsumer;
	@Mock
	private FacetData facetData;

	@Before
	public void setUp()
	{
		CockpitTestUtil.mockZkEnvironment();
		widgetInstanceManager = CockpitTestUtil.mockWidgetInstanceManager();
		facetChartFacetChooserRenderer = new FacetChartFacetChooserRenderer();
	}

	@Test
	public void shouldNotRenderComboBoxWithOneItem()
	{
		facetChartFacetChooserRenderer.render(parent, widgetInstanceManager, onFacetSelectionChangeConsumer,
				Collections.singletonList(facetData));

		final Optional<Combobox> combobox = CockpitTestUtil.find(parent, Combobox.class);
		assertThat(combobox.isPresent()).isFalse();
	}

	@Test
	public void shouldRenderComboBoxWithTreeItem()
	{
		facetChartFacetChooserRenderer.render(parent, widgetInstanceManager, onFacetSelectionChangeConsumer,
				Arrays.asList(facetData, facetData, facetData));

		final Optional<Combobox> combobox = CockpitTestUtil.find(parent, Combobox.class);
		assertThat(combobox).isPresent();
		assertThat(CockpitTestUtil.findAll(combobox.get(), Comboitem.class).count()).isEqualTo(3);
	}

	@Test
	public void shouldFireEventAfterSelectionChange()
	{
		facetChartFacetChooserRenderer.render(parent, widgetInstanceManager, onFacetSelectionChangeConsumer,
				Arrays.asList(facetData, facetData, facetData));

		final Comboitem comboitem = new Comboitem(FACET_NAME);
		comboitem.setValue(FACET_NAME);
		final Set<Comboitem> selected = Sets.newSet(comboitem);
		final SelectEvent<Comboitem, String> selectEvent = new SelectEvent<>("", null, selected);

		facetChartFacetChooserRenderer.onSelectFacet(selectEvent);

		final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
		verify(onFacetSelectionChangeConsumer).accept(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualToIgnoringCase(FACET_NAME);
	}

	@Test
	public void shouldDoNothingWhenNoFacetAreSet()
	{
		facetChartFacetChooserRenderer.render(parent, widgetInstanceManager, onFacetSelectionChangeConsumer,
				Collections.emptyList());

		assertThat(parent.getChildren()).hasSize(0);
	}
}
