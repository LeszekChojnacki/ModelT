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

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Strategy for preparing impex file based on required attributes hierarchy
 */
public interface ImportImpexFactoryStrategy
{

	/**
	 * Indicates whether strategy can handle given attribute
	 * 
	 * @param rootUniqueAttribute
	 * @param importParameters
	 * @return
	 */
	boolean canHandle(RequiredAttribute rootUniqueAttribute, ImportParameters importParameters);

	/**
	 * Creates impex object based on required attributes hierarchy
	 * 
	 * @param rootUniqueAttribute
	 * @param importParameters
	 * @return
	 */
	Impex create(RequiredAttribute rootUniqueAttribute, ImportParameters importParameters);
}
