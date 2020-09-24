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
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.security.Principal;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserGroup;
import de.hybris.platform.util.localization.Localization;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * This restriction restricts vouchers to specified users.
 *
 */
public class UserRestriction extends GeneratedUserRestriction //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(UserRestriction.class.getName());

	@Override
	protected String[] getMessageAttributeValues()
	{
		final StringBuilder logins = new StringBuilder();
		for (final Iterator iterator = getUsers().iterator(); iterator.hasNext();)
		{
			final Principal nextPrincipal = (Principal) iterator.next();
			if (nextPrincipal instanceof User)
			{
				logins.append(((User) nextPrincipal).getLogin());//NOSONAR
			}
			if (nextPrincipal instanceof UserGroup)
			{
				logins.append(((UserGroup) nextPrincipal).getUID());//NOSONAR
			}
			if (iterator.hasNext())
			{
				logins.append(", ");
			}
		}
		return new String[]
		{ Localization.getLocalizedString("type.restriction.positive." + isPositiveAsPrimitive()), logins.toString() };
	}

	/**
	 * Returns a collection of principals which contains users or usergroups
	 *
	 * @param ctx
	 *           the SessionContext
	 * @return Collection of principals
	 */
	@Override
	public Collection getUsers(final SessionContext ctx)
	{
		final Collection principals = super.getUsers(ctx);
		return principals != null ? principals : Collections.emptyList();
	}

	/**
	 * Returns <tt>true</tt> if the specified abstract order fulfills this restriction. More formally, returns
	 * <tt>true</tt> if the principal (user or an user from an usergroup) of the specified abstract order is contained in
	 * the set of principals defined by this restriction.
	 *
	 * @param anOrder
	 *           the abstract order to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified abstract order fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(AbstractOrder)
	 */
	@Override
	protected boolean isFulfilledInternal(final AbstractOrder anOrder)
	{
		if (anOrder == null)
		{
			return false;
		}

		if (isPositiveAsPrimitive())
		{
			// order user must be part of defined principals
			return isPartOfConfiguredPrincipals(anOrder.getUser());
		}
		else
		{
			// order user must not be part of defined principals
			return !isPartOfConfiguredPrincipals(anOrder.getUser());
		}
	}

	/**
	 * returns true if the given user is part of this restriction's defined users, otherwise false.
	 */
	protected boolean isPartOfConfiguredPrincipals(final User user)
	{
		for (final Iterator iterator = getUsers().iterator(); iterator.hasNext();)
		{
			final Principal nextPrincipal = (Principal) iterator.next();
			if ((nextPrincipal instanceof User) && nextPrincipal.getPK().equals(user.getPK()))
			{
				return true;
			}
			else if ((nextPrincipal instanceof UserGroup) && (((UserGroup) nextPrincipal).containsMember(user))) //NOSONAR
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> if the specified product fulfills this restriction.
	 *
	 * @param aProduct
	 *           the product to check whether it fullfills this restriction.
	 * @return <tt>true</tt> if the specified product fulfills this restriction, <tt>false</tt> else.
	 * @see Restriction#isFulfilledInternal(Product)
	 */
	@Override
	protected boolean isFulfilledInternal(final Product aProduct) //NOSONAR
	{
		return true;
	}
}
