/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.dtos;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;


/**
 * DTO used to collect all user input to search for a specific ATP formula.
 */
public class AtpFormDto
{

	private ProductModel product;
	private BaseStoreModel baseStore;
	private PointOfServiceModel pointOfService;

	public AtpFormDto(final ProductModel product, final BaseStoreModel baseStore, final PointOfServiceModel pointOfService)
	{
		this.product = product;
		this.baseStore = baseStore;
		this.pointOfService = pointOfService;
	}

	public ProductModel getProduct()
	{
		return product;
	}

	public void setProduct(final ProductModel product)
	{
		this.product = product;
	}

	public BaseStoreModel getBaseStore()
	{
		return baseStore;
	}

	public void setBaseStore(final BaseStoreModel baseStore)
	{
		this.baseStore = baseStore;
	}

	public PointOfServiceModel getPointOfService()
	{
		return pointOfService;
	}

	public void setPointOfService(final PointOfServiceModel pointOfService)
	{
		this.pointOfService = pointOfService;
	}
}
