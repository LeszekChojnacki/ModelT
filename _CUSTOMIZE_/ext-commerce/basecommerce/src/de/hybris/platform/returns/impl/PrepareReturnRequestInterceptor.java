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

import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Adds a 'generated' code, if there is not a defined one
 * 
 */
public class PrepareReturnRequestInterceptor implements PrepareInterceptor
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PrepareReturnRequestInterceptor.class.getName());
	private KeyGenerator keyGenerator;

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof ReturnRequestModel)
		{
			final ReturnRequestModel authModel = (ReturnRequestModel) model;

			if (authModel.getCode() == null)
			{
				authModel.setCode((String) keyGenerator.generate());
			}
		}
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}