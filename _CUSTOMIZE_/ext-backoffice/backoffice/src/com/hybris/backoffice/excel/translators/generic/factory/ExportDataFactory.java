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
package com.hybris.backoffice.excel.translators.generic.factory;

import java.util.Optional;

import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Exports value based on required attributes hierarchy
 */
public interface ExportDataFactory
{

	/**
	 * Creates exported object based on required attributes hierarchy
	 * 
	 * @param rootUniqueAttribute
	 * @param objectToExport
	 * @return
	 */
	Optional<String> create(RequiredAttribute rootUniqueAttribute, Object objectToExport);
}
