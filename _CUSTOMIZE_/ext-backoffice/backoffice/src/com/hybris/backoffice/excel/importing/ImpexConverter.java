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
package com.hybris.backoffice.excel.importing;

import com.hybris.backoffice.excel.data.Impex;


/**
 * Service responsible for generating impex script based on {@link Impex} object.
 */
public interface ImpexConverter
{

	/**
	 * Generates impex script which contains impex header (for example INSERT_UPDATE
	 * Product;code[unique=true];name[lang=en];) and multi-lines which represent impex's data. Lines which does not
	 * contain all unique attributes are omitted.
	 *
	 * @param impex
	 *           {@link }
	 * @return converted impex script. Lines which does not contain all unique attributes are omitted.
	 */
	String convert(final Impex impex);

}
