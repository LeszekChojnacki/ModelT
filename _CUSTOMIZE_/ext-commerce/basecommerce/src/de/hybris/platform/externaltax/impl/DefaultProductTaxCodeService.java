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
package de.hybris.platform.externaltax.impl;

import de.hybris.platform.basecommerce.model.externaltax.ProductTaxCodeModel;
import de.hybris.platform.externaltax.ProductTaxCodeService;
import de.hybris.platform.externaltax.dao.ProductTaxCodeDao;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link ProductTaxCodeService}.
 */
public class DefaultProductTaxCodeService implements ProductTaxCodeService
{
	private ProductTaxCodeDao productTaxCodeDao;

	@Override
	public String lookupTaxCode(final String productCode, final String taxArea)
	{
		return productTaxCodeDao.getTaxCodeForProductAndAreaDirect(productCode, taxArea);
	}

	@Override
	public Map<String, String> lookupTaxCodes(final Collection<String> productCodes, final String taxArea)
	{
		if (CollectionUtils.isNotEmpty(productCodes))
		{
			final List<List<String>> rows = productTaxCodeDao.getTaxCodesForProductsAndAreaDirect(productCodes, taxArea);

			if (CollectionUtils.isNotEmpty(rows))
			{
				final Map<String, String> ret = new HashMap<String, String>(rows.size() * 2);
				for (final List<String> row : rows)
				{
					final String productCode = row.get(0);
					final String taxCode = row.get(1);

					ret.put(productCode, taxCode);
				}
				return ret;
			}
		}
		return Collections.emptyMap();
	}

	@Override
	public ProductTaxCodeModel getTaxCodeForProductAndArea(final String productCode, final String taxArea)
	{
		return productTaxCodeDao.getTaxCodeForProductAndArea(productCode, taxArea);
	}

	@Override
	public Collection<ProductTaxCodeModel> getTaxCodesForProduct(final String productCode)
	{
		return productTaxCodeDao.getTaxCodesForProduct(productCode);
	}

	@Required
	public void setProductTaxCodeDao(final ProductTaxCodeDao productTaxCodeDao)
	{
		this.productTaxCodeDao = productTaxCodeDao;
	}
}
