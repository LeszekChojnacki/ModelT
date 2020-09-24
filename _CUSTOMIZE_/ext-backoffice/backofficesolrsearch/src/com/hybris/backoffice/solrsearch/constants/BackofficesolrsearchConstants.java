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
package com.hybris.backoffice.solrsearch.constants;

import de.hybris.platform.core.model.type.ComposedTypeModel;


/**
 * Global class for all Ybackoffice constants. You can add global constants for your extension into this class.
 */
public final class BackofficesolrsearchConstants extends GeneratedBackofficesolrsearchConstants
{
	/**
	 * @deprecated since 1808, use {@link ComposedTypeModel#ITEMTYPE} instead
	 */
	@Deprecated
	public static final String TYPE_CODE_FIELD = ComposedTypeModel.ITEMTYPE;
	public static final String EXTENSIONNAME = "backofficesolrsearch";
	public static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String CATALOG_VERSION_PK = "catalogVersionPk";

	private BackofficesolrsearchConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension
}
