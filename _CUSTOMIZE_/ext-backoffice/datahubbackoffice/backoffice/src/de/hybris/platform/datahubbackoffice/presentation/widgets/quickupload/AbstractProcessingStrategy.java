/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.quickupload;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;

import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicRestClient;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.core.async.Operation;
import com.hybris.cockpitng.core.async.Progress;

public abstract class AbstractProcessingStrategy implements ProcessingStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessingStrategy.class);

	protected static final String OPERATION_STATUS = "operationStatus";
	protected static final String EXCEPTION_KEY = "EXCEPTION";

	protected NotificationService notificationService;

	@Override
	public void process(final QuickUploadController controller, final String id, final Map<String, Object> ctx)
	{
		if (!validate(controller, ctx))
		{
			return;
		}
		final Operation operation = new MyOperation(controller, ctx) ;

		final EventListener<Event> callbackEvent = event -> {
			final Map<String, Object> results = (Map<String, Object>) event.getData();
			if( MapUtils.isNotEmpty(results) && results.containsKey(EXCEPTION_KEY) )
			{
				notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
				notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, "datahub.error.connecting.to.server",
						NotificationEvent.Level.FAILURE, results.get(EXCEPTION_KEY));
				return;
			}
			String operationStatus = StringUtils.EMPTY;
			if (MapUtils.isNotEmpty(results) && results.containsKey(OPERATION_STATUS))
			{
				operationStatus = ObjectUtils.toString(results.get(OPERATION_STATUS));
			}
			ctx.put(OPERATION_STATUS, operationStatus);
			notifyWhenFinished(controller, ctx);
		};

		controller.getWidgetInstanceManager().executeOperationInParallel(operation, callbackEvent);
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	protected DataHubServer getSelectedDataHubServer(final Map<String, Object> ctx)
	{
		return getContextValue(ctx, QuickUploadController.DATAHUB_INFO_MODEL_PARAMETER);
	}

	public abstract boolean validate(final QuickUploadController controller, final Map<String, Object> ctx);

	public abstract Map<String, Object> processInternal(final QuickUploadController controller, Map<String, Object> ctx)
			throws InterruptedException;

	public abstract String getBusyMessage(final QuickUploadController controller, Map<String, Object> ctx);

	public abstract String getAlreadyRunning(final QuickUploadController controller, Map<String, Object> ctx);

	/**
	 * Retrieves REST client used by the process. It must be dynamic.
	 *
	 * @return REST client used by the concrete process implementation.
	 */
	protected abstract DynamicRestClient restClient();

	public abstract void notifyWhenFinished(final QuickUploadController controller, Map<String, Object> ctx);

	protected <T> T getContextValue(final Map<String, Object> ctx, final String key)
	{
		return (T) ctx.get(key);
	}

	private class MyOperation implements Operation
	{
		private final QuickUploadController controller;
		private final Map<String, Object> ctx;

		MyOperation(final QuickUploadController controller, final Map<String, Object> ctx)
		{
			this.controller = controller;
			this.ctx = ctx;
		}

		@Override
		public Progress.ProgressType getProgressType()
		{
			return Progress.ProgressType.FAKED;
		}

		@Override
		public Object execute(final Progress progress)
		{
			try
			{
				restClient().useServer(getSelectedDataHubServer(ctx));
				final Object ret = processInternal(controller, ctx);
				restClient().useContextServer();
				return ret;
			}
			catch (final InterruptedException e)
			{
				LOG.error("Current thread was interrupted!", e);
				return null;
			}
			catch(final Exception e)
			{
				LOG.error(e.getMessage(), e);
				final Map<String, Object> map = new HashMap<>();
				map.put(EXCEPTION_KEY, e);
				return map;
			}
		}

		@Override
		public String getLabel()
		{
			return getBusyMessage(controller, ctx);
		}

		@Override
		public boolean isTerminable()
		{
			return false;
		}
	}
}
