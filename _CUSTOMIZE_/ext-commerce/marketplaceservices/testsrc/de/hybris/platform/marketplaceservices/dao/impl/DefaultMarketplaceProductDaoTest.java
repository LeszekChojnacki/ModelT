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
package de.hybris.platform.marketplaceservices.dao.impl;

import static org.junit.Assert.assertEquals;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;


/**
 * Integration test for {@link DefaultMarketplaceProductDao}
 */
@IntegrationTest
public class DefaultMarketplaceProductDaoTest extends ServicelayerTransactionalTest
{

	@Resource(name = "marketplaceProductDao")
	private DefaultMarketplaceProductDao productDao;

	private final String VENDOR_CODE = "electro";

	@Before
	public void prepare() throws ImpExException
	{
		importCsv("/marketplaceservices/test/testProduct.csv", "utf-8");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindAllProductByVendorWithNullParam()
	{
		productDao.findAllProductByVendor(null);
	}

	@Test
	public void testFindAllProductByVendorWithVendorCode()
	{
		final List<ProductModel> productList = productDao.findAllProductByVendor(VENDOR_CODE);
		assertEquals(4, productList.size());

	}
}
