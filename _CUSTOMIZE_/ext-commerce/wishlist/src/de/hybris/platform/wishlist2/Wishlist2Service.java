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
package de.hybris.platform.wishlist2;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.wishlist2.enums.Wishlist2EntryPriority;
import de.hybris.platform.wishlist2.model.Wishlist2EntryModel;
import de.hybris.platform.wishlist2.model.Wishlist2Model;

import java.util.List;


/**
 * This interface provides the wish list related methods.
 */
public interface Wishlist2Service
{

	/**
	 * Returns all wishlists for the given user
	 * 
	 * @param user
	 *           the owner of the wish lists
	 * @return all wishlists of the given user
	 */
	List<Wishlist2Model> getWishlists(UserModel user);

	/**
	 * Resturns all wishlists for the current user
	 * 
	 * @return all wishlists of the current user
	 */
	List<Wishlist2Model> getWishlists();

	/**
	 * @return true if there is a default wishlist for the current user
	 */
	boolean hasDefaultWishlist();

	/**
	 * @return true if there is a default wishlist for the given user
	 */
	boolean hasDefaultWishlist(UserModel user);

	/**
	 * creates a new default wishlist for the current user. For an anonymous customer (or anonymous customer wishlist),
	 * the wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per
	 * default it is set to <i>false</i>.
	 * 
	 * @param name
	 *           The name of the wishlist
	 * @param description
	 *           The description of the wishlist
	 * @throws de.hybris.platform.servicelayer.exceptions.SystemException
	 *            when a default wishlist already exists for the current user
	 */
	Wishlist2Model createDefaultWishlist(String name, String description);

	/**
	 * creates a new default wishlist for the given user. For an anonymous customer (or anonymous customer wishlist), the
	 * wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per
	 * default it is set to <i>false</i>.
	 * 
	 * @param user
	 *           The user for which the whishlist should be created
	 * @param name
	 *           The name of the wishlist
	 * @param description
	 *           The description of the wishlist
	 * @throws de.hybris.platform.servicelayer.exceptions.SystemException
	 *            when a default wishlist already exists for the current user
	 */
	Wishlist2Model createDefaultWishlist(UserModel user, String name, String description);

	/**
	 * creates a new wishlist for the current user. For an anonymous customer (or anonymous customer wishlist), the
	 * wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per
	 * default it is set to <i>false</i>.
	 * 
	 * @param name
	 *           The name of the wishlist
	 * @param description
	 *           The description of the wishlist
	 */
	Wishlist2Model createWishlist(String name, String description);

	/**
	 * creates a new wishlist for the given user. For an anonymous customer (or anonymous customer wishlist), the
	 * wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per
	 * default it is set to <i>false</i>.
	 * 
	 * @param user
	 *           The user for which the whishlist should be created
	 * @param name
	 *           The name of the wishlist
	 * @param description
	 *           The description of the wishlist
	 */
	Wishlist2Model createWishlist(UserModel user, String name, String description);

	/**
	 * Returns the default wishlist for the given user
	 * 
	 * @param user
	 *           the owner of the wishlist
	 * @return the default wishlist
	 * @throws NullPointerException
	 *            if the user has no default wishlist
	 */
	Wishlist2Model getDefaultWishlist(UserModel user);

	/**
	 * Returns the default wishlist for the current user
	 * 
	 * @return the default wishlist
	 * @throws NullPointerException
	 *            if the user has no default wishlist
	 */
	Wishlist2Model getDefaultWishlist();

	/**
	 * Adds the given entry to the given wishlist. For an anonymous customer (or anonymous customer wishlist), the
	 * wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per
	 * default it is set to <i>false</i>.
	 * 
	 * @param wishlist
	 *           the wishlist the entry should be added to
	 * @param entry
	 *           the entry which should be added to the wishlist
	 */
	void addWishlistEntry(Wishlist2Model wishlist, Wishlist2EntryModel entry);

	/**
	 * Adds a given product to the given wishlist
	 * 
	 * @param wishlist
	 *           The wishlist on which the product should be added
	 * @param product
	 *           The product which should be added to the wishlist
	 */
	void addWishlistEntry(Wishlist2Model wishlist, ProductModel product, Integer desired, Wishlist2EntryPriority priority,
			String comment);

	/**
	 * Adds a given product to the default wishlist of the current user
	 * 
	 * @param product
	 *           The product which should be added to the wishlist
	 * @throws NullPointerException
	 *            if the user has no default wishlist
	 */
	void addWishlistEntry(ProductModel product, Integer desired, Wishlist2EntryPriority priority, String comment);

	/**
	 * Adds a given product to the default wishlist of the given user
	 * 
	 * @param user
	 *           the owner of the wishlist
	 * @param product
	 *           The product which should be added to the wishlist
	 * @throws NullPointerException
	 *            if the user has no default wishlist
	 */
	void addWishlistEntry(UserModel user, ProductModel product, Integer desired, Wishlist2EntryPriority priority, String comment);


	/**
	 * Removes a given entry from the wishlist. For an anonymous customer (or anonymous customer wishlist), the wishlist
	 * will only be saved if the spring property <i>saveAnonymousWishlists</i> is set to <i>true</i>. Per default it is
	 * set to <i>false</i>.
	 * 
	 * @param wishlist
	 *           the wishlist from which the entry should be removed
	 * @param entry
	 *           the entry which should be removed
	 */
	void removeWishlistEntry(Wishlist2Model wishlist, Wishlist2EntryModel entry);

	/**
	 * Returns the wishlist entry which contains the given product from the specific wishlist
	 * 
	 * @param product
	 *           the product which is contained in the wishlist entry which needs to be found
	 * @param wishlist
	 *           the wishlist which contains the wishlist entry
	 * @return the found wishlist entry
	 * 
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
	 *            if no wishlist entry that meets the conditions can be found.
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException
	 *            if more than one wishlist entry that meets the conditions can be found
	 * @throws IllegalArgumentException
	 *            if parameter <code>product</code> or <code>wishlist</code> is <code>null</code>.
	 */
	Wishlist2EntryModel getWishlistEntryForProduct(ProductModel product, Wishlist2Model wishlist);

	/**
	 * Removes a wishlist entry from the wishlist which contains the given product. For an anonymous customer (or
	 * anonymous customer wishlist), the wishlist will only be saved if the spring property <i>saveAnonymousWishlists</i>
	 * is set to <i>true</i>. Per default it is set to <i>false</i>.
	 * 
	 * @param product
	 *           the product which is contained in the wishlist entry which will be removed
	 * @param wishlist
	 *           the wishlist from which the entry should be removed
	 * 
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException
	 *            if no wishlist entry that meets the conditions can be found.
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException
	 *            if more than one wishlist entry that meets the conditions can be found
	 * @throws IllegalArgumentException
	 *            if parameter <code>product</code> or <code>wishlist</code> is <code>null</code>.
	 */
	void removeWishlistEntryForProduct(ProductModel product, Wishlist2Model wishlist);

}
