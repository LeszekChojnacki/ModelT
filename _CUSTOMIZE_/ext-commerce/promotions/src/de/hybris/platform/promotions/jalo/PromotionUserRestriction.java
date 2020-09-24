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
package de.hybris.platform.promotions.jalo;


import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.jalo.security.Principal;
import de.hybris.platform.jalo.security.PrincipalGroup;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserGroup;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * PromotionUserRestriction. Prevents the promotion from running if the user is in the restricted users collection.
 *
 */
public class PromotionUserRestriction extends GeneratedPromotionUserRestriction //NOSONAR
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PromotionUserRestriction.class);

	@Override
	public RestrictionResult evaluate(final SessionContext ctx, final Collection<Product> products, final Date date, // NOSONAR
			final AbstractOrder order)
	{
		User user;
		if (order != null)
		{
			user = order.getUser(ctx);
		}
		else
		{
			user = ctx.getUser();
		}

		final boolean positive = isPositiveAsPrimitive(ctx);
		if (user != null && isInUserCollection(ctx, user))
		{
			return positive ? RestrictionResult.ALLOW : RestrictionResult.DENY;
		}
		return positive ? RestrictionResult.DENY : RestrictionResult.ALLOW;
	}

	/**
	 * Test if the user is restricted by this PromotionRestriction.
	 *
	 * @param ctx
	 *           The context
	 * @param user
	 *           The user to test
	 * @return true if the user is restricted, otherwise false
	 */
	protected boolean isInUserCollection(final SessionContext ctx, final User user)
	{
		final Collection<Principal> restrictedUsers = this.getUsers(ctx);
		for (final Principal p : restrictedUsers)
		{
			if (isInUserCollectionRecursive(ctx, p, user))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean isInUserCollectionRecursive(final SessionContext ctx, final Principal principal, final User user)
	{
		if (principal instanceof PrincipalGroup)
		{
			final PrincipalGroup principalGroup = (PrincipalGroup) principal;
			final Collection<PrincipalGroup> superGroups = user.getGroups(ctx);
			return checkGroupForPrincipal(ctx, principalGroup, superGroups);
		}
		else
		{
			return principal.equals(user);
		}
	}

	protected boolean checkGroupForPrincipal(final SessionContext ctx, final PrincipalGroup restrictedGroup,
			final Collection<PrincipalGroup> groups)
	{
		if (groups.contains(restrictedGroup))
		{
			return true;
		}
		else
		{
			for (final PrincipalGroup _group : groups)
			{
				if (checkGroupForPrincipal(ctx, restrictedGroup, _group.getGroups(ctx)))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void buildDataUniqueKey(final SessionContext ctx, final StringBuilder builder)
	{
		super.buildDataUniqueKey(ctx, builder);
		builder.append(isPositiveAsPrimitive(ctx)).append('|');

		final Collection<Principal> users = getUsers(ctx);
		if (users != null && !users.isEmpty())
		{
			for (final Principal p : users)
			{
				builder.append(p.getUID()).append(','); // NOSONAR
			}
		}
		builder.append('|');
	}

	@Override
	protected Object[] getDescriptionPatternArguments(final SessionContext ctx)
	{
		return new Object[]
		{ getRestrictionType(ctx), Integer.valueOf(isPositiveAsPrimitive(ctx) ? 1 : 0), getUserNames(ctx) };
	}

	public String getUserNames(final SessionContext ctx)
	{
		final StringBuilder userNames = new StringBuilder();

		final Collection<Principal> users = getUsers(ctx);
		if (users != null && !users.isEmpty())
		{
			for (final Iterator<Principal> iterator = users.iterator(); iterator.hasNext();)
			{
				final Principal principal = iterator.next();

				if (principal instanceof User)
				{
					userNames.append(((User) principal).getLogin(ctx)); // NOSONAR
				}
				else if (principal instanceof UserGroup)
				{
					userNames.append(((UserGroup) principal).getUID()); // NOSONAR
				}

				if (iterator.hasNext())
				{
					userNames.append(", ");
				}
			}
		}

		return userNames.toString();
	}
}
