/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.renderers;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Listcell;


/**
 * This renderer renders the currency type for OrderEntry which is obtained by the corresponding order
 */
public class CurrencyTypeRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	private PermissionFacade permissionFacade;

	@Override
	public void render(final Listcell listcell, final ListColumn columnConfiguration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (dataType == null || checkPermission(dataType))
		{
			listcell.setLabel(getCurrencyLabelForOrderEntry((OrderEntryModel) object));
		}
	}

	protected boolean checkPermission(final DataType dataType)
	{
		return getPermissionFacade().canReadProperty(dataType.getCode(), OrderModel._TYPECODE);
	}

	protected String getCurrencyLabelForOrderEntry(final OrderEntryModel orderEntry)
	{
		return orderEntry.getOrder().getCurrency().getIsocode();
	}

	protected PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}
}
