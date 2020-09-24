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

import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;
import java.util.Collection;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderEntryLevelPromotionEngineResults;
import de.hybris.platform.util.DiscountValue;

/**
 * Populator responsible for populating data from {@link AbstractOrderEntryModel} to {@link OrderEntryLevelPromotionEngineResults}
 */
public class OrderEntryDiscountPromotionEngineResultsPopulator
		extends AbstractPromotionEngineResultPopulator<AbstractOrderEntryModel, OrderEntryLevelPromotionEngineResults>
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryLevelPromotionEngineResults target)
	{
		super.populate(source, target);
		target.setOrderEntry(source);
		target.setEstimatedAdjustedBasePrice(estimateAdjustedBasePriceTotalPrice(source));
		target.setTotalPrice(calculateTotalPrice(source));
	}

	protected BigDecimal estimateAdjustedBasePriceTotalPrice(final AbstractOrderEntryModel source)
	{
		if (source.getQuantity().longValue() == 0)
		{
			return BigDecimal.ZERO;
		}
		return valueOf(source.getTotalPrice().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_UP)
				.divide(valueOf(source.getQuantity().longValue()), BigDecimal.ROUND_HALF_UP);
	}

	protected BigDecimal calculateTotalPrice(final AbstractOrderEntryModel source)
	{
		return valueOf(source.getBasePrice().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
				.multiply(valueOf(source.getQuantity().longValue()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<DiscountValue> getDiscountValues(final AbstractOrderEntryModel source)
	{
		return source.getDiscountValues();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractOrderModel getOrder(final AbstractOrderEntryModel source)
	{
		return source.getOrder();
	}
}
