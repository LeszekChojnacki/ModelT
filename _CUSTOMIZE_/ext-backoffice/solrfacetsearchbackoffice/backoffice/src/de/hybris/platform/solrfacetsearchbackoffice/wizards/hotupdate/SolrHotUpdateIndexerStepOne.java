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
package de.hybris.platform.solrfacetsearchbackoffice.wizards.hotupdate;

import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.SolrIndexedTypeCodeResolver;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerHotUpdateCronJobModel;
import de.hybris.platform.solrfacetsearchbackoffice.wizards.BaseSolrIndexerWizardStep;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Vlayout;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class SolrHotUpdateIndexerStepOne extends BaseSolrIndexerWizardStep
{
	private static final String ERROR_MESSAGE_SOLR_CONFIG_EMPTY = "Solr Facet Search Configuration cannot be empty!";

	private SolrIndexedTypeCodeResolver solrIndexedTypeCodeResolver;

	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		setWidgetController(widgetInstanceManager);
		initCustomView(parent);
	}

	protected void initCustomView(final Component component)
	{
		final SolrFacetSearchConfigModel selectedConfig = getAttribute(component,
				SolrIndexerHotUpdateCronJobModel.FACETSEARCHCONFIG, SolrFacetSearchConfigModel.class);
		Validate.notNull(selectedConfig, ERROR_MESSAGE_SOLR_CONFIG_EMPTY);

		final Vlayout mainPanel = new Vlayout();
		final Combobox typesCombo = new Combobox();

		final Div firstRow = new Div();
		mainPanel.appendChild(firstRow);

		final Div secondRow = new Div();
		mainPanel.appendChild(secondRow);

		final Label label = new Label(getLabelService().getObjectLabel(SolrIndexedTypeModel._TYPECODE));
		typesCombo.setModel(new ListModelList<>(Lists.newArrayList(getIndexedTypes(selectedConfig))));
		typesCombo.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final Comboitem selectedItem = typesCombo.getSelectedItem();
				final SolrIndexedTypeModel selectedItemValue = selectedItem == null ? null : selectedItem.getValue();
				if (selectedItemValue != null)
				{
					final String indexedTypeCode = solrIndexedTypeCodeResolver.resolveIndexedTypeCode(selectedItemValue);
					setAttribute(component, SolrIndexerHotUpdateCronJobModel.INDEXTYPENAME, indexedTypeCode);

					final Object typeCode = selectedItemValue.getType() == null ? null : selectedItemValue.getType().getCode();
					getWidgetController().setValue(TYPE_CODE, typeCode);
				}
				getWidgetController().updateNavigation();
			}
		});
		typesCombo.setItemRenderer(new ComboitemRenderer<Object>()
		{
			@Override
			public void render(final Comboitem comboitem, final Object entity, final int index) throws Exception
			{
				final SolrIndexedTypeModel indexedType = (SolrIndexedTypeModel) entity;
				final String indexedTypeCode = solrIndexedTypeCodeResolver.resolveIndexedTypeCode(indexedType);
				comboitem.setLabel(indexedTypeCode);
				comboitem.setValue(indexedType);

				final String choosenTypeName = getAttribute(component, SolrIndexerHotUpdateCronJobModel.INDEXTYPENAME, String.class);
				if (indexedTypeCode != null && indexedTypeCode.equals(choosenTypeName))
				{
					typesCombo.setSelectedItem(comboitem);
				}
			}
		});

		firstRow.appendChild(label);
		firstRow.appendChild(typesCombo);

		final Label labelSecondRow = new Label(getLabelService().getObjectLabel(IndexerOperationValues._TYPECODE));

		final Combobox operationsCombo = new Combobox();
		operationsCombo.setModel(
				new ListModelList<Object>(Lists.newArrayList(IndexerOperationValues.UPDATE, IndexerOperationValues.DELETE)));

		operationsCombo.addEventListener(Events.ON_CHANGE, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final Comboitem selectedItem = operationsCombo.getSelectedItem();
				final Object itemToSave = selectedItem == null ? null : selectedItem.getValue();
				setAttribute(component, SolrIndexerHotUpdateCronJobModel.INDEXEROPERATION, itemToSave);
				getWidgetController().updateNavigation();
			}
		});
		operationsCombo.setItemRenderer(new ComboitemRenderer<Object>()
		{
			@Override
			public void render(final Comboitem radio, final Object entity, final int index) throws Exception
			{
				final IndexerOperationValues indexerOperation = (IndexerOperationValues) entity;
				radio.setLabel(getLabelService().getObjectLabel(indexerOperation));
				radio.setValue(entity);

				final IndexerOperationValues choosenOperation = getAttribute(component,
						SolrIndexerHotUpdateCronJobModel.INDEXEROPERATION, IndexerOperationValues.class);
				if (Objects.equals(entity, choosenOperation))
				{
					operationsCombo.setSelectedItem(radio);
				}
			}
		});

		secondRow.appendChild(labelSecondRow);
		secondRow.appendChild(operationsCombo);

		component.appendChild(firstRow);
		component.appendChild(secondRow);
	}

	protected List getIndexedTypes(final SolrFacetSearchConfigModel givenConfig)
	{
		Validate.notNull(givenConfig, ERROR_MESSAGE_SOLR_CONFIG_EMPTY);
		return givenConfig.getSolrIndexedTypes();
	}

	public SolrIndexedTypeCodeResolver getSolrIndexedTypeCodeResolver()
	{
		return solrIndexedTypeCodeResolver;
	}

	@Required
	public void setSolrIndexedTypeCodeResolver(final SolrIndexedTypeCodeResolver solrIndexedTypeCodeResolver)
	{
		this.solrIndexedTypeCodeResolver = solrIndexedTypeCodeResolver;
	}
}
