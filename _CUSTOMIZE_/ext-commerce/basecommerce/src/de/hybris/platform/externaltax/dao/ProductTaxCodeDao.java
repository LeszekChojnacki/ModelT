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
package de.hybris.platform.externaltax.dao;

import de.hybris.platform.basecommerce.model.externaltax.ProductTaxCodeModel;

import java.util.Collection;
import java.util.List;


public interface ProductTaxCodeDao
{
	String getTaxCodeForProductAndAreaDirect(String productCode, String taxArea);

	List<List<String>> getTaxCodesForProductsAndAreaDirect(Collection<String> productCodes, String taxArea);

	ProductTaxCodeModel getTaxCodeForProductAndArea(String productCode, String taxArea);

	List<ProductTaxCodeModel> getTaxCodesForProduct(String productCode);
}
