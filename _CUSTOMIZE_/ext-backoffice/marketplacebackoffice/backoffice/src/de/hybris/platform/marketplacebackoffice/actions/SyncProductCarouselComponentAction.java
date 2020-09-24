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
package de.hybris.platform.marketplacebackoffice.actions;

import de.hybris.platform.catalog.enums.SyncItemStatus;
import de.hybris.platform.cms2lib.model.components.ProductCarouselComponentModel;
import de.hybris.platform.marketplaceservices.vendor.VendorCMSService;

import java.util.EnumSet;

import javax.annotation.Resource;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;


/**
 * An action for synchronize a carousel component.
 */
public class SyncProductCarouselComponentAction implements CockpitAction<ProductCarouselComponentModel, Object>
{

	private static final String CONFIRMATION_KEY = "productcarousel.sync.confirm";
	private static final String SYNC_PRODUCT_CAROUSEL_EVENT_TYPE = "SyncProductCarousel";

	@Resource
	private VendorCMSService vendorCmsService;

	@Resource
	private NotificationService notificationService;

	@Override
	public ActionResult<Object> perform(final ActionContext<ProductCarouselComponentModel> ctx)
	{
		final ProductCarouselComponentModel carousel = ctx.getData();
		getVendorCmsService().performProductCarouselSynchronization(carousel, true);

		notificationService.notifyUser(notificationService.getWidgetNotificationSource(ctx), SYNC_PRODUCT_CAROUSEL_EVENT_TYPE,
				NotificationEvent.Level.SUCCESS, carousel);

		final ActionResult<Object> actionResult = new ActionResult<>(ActionResult.SUCCESS);
		actionResult.setStatusFlags(EnumSet.of(StatusFlag.OBJECT_MODIFIED));

		return actionResult;
	}

	@Override
	public boolean canPerform(final ActionContext<ProductCarouselComponentModel> ctx)
	{
		final ProductCarouselComponentModel carousel = ctx.getData();
		return carousel != null
				&& getVendorCmsService().getProductCarouselSynchronizationStatus(carousel) == SyncItemStatus.NOT_SYNC;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<ProductCarouselComponentModel> ctx)
	{
		return true;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<ProductCarouselComponentModel> ctx)
	{
		return ctx.getLabel(CONFIRMATION_KEY);
	}

	protected VendorCMSService getVendorCmsService()
	{
		return vendorCmsService;
	}

}
