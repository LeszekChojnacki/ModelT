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
package de.hybris.platform.voucher.jalo;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.voucher.jalo.util.VoucherEntrySet;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * The [y] hybris Platform <i>voucher extension </i> enables users to assign a set of restrictions to a voucher for
 * confining the usage of it. A combination of none, one, or many of the restrictions is possible. Developers can
 * implement other restrictions in addition to those already there.
 * <p />
 * In an order containing multiple items, (percentage) discounts will apply only to the products that match all of the
 * criteria given by its assigned restrictions. The other products in the order are not discounted. Vouchers provide an
 * interface for getting the eligible entries within an given order.
 * 
 */
public abstract class Restriction extends GeneratedRestriction
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Restriction.class.getName());


	private String format(final String pattern)
	{
		String result = null;
		if (pattern != null)
		{
			result = MessageFormat.format(pattern, (Object[]) getMessageAttributeValues());
		}
		return result;
	}

	/**
	 * Returns a map containing all localized names of the type of this restriction.
	 * 
	 * @param ctx
	 *           the context of the actual session
	 * @return a <tt>Map</tt> associating <tt>String</tt> objects as values to <tt>Language</tt> objects as keys.
	 */
	@Override
	public Map getAllRestrictionType(final SessionContext ctx)
	{
		final Map result = new HashMap();
		final SessionContext myctx = getSession().createSessionContext();
		for (final Iterator iterator = getSession().getC2LManager().getAllLanguages().iterator(); iterator.hasNext();)   //NOSONAR
		{
			final Language language = (Language) iterator.next();
			myctx.setLanguage(language);
			final String itemTypeName = getRestrictionType(myctx);
			if (itemTypeName != null)
			{
				result.put(language, itemTypeName);
			}
		}
		return result;
	}

	public VoucherEntrySet getApplicableEntries(final AbstractOrder anOrder)
	{
		final VoucherEntrySet entries = new VoucherEntrySet();
		if (isFulfilled(anOrder))
		{
			entries.addAll(anOrder.getAllEntries());   //NOSONAR
		}
		return entries;
	}

	/**
	 * Returns a description for this restriction. To get a more specific description, this method replaces all place
	 * holders in the localized description returned by <tt>super.getDescription(SessionContext)</tt> by the values
	 * returned by <tt>getMessageAttributeValues()</tt>.
	 * 
	 * @return a description for this restriction.
	 */
	@Override
	public String getDescription(final SessionContext ctx)
	{
		return format(super.getDescription(ctx));
	}

	/**
	 * Convenience method. Returns the localized name of the type of this restriction, equivalent to
	 * <tt>getComposedType().getName()</tt>.
	 * 
	 * @param ctx
	 *           the context of the actual session.
	 * @return the locaalized name of this restriction's type.
	 */
	@Override
	public String getRestrictionType(final SessionContext ctx)
	{
		return this.getComposedType().getName(ctx);
	}

	/**
	 * Returns a message explaining on what terms this restriction is fulfilled. To get a more specific violation
	 * message, this method replaces all place holders in the localized message returned by
	 * <tt>super.getViolationMessage(SessionContext)</tt> by the values returned by <tt>getMessageAttributeValues()</tt>.
	 * 
	 * @return a message explaining on what terms this restriction is fulfilled.
	 */
	@Override
	public final String getViolationMessage(final SessionContext ctx)
	{
		return format(super.getViolationMessage(ctx));
	}

	/**
	 * Returns the values for making violation messages more useful.
	 */
	protected String[] getMessageAttributeValues()
	{
		return new String[]
		{ getRestrictionType() };
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order is not null and fulfills this restriction.
	 * 
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order is not null and fulfills this restriction, <tt>false</tt>
	 *         else.
	 */
	public final boolean isFulfilled(final AbstractOrder anOrder)
	{
		return anOrder != null && isFulfilledInternal(anOrder);
	}

	/**
	 * Returns <tt>true</tt> if the specified product is not null and fulfills this restriction.
	 * 
	 * @param aProduct
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product is not null and fulfills this restriction, <tt>false</tt> else.
	 */
	public final boolean isFulfilled(final Product aProduct)   //NOSONAR
	{
		return aProduct != null && isFulfilledInternal(aProduct);
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction.
	 * 
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 */
	protected abstract boolean isFulfilledInternal(AbstractOrder anOrder);

	/**
	 * Returns <tt>true</tt> if the specified product fulfills this restriction.
	 * 
	 * @param aProduct
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product fulfills this restriction, <tt>false</tt> else.
	 */
	protected abstract boolean isFulfilledInternal(Product aProduct);   //NOSONAR
}
