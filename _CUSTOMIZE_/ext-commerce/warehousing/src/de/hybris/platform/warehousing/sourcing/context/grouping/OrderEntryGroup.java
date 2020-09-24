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
package de.hybris.platform.warehousing.sourcing.context.grouping;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;


/**
 * Data transfer object used to maintain a group/collection of {@link AbstractOrderEntryModel}.
 */
public class OrderEntryGroup
{
	private final Collection<AbstractOrderEntryModel> entries;

	public OrderEntryGroup(final Collection<AbstractOrderEntryModel> entries)
	{
		Preconditions.checkArgument(entries != null, "Entries cannot be null");
		this.entries = entries;
	}

	/**
	 * Add an entry to the group.
	 *
	 * @param entry
	 *           - the abstract order entry model
	 */
	public void add(final AbstractOrderEntryModel entry)
	{
		if (entry != null)
		{
			entries.add(entry);
		}
	}

	/**
	 * Add a collection of entries to the group.
	 *
	 * @param entries
	 *           - an immutable collection of abstract order entry models
	 */
	public void addAll(final Collection<AbstractOrderEntryModel> entries)
	{
		if (entries != null)
		{
			entries.addAll(entries);
		}
	}

	/**
	 * Gets the size of group.
	 *
	 * @return size of group
	 */
	public int size()
	{
		return entries.size();
	}

	/**
	 * Gets the entries in the group.
	 *
	 * @return entries in the group; never <tt>null</tt>
	 */
	public Collection<AbstractOrderEntryModel> getEntries()
	{
		return Collections.unmodifiableCollection(entries);
	}
}
