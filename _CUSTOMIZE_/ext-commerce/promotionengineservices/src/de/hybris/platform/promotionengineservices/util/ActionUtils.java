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
package de.hybris.platform.promotionengineservices.util;

import java.util.UUID;


/**
 * Provides utility methods for generating and validating Action guids.
 */
public class ActionUtils
{
	/**
	 * Creates a new unique id to identify an action instance.
	 *
	 * @return a unique identifier based on {@link UUID#randomUUID()}
	 */
	public String createActionUUID() {
		return "Action[" + (UUID.randomUUID()).toString() + "]";
	}

	/**
	 * Validates if a guid identifies an action instance.
	 */
	public boolean isActionUUID(final String guid) {
		return guid.matches("^Action\\[[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}]$");
	}
}
