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
package de.hybris.platform.basecommerce.model;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;


/**
 * Dynamic attribute handler for the Address.line1 attribute. The Address.line1 attribute is directly mapped into the
 * Address.streetname attribute.
 * 
 * Hybris payment extension maps the Address.streetname into BillingInfo.street1, and the Address.streetnumber into
 * BillingInfo.street2. NOTE that in Germany the street name is written before the street number. In order to avoid
 * confusion we use Address.line1 and Address.line2 instead, but we retain the same mapping as hybris.
 */
public class AddressLine1Attribute extends AbstractDynamicAttributeHandler<String, AddressModel>
{
	@Override
	public String get(final AddressModel addressModel)
	{
		if (addressModel == null)
		{
			throw new IllegalArgumentException("address model is required");
		}

		return addressModel.getStreetname();
	}

	@Override
	public void set(final AddressModel addressModel, final String value)
	{
		if (addressModel != null)
		{
			addressModel.setStreetname(value);
		}
	}
}
