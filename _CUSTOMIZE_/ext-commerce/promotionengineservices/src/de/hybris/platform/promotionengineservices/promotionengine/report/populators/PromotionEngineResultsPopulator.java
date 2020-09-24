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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderEntryLevelPromotionEngineResults;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderLevelPromotionEngineResults;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResults;
import de.hybris.platform.servicelayer.dto.converter.Converter;


/**
 * Populator responsible for populating data from {@link AbstractOrderModel} to {@link PromotionEngineResults}
 */
public class PromotionEngineResultsPopulator implements Populator<AbstractOrderModel, PromotionEngineResults>
{
	private Converter<AbstractOrderModel, OrderLevelPromotionEngineResults> orderDiscountPromotionsConverter;
	private Converter<AbstractOrderEntryModel, OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotionConverter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(final AbstractOrderModel source, final PromotionEngineResults target)
	{
		checkArgument(nonNull(source),"Source cannot be null");
		checkArgument(nonNull(target),"Target cannot be null");
		final OrderLevelPromotionEngineResults orderDiscountPromotions = getOrderDiscountPromotionsConverter().convert(source);
		target.setOrderLevelPromotionEngineResults(orderDiscountPromotions);

		final List<OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotions = getOrderEntryDiscountPromotionConverter().convertAll(source.getEntries());
		target.setOrderEntryLevelPromotionEngineResults(orderEntryDiscountPromotions);
	}

	protected Converter<AbstractOrderModel, OrderLevelPromotionEngineResults> getOrderDiscountPromotionsConverter()
	{
		return orderDiscountPromotionsConverter;
	}

	@Required
	public void setOrderDiscountPromotionsConverter(
			final Converter<AbstractOrderModel, OrderLevelPromotionEngineResults> orderDiscountPromotionsConverter)
	{
		this.orderDiscountPromotionsConverter = orderDiscountPromotionsConverter;
	}

	protected Converter<AbstractOrderEntryModel, OrderEntryLevelPromotionEngineResults> getOrderEntryDiscountPromotionConverter()
	{
		return orderEntryDiscountPromotionConverter;
	}

	@Required
	public void setOrderEntryDiscountPromotionConverter(
			final Converter<AbstractOrderEntryModel, OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotionConverter)
	{
		this.orderEntryDiscountPromotionConverter = orderEntryDiscountPromotionConverter;
	}

}
