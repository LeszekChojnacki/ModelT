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
package de.hybris.platform.ruleengineservices.rao.providers.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ruleengineservices.calculation.RuleEngineCalculationService;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.PaymentModeRAO;
import de.hybris.platform.ruleengineservices.rao.ProductConsumedRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.UserGroupRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.ruleengineservices.rao.providers.RAOFactsExtractor;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * The default implementation for the cart {@code RAO} which allows customization of how and which cart-based
 * {@code RAO}s will be created based on the defined options. Specializations of this class can overwrite the
 * {@link #expandRAO(CartRAO, Collection)} method and define its own {@code option} strings.
 */
public class DefaultCartRAOProvider extends AbstractExpandedRAOProvider<AbstractOrderModel, CartRAO>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCartRAOProvider.class);

	public static final String INCLUDE_CART = "INCLUDE_CART";
	public static final String EXPAND_ENTRIES = "EXPAND_ENTRIES";
	public static final String EXPAND_PRODUCTS = "EXPAND_PRODUCTS";
	public static final String EXPAND_DISCOUNTS = "EXPAND_DISCOUNTS";
	public static final String AVAILABLE_DELIVERY_MODES = "EXPAND_AVAILABLE_DELIVERY_MODES";
	public static final String EXPAND_CATEGORIES = "EXPAND_CATEGORIES";
	public static final String EXPAND_USERS = "EXPAND_USERS";
	public static final String EXPAND_PAYMENT_MODE = "EXPAND_PAYMENT_MODE";

	private Converter<AbstractOrderModel, CartRAO> cartRaoConverter;
	private RuleEngineCalculationService ruleEngineCalculationService;

	public DefaultCartRAOProvider()
	{
		validOptions = Arrays
				.asList(INCLUDE_CART, EXPAND_ENTRIES, EXPAND_PRODUCTS, EXPAND_CATEGORIES, EXPAND_USERS,
						EXPAND_PAYMENT_MODE, AVAILABLE_DELIVERY_MODES, EXPAND_DISCOUNTS);
		defaultOptions = Arrays
				.asList(INCLUDE_CART, EXPAND_ENTRIES, EXPAND_PRODUCTS, EXPAND_CATEGORIES, EXPAND_USERS,
						EXPAND_PAYMENT_MODE, AVAILABLE_DELIVERY_MODES);
		minOptions = Collections.singletonList(INCLUDE_CART);
	}

	@Override
	protected CartRAO createRAO(final AbstractOrderModel cart)
	{
		final CartRAO rao = getCartRaoConverter().convert(cart);
		getRuleEngineCalculationService().calculateTotals(rao);
		return rao;
	}

	@Override
	protected Set<Object> expandRAO(final CartRAO cart, final Collection<String> options)
	{
		final Set<Object> facts = new LinkedHashSet<>(super.expandRAO(cart, options));
		options.forEach(option -> expandRAOForOption(cart, facts, option));

		return facts;
	}

	protected void expandRAOForOption(final CartRAO cart, final Set<Object> facts, final String option)
	{
		final Set<OrderEntryRAO> entries = cart.getEntries();
		switch (option)
		{
			case INCLUDE_CART:
				facts.add(cart);
				break;
			case EXPAND_DISCOUNTS:
				facts.addAll(cart.getDiscountValues());
				break;
			case EXPAND_ENTRIES:
				addEntries(facts, entries);
			case EXPAND_PRODUCTS:
				addProducts(facts, entries);
				break;
			case EXPAND_CATEGORIES:
				addProductCategories(facts, entries);
				break;
			case EXPAND_USERS:
				addUserGroups(facts, cart.getUser());
				break;
			case EXPAND_PAYMENT_MODE:
				addPaymentMode(facts, cart.getPaymentMode());
				break;
			default:
				LOGGER.debug("Unknown option: '{}'. Skipping", option);
		}
	}

	protected void addProductCategories(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (isNotEmpty(entries))
		{
			for (final OrderEntryRAO orderEntry : entries)
			{
				final ProductRAO product = orderEntry.getProduct();
				if (Objects.nonNull(product) && CollectionUtils.isNotEmpty(product.getCategories()))
				{
					facts.addAll(product.getCategories());
				}
			}
		}
	}

	protected void addUserGroups(final Set<Object> facts, final UserRAO userRAO)
	{
		if (Objects.nonNull(userRAO))
		{
			facts.add(userRAO);
			final Set<UserGroupRAO> groups = userRAO.getGroups();
			if (CollectionUtils.isNotEmpty(groups))
			{
				facts.addAll(groups);
			}
		}
	}

	protected void addPaymentMode(final Set<Object> facts, final PaymentModeRAO paymentModeRAO)
	{
		if (Objects.nonNull(paymentModeRAO))
		{
			facts.add(paymentModeRAO);
		}
	}

	protected void addProducts(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (isNotEmpty(entries))
		{
			entries.forEach(orderEntry -> facts.add(orderEntry.getProduct()));
		}
	}

	protected void addEntries(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (isNotEmpty(entries))
		{
			facts.addAll(entries);
		}
	}

	/**
	 * @deprecated since 18.11
	 */
	@Deprecated
	protected void addConsumed(final Set<Object> facts, final CartRAO cart, final Set<OrderEntryRAO> entries) // NOSONAR
	{
		addConsumed(facts, entries);
	}

	/**
	 * @deprecated since 18.11
	 */
	@Deprecated
	protected void addConsumed(final Set<Object> facts, final Set<OrderEntryRAO> entries)
	{
		if (isNotEmpty(entries))
		{
			facts.addAll(entries.stream().map(this::createProductConsumedRAO).collect(Collectors.toSet()));
		}
	}

	/**
	 * @deprecated since 18.11
	 */
	@Deprecated
	protected ProductConsumedRAO createProductConsumedRAO(final OrderEntryRAO orderEntryRAO)
	{
		final ProductConsumedRAO productConsumedRAO = new ProductConsumedRAO();
		productConsumedRAO.setOrderEntry(orderEntryRAO);
		productConsumedRAO.setAvailableQuantity(
				getRuleEngineCalculationService().getProductAvailableQuantityInOrderEntry(orderEntryRAO));
		return productConsumedRAO;
	}

	protected Predicate<RAOFactsExtractor> isEnabled(final Collection<String> options)
	{
		return e -> StringUtils.isNotEmpty(e.getTriggeringOption()) && options.contains(e.getTriggeringOption());
	}

	protected Converter<AbstractOrderModel, CartRAO> getCartRaoConverter()
	{
		return cartRaoConverter;
	}

	@Required
	public void setCartRaoConverter(final Converter<AbstractOrderModel, CartRAO> cartRaoConverter)
	{
		this.cartRaoConverter = cartRaoConverter;
	}

	public void setDefaultOptions(final Collection<String> defaultOptions)
	{
		this.defaultOptions = defaultOptions;
	}

	protected RuleEngineCalculationService getRuleEngineCalculationService()
	{
		return ruleEngineCalculationService;
	}

	@Required
	public void setRuleEngineCalculationService(final RuleEngineCalculationService ruleEngineCalculationService)
	{
		this.ruleEngineCalculationService = ruleEngineCalculationService;
	}

	public void setMinOptions(final Collection<String> minOptions)
	{
		this.minOptions = minOptions;
	}

}
