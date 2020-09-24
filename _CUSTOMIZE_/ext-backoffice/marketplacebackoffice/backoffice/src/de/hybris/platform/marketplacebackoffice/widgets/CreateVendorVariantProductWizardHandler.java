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

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.util.notifications.NotificationService;
import com.hybris.cockpitng.util.notifications.event.NotificationEvent;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Custom handler to handle saving a variant product.
 */
public class CreateVendorVariantProductWizardHandler extends CreateVendorProductWizardHandler
{
	private static final String CREATE_VENDOR_VARIANT_PRODUCT_EVENT_TYPE = "CreateVendorVariantProduct";

	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final VariantProductModel product = adapter.getWidgetInstanceManager().getModel().getValue("newProduct",
				VariantProductModel.class);
		final UserModel currentUser = getSessionService().getAttribute("user");
		if (currentUser instanceof VendorUserModel)
		{
			setProductAttributeForVendor(product, ((VendorUserModel) currentUser).getVendor());
		}
		try
		{
			getModelService().save(product);
			final DefaultContext internalContext = new DefaultContext();
			internalContext.addAttribute("updatedObjectIsNew", Boolean.TRUE);
			publishEvent("objectUpdated", product, internalContext);
			adapter.done();
		}
		catch (final ModelSavingException e)//NOSONAR
		{
			getNotificationService().notifyUser(
					getNotificationService().getWidgetNotificationSource(adapter.getWidgetInstanceManager()),
					CREATE_VENDOR_VARIANT_PRODUCT_EVENT_TYPE,
					NotificationEvent.Level.FAILURE, e.getMessage());
		}
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
