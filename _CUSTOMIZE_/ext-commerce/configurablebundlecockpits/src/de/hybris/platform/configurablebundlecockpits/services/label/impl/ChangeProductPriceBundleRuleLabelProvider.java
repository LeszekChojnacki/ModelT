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

package de.hybris.platform.configurablebundlecockpits.services.label.impl;

import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.configurablebundleservices.model.ChangeProductPriceBundleRuleModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Label provider implementation for {@link ChangeProductPriceBundleRuleModel}
 */
public class ChangeProductPriceBundleRuleLabelProvider extends AbstractBundleRuleLabelProvider<ChangeProductPriceBundleRuleModel>
{
	private L10NService l10NService;

	@Override
	protected String getItemLabel(final ChangeProductPriceBundleRuleModel changeProductPriceRule)
	{

		return getL10NService().getLocalizedString(
				"cockpit.bundle.changepricerule",
				new Object[]
				{ getProductNames(changeProductPriceRule.getTargetProducts()),
						getProductNames(changeProductPriceRule.getConditionalProducts()) });
	}

	@Required
	public void setL10NService(final L10NService l10NService)
	{
		this.l10NService = l10NService;
	}

	protected L10NService getL10NService()
	{
		return l10NService;
	}
}
