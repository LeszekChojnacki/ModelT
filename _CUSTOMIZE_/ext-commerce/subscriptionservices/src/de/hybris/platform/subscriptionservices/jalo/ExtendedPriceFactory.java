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
package de.hybris.platform.subscriptionservices.jalo;

import de.hybris.platform.europe1.jalo.PriceRow;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.price.JaloPriceFactoryException;
import de.hybris.platform.jalo.order.price.PriceFactory;
import de.hybris.platform.jalo.product.Product;


/**
 * 
 * Overrides the {@link PriceFactory} in order to retrieve the {@link PriceRow} for a given product or entry whereas the
 * {@link PriceFactory} only returns Price informations
 * 
 */
public interface ExtendedPriceFactory extends PriceFactory
{
	/**
	 * @param product
	 *           for which the first applicable {@link PriceRow} is returned
	 * @return {@link PriceRow} or <code>null</code>
	 * @throws JaloPriceFactoryException
	 */
	PriceRow getPriceRow(Product product) throws JaloPriceFactoryException;

	/**
	 * @param entry
	 *           for whose product the first applicable {@link PriceRow} is returned
	 * @return {@link PriceRow} or <code>null</code>
	 * @throws JaloPriceFactoryException
	 */
	PriceRow getPriceRow(AbstractOrderEntry entry) throws JaloPriceFactoryException;
}
