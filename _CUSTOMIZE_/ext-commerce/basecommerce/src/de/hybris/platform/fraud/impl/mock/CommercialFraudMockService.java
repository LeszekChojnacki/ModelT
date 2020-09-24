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
package de.hybris.platform.fraud.impl.mock;


import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.fraud.constants.FrauddetectionConstants;
import de.hybris.platform.fraud.impl.AbstractFraudServiceProvider;
import de.hybris.platform.fraud.impl.FraudServiceResponse;
import de.hybris.platform.fraud.impl.FraudSymptom;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Assert;


/**
 * Mock implementation of external fraud detection provider.
 */
@SuppressWarnings("PMD")
public class CommercialFraudMockService extends AbstractFraudServiceProvider
{

	//mockup 3rd party knowledge base
	private static final Set<String> bannedDomains;
	private static final Set<String> bannedIPs;
	private static final Set<String> stolenCards;
	private static final Map<String, String> fakeGeolocation;

	private static final int DEFAULT_INCREMENT = 100;

	static
	{
		//initialize the service knowledge base
		bannedDomains = new HashSet<>();
		bannedDomains.add("foo.pl");
		bannedDomains.add("foo.de");
		bannedDomains.add("foo.com");

		bannedIPs = new HashSet<>();
		bannedIPs.add("212.60.65.173"); // NOSONAR
		bannedIPs.add("196.46.71.251"); // NOSONAR
		bannedIPs.add("202.105.37.196"); // NOSONAR
		bannedIPs.add("41.218.203.86"); // NOSONAR
		bannedIPs.add("81.91.228.100"); // NOSONAR
		bannedIPs.add("74.125.16.3"); // NOSONAR
		bannedIPs.add("41.191.68.197"); // NOSONAR
		bannedIPs.add("41.189.4.251"); // NOSONAR
		bannedIPs.add("41.202.76.10"); // NOSONAR
		bannedIPs.add("41.207.214.7"); // NOSONAR
		bannedIPs.add("196.207.228.102");// NOSONAR
		bannedIPs.add("41.215.160.133"); // NOSONAR
		bannedIPs.add("41.189.35.234"); // NOSONAR
		bannedIPs.add("217.117.5.118"); // NOSONAR
		bannedIPs.add("41.207.212.232"); // NOSONAR
		bannedIPs.add("68.68.107.24"); // NOSONAR
		bannedIPs.add("112.110.109.247");// NOSONAR
		bannedIPs.add("41.216.40.59"); // NOSONAR
		bannedIPs.add("41.207.25.103"); // NOSONAR
		bannedIPs.add("41.207.162.2"); // NOSONAR

		stolenCards = new HashSet<>();
		stolenCards.add("0000-0000-00000");
		stolenCards.add("1111-1111-11111");

		fakeGeolocation = new HashMap<>();
		fakeGeolocation.put("USA", "83.13.130.42"); // NOSONAR
		fakeGeolocation.put("GER", "83.13.130.42"); // NOSONAR
	}

	/**
	 * whether the given email is from a free email service
	 *
	 * @param email
	 *           the email to check
	 * @return the score (0 if no free email, otherwise {@link #DEFAULT_INCREMENT})
	 */
	public int isFreeEmailService(final String email)
	{
		if (null == email)
		{
			throw new JaloInvalidParameterException("Email must not be null", 0);
		}
		return email.toLowerCase(Utilities.getDefaultLocale()).matches(".*free.*") ? DEFAULT_INCREMENT : 0;
	}

	/**
	 * whether the given domain is banned
	 *
	 * @param domain
	 *           the domain to check
	 * @return the score (0 if not banned, otherwise {@link #DEFAULT_INCREMENT})
	 */
	public int isBannedDomain(final String domain)
	{
		if (null == domain)
		{
			throw new JaloInvalidParameterException("Domain must not be null", 0);
		}
		return bannedDomains.contains(domain) ? DEFAULT_INCREMENT : 0;
	}

	/**
	 * whether the given ip address is banned
	 *
	 * @param ipAddress
	 *           the ip address to check
	 * @return the score (0 if not banned, otherwise {@link #DEFAULT_INCREMENT})
	 */
	public int isBannedIP(final String ipAddress)
	{
		if (null == ipAddress)
		{
			throw new JaloInvalidParameterException("IP address must not be null", 0);
		}
		return bannedIPs.contains(ipAddress) ? DEFAULT_INCREMENT : 0;
	}

	/**
	 * whether the given hashed credit card number is fraudulent
	 *
	 * @param hashedNumber
	 *           the hashed credit card number to check
	 * @return the score (0 if not fraudulent, otherwise {@link #DEFAULT_INCREMENT})
	 */
	public int isFraudulentCreditCard(final String hashedNumber)
	{
		return stolenCards.contains(hashedNumber) ? DEFAULT_INCREMENT : 0;
	}

