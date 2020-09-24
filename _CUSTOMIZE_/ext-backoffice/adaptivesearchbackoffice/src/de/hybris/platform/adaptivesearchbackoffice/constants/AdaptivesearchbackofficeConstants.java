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
package de.hybris.platform.adaptivesearchbackoffice.constants;


public final class AdaptivesearchbackofficeConstants
{
	public static final String EXTENSIONNAME = "adaptivesearchbackoffice";

	public static final String NAVIGATION_CONTEXT_SOCKET = "navigationContext";
	public static final String SEARCH_CONTEXT_SOCKET = "searchContext";
	public static final String SEARCH_REQUEST_SOCKET = "searchRequest";
	public static final String SEARCH_RESULT_SOCKET = "searchResult";
	public static final String REFRESH_SOCKET = "refresh";

	public static final String SEARCH_PROFILE_SOCKET = "searchProfile";
	public static final String SEARCH_CONFIGURATION_SOCKET = "searchConfiguration";

	public static final String ACTION_WIDGET_INSTANCE_MANAGER_KEY = "widgetInstanceManager";
	public static final String EDITOR_WIDGET_INSTANCE_MANAGER_KEY = "wim";

	public static final String CURRENT_OBJECT_KEY = "currentObject";
	public static final String PARENT_OBJECT_KEY = "parentObject";
	public static final String VALUE_CHANGED_KEY = "valueChanged";

	public static final String VALID_SCLASS = "yas-valid";
	public static final String INVALID_SCLASS = "yas-invalid";
	public static final String FROM_SEARCH_PROFILE_SCLASS = "yas-from-search-profile";
	public static final String FROM_SEARCH_CONFIGURATION_SCLASS = "yas-from-search-configuration";
	public static final String OVERRIDE_SCLASS = "yas-override";
	public static final String OVERRIDE_FROM_SEARCH_PROFILE_SCLASS = "yas-override-from-search-profile";

	public static final String ZK_LISTBOX_RIGHT_SELECT_ATTRIBUTE = "org.zkoss.zul.listbox.rightSelect";
	public static final String ZK_LISTBOX_ROD_ATTRIBUTE = "org.zkoss.zul.listbox.rod";
	public static final String ZK_LISTBOX_INIT_ROD_SIZE_ATTRIBUTE = "org.zkoss.zul.listbox.initRodSize";
	public static final String ZK_LISTBOX_PRELOAD_SIZE_ATTRIBUTE = "org.zkoss.zul.listbox.preloadSize";

	private AdaptivesearchbackofficeConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension
}
