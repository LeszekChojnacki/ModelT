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

import de.hybris.platform.cronjob.model.JobModel;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerHotUpdateCronJobModel;
import de.hybris.platform.solrfacetsearchbackoffice.wizards.BaseSolrIndexerWizardStep;

import java.util.Map;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.ViewType;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class SolrHotUpdateIndexerStepTwo extends BaseSolrIndexerWizardStep
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
		final Vlayout vlayout = new Vlayout();

		final Div firstRow = new Div();
		vlayout.appendChild(firstRow);

		final Div secondRow = new Div();
		vlayout.appendChild(secondRow);

		final Label label = new Label(getLabelService()
				.getObjectLabel(SolrIndexerHotUpdateCronJobModel._TYPECODE + "." + SolrIndexerHotUpdateCronJobModel.ITEMS));

		final Editor editor = new Editor();
		final String itemsAttribute = normalizeAttribute(SolrIndexerHotUpdateCronJobModel.ITEMS);
		editor.setProperty(itemsAttribute);
		editor.setWidgetInstanceManager(getWidgetController().getWidgetInstanceManager());

		final String typeCode = getWidgetController().getValue(TYPE_CODE, String.class);
		final String type = String.format("%s%s%s", "MultiReference-LIST(", typeCode, ")");

		editor.setType(type);
		editor.afterCompose();

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

		firstRow.appendChild(label);
		firstRow.appendChild(editor);

		secondRow.appendChild(startBtn);
		component.appendChild(vlayout);
	}

	protected void adjustSolrIndexerCronJobModel(final SolrIndexerCronJobModel cronJob)
	{
		final JobModel jobModel = getCronJobService().getJob(SolrfacetsearchConstants.INDEXER_HOTUPDATE_JOB_SPRING_ID);
		cronJob.setJob(jobModel);
		cronJob.setLogToDatabase(Boolean.TRUE);
	}

}
