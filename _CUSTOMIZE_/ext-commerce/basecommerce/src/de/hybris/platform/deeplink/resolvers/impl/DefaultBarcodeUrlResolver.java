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
package de.hybris.platform.deeplink.resolvers.impl;

import de.hybris.platform.deeplink.dao.DeeplinkUrlDao;
import de.hybris.platform.deeplink.pojo.DeeplinkUrlInfo;
import de.hybris.platform.deeplink.resolvers.BarcodeUrlResolver;


/**
 * The Class DefaultBarcodeUrlResolver. Default implementation of {@link BarcodeUrlResolver}
 *
 *
 *
 * @spring.bean barcodeUrlResolver
 *
 */
public class DefaultBarcodeUrlResolver implements BarcodeUrlResolver
{
	public static final String TOKEN_VALUE_SEPARATOR = "-";
	private DeeplinkUrlDao deeplinkUrlDao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.deeplink.resolvers.BarcodeUrlResolver#resolve(java.lang.String)
	 */
	@Override
	public DeeplinkUrlInfo resolve(final String token)
	{
		final String[] splittedTokenValue = token.split(TOKEN_VALUE_SEPARATOR);

		Object contextObject = null;
		if (splittedTokenValue.length == 2)
		{
			contextObject = getDeeplinkUrlDao().findObject(splittedTokenValue[1]);
		}

		final DeeplinkUrlInfo deeplinkUrlInfo = new DeeplinkUrlInfo();
		deeplinkUrlInfo.setContextObject(contextObject);
		deeplinkUrlInfo.setDeeplinkUrl(getDeeplinkUrlDao().findDeeplinkUrlModel(splittedTokenValue[0]));

		return deeplinkUrlInfo;
	}

	/**
	 * Gets the deeplink url dao.
	 *
	 * @return the deeplinkUrlDao
	 */
	public DeeplinkUrlDao getDeeplinkUrlDao()
	{
		return deeplinkUrlDao;
	}

	/**
	 * Sets the deeplink url dao.
	 *
	 * @param deeplinkUrlDao
	 *           the deeplinkUrlDao to set
	 */
	public void setDeeplinkUrlDao(final DeeplinkUrlDao deeplinkUrlDao)
	{
		this.deeplinkUrlDao = deeplinkUrlDao;
	}

}
