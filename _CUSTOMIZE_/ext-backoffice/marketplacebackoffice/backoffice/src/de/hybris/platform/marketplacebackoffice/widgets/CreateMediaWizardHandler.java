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
package de.hybris.platform.marketplacebackoffice.widgets;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;


/**
 * Custom handler to handle saving a vendor.
 */
public class CreateMediaWizardHandler implements FlowActionHandler
{
	private CockpitEventQueue cockpitEventQueue;
	private UserService userService;
	private ModelService modelService;
	private MediaService mediaService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> args)
	{
		final MediaModel media = adapter.getWidgetInstanceManager().getModel()
				.getValue("newMedia", MediaModel.class);
		bindCatalogVersion(media);
		bindMediaFolder(media);
		getModelService().save(media);
		final DefaultContext internalContext = new DefaultContext();
		internalContext.addAttribute("updatedObjectIsNew", Boolean.TRUE);
		this.publishEvent("objectUpdated", media, internalContext);
		adapter.done();
	}

	protected void bindCatalogVersion(final MediaModel media)
	{
		final VendorUserModel currentUser = (VendorUserModel) userService.getCurrentUser();
		media.setCatalogVersion(currentUser.getVendor().getCatalog().getActiveCatalogVersion());
	}
	
	protected void bindMediaFolder(final MediaModel media)
	{
		media.setFolder(getMediaService().getFolder("images"));
	}

	protected void publishEvent(final String eventName, final Object object, final Context ctx)
	{
		if (!this.isCockpitEventNotificationDisabledInCtx(ctx))
		{
			final DefaultCockpitEvent event = new DefaultCockpitEvent(eventName, object, (Object) null);
			this.populateEventContext(ctx, event);
			this.cockpitEventQueue.publishEvent(event);
		}
	}

	protected void populateEventContext(final Context source, final DefaultCockpitEvent destination)
	{
		if (source != null)
		{
			source.getAttributeNames().stream().forEach(a -> destination.getContext().put(a, source.getAttribute(a)));
		}

	}

	protected boolean isCockpitEventNotificationDisabledInCtx(final Context ctx)
	{
		return ctx != null && BooleanUtils.isTrue((Boolean) ctx.getAttribute("ctxDisableCRUDCockpitEventNotification"));
	}

	protected CockpitEventQueue getCockpitEventQueue()
	{
		return this.cockpitEventQueue;
	}

	@Required
	public void setCockpitEventQueue(final CockpitEventQueue cockpitEventQueue)
	{
		this.cockpitEventQueue = cockpitEventQueue;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected MediaService getMediaService() {
		return mediaService;
	}
	
	@Required
	public void setMediaService(final MediaService mediaService) {
		this.mediaService = mediaService;
	}

}
