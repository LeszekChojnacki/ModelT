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

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.marketplacebackoffice.data.StockLevelForm;
import de.hybris.platform.marketplaceservices.vendor.VendorService;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.servicelayer.model.ModelService;

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
 * Custom handler to saving a stock level.
 */
public class CreateStockLevelWizardHandler implements FlowActionHandler
{

	private VendorService vendorService;
	private CockpitEventQueue cockpitEventQueue;
	private ModelService modelService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final StockLevelForm form = adapter.getWidgetInstanceManager().getModel()
				.getValue("saveStockLevelForm", StockLevelForm.class);
		final StockLevelModel stockLevel = convert(form);
		getModelService().save(stockLevel);

		final DefaultContext internalContext = new DefaultContext();
		internalContext.addAttribute("updatedObjectIsNew", Boolean.TRUE);
		publishEvent("objectUpdated", stockLevel, internalContext);
		adapter.done();
	}

	protected StockLevelModel convert(final StockLevelForm form)
	{
		final StockLevelModel stockLevel = getModelService().create(StockLevelModel.class);
		stockLevel.setAvailable(form.getAvailable());
		stockLevel.setProductCode(form.getProduct().getCode());
		stockLevel.setInStockStatus(InStockStatus.NOTSPECIFIED);
		getVendorService().getVendorByProduct(form.getProduct()).ifPresent(
				vendor -> vendor.getWarehouses().stream().findAny().ifPresent(w -> stockLevel.setWarehouse(w)));
		return stockLevel;
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected VendorService getVendorService()
	{
		return vendorService;
	}

	@Required
	public void setVendorService(final VendorService vendorService)
	{
		this.vendorService = vendorService;
	}

}
