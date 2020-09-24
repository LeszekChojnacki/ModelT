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

import de.hybris.platform.marketplaceservices.vendor.VendorService;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;
import de.hybris.platform.validation.services.ValidationService;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.events.CockpitEventQueue;
import com.hybris.cockpitng.core.events.impl.DefaultCockpitEvent;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Custom handler to handle saving a vendor.
 */
public class CreateVendorWizardHandler implements FlowActionHandler
{

	private CockpitEventQueue cockpitEventQueue;
	private ModelService modelService;
	private VendorService vendorService;
	private ValidationService validationService;
	private FlexibleSearchService flexibleSearchService;


	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> args)
	{
		final WidgetModel model = adapter.getWidgetInstanceManager().getModel();
		final VendorModel vendor = model.getValue("newVendor", VendorModel.class);

		final ConstraintGroupModel constraintGroup = getBackofficeConstraintGroup();

		if (constraintGroup != null && getValidationService().validate(vendor, Collections.singleton(constraintGroup)).isEmpty())
		{
			final Boolean useCustomPage = model.getValue("useCustomPageOnVendorCreate", Boolean.class);
			getVendorService().createVendor(vendor, useCustomPage == null ? false : useCustomPage.booleanValue());

			final DefaultContext internalContext = new DefaultContext();
			internalContext.addAttribute("updatedObjectIsNew", Boolean.TRUE);
			this.publishEvent("objectUpdated", vendor, internalContext);
			adapter.cancel();
		}
		else
		{
			adapter.done();
		}
	}

	protected ConstraintGroupModel getBackofficeConstraintGroup()
	{
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery("SELECT {PK} FROM {ConstraintGroup} WHERE {id}=?id");
		fsq.addQueryParameter("id", "defaultBackofficeValidationGroup");
		final SearchResult<ConstraintGroupModel> result = getFlexibleSearchService().search(fsq);
		if (result.getTotalCount() == 1)
		{
			final ConstraintGroupModel constraintGroup = result.getResult().get(0);
			getModelService().refresh(constraintGroup);
			getModelService().detach(constraintGroup);
			return constraintGroup;
		}

		return null;
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


	protected VendorService getVendorService()
	{
		return vendorService;
	}

	@Required
	public void setVendorService(final VendorService vendorService)
	{
		this.vendorService = vendorService;
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

	protected ValidationService getValidationService()
	{
		return validationService;
	}

	@Required
	public void setValidationService(final ValidationService validationService)
	{
		this.validationService = validationService;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}


}
