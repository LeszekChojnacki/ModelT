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
package de.hybris.platform.promotionengineservices.dao;

import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;


/**
 * Data Access Object for looking up items related to {@link AbstractPromotionModel}.
 */
public interface PromotionDao
{
	/**
	 * Find a stored item of AbstractPromotionModel type by it code (even if has immutableKeyHash != null). Or null if it
	 * is not found.
	 */
	AbstractPromotionModel findPromotionByCode(final String code);

	/**
	 * Finds PromotionGroup objects from the code identifier
	 *
	 * @param code
	 *           identifier for the promotion group
	 * @return PromotionGroupModel matching the code
	 */
	PromotionGroupModel findPromotionGroupByCode(String code);

	/**
	 * Finds default PromotionGroup object
	 *
	 * @return default PromotionGroupModel
	 */
	PromotionGroupModel findDefaultPromotionGroup();

}
