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
package de.hybris.platform.deeplink.resolvers;

import de.hybris.platform.deeplink.pojo.DeeplinkUrlInfo;


/**
 * The Interface BarcodeUrlResolver responsible for resolving barcode string tokens.
 * 
 *
 * 
 * @spring.bean barcodeUrlResolver
 * 
 */
public interface BarcodeUrlResolver
{

	/**
	 * Resolves barcode token and finds proper items based on token values.
	 * 
	 * @param token
	 *           the token containing necessery info to fill ResolvedToken object with values
	 * @return the DeeplinkUrlInfo POJO
	 */
	DeeplinkUrlInfo resolve(String token);
}
