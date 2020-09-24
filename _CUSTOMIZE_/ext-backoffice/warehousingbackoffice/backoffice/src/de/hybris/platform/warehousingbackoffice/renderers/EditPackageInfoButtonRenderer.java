/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.renderers;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.warehousing.model.PackagingInfoModel;

import java.util.Collections;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;


/**
 * Renders the edit button for PackagingInfo
 */
public class EditPackageInfoButtonRenderer implements WidgetComponentRenderer<Listcell, ListColumn, Object>
{
	protected static final String CONSIGNMENT_PACKAGINGINFO = "PackagingInfo";

	private PermissionFacade permissionFacade;

	public static final String EDIT_BUTTON = "edititem";
	public static final String DISABLED = "disabled";

	@Override
	public void render(final Listcell listcell, final ListColumn columnConfiguration, final Object object, DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Button button = new Button();
		String buttonClass = EDIT_BUTTON;

		button.setParent(listcell);

		if (!checkEditability((PackagingInfoModel) object))
		{
			button.setDisabled(true);
			buttonClass = buttonClass + " " + DISABLED;
		}
		button.setSclass(buttonClass);

		final TypeAwareSelectionContext typeAwareSelectionContext = new TypeAwareSelectionContext(CONSIGNMENT_PACKAGINGINFO, object,
				Collections.singletonList(object));
		button.addEventListener(Events.ON_CLICK,
				event -> widgetInstanceManager.sendOutput("referenceSelected", typeAwareSelectionContext));
	}

	/**
	 * Checks if the {@link PackagingInfoModel} should be editable.
	 *
	 * @param packageInfo
	 * @return
	 * 		true if the consignment has not been already shipped or picked up and that no shipping label was generated.
	 */
	protected boolean checkEditability(final PackagingInfoModel packageInfo)
	{
		return !(ConsignmentStatus.PICKUP_COMPLETE.equals(packageInfo.getConsignment().getStatus()) || ConsignmentStatus.SHIPPED
				.equals(packageInfo.getConsignment().getStatus()) || packageInfo.getConsignment().getShippingLabel() != null);
	}

	/**
	 * 
	 * @param dataType
	 * @param object
	 * @return true if the user is allowed to edit this attribute.
	 */
	protected boolean checkPermission(final DataType dataType, final Object object)
	{
		return getPermissionFacade().canReadProperty(dataType.getCode(), ((OrderEntryModel) object).getOrder().getItemtype());
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
