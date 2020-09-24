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
package de.hybris.platform.customersupportbackoffice.labelproviders;

import de.hybris.platform.core.model.user.AddressModel;

import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.labels.LabelProvider;


public class AddressLabelProvider implements LabelProvider<AddressModel>
{

	public static final String DASH = " - ";

	@Override
	public String getLabel(final AddressModel address)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(address.getLine1());
		if (StringUtils.isNotEmpty(address.getLine2()))
		{
			stringBuilder.append(DASH).append(address.getLine2());
		}

		stringBuilder.append(DASH).append(address.getTown());

		if (address.getRegion() != null && StringUtils.isNotEmpty(address.getRegion().getName()))
		{
			stringBuilder.append(DASH).append(address.getRegion().getName());
		}

		stringBuilder.append(DASH).append(address.getPostalcode());

		if (address.getCountry() != null)
		{
			stringBuilder.append(DASH).append(address.getCountry().getName());
		}
		return stringBuilder.toString();
	}

	@Override
	public String getDescription(final AddressModel address)
	{
		return getLabel(address);
	}

	@Override
	public String getIconPath(final AddressModel address)
	{
		return null;
	}
}
