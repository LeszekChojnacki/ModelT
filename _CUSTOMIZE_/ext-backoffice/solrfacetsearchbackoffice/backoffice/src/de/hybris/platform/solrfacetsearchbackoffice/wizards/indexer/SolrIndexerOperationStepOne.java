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
package de.hybris.platform.solrfacetsearchbackoffice.wizards.indexer;

import de.hybris.platform.cronjob.model.JobModel;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerHotUpdateCronJobModel;
import de.hybris.platform.solrfacetsearchbackoffice.wizards.BaseSolrIndexerWizardStep;

import java.util.Map;
import java.util.Objects;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Vlayout;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class SolrIndexerOperationStepOne extends BaseSolrIndexerWizardStep
{
	@Override
	public void render(final Component parent, final ViewType customView, final Map<String, String> parameters,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		setWidgetController(widgetInstanceManager);
		initCustomView(parent);
	}

	protected void initCustomView(final Component component)
	{
		getWidgetController().setValue(PARAM_FINISHED, Boolean.FALSE);

		final Vlayout vlayout = new Vlayout();
		final Div secondRow = new Div();
		vlayout.appendChild(secondRow);

		final Label labelSecondRow = new Label(getLabelService().getObjectLabel(IndexerOperationValues._TYPECODE));

		final Combobox operationsCombo = new Combobox();
		operationsCombo.setModel(new ListModelList<Object>(Lists.newArrayList(IndexerOperationValues.values())));

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

				if (IndexerOperationValues.PARTIAL_UPDATE.equals(indexerOperation))
				{
					radio.setDisabled(true);
					radio.setVisible(false);
				}
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

		final Separator separator = new Separator();
		separator.setBar(true);
		vlayout.appendChild(separator);

		final Div thirdRow = new Div();
		vlayout.appendChild(thirdRow);

		final Button startBtn = new Button();
		startBtn.setLabel(Labels.getLabel(LABEL_CRONJOB_START));
		startBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event) throws Exception
			{
				final SolrIndexerCronJobModel cronJob = getCurrentObject(component, SolrIndexerCronJobModel.class);
				adjustSolrIndexerCronJobModel(cronJob);
				startCronJob(getWidgetController(), cronJob);
			}
		});
		thirdRow.appendChild(startBtn);

		component.appendChild(vlayout);
	}

	protected void adjustSolrIndexerCronJobModel(final SolrIndexerCronJobModel cronJob)
	{
		final JobModel jobModel = getCronJobService().getJob(SolrfacetsearchConstants.INDEXER_JOB_SPRING_ID);
		cronJob.setJob(jobModel);
		cronJob.setLogToDatabase(Boolean.TRUE);
	}
}
