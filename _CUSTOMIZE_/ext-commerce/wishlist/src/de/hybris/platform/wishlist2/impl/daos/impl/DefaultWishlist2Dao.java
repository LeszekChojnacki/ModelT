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
package de.hybris.platform.wishlist2.impl.daos.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.wishlist2.impl.daos.Wishlist2Dao;
import de.hybris.platform.wishlist2.model.Wishlist2EntryModel;
import de.hybris.platform.wishlist2.model.Wishlist2Model;

import java.util.List;

import org.apache.log4j.Logger;


public class DefaultWishlist2Dao extends AbstractItemDao implements Wishlist2Dao
{

	private static final Logger LOG = Logger.getLogger(DefaultWishlist2Dao.class.getName());

	@Override
	public List<Wishlist2Model> findAllWishlists(final UserModel user)
	{
		final String query = "SELECT {" + Wishlist2Model.PK + "} FROM {" + Wishlist2Model._TYPECODE + "} WHERE {"
				+ Wishlist2Model.USER + "} = ?" + Wishlist2Model.USER;
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter(Wishlist2Model.USER, user);
		final SearchResult<Wishlist2Model> result = search(fQuery);
		return result.getResult();
	}

	@Override
	public Wishlist2Model findDefaultWishlist(final UserModel user)
	{
		final String query = "SELECT {" + Wishlist2Model.PK + "} FROM {" + Wishlist2Model._TYPECODE + "} WHERE {"
				+ Wishlist2Model.USER + "} = ?" + Wishlist2Model.USER + " AND {" + Wishlist2Model.DEFAULT + "} = ?trueValue";
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
		fQuery.addQueryParameter(Wishlist2Model.USER, user);
		fQuery.addQueryParameter("trueValue", Boolean.TRUE);

		final SearchResult<Wishlist2Model> result = search(fQuery);
		if (result.getCount() > 1)
		{
			LOG.warn("More than one default wishlist defined for user " + user.getName() + ". Returning first!");
		}
		if (result.getCount() > 0)
		{
			return result.getResult().iterator().next();
		}
		return null;
	}

	@Override
	public List<Wishlist2EntryModel> findWishlistEntryByProduct(final ProductModel product, final Wishlist2Model wishlist)
	{
		final StringBuilder query = new StringBuilder(//
				"SELECT {" + Wishlist2EntryModel.PK + "} FROM {" + Wishlist2EntryModel._TYPECODE + "} WHERE {");
		query.append(Wishlist2EntryModel.PRODUCT + "} = ?" + Wishlist2EntryModel.PRODUCT + " AND {");
		query.append(Wishlist2EntryModel.WISHLIST + "} = ?" + Wishlist2EntryModel.WISHLIST);
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query.toString());
		fQuery.addQueryParameter(Wishlist2EntryModel.PRODUCT, product);
		fQuery.addQueryParameter(Wishlist2EntryModel.WISHLIST, wishlist);

		final SearchResult<Wishlist2EntryModel> result = search(fQuery);
		final List<Wishlist2EntryModel> entries = result.getResult();
		return entries;
	}

}
