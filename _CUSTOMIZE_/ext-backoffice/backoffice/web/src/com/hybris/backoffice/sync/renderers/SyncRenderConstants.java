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
package com.hybris.backoffice.sync.renderers;

/**
 * Common sclass constants for sync
 */
public class SyncRenderConstants
{
	public static final String YW_IMAGE_ATTRIBUTE_SYNC_STATUS_UNDEFINED = "yw-image-attribute-sync-status-undefined";
	public static final String YW_IMAGE_ATTRIBUTE_SYNC_STATUS_LOADING = "yw-image-attribute-sync-status-loading";
	public static final String YW_IMAGE_ATTRIBUTE_SYNC_STATUS_IN_SYNC = "yw-image-attribute-sync-status-in-sync";
	public static final String YW_IMAGE_ATTRIBUTE_SYNC_STATUS_OUT_OF_SYNC = "yw-image-attribute-sync-status-out-of-sync";
	public static final String YW_IMAGE_ATTRIBUTE_SYNC_STATUS_ERROR = "yw-image-attribute-sync-status-error";
	public static final String LABEL_SYNC_UNDEFINED = "sync.status.undefined.label";
	public static final String LABEL_SYNC_UNDEFINED_TOOLTIP = "sync.status.undefined.tooltip";
	public static final String LABEL_SYNC_ERROR_TOOLTIP = "sync.status.error.tooltip";
	public static final String LABEL_SYNC_ERROR_LABEL = "sync.status.error.label";

	private SyncRenderConstants()
	{
		throw new AssertionError("creating instances of this class is prohibited");
	}
}
