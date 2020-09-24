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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.converters.Converters;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.order.price.DiscountModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountValueRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.PaymentModeRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Converter implementation for {@link AbstractOrderModel} as source and {@link AbstractOrderRAO} as target type.
 */
public abstract class AbstractOrderRaoPopulator<T extends AbstractOrderModel, P extends AbstractOrderRAO>
		implements Populator<T, P>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOrderRaoPopulator.class);

	private Converter<DiscountModel, DiscountValueRAO> discountConverter;
	private Converter<AbstractOrderEntryModel, OrderEntryRAO> entryConverter;
	private Converter<UserModel, UserRAO> userConverter;
	private Converter<PaymentModeModel, PaymentModeRAO> paymentModeConverter;

	@Override
	public void populate(final T source, final P target)
	{
		if (target.getActions() == null)
		{
			target.setActions(new LinkedHashSet<>());
		}
		target.setCode(source.getCode());
		if (source.getCurrency() != null)
		{
			target.setCurrencyIsoCode(source.getCurrency().getIsocode());
		}

		target.setTotal(
				isNull(source.getTotalPrice()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getTotalPrice().doubleValue()));
		target.setSubTotal(isNull(source.getSubtotal()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getSubtotal().doubleValue()));
		target.setDeliveryCost(
				isNull(source.getDeliveryCost()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getDeliveryCost().doubleValue()));
		target.setPaymentCost(
				isNull(source.getPaymentCost()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getPaymentCost().doubleValue()));
		// convert entries and set order on them first, then add them to a LinkedHashSet
		// (the ordering is important, as 'order' is used in the hashcode/equals method and
		// therefore must not be changed after inserting them in the map-backed hashset)
		if (isNotEmpty(source.getEntries()))
		{
			final List<OrderEntryRAO> list = Converters.convertAll(source.getEntries(), getEntryConverter());
			list.forEach(entry -> entry.setOrder(target));
			target.setEntries(new LinkedHashSet<>(list));
		}
		else
		{
			LOGGER.debug("Order entry list is empty, skipping the conversion");
		}
		if (isNotEmpty(source.getDiscounts()))
		{
			target.setDiscountValues(Converters.convertAll(source.getDiscounts(), getDiscountConverter()));
		}
		else
		{
			LOGGER.debug("Order discount list is empty, skipping the conversion");
		}

		convertAndSetUser(target, source.getUser());
		convertAndSetPaymentMode(target, source.getPaymentMode());
	}

	protected void convertAndSetUser(final P target, final UserModel user)
	{
		if (nonNull(user))
		{
			target.setUser(getUserConverter().convert(user));
		}
	}

	protected void convertAndSetPaymentMode(final P target, final PaymentModeModel paymentMode)
	{
		if (nonNull(paymentMode))
		{
			target.setPaymentMode(getPaymentModeConverter().convert(paymentMode));
		}
	}

	protected Converter<DiscountModel, DiscountValueRAO> getDiscountConverter()
	{
		return discountConverter;
	}

	@Required
	public void setDiscountConverter(final Converter<DiscountModel, DiscountValueRAO> discountConverter)
	{
		this.discountConverter = discountConverter;
	}

	protected Converter<AbstractOrderEntryModel, OrderEntryRAO> getEntryConverter()
	{
		return entryConverter;
	}

	@Required
	public void setEntryConverter(final Converter<AbstractOrderEntryModel, OrderEntryRAO> entryConverter)
	{
		this.entryConverter = entryConverter;
	}

	protected Converter<UserModel, UserRAO> getUserConverter()
	{
		return userConverter;
	}

	@Required
	public void setUserConverter(final Converter<UserModel, UserRAO> userConverter)
	{
		this.userConverter = userConverter;
	}

	protected Converter<PaymentModeModel, PaymentModeRAO> getPaymentModeConverter()
	{
		return paymentModeConverter;
	}

	@Required
	public void setPaymentModeConverter(final Converter<PaymentModeModel, PaymentModeRAO> paymentModeConverter)
	{
		this.paymentModeConverter = paymentModeConverter;
	}

}
