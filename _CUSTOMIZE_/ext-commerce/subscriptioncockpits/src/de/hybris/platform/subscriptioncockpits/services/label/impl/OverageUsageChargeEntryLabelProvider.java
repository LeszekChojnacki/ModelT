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
package de.hybris.platform.subscriptioncockpits.services.label.impl;

import de.hybris.platform.subscriptionservices.model.OverageUsageChargeEntryModel;
import de.hybris.platform.subscriptionservices.model.VolumeUsageChargeModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Label provider implementation for {@link OverageUsageChargeEntryModel}
 */
public class OverageUsageChargeEntryLabelProvider extends AbstractSubscriptionModelLabelProvider<OverageUsageChargeEntryModel>
{

	@Override
	protected String getItemLabel(final OverageUsageChargeEntryModel model)
	{
		String currency = "<null>";
		String price = "<null>";

		if (model == null)
		{
			return getL10NService().getLocalizedString("cockpit.usagechargeentry.overage.name", new Object[]
			{ "<null>", "<null>" });
		}

		if (model.getPrice() != null)
		{
			final NumberFormat decimalFormat = DecimalFormat.getInstance();
			price = decimalFormat.format(model.getPrice().doubleValue());
		}

		if (model.getCurrency() != null)
		{
			currency = model.getCurrency().getSymbol();
		}

		if (model.getUsageCharge() instanceof VolumeUsageChargeModel)
		{
			return getL10NService().getLocalizedString("cockpit.usagechargeentry.overage.each.name", new Object[]
			{ currency, price });
		}

		return getL10NService().getLocalizedString("cockpit.usagechargeentry.overage.name", new Object[]
		{ currency, price });
	}
}