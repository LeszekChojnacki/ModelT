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
package de.hybris.platform.warehousing.sourcing.result;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;

import java.util.Collection;
import java.util.Map;

public interface SourcingResultFactory
{

	/**
	 * Create a sourcing result.
	 *
	 * @param orderEntry       the abstract order entry model
	 * @param sourcingLocation the location from where to source the product.
	 * @param quantity         the quantity to source for this product and this location.
	 * @return the sourcing result
	 */
	SourcingResult create(AbstractOrderEntryModel orderEntry, SourcingLocation sourcingLocation,
			Long quantity);

	/**
	 * Create a sourcing result where all order entries are sourced from the given sourcingLocation and using the
	 * quantity specified by {@link AbstractOrderEntryModel#getQuantityUnallocated()}.
	 *
	 * @param orderEntries
	 *           - the collection of abstract order entry model
	 * @param sourcingLocation
	 *           - the location from where to source the product
	 * @return the sourcing result
	 */
	SourcingResult create(Collection<AbstractOrderEntryModel> orderEntries, SourcingLocation sourcingLocation);

	/**
	 * Create a sourcing result where all order entries are sourced from the given sourcingLocation and using the
	 * quantity specified in the allocation map.
	 *
	 * @param allocation
	 *           - the map of abstract order entry model and the quantity allocated
	 * @param sourcingLocation
	 *           - the location from where to source the product
	 * @return the sourcing result
	 */
	SourcingResult create(Map<AbstractOrderEntryModel, Long> allocation, SourcingLocation sourcingLocation);

	/**
	 * Create a single sourcing result from a collection of sourcing results.
	 *
	 * @param results - the collection of sourcing results; should not be <tt>null</tt>
	 * @return the merged sourcing result; never <tt>null</tt>
	 */
	SourcingResults create(Collection<SourcingResults> results);

}
