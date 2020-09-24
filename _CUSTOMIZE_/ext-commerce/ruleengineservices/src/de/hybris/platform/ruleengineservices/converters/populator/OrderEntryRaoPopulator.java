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
package de.hybris.platform.ruleengineservices.converters.populator;

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ruleengineservices.rao.DiscountValueRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.util.CurrencyUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.util.DiscountValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;


/**
 * Converter implementation for {@link AbstractOrderEntryModel} as source and {@link OrderEntryRAO} as target type.
 */
public class OrderEntryRaoPopulator implements Populator<AbstractOrderEntryModel, OrderEntryRAO>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderEntryRaoPopulator.class);
	private Converter<ProductModel, ProductRAO> productConverter;
	private Converter<DiscountValue, DiscountValueRAO> discountValueConverter;
	private CurrencyUtils currencyUtils;

	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryRAO target)
	{
		if (nonNull(source.getProduct()))
		{
			target.setProduct(getProductConverter().convert(source.getProduct()));
		}
		if (nonNull(source.getQuantity()))
		{
			target.setQuantity(source.getQuantity().intValue());
		}
		final Double basePrice = source.getBasePrice();
		if (nonNull(basePrice))
		{
			target.setBasePrice(BigDecimal.valueOf(basePrice.doubleValue()));
			target.setPrice(target.getBasePrice());

			final AbstractOrderModel order = source.getOrder();
			if (nonNull(order) && nonNull(order.getCurrency()))
			{
				target.setCurrencyIsoCode(order.getCurrency().getIsocode());
			}
			else
			{
				LOGGER.warn("Order is null or the order currency is not set correctly");
			}
		}
		if (nonNull(source.getEntryNumber()))
		{
			target.setEntryNumber(source.getEntryNumber());
		}
		if (isNotEmpty(source.getDiscountValues()))
		{
			source.getDiscountValues().forEach(discountValue -> applyDiscount(target, discountValue));

			target.setDiscountValues(Converters.convertAll(source.getDiscountValues(), getDiscountValueConverter()));
		}
		target.setGiveAway(toBoolean(source.getGiveAway()));
	}

	protected void applyDiscount(final OrderEntryRAO target, final DiscountValue discountValue)
	{
		final BigDecimal discountAmount = BigDecimal.valueOf(
				discountValue.apply(1, target.getBasePrice().doubleValue(),
				getCurrencyUtils().getDigitsOfCurrencyOrDefault(target.getCurrencyIsoCode()),
				target.getCurrencyIsoCode()).getAppliedValue());

		target.setPrice(target.getPrice().subtract(discountAmount));
	}

	protected Converter<ProductModel, ProductRAO> getProductConverter()
	{
		return productConverter;
	}

	@Required
	public void setProductConverter(final Converter<ProductModel, ProductRAO> productConverter)
	{
		this.productConverter = productConverter;
	}

	protected Converter<DiscountValue, DiscountValueRAO> getDiscountValueConverter()
	{
		return discountValueConverter;
	}

	@Required
	public void setDiscountValueConverter(final Converter<DiscountValue, DiscountValueRAO> discountValueConverter)
	{
		this.discountValueConverter = discountValueConverter;
	}

	protected CurrencyUtils getCurrencyUtils()
	{
		return currencyUtils;
	}

	@Required
	public void setCurrencyUtils(final CurrencyUtils currencyUtils)
	{
		this.currencyUtils = currencyUtils;
	}
}
