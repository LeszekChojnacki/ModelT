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
package de.hybris.platform.wishlist2.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.core.Constants;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.wishlist2.Wishlist2Service;
import de.hybris.platform.wishlist2.enums.Wishlist2EntryPriority;
import de.hybris.platform.wishlist2.impl.daos.Wishlist2Dao;
import de.hybris.platform.wishlist2.model.Wishlist2EntryModel;
import de.hybris.platform.wishlist2.model.Wishlist2Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class DefaultWishlist2Service implements Wishlist2Service
{
	/**
	 * @deprecated Since 4.1. Implement the Wishlist2Dao as own property
	 */
	@Deprecated
	protected Wishlist2Dao wishlistDao;

	/**
	 * @deprecated Since 4.1. Implement the saveAnonymousWishlists boolean as own property
	 */
	@Deprecated
	protected boolean saveAnonymousWishlists;

	private ModelService modelService;

	protected UserModel getCurrentUser()
	{
		return getModelService().get(JaloSession.getCurrentSession().getUser());
	}

	protected boolean saveWishlist(final UserModel user)
	{
		if (user == null)
		{
			return false;
		}
		final boolean anonymous = Constants.USER.ANONYMOUS_CUSTOMER.equals(user.getUid());
		return (!anonymous || (anonymous && saveAnonymousWishlists));
	}

	protected boolean saveWishlist(final Wishlist2Model wishlist)
	{
		final UserModel user = wishlist.getUser();
		return saveWishlist(user);
	}

	@Override
	public Wishlist2Model getDefaultWishlist(final UserModel user)
	{
		return wishlistDao.findDefaultWishlist(user);
	}

	@Override
	public Wishlist2Model getDefaultWishlist()
	{
		return getDefaultWishlist(getCurrentUser());
	}

	@Override
	public List<Wishlist2Model> getWishlists(final UserModel user)
	{
		return wishlistDao.findAllWishlists(user);
	}

	@Override
	public List<Wishlist2Model> getWishlists()
	{
		return getWishlists(getCurrentUser());
	}

	@Override
	public Wishlist2EntryModel getWishlistEntryForProduct(final ProductModel product, final Wishlist2Model wishlist)
	{
		validateParameterNotNull(product, "Parameter 'product' was null.");
		validateParameterNotNull(wishlist, "Parameter 'wishlist' was null.");
		final List<Wishlist2EntryModel> entries = wishlistDao.findWishlistEntryByProduct(product, wishlist);
		if (entries.isEmpty())
		{
			throw new UnknownIdentifierException("Wishlist entry with product [" + product.getCode() + "] in wishlist ["
					+ wishlist.getName() + " ] not found.");
		}
		if (entries.size() > 1)
		{
			throw new AmbiguousIdentifierException("Wishlist entry with product [" + product.getCode() + "] in wishlist ["
					+ wishlist.getName() + "] is not unique, " + entries.size() + " entries found.");
		}
		return entries.iterator().next();
	}

	@Override
	public void removeWishlistEntryForProduct(final ProductModel product, final Wishlist2Model wishlist)
	{
		final Wishlist2EntryModel entry = getWishlistEntryForProduct(product, wishlist);
		removeWishlistEntry(wishlist, entry);
	}

	@Override
	public void addWishlistEntry(final Wishlist2Model wishlist, final Wishlist2EntryModel entry)
	{
		if (saveWishlist(wishlist))
		{
			getModelService().save(entry);
		}
		final List<Wishlist2EntryModel> entries = new ArrayList<Wishlist2EntryModel>(wishlist.getEntries());
		entries.add(entry);
		wishlist.setEntries(entries);
		if (saveWishlist(wishlist))
		{
			getModelService().save(wishlist);
		}
	}

	@Override
	public void removeWishlistEntry(final Wishlist2Model wishlist, final Wishlist2EntryModel entry)
	{
		final List<Wishlist2EntryModel> entries = new ArrayList<Wishlist2EntryModel>(wishlist.getEntries());
		entries.remove(entry);
		wishlist.setEntries(entries);
		if (saveWishlist(wishlist))
		{
			getModelService().save(wishlist);
		}
	}

	@Override
	public void addWishlistEntry(final Wishlist2Model wishlist, final ProductModel product, final Integer desired,
			final Wishlist2EntryPriority priority, final String comment)
	{
		final Wishlist2EntryModel entry = new Wishlist2EntryModel();
		entry.setProduct(product);
		entry.setDesired(desired);
		entry.setPriority(priority);
		entry.setComment(comment);
		entry.setAddedDate(new Date());
		addWishlistEntry(wishlist, entry);
	}

	@Override
	public void addWishlistEntry(final ProductModel product, final Integer desired, final Wishlist2EntryPriority priority,
			final String comment)
	{
		final Wishlist2Model wishlist = getDefaultWishlist();
		addWishlistEntry(wishlist, product, desired, priority, comment);
	}

	@Override
	public void addWishlistEntry(final UserModel user, final ProductModel product, final Integer desired,
			final Wishlist2EntryPriority priority, final String comment)
	{
		final Wishlist2Model wishlist = getDefaultWishlist(user);
		addWishlistEntry(wishlist, product, desired, priority, comment);
	}

	@Override
	public Wishlist2Model createDefaultWishlist(final String name, final String description)
	{
		return createDefaultWishlist(getCurrentUser(), name, description);
	}

	@Override
	public Wishlist2Model createDefaultWishlist(final UserModel user, final String name, final String description)
	{
		if (hasDefaultWishlist())
		{
			throw new SystemException("An default wishlist for the user <" + user.getName() + "> already exists");
		}
		return createWishlist(user, name, description, Boolean.TRUE);
	}

	@Override
	public Wishlist2Model createWishlist(final String name, final String description)
	{
		return createWishlist(getCurrentUser(), name, description);
	}

	@Override
	public Wishlist2Model createWishlist(final UserModel user, final String name, final String description)
	{
		return createWishlist(user, name, description, Boolean.FALSE);
	}

	private Wishlist2Model createWishlist(final UserModel user, final String name, final String description,
			final Boolean defaultWL)
	{
		final Wishlist2Model wishlist = new Wishlist2Model();
		wishlist.setName(name);
		wishlist.setDescription(description);
		wishlist.setDefault(defaultWL);
		wishlist.setUser(user);
		if (saveWishlist(user))
		{
			getModelService().save(wishlist);
		}
		return wishlist;
	}

	@Override
	public boolean hasDefaultWishlist()
	{
		return hasDefaultWishlist(getCurrentUser());
	}

	@Override
	public boolean hasDefaultWishlist(final UserModel user)
	{
		return wishlistDao.findDefaultWishlist(user) != null;
	}

	public void setWishlistDao(final Wishlist2Dao wishlistDao)
	{
		this.wishlistDao = wishlistDao;
	}

	public void setSaveAnonymousWishlists(final boolean saveAnonymousWishlists)
	{
		this.saveAnonymousWishlists = saveAnonymousWishlists;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}


}