	/**
	 * whether the given email address is fraudulent
	 *
	 * @param email
	 *           the email to check
	 * @return the score (0 if not fraudulent, otherwise {@link #DEFAULT_INCREMENT})
	 */
	public int isFraudulentEmailAddress(final String email)
	{
		//i.e.
		//some services lookup the email address in the knowlegde base
		//or look for some extreme names, nouns in the account name

		return email.hashCode() % 4 == 0 ? DEFAULT_INCREMENT : 0;
	}

	/**
	 * whether the given geolocation is fraudulent
	 *
	 * @param ipAddress
	 *           the ip address to check
	 * @param country
	 *           the country to check
	 * @param zipCode
	 *           the zip code to check
	 * @param state
	 *           the state to check
	 * @return the score (0 if not fraudulent, otherwise {@link #DEFAULT_INCREMENT})
	 */
	@SuppressWarnings("unused")
	public int isFraudulentGeolocation(final String ipAddress, final String country, final String zipCode, final String state)
	{
		//i.e. MaxMind GeoIp service
		return fakeGeolocation.get(country).equals(ipAddress) ? DEFAULT_INCREMENT : 0;
	}

	// YTODO get rid of the decomposing stuff
	protected Map<String, String> decomposeOrderModel(final AbstractOrderModel order)
	{
		final Map<String, String> result = new HashMap<>();
		final AddressModel address = order.getDeliveryAddress();
		Assert.assertNotNull(address);
		if (address.getCountry() != null)
		{
			result.put(FrauddetectionConstants.PARAM_COUNTRY, address.getCountry().getIsocode());
		}
		if (address.getTown() != null)
		{
			result.put(FrauddetectionConstants.PARAM_TOWN, address.getTown());
		}
		if (address.getPostalcode() != null)
		{
			result.put(FrauddetectionConstants.PARAM_ZIPCODE, address.getPostalcode());
		}
		if (address.getDistrict() != null)
		{
			result.put(FrauddetectionConstants.PARAM_STATE, address.getDistrict());
		}
		if (address.getEmail() != null)
		{
			result.put(FrauddetectionConstants.PARAM_EMAIL, address.getEmail());
		}
		return result;
	}

	@Override
	public FraudServiceResponse recognizeOrderFraudSymptoms(final AbstractOrderModel order)
	{
		final Map<String, Double> serviceResponse = doAll(decomposeOrderModel(order));
		final FraudServiceResponse response = new FraudServiceResponse(null, getProviderName());
		for (final Entry<String, Double> entry : serviceResponse.entrySet())
		{
			response.addSymptom(new FraudSymptom(entry.getKey(), serviceResponse.get(entry.getKey()).doubleValue()));

		}
		return response;
	}

	// YTODO get rid of the decomposing stuff
	protected Map<String, Double> doAll(final Map<String, String> parameters)
	{
		final Map<String, Double> result = new HashMap<>();

		if (parameters.containsKey(FrauddetectionConstants.PARAM_EMAIL))
		{
			result.put("Free email service",
					Double.valueOf(isFreeEmailService(parameters.get(FrauddetectionConstants.PARAM_EMAIL))));
			result.put("Suspicious email", Double.valueOf(isFraudulentEmailAddress(FrauddetectionConstants.PARAM_EMAIL)));
		}
		if (parameters.containsKey(FrauddetectionConstants.PARAM_DOMAIN))
		{
			result.put("Banned domain", Double.valueOf(isBannedDomain(parameters.get(FrauddetectionConstants.PARAM_DOMAIN))));
		}
		if (parameters.containsKey(FrauddetectionConstants.PARAM_IP))
		{
			result.put("Banned IP", Double.valueOf(isBannedDomain(parameters.get(FrauddetectionConstants.PARAM_IP))));
			if (parameters.containsKey(FrauddetectionConstants.PARAM_COUNTRY)
					&& parameters.containsKey(FrauddetectionConstants.PARAM_ZIPCODE)
					&& parameters.containsKey(FrauddetectionConstants.PARAM_STATE))
			{
				result.put("Fraudulent IP GeoLocation",
						Double.valueOf(isFraudulentGeolocation(parameters.get(FrauddetectionConstants.PARAM_IP),
								parameters.get(FrauddetectionConstants.PARAM_COUNTRY),
								parameters.get(FrauddetectionConstants.PARAM_ZIPCODE),
								parameters.get(FrauddetectionConstants.PARAM_STATE))));
			}
		}

		return result;
	}

	@Override
	public FraudServiceResponse recognizeUserActivitySymptoms(final UserModel order)
	{
		throw new NotImplementedException(getClass());
	}

}
