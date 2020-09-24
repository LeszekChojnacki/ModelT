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
package de.hybris.platform.promotionengineservices.validators;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruleengine.enums.RuleType;


/**
 * Validator checks if a promotion is applicable in the catalogVersion context
 */
public interface RuleBasedPromotionsContextValidator
{
	/**
	 * finds rule based promotions for the given catalog versions and rule type
	 *
	 * @param ruleBasedPromotionModel
	 *           the promotion that is a validation subject
	 * @param catalogVersion
	 *           the catalog version to look up rule based promotions for
	 * @param ruleType
	 *           filters to return only mappings which rules module is of the given rule type
	 * @return decision - true if applicable otherwise false
	 */
	boolean isApplicable(RuleBasedPromotionModel ruleBasedPromotionModel, CatalogVersionModel catalogVersion, RuleType ruleType);
}
