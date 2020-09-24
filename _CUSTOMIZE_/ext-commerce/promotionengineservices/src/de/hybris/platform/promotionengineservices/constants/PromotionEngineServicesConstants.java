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
package de.hybris.platform.promotionengineservices.constants;

import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;


/**
 * Global class for all PromotionEngineServices constants. You can add global constants for your extension into this
 * class.
 */
public final class PromotionEngineServicesConstants extends GeneratedPromotionEngineServicesConstants // NOSONAR
{
	public static final String EXTENSIONNAME = "promotionengineservices";

	/**
	 * {@link RuleBasedPromotionModel}.code set to {@link AbstractRuleEngineRuleModel}.code +
	 * RULE_BASED_PROMOTION_MODEL_CODE_POSTFIX
	 */
	public static final String DEFAULT_PROMOTION_GROUP_IDENTIFIER = "default";

	private PromotionEngineServicesConstants()
	{
		//empty to avoid instantiating this constant class
	}

	// implement here constants used by this extension
	public enum PromotionCertainty
	{
		FIRED(1.0F), POTENTIAL(0.5F);
		private Float certainty;
		private double epsilon = 0.000000001F;

		PromotionCertainty(final float certainty)
		{
			this.certainty = Float.valueOf(certainty);
		}

		public boolean around(final Float value)
		{
			return Math.abs(value.doubleValue() - certainty.doubleValue()) < epsilon;
		}

		public Float value()
		{
			return certainty;
		}
	}
}
