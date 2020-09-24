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

import de.hybris.platform.datahubbackoffice.dataaccess.pool.PoolTypeFacadeStrategy;
import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicPublicationClient;
import de.hybris.platform.datahubbackoffice.datahub.rest.DynamicRestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.datahub.dto.event.PublicationActionData;
import com.hybris.datahub.dto.metadata.TargetSystemData;
import com.hybris.datahub.dto.pool.PoolData;

public class PublishProcessingStrategy extends AbstractProcessingStrategy
{
	private static final String PUBLICATION_FINISHED = "datahub.quickupload.finished.publication";
	private static final String PUBLICATION_IN_PROGRESS = "publication.inprogress";

	private DynamicPublicationClient publicationClient;

	@Override
	public Map<String, Object> processInternal(final QuickUploadController widgetInstanceManager, final Map<String, Object> ctx)
			throws InterruptedException
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
		final Set<TargetSystemData> selectedTargetSystems = getContextValue(ctx,
				QuickUploadController.TARGET_SYSTEMS_MODEL_PARAMETER);

		PublicationActionData publicationActionData = publicationClient
				.triggerExportToCoreSystem(extractTargetSystemNames(selectedTargetSystems), selectedPool.getPoolName());
		publicationActionData = publicationClient.pollForPublicationCompletion(publicationActionData.getActionId(),
				publicationActionData.getPoolName());
		final Map<String, Object> results = Maps.newHashMap();
		results.put(OPERATION_STATUS, publicationActionData.getStatus());
		return results;
	}

	private static List<String> extractTargetSystemNames(final Set<TargetSystemData> targetSystemsObjects)
	{
		final List<String> ret = Lists.newArrayList();
		for (final TargetSystemData system : targetSystemsObjects)
		{
			ret.add(system.getTargetSystemName());
		}
		return ret;
	}

	@Override
	public boolean supports(final String key)
	{
		final String PUBLISH_ID = "publish";
		return StringUtils.equals(PUBLISH_ID, key);
	}

	@Override
	public boolean validate(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		boolean valid = true;
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
		final Set<TargetSystemData> selectedTargetSystems = getContextValue(ctx,
				QuickUploadController.TARGET_SYSTEMS_MODEL_PARAMETER);

		String editorId = StringUtils.EMPTY;
		if (selectedPool == null)
		{
			editorId = String.format("%s.%s", PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE,
					QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
			valid = false;

		}
		else if (CollectionUtils.isEmpty(selectedTargetSystems))
		{
			editorId = String.format("%s.%s", QuickUploadController.DATAHUB_TARGET_SYSTEM_TYPECODE,
					QuickUploadController.TARGET_SYSTEMS_MODEL_PARAMETER);
			valid = false;
		}

		if (!valid)
		{
			controller.markEditorError(editorId);
		}
		return valid;
	}

	@Override
	public String getBusyMessage(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
		return controller.getLabel(PUBLICATION_IN_PROGRESS, new String[] {selectedPool.getPoolName()});
	}

	@Override
	public void notifyWhenFinished(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
		final String status = getContextValue(ctx, OPERATION_STATUS);
		notificationService.notifyUser(controller.getWidgetId(), PUBLICATION_FINISHED,
				NotificationEvent.Level.INFO, selectedPool.getPoolName(), status);
	}

	@Override
	public String getAlreadyRunning(final QuickUploadController controller, final Map<String, Object> ctx)
	{
		final PoolData selectedPool = getContextValue(ctx, QuickUploadController.PUBLISH_POOL_MODEL_PARAMETER);
		return controller.getLabel(PUBLICATION_IN_PROGRESS, new String[] {selectedPool.getPoolName()});
	}

	@Override
	protected DynamicRestClient restClient()
	{
		return publicationClient;
	}

	public void setPublicationClient(final DynamicPublicationClient client)
	{
		publicationClient = client;
	}
}
