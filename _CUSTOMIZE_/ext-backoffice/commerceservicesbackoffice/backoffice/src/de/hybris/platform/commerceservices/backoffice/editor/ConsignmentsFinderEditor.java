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
package de.hybris.platform.commerceservices.backoffice.editor;


import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.AdvancedSearch;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class ConsignmentsFinderEditor extends AbstractComponentWidgetAdapterAware implements CockpitEditorRenderer<Object>
{
	private static final Logger LOG = Logger.getLogger(ConsignmentsFinderEditor.class);
	private static final String OUTPUT_SOCKET = "finderOutput";
	private static final String CONSIGNMENT_TYPE_CODE = "Consignment";
	private static final String CONSIGNMENT_SEARCH_EDITOR_NAME = "consignment-advanced-search";

	@Override
	public void render(final Component parent, final EditorContext<Object> warehouseEditorContext,
			final EditorListener<Object> warehouseEditorListener)
	{
		final Div cnt = new Div();
		final Button button = new Button(Labels.getLabel("hmc.findconsignmentsforwarehouse"));

		button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event)
			{
				final AdvancedSearchData searchData = new AdvancedSearchData();
				searchData.setTypeCode(CONSIGNMENT_TYPE_CODE);
				final WidgetInstanceManager wim = (WidgetInstanceManager) warehouseEditorContext.getParameter("wim");
				searchData.setGlobalOperator(ValueComparisonOperator.AND);

				final AdvancedSearchInitContext initContext = createSearchContext(searchData, wim, warehouseEditorContext);
				sendOutput(OUTPUT_SOCKET, initContext);

			}
		});

		parent.appendChild(cnt);
		cnt.appendChild(button);
	}

	protected AdvancedSearchInitContext createSearchContext(final AdvancedSearchData searchData, final WidgetInstanceManager wim,
			final EditorContext<Object> warehouseEditorContext)
	{
		final AdvancedSearch config = loadAdvancedConfiguration(wim);

		for (final FieldType field : config.getFieldList().getField())
		{
			if ("warehouse".equals(field.getName()))
			{
				searchData.addCondition(field, ValueComparisonOperator.EQUALS, warehouseEditorContext.getParameter("parentObject"));
				field.setDisabled(Boolean.TRUE);
			}

			if ("order".equals(field.getName()))
			{
				searchData.addCondition(field, ValueComparisonOperator.IS_NOT_EMPTY, null);
			}
		}

		return new AdvancedSearchInitContext(searchData, config);

	}

	protected AdvancedSearch loadAdvancedConfiguration(final WidgetInstanceManager wim)
	{
		final DefaultConfigContext context = new DefaultConfigContext(CONSIGNMENT_SEARCH_EDITOR_NAME, CONSIGNMENT_TYPE_CODE);
		try
		{
			return wim.loadConfiguration(context, AdvancedSearch.class);
		}
		catch (final CockpitConfigurationException cce)
		{
			LOG.error("Failed to load advanced configuration.", cce);
			return null;
		}
	}
}
