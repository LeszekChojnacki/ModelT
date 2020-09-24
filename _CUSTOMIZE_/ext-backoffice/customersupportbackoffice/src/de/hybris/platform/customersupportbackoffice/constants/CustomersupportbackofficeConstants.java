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
package de.hybris.platform.customersupportbackoffice.constants;

/**
 * Global class for all Ybackoffice constants. You can add global constants for your extension into this class.
 */
public final class CustomersupportbackofficeConstants
{
	public static final String EXTENSIONNAME = "customersupportbackoffice";

	// implement here constants used by this extension

	public static final String SESSION_CONEXT_ID = "sessionContext";

	public static final String ITEM_TO_SHOW_SOCKET = "itemToShow";

	public static final String SELECTED_ITEM_SOCKET = "selectedItem";
	public static final String CREATED_ITEM_SOCKET = "itemCreated";

	public static final String SEARCH_VIEW_TYPE_SOCKET = "viewData";

	public static final String SESSION_CONTEXT_UID_SESSION_ATTR = "sessionContextUID";

	public static final String SIMPLE_SELECT_OUT_SOCKET_ID = "itemSelected";
	public static final String SIMPLE_SELECT_PARAM = "isSimpleSelectEnabled";
	public static final String ASM_PREFIX_PARAM = "showASMPrefix";
	public static final String ASM_FORWARD_URL_PARAM = "forwardURL";

	public static final String REPLY_TYPE = "replyTo";

	public static final String NOTIFICATION_TYPE = "JustMessage";

	
	private CustomersupportbackofficeConstants()
	{
		//empty to avoid instantiating this constant class
	}
}
