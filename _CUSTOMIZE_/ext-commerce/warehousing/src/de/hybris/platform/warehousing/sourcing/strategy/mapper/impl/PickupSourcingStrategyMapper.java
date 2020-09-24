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
package de.hybris.platform.warehousing.sourcing.strategy.mapper.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.sourcing.strategy.AbstractSourcingStrategyMapper;

import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Mapper to map a sourcing strategy to handle pickup in store.
 * </p>
 * <p>
 * This mapper will return a match if and only if every order entry in the context has a delivery point of service set.
 * </p>
 */
public class PickupSourcingStrategyMapper extends AbstractSourcingStrategyMapper
{

	private static final Logger LOG = LoggerFactory.getLogger(PickupSourcingStrategyMapper.class);

	@Override
	public Boolean isMatch(final SourcingContext context)
	{
		if (CollectionUtils.isEmpty(context.getOrderEntries()))
		{
			return Boolean.FALSE;
		}

		// Collect all of the matches. If one of them was false, then there was no match overall.
		final Boolean match = !context.getOrderEntries().stream().map(entry -> isMatch(entry)).collect(Collectors.toSet())
				.contains(Boolean.FALSE);

		if (LOG.isDebugEnabled() && match)
		{
			LOG.debug("Match found for context.");
		}

		return match;
	}

	/**
	 * Check to see if the order entry provides a match.
	 *
	 * @param entry
	 *           - the abstract order entry model
	 * @return <tt>true</tt> if the delivery point of service is set on the order entry; <tt>false</tt> otherwise
	 */
	protected Boolean isMatch(final AbstractOrderEntryModel entry)
	{
		return Boolean.valueOf(entry.getDeliveryPointOfService() != null);
	}

}
