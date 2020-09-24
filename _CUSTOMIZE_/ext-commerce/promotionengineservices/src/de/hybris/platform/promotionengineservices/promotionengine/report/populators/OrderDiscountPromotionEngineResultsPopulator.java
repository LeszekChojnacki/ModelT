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
package de.hybris.platform.promotionengineservices.promotionengine.report.populators;

import java.util.List;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderLevelPromotionEngineResults;
import de.hybris.platform.util.DiscountValue;


/**
 * Populator responsible for populating data from {@link AbstractOrderModel} to {@link OrderLevelPromotionEngineResults}
 */
public class OrderDiscountPromotionEngineResultsPopulator
		extends AbstractPromotionEngineResultPopulator<AbstractOrderModel, OrderLevelPromotionEngineResults>
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(final AbstractOrderModel source, final OrderLevelPromotionEngineResults target)
	{
		super.populate(source, target);
		target.setOrder(source);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<DiscountValue> getDiscountValues(final AbstractOrderModel source)
	{
		return source.getGlobalDiscountValues();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractOrderModel getOrder(final AbstractOrderModel source)
	{
		return source;
	}
}
