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

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;

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


/**
 *
 */
public class CreateVendorProductWizardHandler implements FlowActionHandler
{
	private CockpitEventQueue cockpitEventQueue;
	private SessionService sessionService;
	private ModelService modelService;


	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final ProductModel product = adapter.getWidgetInstanceManager().getModel().getValue("newProduct", ProductModel.class);
		final UserModel currentUser = sessionService.getAttribute("user");
		if (currentUser instanceof VendorUserModel)
		{
			setProductAttributeForVendor(product, ((VendorUserModel) currentUser).getVendor());
		}
		getModelService().save(product);
		final DefaultContext internalContext = new DefaultContext();
		internalContext.addAttribute("updatedObjectIsNew", Boolean.TRUE);
		this.publishEvent("objectUpdated", product, internalContext);
		adapter.done();
	}

	protected void setProductAttributeForVendor(final ProductModel product, final VendorModel vendor)
	{
		product.setApprovalStatus(ArticleApprovalStatus.EDITING);
		product.setCatalogVersion(vendor.getCatalog().getActiveCatalogVersion());
		product.setCode(vendor.getCode() + "_" + product.getVendorSku());
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

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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

}
