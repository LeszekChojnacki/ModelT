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
package de.hybris.platform.promotions.util;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.product.Product;


/**
 * Interface implemeneted by composite products that want to be treated as such by the promotions extension
 */
public interface CompositeProduct
{
	Product getCompositeParentProduct(final SessionContext ctx);  //NOSONAR
}
