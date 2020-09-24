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
package de.hybris.platform.wishlist2.impl.daos;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;
import de.hybris.platform.wishlist2.model.Wishlist2EntryModel;
import de.hybris.platform.wishlist2.model.Wishlist2Model;

import java.util.List;


public interface Wishlist2Dao extends Dao
{

	/**
	 * Retrieves all wishlists of the specific {@link UserModel}
	 * 
	 * @param user
	 *           the owner of the wishlists
	 * 
	 * @return all found wishlists
	 */
	List<Wishlist2Model> findAllWishlists(UserModel user);

	/**
	 * Retrieves the default wishlist of the specific {@link UserModel}
	 * 
	 * @param user
	 *           the owner of the default wishlist
	 * 
	 * @return the found wishlist, or null if no default wishlist can be found.
	 */
	Wishlist2Model findDefaultWishlist(UserModel user);

	/**
	 * Retrieves all wishlist entries which contain the given product from the specific wishlist
	 * 
	 * @param product
	 *           the product which is contained in the wishlist entry which needs to be found
	 * @param wishlist
	 *           the wishlist which contains the wishlist entry
	 * 
	 * @return all found wishlist entries
	 */
	List<Wishlist2EntryModel> findWishlistEntryByProduct(ProductModel product, Wishlist2Model wishlist);

}
