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

import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.ruledefinitions.conditions.builders.IrConditions.empty;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeConditionBuilder.newAttributeConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrGroupConditionBuilder.newGroupConditionOf;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Required;


/**
 * Creates the intermediate representation of Order Threshold Perfect Partner condition.
 */
public class RuleOrderThresholdPerfectPartnerConditionTranslator extends AbstractRuleConditionTranslator
{
    public static final String PARTNER_PRODUCT_PARAM = "product";
    public static final String CART_THRESHOLD_PARAM = "cart_threshold";
    public static final String IS_DISCOUNTED_PRICE_INCLUDED_PARAM = "is_discounted_price_included";

	/**
	 * the attribute used for the order threshold condition ("subTotal" or "total")
	 */
	private String orderThresholdConditionAttribute;

    @Override
    public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
                                     final RuleConditionDefinitionData conditionDefinition)
    {
        final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        final RuleParameterData partnerProductParam = conditionParameters.get(PARTNER_PRODUCT_PARAM);
        final RuleParameterData cartThresholdParam = conditionParameters.get(CART_THRESHOLD_PARAM);
        final RuleParameterData isPriceIncludedParam = conditionParameters.get(IS_DISCOUNTED_PRICE_INCLUDED_PARAM);

        if (verifyAllPresent(partnerProductParam, cartThresholdParam, isPriceIncludedParam))
        {
            final String partnerProduct = partnerProductParam.getValue();
            final Map<String, BigDecimal> cartThreshold = cartThresholdParam.getValue();
            final Boolean isPriceIncluded = isPriceIncludedParam.getValue();
            if (verifyAllPresent(partnerProduct, cartThreshold, isPriceIncluded))
            {
                return getConditions(context, partnerProduct, cartThreshold);
            }
        }
		return empty();
    }

    protected RuleIrCondition getConditions(final RuleCompilerContext context, final String partnerProduct,
            final Map<String, BigDecimal> cartThreshold)
    {
        final RuleIrGroupCondition irGroupCondition = newGroupConditionOf(RuleIrGroupOperator.OR).build();

        final String productRaoVariable = context.generateVariable(ProductRAO.class);
        final String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
        final String cartRaoVariable = context.generateVariable(CartRAO.class);

        for (final Entry<String, BigDecimal> entry : cartThreshold.entrySet())
        {
            if (verifyAllPresent(entry.getKey(), entry.getValue()))
            {
                final List<RuleIrCondition> conditions = newArrayList();

                final RuleIrGroupCondition irCurrencyGroupCondition = newGroupConditionOf(RuleIrGroupOperator.AND)
                        .withChildren(conditions).build();

                conditions.add(newAttributeConditionFor(productRaoVariable)
                        .withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.EQUAL)
                        .withValue(partnerProduct)
                        .build());
                conditions.add(newAttributeRelationConditionFor(orderEntryRaoVariable)
                        .withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.EQUAL)
                        .withTargetVariable(productRaoVariable)
                        .build());
                conditions.add(newAttributeRelationConditionFor(cartRaoVariable)
                        .withAttribute(CART_RAO_ENTRIES_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.CONTAINS)
                        .withTargetVariable(orderEntryRaoVariable).build());
                conditions.add(newAttributeConditionFor(cartRaoVariable).withAttribute(CART_RAO_CURRENCY_ATTRIBUTE)
                        .withOperator(RuleIrAttributeOperator.EQUAL).withValue(entry.getKey())
                        .build());
                conditions.add(newAttributeConditionFor(cartRaoVariable)
						.withAttribute(getOrderThresholdConditionAttribute())
                        .withOperator(RuleIrAttributeOperator.GREATER_THAN_OR_EQUAL)
                        .withValue(entry.getValue())
                        .build());

                conditions.addAll(createProductConsumedCondition(context, orderEntryRaoVariable));

                irGroupCondition.getChildren().add(irCurrencyGroupCondition);
            }
        }
        return irGroupCondition;
    }

	protected String getOrderThresholdConditionAttribute()
	{
		return orderThresholdConditionAttribute;
	}

	/**
	 * attribute name used for the order threshold condition ("total" by default, you can redeclare this property to use
	 * e.g. "subTotal" instead)
	 *
	 * @param orderThresholdConditionAttribute
	 *           attribute name used for the order threshold condition
	 */
	@Required
	public void setOrderThresholdConditionAttribute(final String orderThresholdConditionAttribute)
	{
		this.orderThresholdConditionAttribute = orderThresholdConditionAttribute;
	}
}
