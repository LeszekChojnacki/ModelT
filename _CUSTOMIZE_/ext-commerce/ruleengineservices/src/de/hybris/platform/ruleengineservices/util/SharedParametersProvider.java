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

package de.hybris.platform.ruleengineservices.util;


import java.util.Set;


/**
 * Provides all parameters which are shared between rule conditions and actions.
 */
public interface SharedParametersProvider
{

    String CART_THRESHOLD = "cart_threshold";
    String CART_TOTAL_OPERATOR = "cart_total_operator";
    String IS_DISCOUNTED_PRICE_INCLUDED = "is_discounted_price_included";

    /**
     * @return all parameters which are shared between rule conditions and actions.
     */
    Set<String> getAll();
}
