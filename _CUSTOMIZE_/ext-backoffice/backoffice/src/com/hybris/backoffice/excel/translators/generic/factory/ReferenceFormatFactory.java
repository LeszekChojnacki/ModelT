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

import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Creates reference format based on required attributes hierarchy
 */
public interface ReferenceFormatFactory
{

    /**
     * Creates reference format based on required attributes hierarchy
     * @param requiredAttribute
     * @return
     */
	String create(RequiredAttribute requiredAttribute);
}
