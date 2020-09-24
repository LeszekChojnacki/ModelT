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
package de.hybris.platform.promotionengineservices.promotionengine.report.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import com.google.common.collect.Maps;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.promotionengineservices.model.AbstractRuleBasedPromotionActionModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.dao.RuleBasedPromotionActionDao;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.DiscountValue;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;


/**
 * Default implementation of {@link RuleBasedPromotionActionDao}
 */
public class DefaultRuleBasedPromotionActionDao extends DefaultGenericDao<AbstractRuleBasedPromotionActionModel>
		implements RuleBasedPromotionActionDao
{

	private static final String FIND_PROMOTION_ACTIONS_QUERY = "SELECT {P:" + AbstractRuleBasedPromotionActionModel.PK + "} FROM {"
			+ AbstractOrderModel._TYPECODE + " AS O JOIN " + PromotionResultModel._TYPECODE + " AS PR "
			+ "ON {O:" + OrderModel.PK + "}={PR:" + PromotionResultModel.ORDER + "} "
			+ "JOIN " + AbstractRuleBasedPromotionActionModel._TYPECODE + " AS P "
			+ "ON {P:" + AbstractRuleBasedPromotionActionModel.PROMOTIONRESULT + "}={PR:" + PromotionResultModel.PK + "}} "
			+ "WHERE {O:" + OrderModel.PK + "}=?order AND "
			+ "{P:" + AbstractRuleBasedPromotionActionModel.GUID + "} IN (?guids)";

	public DefaultRuleBasedPromotionActionDao()
	{
		super(AbstractRuleBasedPromotionActionModel._TYPECODE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractRuleBasedPromotionActionModel findRuleBasedPromotionByGuid(final String guid)
	{
		final Map<String, Object> params = ImmutableMap.of(AbstractRuleBasedPromotionActionModel.GUID, guid);
		final List<AbstractRuleBasedPromotionActionModel> results = find(params);
		final String unknownIdException = "Cannot find  AbstractRuleBasedPromotionActionModel by guid " + guid;
		final String ambiguousIdException = "Found " + results.size() + " AbstractRuleBasedPromotionAction items for guid";
		ServicesUtil.validateIfSingleResult(results, unknownIdException, ambiguousIdException);
		return results.iterator().next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AbstractRuleBasedPromotionActionModel> findRuleBasedPromotions(
			final AbstractOrderModel order, final Collection<DiscountValue> discountValues)
	{
		checkArgument(nonNull(order),"Order cannot be null");
		checkArgument(nonNull(discountValues),"Discount values cannot be null");
		if (discountValues.isEmpty()) { // NOSONAR
			return Collections.emptyList();
		}

		final List<String> guids = discountValues.stream().map(DiscountValue::getCode).collect(Collectors.toList());
		final Map<String, Object> queryParameters = Maps.newHashMap();
		queryParameters.put("order", order.getPk());
		queryParameters.put("guids", guids);

		return getFlexibleSearchService().<AbstractRuleBasedPromotionActionModel>search(FIND_PROMOTION_ACTIONS_QUERY, queryParameters).getResult();
	}
}
