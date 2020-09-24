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
package de.hybris.platform.solrfacetsearchbackoffice.wizards;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.RequestContextHolder;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;

import com.hybris.cockpitng.core.async.Operation;
import com.hybris.cockpitng.core.async.Progress;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;


public abstract class BaseSolrIndexerWizardStep extends AbstractCustomWizardStep
{
	protected static final String LABEL_CRONJOB_RUNNING = "com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.running";
	protected static final String LABEL_CRONJOB_SUCCESS = "com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.success";
	protected static final String LABEL_CRONJOB_ERROR = "com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.error";
	protected static final String LABEL_CRONJOB_START = "com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.start";
	protected static final String LABEL_CRONJOB_OPERATION = "com.hybris.cockpitng.widgets.configurableflow.create.solrindexer.cronjob.operation";
	protected static final String PARAM_FINISHED = "finished";
	protected static final String TYPE_CODE = "typeCode";

	private LabelService labelService;
	private CronJobService cronJobService;
	private ModelService modelService;

	protected void startCronJob(final ConfigurableFlowController controller, final SolrIndexerCronJobModel cronJobModel)
	{
		if (cronJobModel != null)
		{
			modelService.save(cronJobModel);

			//workaround for CT-107, specifically CNG-658
			RequestContextHolder.resetRequestAttributes();
			final Component currentContent = controller.getContentDiv().getParent();
			Clients.showBusy(currentContent, Labels.getLabel(LABEL_CRONJOB_RUNNING));
			controller.getWidgetInstanceManager().executeOperation(new WizardCronJobAsyncOperation(cronJobModel),
					getCallbaclEventListener(currentContent, controller), StringUtils.EMPTY);
		}
	}

	protected EventListener<Event> getCallbaclEventListener(final Component component, final ConfigurableFlowController controller)
	{
		return new EventListener<Event>()
		{
			@Override
			public void onEvent(final Event event)
			{
				final SolrIndexerCronJobModel cronJobModel = (SolrIndexerCronJobModel) event.getData();
				Clients.clearBusy(component);
				if (cronJobService.isSuccessful(cronJobModel))
				{
					onWizardFinish(controller, Labels.getLabel(LABEL_CRONJOB_SUCCESS), cronJobService.getLogsAsText(cronJobModel));
				}
				else
				{
					onWizardFinish(controller, Labels.getLabel(LABEL_CRONJOB_ERROR), cronJobService.getLogsAsText(cronJobModel));
				}

			}
		};
	}

	protected void onWizardFinish(final ConfigurableFlowController controller, final String message, final String logs)
	{
		final Component contentHolder = controller.getContentDiv().getFirstChild().getFirstChild();

		final Component breadcrumbContent = controller.getBreadcrumbDiv();

		breadcrumbContent.getChildren().clear();
		contentHolder.getChildren().clear();

		contentHolder.appendChild(new Label(message));

		final Label logLabel = new Label(logs);
		logLabel.setPre(true);
		logLabel.setSclass("failure");
		contentHolder.appendChild(logLabel);

		controller.setValue(PARAM_FINISHED, Boolean.TRUE);
		controller.updateNavigation();
		breadcrumbContent.invalidate();
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	public CronJobService getCronJobService()
	{
		return this.cronJobService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public ModelService getModelService()
	{
		return this.modelService;
	}

	protected class WizardCronJobAsyncOperation implements Operation
	{
		private final CronJobModel cronJobModel;

		protected WizardCronJobAsyncOperation(final CronJobModel cronJobModel)
		{
			this.cronJobModel = cronJobModel;

		}

		@Override
		public Progress.ProgressType getProgressType()
		{
			return Progress.ProgressType.NONE;
		}

		@Override
		public Object execute(final Progress progress)
		{
			cronJobService.performCronJob(cronJobModel, true);
			return cronJobModel;
		}

		@Override
		public String getLabel()
		{
			return Labels.getLabel(LABEL_CRONJOB_OPERATION);
		}

		@Override
		public boolean isTerminable()
		{
			return false;
		}
	}
}
