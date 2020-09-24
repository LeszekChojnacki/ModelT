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
package de.hybris.platform.externaltax;

import de.hybris.platform.basecommerce.model.externaltax.ProductTaxCodeModel;

import java.util.Collection;
import java.util.Map;


/**
 * Service for looking up {@link ProductTaxCodeModel} items.
 */
public interface ProductTaxCodeService
{
	/**
	 * Performs a direct tax code lookup for a given product code and tax area code.
	 *
	 * @param productCode
	 * 		product code to lookup the tax code for
	 * @param taxAreaCode
	 * 		tax area code
	 * @return tax code if found, null otherwise
	 */
	String lookupTaxCode(String productCode, String taxAreaCode);

	/**
	 * Performs a direct bulk tax code lookup for a given collection of product codes and tax area code.
	 *
	 * @param productCodes
	 * 		collection of product codes to lookup the tax codes for
	 * @param taxAreaCode
	 * 		tax area code
	 * @return map of product codes to tax codes
	 */
	Map<String, String> lookupTaxCodes(Collection<String> productCodes, String taxAreaCode);

	/**
	 * Finds a tax code item for a given product code and tax area code. Returns null if no such code exists yet.
	 *
	 * @param productCode
	 * 		product code
	 * @param taxAreaCode
	 * 		tax area code
	 * @return instance of {@link ProductTaxCodeModel}
	 */
	ProductTaxCodeModel getTaxCodeForProductAndArea(String productCode, String taxAreaCode);

	/**
	 * Finds all existing tax code items for a given product code.
	 *
	 * @param productCode
	 * 		product code
	 * @return collection of {@link ProductTaxCodeModel} for a given product code
	 */
	Collection<ProductTaxCodeModel> getTaxCodesForProduct(String productCode);
}
