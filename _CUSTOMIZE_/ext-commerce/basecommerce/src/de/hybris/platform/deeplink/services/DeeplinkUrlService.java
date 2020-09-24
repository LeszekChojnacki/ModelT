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
package de.hybris.platform.deeplink.services;

import de.hybris.platform.deeplink.model.rules.DeeplinkUrlModel;

import org.apache.velocity.VelocityContext;


/**
 * The Interface DeeplinkUrlService. Service responsible for generating URLs for use with barcodes.
 * 
 *
 * 
 * @spring.bean deeplinkUrlService
 * 
 */
public interface DeeplinkUrlService
{

	/**
	 * The Class LongUrlInfo container for generated URL and forward or redirect info
	 */
	class LongUrlInfo
	{
		private String url;
		private boolean useForward;

		public LongUrlInfo(final String url, final boolean useForward)
		{
			this.url = url;
			this.useForward = useForward;
		}

		/**
		 * @return the url
		 */
		public String getUrl()
		{
			return url;
		}

		/**
		 * @param url
		 *           the url to set
		 */
		public void setUrl(final String url)
		{
			this.url = url;
		}

		/**
		 * @return the useForward
		 */
		public boolean isUseForward()
		{
			return useForward;
		}

		/**
		 * @param useForward
		 *           the useForward to set
		 */
		public void setUseForward(final boolean useForward)
		{
			this.useForward = useForward;
		}

	}

	/**
	 * Generate long url.
	 * 
	 * @param barcodeToken
	 *           the barcode token
	 * @return the LongUrlInfo object
	 */
	LongUrlInfo generateUrl(String barcodeToken);

	/**
	 * Generate short url.
	 * 
	 * @param deeplinkUrlModel
	 *           the deeplink url model
	 * @param contextObject
	 *           the context object
	 * @return the string
	 */
	String generateShortUrl(DeeplinkUrlModel deeplinkUrlModel, Object contextObject);

	/**
	 * Parses the template.
	 * 
	 * @param template
	 *           the template
	 * @param context
	 *           the context
	 * 
	 * @return the string
	 */
	String parseTemplate(String template, VelocityContext context);

}
