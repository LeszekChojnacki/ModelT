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
package com.hybris.backoffice.labels;

/**
 * This interface allows to obtain labels for objects using transformation object.
 * @param <VALUE>
 *           this generic argument represents the type of value to be transformed.
 * @param <MODIFIER>
 *           this generic argument represents the type of object used to define the transformation of the passed-in
 *           object.
 */
public interface LabelHandler<VALUE, MODIFIER>
{

    /**
     *
     * @param value object to be labelled with the {@param modifier}.
     * @param modifier object used to define how the label should be calculated.
     * @return a string that represents the passed-in object using the given modifier.
     */
	String getLabel(final VALUE value, final MODIFIER modifier);

}
