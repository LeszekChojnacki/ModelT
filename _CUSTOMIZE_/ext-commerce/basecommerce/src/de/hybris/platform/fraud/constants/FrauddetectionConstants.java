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
package de.hybris.platform.fraud.constants;

/**
 * Global class for all fraud detection constants. You can add global constants for your extension into this class.
 */
public final class FrauddetectionConstants
{

	// implement here constants used by this extension
	public static final String ORDER_KEY = "ORDER";
	public static final String NULL_ORDER_MSG = "Process does not contain order";
	public static final String ORDERCONTEXT_KEY = "orderContext";


	//symptom properties
	public static final String STRATEGY_SUFFIX = " strategy";
	public static final String USERID = "Uid";
	public static final String EMAIL = "email";

	//Commercial services parameter names
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_IP = "IP";
	public static final String PARAM_DOMAIN = "domain";
	public static final String PARAM_CARDNUMBER = "cardNumber";
	public static final String PARAM_COUNTRY = "country";
	public static final String PARAM_ZIPCODE = "zipCode";
	public static final String PARAM_TOWN = "town";
	public static final String PARAM_STATE = "state";

	private FrauddetectionConstants()
	{
		// prevent instantiation
	}

}
