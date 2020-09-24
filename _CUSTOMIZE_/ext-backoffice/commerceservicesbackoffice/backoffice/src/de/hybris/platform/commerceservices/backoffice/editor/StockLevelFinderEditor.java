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


import de.hybris.platform.commons.renderer.exceptions.RendererException;
import de.hybris.platform.core.model.product.ProductModel;

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


public class StockLevelFinderEditor extends AbstractComponentWidgetAdapterAware implements CockpitEditorRenderer<Object>
{

	private enum SearchFieldType
	{
		WAREHOUSE, PRODUCT
	}

	private static final Logger LOG = Logger.getLogger(StockLevelFinderEditor.class);
	private static final String STOCK_LEVEL_SEARCH_FIELD = "stockLevelSearchField";
	private static final String OUTPUT_SOCKET = "finderOutput";
	private static final String STOCK_LEVEL_TYPE_CODE = "StockLevel";
	private static final String WAREHOUSE_SEARCH_EDITOR_NAME = "warehouse-advanced-search";
	private static final String PRODUCTCODE_SEARCH_EDITOR_NAME = "productcode-advanced-search";

	@Override
	public void render(final Component parent, final EditorContext<Object> warehouseEditorContext,
			final EditorListener<Object> warehouseEditorListener)
	{
		final Object stockLevelSearchField = warehouseEditorContext.getParameter(STOCK_LEVEL_SEARCH_FIELD);

		if (stockLevelSearchField == null)
		{
			throw new RendererException("No parameter stockLevelSearchField found");
		}

		final Div cnt = new Div();
		final SearchFieldType fieldType = SearchFieldType.valueOf(stockLevelSearchField.toString().toUpperCase());
		final Button button = createFinderButton(fieldType);

		button.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event)
			{
				final AdvancedSearchData searchData = new AdvancedSearchData();
				searchData.setTypeCode(STOCK_LEVEL_TYPE_CODE);
				final WidgetInstanceManager wim = (WidgetInstanceManager) warehouseEditorContext.getParameter("wim");
				searchData.setGlobalOperator(ValueComparisonOperator.AND);

				final AdvancedSearchInitContext initContext = createSearchContext(searchData, fieldType, wim, warehouseEditorContext);
				sendOutput(OUTPUT_SOCKET, initContext);

			}
		});

		parent.appendChild(cnt);
		cnt.appendChild(button);
	}

	protected Button createFinderButton(final SearchFieldType fieldType)
	{
		if (SearchFieldType.WAREHOUSE == fieldType)
		{
			return new Button(Labels.getLabel("hmc.findstocklevelsforwarehouse"));
		}
		else if (SearchFieldType.PRODUCT == fieldType)
		{
			return new Button(Labels.getLabel("hmc.findstocklevelsforproduct"));
		}

		return null;
	}

	protected AdvancedSearchInitContext createSearchContext(final AdvancedSearchData searchData, final SearchFieldType type,
			final WidgetInstanceManager wim, final EditorContext<Object> warehouseEditorContext)
	{
		if (SearchFieldType.WAREHOUSE == type)
		{
			final AdvancedSearch config = loadAdvancedConfiguration(wim, WAREHOUSE_SEARCH_EDITOR_NAME);

			for (final FieldType field : config.getFieldList().getField())
			{
				if ("warehouse".equals(field.getName()))
				{
					searchData
							.addCondition(field, ValueComparisonOperator.EQUALS, warehouseEditorContext.getParameter("parentObject"));
					field.setDisabled(Boolean.TRUE);
				}

				if ("productCode".equals(field.getName()))
				{
					searchData.addCondition(field, ValueComparisonOperator.STARTS_WITH, "");
				}
			}

			return new AdvancedSearchInitContext(searchData, config);
		}
		else if (SearchFieldType.PRODUCT == type)
		{
			final AdvancedSearch config = loadAdvancedConfiguration(wim, PRODUCTCODE_SEARCH_EDITOR_NAME);

			for (final FieldType field : config.getFieldList().getField())
			{
				if ("productCode".equals(field.getName()))
				{
					searchData.addCondition(field, ValueComparisonOperator.EQUALS,
							((ProductModel) warehouseEditorContext.getParameter("parentObject")).getCode());
					field.setDisabled(Boolean.TRUE);
				}

				if ("warehouse".equals(field.getName()))
				{
					searchData.addCondition(field, ValueComparisonOperator.IS_NOT_EMPTY, null);
				}
			}
			return new AdvancedSearchInitContext(searchData, config);
		}
		return null;
	}

	protected AdvancedSearch loadAdvancedConfiguration(final WidgetInstanceManager wim, final String name)
	{
		final DefaultConfigContext context = new DefaultConfigContext(name, STOCK_LEVEL_TYPE_CODE);
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
