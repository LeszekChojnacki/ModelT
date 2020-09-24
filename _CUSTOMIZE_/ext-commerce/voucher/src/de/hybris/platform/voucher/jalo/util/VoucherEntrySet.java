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
package de.hybris.platform.voucher.jalo.util;

import de.hybris.platform.jalo.order.AbstractOrderEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class represents a set of <code>VoucherEntry</code> objects
 */
public class VoucherEntrySet implements Set
{
	// --------------------------------------------------------------- Constants
	// ------------------------------------------------------ Instance Variables
	private final Set set;

	// ------------------------------------------------------------ Constructors
	/**
	 * Creates a new instance of <code>VoucherEntrySet</code>.
	 */
	public VoucherEntrySet()
	{
		this.set = new HashSet();
	}

	public VoucherEntrySet(final Collection c)
	{
		this();
		addAll(c); //NOSONAR
	}

	// -------------------------------------------------------------- Properties
	@Override
	public boolean add(final Object o)
	{
		final VoucherEntry newEntry = checkForVoucherEntry(o);
		if (newEntry == null)
		{
			throw new IllegalArgumentException("Unable to add object of class " + o.getClass().getName() + "!");
		}
		final VoucherEntry existingEntry = getVoucherEntryByOrderEntry(newEntry.getOrderEntry());
		if (existingEntry == null)
		{
			return this.set.add(newEntry);
		}
		else
		{
			existingEntry.setQuantity(
					existingEntry.getQuantity() + newEntry.getUnit().convert(existingEntry.getUnit(), newEntry.getQuantity()));
			return true;
		}
	}

	@Override
	public boolean addAll(final Collection c)
	{
		for (final Iterator iterator = c.iterator(); iterator.hasNext();)
		{
			final Object next = iterator.next();
			if (!(next instanceof VoucherEntry) && !(next instanceof AbstractOrderEntry))
			{
				throw new IllegalArgumentException("Unable to add given collection!");
			}
		}
		boolean changed = false;
		for (final Iterator iterator = c.iterator(); iterator.hasNext();)
		{
			changed |= add(iterator.next());
		}
		return changed;
	}

	protected static VoucherEntry checkForVoucherEntry(final Object o)
	{
		if (o instanceof VoucherEntry)
		{
			return (VoucherEntry) o;
		}
		else if (o instanceof AbstractOrderEntry)
		{
			final AbstractOrderEntry entry = (AbstractOrderEntry) o;
			return new VoucherEntry(entry, entry.getQuantity().longValue(), entry.getUnit());
		}
		else
		{
			return null;
		}
	}

	@Override
	public void clear()
	{
		this.set.clear();
	}

	@Override
	public boolean contains(final Object o)
	{
		final VoucherEntry voucherEntry = checkForVoucherEntry(o);
		return voucherEntry != null && containsInternal(voucherEntry);
	}

	private boolean containsInternal(final VoucherEntry aVoucherEntry)
	{
		final VoucherEntry existingEntry = getVoucherEntryByOrderEntry(aVoucherEntry.getOrderEntry());
		return existingEntry != null && existingEntry.getQuantity() > aVoucherEntry.getUnit().convert(existingEntry.getUnit(),
				aVoucherEntry.getQuantity());
	}

	@Override
	public boolean containsAll(final Collection c)
	{
		boolean contains = true;
		for (final Iterator iterator = c.iterator(); iterator.hasNext();)
		{
			contains &= contains(iterator.next());
		}
		return contains;
	}

	private VoucherEntry getVoucherEntryByOrderEntry(final AbstractOrderEntry anAbstractOrderEntry)
	{
		return getVoucherEntryByOrderEntry(this.set, anAbstractOrderEntry);
	}

	private VoucherEntry getVoucherEntryByOrderEntry(final Collection c, final AbstractOrderEntry anAbstractOrderEntry)
	{
		for (final Iterator iterator = c.iterator(); iterator.hasNext();)
		{
			try
			{
				final VoucherEntry nextEntry = checkForVoucherEntry(iterator.next());
				if (nextEntry.getOrderEntry().equals(anAbstractOrderEntry)) //NOSONAR
				{
					return nextEntry;
				}
			}
			catch (final IllegalArgumentException e) // NOSONAR
			{
				continue;
			}
		}
		return null;
	}

	@Override
	public boolean isEmpty()
	{
		return this.set.isEmpty();
	}

	@Override
	public Iterator iterator()
	{
		return this.set.iterator();
	}

	@Override
	public boolean remove(final Object o)
	{
		if (contains(o))
		{
			final VoucherEntry entry = checkForVoucherEntry(o);
			final VoucherEntry existingEntry = getVoucherEntryByOrderEntry(entry.getOrderEntry()); //NOSONAR
			if (existingEntry != null)
			{
				if (existingEntry.getQuantity() > entry.getQuantity())
				{
					existingEntry.setQuantity(existingEntry.getQuantity() - entry.getQuantity());
				}
				else
				{
					this.set.remove(entry);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(final Collection c)
	{
		boolean changed = false;
		for (final Iterator iterator = c.iterator(); iterator.hasNext();)
		{
			changed |= remove(iterator.next());
		}
		return changed;
	}

	@Override
	public boolean retainAll(final Collection c)
	{
		boolean changed = false;
		for (final Iterator iterator = this.set.iterator(); iterator.hasNext();)
		{
			final VoucherEntry nextEntry = (VoucherEntry) iterator.next();
			final VoucherEntry retainingEntry = getVoucherEntryByOrderEntry(c, nextEntry.getOrderEntry());
			if (retainingEntry == null)
			{
				iterator.remove();
				changed = true;
			}
			else if (!nextEntry.equals(retainingEntry))
			{
				nextEntry.setQuantity(Math.min(nextEntry.getQuantity(),
						retainingEntry.getUnit().convert(nextEntry.getUnit(), retainingEntry.getQuantity())));
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public int size()
	{
		return this.set.size();
	}

	@Override
	public Object[] toArray()
	{
		return this.set.toArray();
	}

	@Override
	public Object[] toArray(final Object[] a)
	{
		return this.set.toArray(a);
	}
}
