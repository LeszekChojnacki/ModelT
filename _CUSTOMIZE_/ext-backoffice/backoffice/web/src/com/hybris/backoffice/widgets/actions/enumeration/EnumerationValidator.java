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
package com.hybris.backoffice.widgets.actions.enumeration;

import java.util.Collection;

import com.hybris.cockpitng.actions.ActionContext;


/**
 * EnumerationValidator allows to decide whether the given data can be updated
 */
public interface EnumerationValidator
{

	/**
	 * Allows to decide whether the given data can be updated
	 */
	boolean validate(final ActionContext<Collection<Object>> context);

}
