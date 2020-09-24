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

import de.hybris.platform.subscriptionservices.model.RecurringChargeEntryModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

public class RecurringChargeEntryLabelProvider extends AbstractSubscriptionModelLabelProvider<RecurringChargeEntryModel> {

	@Override
	protected String getItemLabel(final RecurringChargeEntryModel model) {
		String price = "<null>";
		String currency = "<null>";
		String cycleStart = "<null>";
		String cycleEnd = StringUtils.EMPTY;
		String label;

		if (model == null) {
			return getL10NService().getLocalizedString("cockpit.recurringchargeentryperiod.intervall.name",
			        new Object[] { "<null>", "<null>", "<null>", "<null>" });
		}

		if (model.getPrice() != null) {
			final NumberFormat decimalFormat = DecimalFormat.getInstance();
			price = decimalFormat.format(model.getPrice().doubleValue());
		}

		if (model.getCurrency() != null) {
			currency = model.getCurrency().getSymbol();
		}

		if (model.getCycleStart() != null) {
			cycleStart = Integer.toString(model.getCycleStart());
		}

		if (model.getCycleEnd() != null) {
			cycleEnd = Integer.toString(model.getCycleEnd());
		}

		if (StringUtils.isEmpty(cycleEnd)) {
			label = getL10NService().getLocalizedString("cockpit.recurringchargeentryperiod.intervall.openend.name",
			        new Object[] { cycleStart, currency, price });
		} else {
			label = getL10NService().getLocalizedString("cockpit.recurringchargeentryperiod.intervall.name",
			        new Object[] { cycleStart, cycleEnd, currency, price });
		}

		return label;
	}
}