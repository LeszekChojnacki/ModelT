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
package de.hybris.platform.ruledefinitions.conditions;

import com.google.common.collect.Lists;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rao.ProductConsumedRAO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeConditionBuilder.newAttributeConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.fest.util.Collections.isEmpty;


public abstract class AbstractRuleConditionTranslator implements RuleConditionTranslator
{

	protected static final String OPERATOR_PARAM = "operator";
	protected static final String VALUE_PARAM = "value";
	protected static final String CART_RAO_CURRENCY_ATTRIBUTE = "currencyIsoCode";
	protected static final String ORDER_CONSUMED_RAO_CART_ATTRIBUTE = "cart";
	protected static final String CART_RAO_TOTAL_ATTRIBUTE = "total";
	protected static final String CART_RAO_ENTRIES_ATTRIBUTE = "entries";
	protected static final String PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE = "orderEntry";
	protected static final String ORDER_ENTRY_RAO_BASE_PRICE_ATTRIBUTE = "basePrice";
	protected static final String QUANTITY_PARAM = "quantity";
	protected static final String AVAILABLE_QUANTITY_PARAM = "availableQuantity";
	protected static final String CATEGORIES_OPERATOR_PARAM = "categories_operator";
	protected static final String CATEGORIES_PARAM = "categories";
	protected static final String CATEGORY_RAO_CODE_ATTRIBUTE = "code";
	protected static final String ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE = "product";
	protected static final String PRODUCT_RAO_CODE_ATTRIBUTE = "code";
	protected static final String PRODUCT_RAO_CATEGORIES_ATTRIBUTE = "categories";
	protected static final String PRODUCTS_PARAM = "products";
	protected static final String BASE_PRODUCT_CODES_ATTRIBUTE = "baseProductCodes";

	private boolean consumptionEnabled = true;

	protected boolean verifyAllPresent(final Object... objects)
	{
		boolean isPresent = true;
		if (isNotEmpty(objects))
		{
			isPresent = stream(objects).map(this::covertToNullIfEmptyCollection).map(this::covertToNullIfEmptyMap)
					.noneMatch(Objects::isNull);
		}
		return isPresent;
	}

	protected boolean verifyAnyPresent(final Object... objects)
	{
		boolean anyPresent = true;
		if (isNotEmpty(objects))
		{
			anyPresent = stream(objects).map(this::covertToNullIfEmptyCollection).map(this::covertToNullIfEmptyMap)
					.anyMatch(Objects::nonNull);
		}
		return anyPresent;
	}

	protected Object covertToNullIfEmptyCollection(final Object seedObject)
	{
		return seedObject instanceof Collection && isEmpty((Collection) seedObject) ? null : seedObject;
	}

	protected Object covertToNullIfEmptyMap(final Object seedObject)
	{
		return seedObject instanceof Map && isEmpty((Map) seedObject) ? null : seedObject;
	}

	/**
	 * @deprecated since 6.7
	 */
	@Deprecated
	protected boolean isRuleNonStackable(final RuleCompilerContext context)
	{
		final AbstractRuleModel rule = context.getRule();
		if (nonNull(rule))
		{
			return isFalse(rule.getStackable());
		}
		return Boolean.TRUE.booleanValue();
	}

	protected List<RuleIrCondition> createProductConsumedCondition(final RuleCompilerContext context,
			final String orderEntryRaoVariable)
	{
		if (!isConsumptionEnabled())
		{
			return Collections.emptyList();
		}

		final List<RuleIrCondition> conditions = Lists.newArrayList();

		final String productConsumedRaoVariable = context.generateVariable(ProductConsumedRAO.class);

		conditions.add(newAttributeRelationConditionFor(productConsumedRaoVariable)
				.withAttribute(PRODUCT_CONSUMED_RAO_ENTRY_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.EQUAL)
				.withTargetVariable(orderEntryRaoVariable)
				.build());

		conditions.add(newAttributeConditionFor(productConsumedRaoVariable)
				.withAttribute(AVAILABLE_QUANTITY_PARAM)
				.withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL)
				.withValue(1).build());

		return conditions;
	}

	protected boolean isConsumptionEnabled()
	{
		return consumptionEnabled;
	}

	public void setConsumptionEnabled(final boolean consumptionEnabled)
	{
		this.consumptionEnabled = consumptionEnabled;
	}
}
