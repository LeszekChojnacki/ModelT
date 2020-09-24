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

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.datahub.dto.event.CompositionActionData;
import com.hybris.datahub.dto.pool.PoolData;

import de.hybris.platform.datahubbackoffice.dataaccess.pool.PoolTypeFacadeStrategy;
import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicPoolActionClient;
import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicRestClient;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

public class ComposeProcessingStrategy extends AbstractProcessingStrategy
{
	private static final String COMPOSE_ID = "compose";
	private static final String COMPOSITION_FINISHED = "datahub.quickupload.finished.composition";
	private static final String COMPOSITION_IN_PROGRESS = "composition.inprogress";

	private DynamicPoolActionClient poolActionClient;

	@Override
	public Map<String, Object> processInternal(final QuickUploadController widgetInstanceManager, final Map<String, Object> ctx)
			throws InterruptedException
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.COMPOSE_POOL_MODEL_PARAMETER);

		CompositionActionData compositionActionData = poolActionClient.initiateCompositionActionAsync(selectedPool.getPoolName());
		final Long actionId = compositionActionData.getActionId();
		compositionActionData = poolActionClient.pollForCompositionCompletion(actionId, selectedPool.getPoolName());
		final Map<String, Object> results = Maps.newHashMap();
		results.put(OPERATION_STATUS, compositionActionData.getStatus());
		return results;
	}

	@Override
	public boolean supports(final String key)
	{
		return StringUtils.equals(COMPOSE_ID, key);
	}

	@Override
	public String getBusyMessage(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.COMPOSE_POOL_MODEL_PARAMETER);
		return controller.getLabel(COMPOSITION_IN_PROGRESS, new String[] { selectedPool.getPoolName() });
	}

	@Override
	public void notifyWhenFinished(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.COMPOSE_POOL_MODEL_PARAMETER);
		final String status = getContextValue(ctx, OPERATION_STATUS);
		notificationService.notifyUser(controller.getWidgetId(), COMPOSITION_FINISHED,
				NotificationEvent.Level.INFO, selectedPool.getPoolName(), status);
	}

	@Override
	public String getAlreadyRunning(final QuickUploadController widgetInstanceManager, final Map<String, Object> ctx)
	{
		return widgetInstanceManager.getLabel(COMPOSITION_IN_PROGRESS);
	}

	@Override
	protected DynamicRestClient restClient()
	{
		return poolActionClient;
	}

	@Override
	public boolean validate(final QuickUploadController controller, final  Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.COMPOSE_POOL_MODEL_PARAMETER);
		final boolean valid = selectedPool != null;
		if (!valid)
		{
			controller.markEditorError(String.format("%s.%s", PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE,
					QuickUploadController.COMPOSE_POOL_MODEL_PARAMETER));
		}
		return valid;
	}

	public void setPoolActionClient(final DynamicPoolActionClient client)
	{
		poolActionClient = client;
	}
}
