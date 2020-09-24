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
 */
package de.hybris.platform.datahubbackoffice.dataaccess.metadata;

import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.datahub.dto.item.ItemData;

import java.util.Collection;

/**
 * This service provides canonical item type definitions for the data access operations.
 */
public interface DataHubTypeService
{
	/**
	 * Determines whether a canonical type matching the specified type code exists in a DataHub.
	 * 
	 * @param code
	 *           a type code to check.
	 * @return <code>true</code>, if the type exists; <code>false</code>, otherwise.
	 */
	boolean exists(String code);

	/**
	 * Retrieves codes of all types defined in a DataHub.
	 * 
	 * @return a collection of all type codes existing in a DataHub.
	 */
	Collection<String> getAllTypeCodes();

	/**
	 * Retrieves definition of a data type.
	 * 
	 * @param code
	 *           type code for the data type to retrieve.
	 * @return details about how the type is defined or <code>null</code>, if the type does not exist.
	 */
	DataType getType(String code);

	/**
	 * Determines type of the specified item.
	 * 
	 * @param item
	 *           an item to determine type of.
	 * @return code of the item's type.
	 */
	String deriveTypeCode(ItemData item);
}
