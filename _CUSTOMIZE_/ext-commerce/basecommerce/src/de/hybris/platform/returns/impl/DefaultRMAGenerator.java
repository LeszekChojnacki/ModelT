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
package de.hybris.platform.returns.impl;

import de.hybris.platform.returns.RMAGenerator;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import org.springframework.beans.factory.annotation.Required;


public class DefaultRMAGenerator implements RMAGenerator
{
	private KeyGenerator keyGenerator;

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}

	@Override
	public String generateRMA(final ReturnRequestModel request)
	{
		return keyGenerator.generate().toString();
	}
}
