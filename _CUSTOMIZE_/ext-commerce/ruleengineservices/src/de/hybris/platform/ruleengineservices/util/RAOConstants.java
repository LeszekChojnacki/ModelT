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
package de.hybris.platform.ruleengineservices.util;

/**
 * constants referenced in RAO action classes
 */
public class RAOConstants
{
	public static final String DELIVERY_MODE_PARAM = "delivery_mode";
	public static final String PRODUCT_PARAM = "product";
	public static final String QUANTITY_PARAM = "quantity";
	public static final String SUB_TOTALS_THRESHOLD_PARAM = "threshold";
	public static final String CONSUMED_PARAM = "consumed";
	public static final String VALUE_PARAM = "value";
	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	public static final String STACKABLE_PARAM = "stackable";
	public static final String ORDER_ENTRY_RAO = "order_entry";
	public static final String ABSOLUTE_VALUE_FLAG = "absolute_value_flag";
	public static final String SELECTION_STRATEGY_PARAM = "selection_strategy";
	public static final String SELECTION_STRATEGY_RPDS_PARAM = "selection_strategy_rpds";
	public static final String MAX_QUANTITY = "max_quantity";
	public static final String CURRENCY_ISO_CODE = "currency_iso_code";
	public static final String CATEGORIES_OPERATOR_PARAM = "categories_operator";
	public static final String CATEGORIES_PARAM = "categories";
	public static final String EXCLUDED_CATEGORIES_PARAM = "excluded_categories";
	public static final String EXCLUDED_PRODUCTS_PARAM = "excluded_products";
	public static final String PRODUCTS_PARAM = "products";

	private RAOConstants()
	{
	}

}
