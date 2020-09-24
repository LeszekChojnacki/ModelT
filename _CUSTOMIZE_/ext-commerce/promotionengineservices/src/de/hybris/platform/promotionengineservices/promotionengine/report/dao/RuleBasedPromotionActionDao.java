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
package de.hybris.platform.promotionengineservices.promotionengine.report.dao;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.util.DiscountValue;

import java.util.Collection;
import java.util.List;


/**
 * Offers possibility to query for {@link AbstractRuleBasedPromotionActionModel}
 */
public interface RuleBasedPromotionActionDao extends GenericDao<AbstractRuleBasedPromotionActionModel>
{
	/**
	 * Provides {@link AbstractRuleBasedPromotionActionModel} selected by guid
	 * @param guid
	 * @return rule based promotion indentified by guid
	 * @throws {@link ModelNotFoundException} if action not found
	 * @throws {@link AmbiguousIdentifierException} if more than one action with guid found
	 * @deprecated since 18.08 (we're using {@link #findRuleBasedPromotions} instead)
	 */
	@Deprecated
	AbstractRuleBasedPromotionActionModel findRuleBasedPromotionByGuid(String guid);

	/**
	 * Provides list of {@link AbstractRuleBasedPromotionActionModel} linked to the order selected by discount values
	 * @param order
	 * @param discountValues
	 */
	List<AbstractRuleBasedPromotionActionModel> findRuleBasedPromotions(
			final AbstractOrderModel order, final Collection<DiscountValue> discountValues);
}
