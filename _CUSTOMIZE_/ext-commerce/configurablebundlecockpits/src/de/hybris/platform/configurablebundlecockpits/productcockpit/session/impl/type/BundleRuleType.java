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

package de.hybris.platform.configurablebundlecockpits.productcockpit.session.impl.type;

public enum BundleRuleType
{

	CHANGE_PRODUCT_PRICE_BUNDLE_RULE("BundlePriceSearchBrowserModel", "ChangeProductPriceBundleRule.bundleTemplate",
			"ChangeProductPriceBundleRule"), 
	DISABLE_PRODUCT_BUNDLE_RULE("BundleDisabledRuleSearchBrowserModel","DisableProductBundleRule.bundleTemplate",
			"DisableProductBundleRule");

	private String templateBundleName;
	private String modelName;
	private String ruleName;

	BundleRuleType(final String modelName, final String templateBundleName, final String ruleName)
	{
		this.templateBundleName = templateBundleName;
		this.modelName = modelName;
		this.ruleName = ruleName;
	}

	public String getTemplateBundleName()
	{
		return templateBundleName;
	}

	public String getModelName()
	{
		return modelName;
	}

	public String getRuleName()
	{
		return ruleName;
	}

	public static BundleRuleType fromValue(final String v)
	{
		for (final BundleRuleType type : values())
		{
			if (type.getRuleName().equals(v))
			{
				return type;
			}
		}
		return null;
	}
}
