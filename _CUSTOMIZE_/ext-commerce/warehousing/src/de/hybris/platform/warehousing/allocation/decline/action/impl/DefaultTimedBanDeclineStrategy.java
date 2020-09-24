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
package de.hybris.platform.warehousing.allocation.decline.action.impl;

import de.hybris.platform.warehousing.allocation.decline.action.DeclineActionStrategy;
import de.hybris.platform.warehousing.data.allocation.DeclineEntry;
import de.hybris.platform.warehousing.enums.DeclineReason;
import de.hybris.platform.warehousing.sourcing.ban.service.SourcingBanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfAnyResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Strategy to apply when the decline reason for a consignment entry is {@link DeclineReason#TOOBUSY}.
 * Or for each {@link DeclineReason}, when declining the consignment allocated from the external {@link de.hybris.platform.ordersplitting.model.WarehouseModel}
 */
public class DefaultTimedBanDeclineStrategy implements DeclineActionStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTimedBanDeclineStrategy.class);
	private SourcingBanService sourcingBanService;

	@Override
	public void execute(final DeclineEntry declineEntry)
	{
		validateParameterNotNull(declineEntry, "Decline Entry cannot be null");
		LOGGER.debug("Default Decline Action Ban Strategy is being invoked, a Sourcing Ban will be created for warehouse: " +
				declineEntry.getConsignmentEntry().getConsignment().getWarehouse());

		getSourcingBanService().createSourcingBan(declineEntry.getConsignmentEntry().getConsignment().getWarehouse());
	}

	@Override
	public void execute(final Collection<DeclineEntry> declineEntries)
	{
		validateIfAnyResult(declineEntries,"Nothing to decline");
		final DeclineEntry declineEntry = declineEntries.iterator().next();
		this.execute(declineEntry);
	}

	protected SourcingBanService getSourcingBanService()
	{
		return sourcingBanService;
	}

	@Required
	public void setSourcingBanService(final SourcingBanService sourcingBanService)
	{
		this.sourcingBanService = sourcingBanService;
	}
}
