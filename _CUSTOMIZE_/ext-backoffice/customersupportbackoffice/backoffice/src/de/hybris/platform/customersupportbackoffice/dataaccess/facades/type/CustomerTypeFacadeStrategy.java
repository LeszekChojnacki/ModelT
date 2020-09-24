/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.dataaccess.facades.type;

import com.google.common.base.Preconditions;
import com.hybris.backoffice.cockpitng.dataaccess.facades.type.DefaultPlatformTypeFacadeStrategy;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.CollectionDataType;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.CustomerModel;


/**
 *
 * Facade strategy responsible for extending the Customer with extra parameter
 */
public class CustomerTypeFacadeStrategy extends DefaultPlatformTypeFacadeStrategy
{
	private static final String SAVED_CARTS_ATTR = "savedCarts";
	private static final String SAVED_CARTS_LIST = "savedCartsList";
	private DataType customerDataType;

	@Override
	public boolean canHandle(final String typeCode)
	{
		return CustomerModel._TYPECODE.equals(typeCode);
	}

	@Override
	public DataType load(final String code, final Context ctx) throws TypeNotFoundException
	{
		Preconditions.checkArgument(code != null, "code is null");

		if (customerDataType == null)
		{
			//load standard attributes
			final DataType orginalCustomerType = super.load(code, ctx);

			//build the new attribute to add to the Customer
			final CollectionDataType.CollectionBuilder savedCartsListBuilder = new CollectionDataType.CollectionBuilder(
					SAVED_CARTS_LIST);

			savedCartsListBuilder.valueType(orginalCustomerType).supertype(ItemModel._TYPECODE);

			final DataAttribute.Builder attributeBuilder = new DataAttribute.Builder(SAVED_CARTS_ATTR).primitive(false)
					.ordered(true);
			attributeBuilder.searchable(false).localized(false).unique(false).writable(true).mandatory(false)
					.valueType(savedCartsListBuilder.build());

			final DataType.Builder finalCustomerTypeBuilder = new DataType.Builder(code);

			for (final DataAttribute dataAttr : orginalCustomerType.getAttributes())
			{
				finalCustomerTypeBuilder.attribute(dataAttr);
			}

			finalCustomerTypeBuilder.labels(orginalCustomerType.getAllLabels());
			finalCustomerTypeBuilder.clazz(orginalCustomerType.getClazz());
			finalCustomerTypeBuilder.supertype(orginalCustomerType.getSuperType());
			finalCustomerTypeBuilder.type(orginalCustomerType.getType());

			//build the attribute after setting it up
			customerDataType = finalCustomerTypeBuilder.attribute(attributeBuilder.build()).build();

		}
		return customerDataType;
	}
}
