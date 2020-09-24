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

import de.hybris.platform.subscriptionservices.model.PerUnitUsageChargeModel;
import de.hybris.platform.subscriptionservices.model.TierUsageChargeEntryModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Label provider implementation for {@link TierUsageChargeEntryModel}
 */
public class TierUsageChargeEntryLabelProvider extends AbstractSubscriptionModelLabelProvider<TierUsageChargeEntryModel>
{

	@Override
	protected String getItemLabel(final TierUsageChargeEntryModel model)
	{
		if (model == null)
		{
			return getL10NService().getLocalizedString("cockpit.usagechargeentry.tier.name", new Object[]
			{ "<null>", "<null>", "<null>", "<null>", "<null>" });
		}

		final String currency = getCurrency(model);
		final String usageUnit = getUsageUnit(model);
		final String tierStart = getTierStart(model);
		final String tierEnd = getTierEnd(model);
		final String price = getPrice(model);

		if (model.getUsageCharge() instanceof PerUnitUsageChargeModel)
		{
			return getL10NService().getLocalizedString("cockpit.usagechargeentry.tier.each.name", new Object[]
			{ tierStart, tierEnd, usageUnit, currency, price });
		}

		return getL10NService().getLocalizedString("cockpit.usagechargeentry.tier.name", new Object[]
		{ tierStart, tierEnd, usageUnit, currency, price });
	}

	protected String getCurrency(final TierUsageChargeEntryModel model)
	{
		return model.getCurrency() == null ? "<null>" : model.getCurrency().getSymbol();
	}

	protected String getUsageUnit(final TierUsageChargeEntryModel model)
	{
		return model.getUsageCharge() == null ? "<null>" : model.getUsageCharge().getUsageUnit().getNamePlural();
	}

	protected String getTierStart(final TierUsageChargeEntryModel model)
	{
		return model.getTierStart() == null ? "<null>" : model.getTierStart().toString();
	}

	protected String getTierEnd(final TierUsageChargeEntryModel model)
	{
		return model.getTierEnd() == null ? "<null>" : model.getTierEnd().toString();
	}

	protected String getPrice(final TierUsageChargeEntryModel model)
	{
		if (model.getPrice() != null)
		{
			final NumberFormat decimalFormat = DecimalFormat.getInstance();
			return decimalFormat.format(model.getPrice().doubleValue());
		}
		return "<null>";
	}
}
