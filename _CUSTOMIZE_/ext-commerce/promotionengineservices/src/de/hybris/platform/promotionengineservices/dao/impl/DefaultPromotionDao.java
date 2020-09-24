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
package de.hybris.platform.promotionengineservices.dao.impl;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.promotionengineservices.dao.PromotionDao;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotions.constants.PromotionsConstants;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.PromotionGroupModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of the {@link PromotionDao}.
 */
public class DefaultPromotionDao extends AbstractItemDao implements PromotionDao
{
	@Override
	public AbstractPromotionModel findPromotionByCode(final String code)
	{
		final Map<String, String> params = new HashMap<>();
		params.put("code", code);

		final String query = "SELECT {" + Item.PK + "} " + "FROM   {" + AbstractPromotionModel._TYPECODE + "} " + "WHERE  {"
				+ "code" + "} = ?code";
		final SearchResult<RuleBasedPromotionModel> searchResult = getFlexibleSearchService().search(query, params);
		if (searchResult.getCount() != 0)
		{
			return searchResult.getResult().get(0);
		}
		return null;
	}

	@Override
	public PromotionGroupModel findPromotionGroupByCode(final String identifier)
	{
		final Map<String, String> params = new HashMap<>();
		params.put("identifier", identifier);

		final String query = "SELECT {" + Item.PK + "} FROM {" + PromotionGroupModel._TYPECODE + "} WHERE  {"
				+ PromotionGroupModel.IDENTIFIER + "} = ?identifier";
		final SearchResult<PromotionGroupModel> searchResult = getFlexibleSearchService().search(query, params);
		if (searchResult.getCount() != 0)
		{
			return searchResult.getResult().get(0);
		}
		return null;
	}

	@Override
	public PromotionGroupModel findDefaultPromotionGroup()
	{
		return findPromotionGroupByCode(PromotionsConstants.DEFAULT_PROMOTION_GROUP_IDENTIFIER);
	}

}
