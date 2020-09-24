/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.constants;

/**
 * Global class for all Warehousing constants. You can add global constants for your extension into this class.
 */
public final class WarehousingConstants extends GeneratedWarehousingConstants
{
	public static final String EXTENSIONNAME = "warehousing";
	public static final String CONSIGNMENT_PROCESS_CODE_SUFFIX = "_ordermanagement";
	public static final String CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME = "ConsolidatedConsignmentModels";


	private WarehousingConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension
	public static final String CAPTURE_PAYMENT_ON_CONSIGNMENT_PROPERTY_NAME = "warehousing.capturepaymentonconsignment";
}
