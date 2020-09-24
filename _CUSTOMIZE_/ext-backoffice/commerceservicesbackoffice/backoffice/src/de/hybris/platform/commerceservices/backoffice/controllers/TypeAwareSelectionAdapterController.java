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
package de.hybris.platform.commerceservices.backoffice.controllers;

import java.util.Collections;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class TypeAwareSelectionAdapterController extends DefaultWidgetController
{

	protected transient TypeFacade typeFacade;

	@SocketEvent(socketId = "object")
	public void adjustForNavigationNode(final Object object)
	{
		if (object != null)
		{
			final String type = typeFacade.getType(object);
			final TypeAwareSelectionContext typeAwareObject = new TypeAwareSelectionContext(type, object,
					Collections.singletonList(object));
			sendOutput("typeAwareObject", typeAwareObject);
		}
	}




}
