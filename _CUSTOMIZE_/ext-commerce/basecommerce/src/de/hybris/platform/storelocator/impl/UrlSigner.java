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
package de.hybris.platform.storelocator.impl;

import de.hybris.platform.util.Base64;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class UrlSigner
{

	// Note: Generally, you should store your private key someplace safe
	// and read them into your code

	// This variable stores the binary key, which is computed from the string
	// (Base64) key
	private static byte[] key;

	public UrlSigner(final String keyString) throws IOException // NOSONAR
	{
		// Convert the key from 'web safe' base 64 to binary
		final String convertedKeyString = keyString.replace('-', '+').replace('_', '/');
		key = Base64.decode(convertedKeyString);
	}

	public String signRequest(final String path, final String query) throws GeneralSecurityException
	{
		// Retrieve the proper URL components to sign
		final String resource = path + '?' + query;

		// Get an HMAC-SHA1 signing key from the raw key bytes
		final SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");

		// Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1
		// key
		final Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(sha1Key);

		// compute the binary signature for the request
		final byte[] sigBytes = mac.doFinal(resource.getBytes());

		// base 64 encode the binary signature
		String signature = Base64.encodeBytes(sigBytes);

		// convert the signature to 'web safe' base 64
		signature = signature.replace('+', '-');
		signature = signature.replace('/', '_');

		return resource + "&signature=" + signature;
	}
}
