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
package de.hybris.platform.marketplaceservices.jalo.restrictions;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.ordersplitting.jalo.Vendor;
import de.hybris.platform.util.localization.Localization;

public class VendorRestriction extends GeneratedVendorRestriction
{

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		// business code placed here will be executed before the item is created
		// then create the item
		final Item item = super.createItem(ctx, type, allAttributes);
		// business code placed here will be executed after the item was created
		// and return the item
		return item;
	}

	@Override
	public String getDescription(final SessionContext ctx)
	{
		final Vendor vendor = this.getVendor();
		final StringBuilder result = new StringBuilder();
		if (vendor != null)
		{
			final String localizedString = Localization.getLocalizedString("type.CMSVendorRestriction.description.text");
			result.append(localizedString == null ? "Page only applies on vendor" : localizedString);
			result.append(" ").append(vendor.getName(ctx)).append(" (").append(vendor.getCode()).append(");");
		}
		return result.toString();
	}
}
