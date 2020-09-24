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
package de.hybris.platform.warehousing.labels.comparator;

import de.hybris.platform.warehousing.data.pickslip.ConsolidatedPickSlipFormEntry;

import java.util.Comparator;


/**
 * Comparator that sorts the bin number of {@link ConsolidatedPickSlipFormEntry} ascending by alphabetical order.
 */
public class DefaultConsolidatedPickSlipBinComparator implements Comparator<ConsolidatedPickSlipFormEntry>
{
	@Override
	public int compare(final ConsolidatedPickSlipFormEntry entry1, final ConsolidatedPickSlipFormEntry entry2)
	{
		return entry1.getBin().compareTo(entry2.getBin());
	}
}
