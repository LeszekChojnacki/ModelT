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

package de.hybris.platform.ruleengineservices.util.impl;


import com.google.common.collect.ImmutableSet;
import de.hybris.platform.ruleengineservices.util.SharedParametersProvider;

import java.util.Set;


/**
 * Provides all parameters which are shared between rule conditions and actions.
 * Default implementation.
 */
public class DefaultSharedParametersProvider implements SharedParametersProvider
{

    /**
     * @return all parameters which are shared between rule conditions and actions.
     */
    public Set<String> getAll()
    {
        return ImmutableSet.of(CART_THRESHOLD, CART_TOTAL_OPERATOR, IS_DISCOUNTED_PRICE_INCLUDED);
    }
}
