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

import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


public class AssignVendorUserUserGroupWizardHandler implements FlowActionHandler
{
	private static final String VENDOR_ADMIN_GROUP = "vendoradministratorgroup";
	private UserService userService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> args)
	{
		final VendorUserModel vendorUser = adapter.getWidgetInstanceManager().getModel().getValue("newVendorUser",
				VendorUserModel.class);

		vendorUser.setGroups(Collections.singleton(getUserService().getUserGroupForUID(VENDOR_ADMIN_GROUP)));

		adapter.custom();
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

}
