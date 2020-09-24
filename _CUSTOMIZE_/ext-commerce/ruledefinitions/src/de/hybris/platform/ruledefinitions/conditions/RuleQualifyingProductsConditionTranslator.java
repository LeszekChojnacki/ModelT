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

import de.hybris.platform.ruledefinitions.AmountOperator;
import de.hybris.platform.ruledefinitions.CollectionOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExistsCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;

public class RuleQualifyingProductsConditionTranslator extends AbstractRuleConditionTranslator
{
	public static final String PRODUCTS_OPERATOR_PARAM = "products_operator";

	@Override
	public final RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
		final RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
		final RuleParameterData quantityParameter = conditionParameters.get(QUANTITY_PARAM);
		final RuleParameterData productsOperatorParameter = conditionParameters.get(PRODUCTS_OPERATOR_PARAM);
		final RuleParameterData productsParameter = conditionParameters.get(PRODUCTS_PARAM);

		if (verifyAllPresent(operatorParameter, quantityParameter, productsOperatorParameter, productsParameter))
		{
			final AmountOperator operator = operatorParameter.getValue();
			final Integer quantity = quantityParameter.getValue();
			final CollectionOperator productsOperator = productsOperatorParameter.getValue();
			final List<String> products = productsParameter.getValue();

			if (verifyAllPresent(operator, quantity, productsOperator, products))
			{
				return getQualifyingProductsCondition(context, operator, quantity, productsOperator, products);
			}
		}
		return empty();
	}

	protected RuleIrGroupCondition getQualifyingProductsCondition(final RuleCompilerContext context,
			final AmountOperator operator, final Integer quantity, final CollectionOperator productsOperator,
			final List<String> products)
	{
		final RuleIrGroupCondition irQualifyingProductsCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();

		addQualifyingProductsCondition(context, operator, quantity, productsOperator, products, irQualifyingProductsCondition);
		return irQualifyingProductsCondition;
	}

	protected void addQualifyingProductsCondition(final RuleCompilerContext context, final AmountOperator operator,
			final Integer quantity, final CollectionOperator productsOperator, final List<String> products,
			final RuleIrGroupCondition irQualifyingProductsCondition)
	{
		final String productRaoVariable = context.generateVariable(ProductRAO.class);
		final String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		final List<RuleIrCondition> irConditions = newArrayList();

		final RuleIrGroupCondition baseProductOrGroupCondition = newGroupConditionOf(RuleIrGroupOperator.OR).build();
		baseProductOrGroupCondition.getChildren().add(
				newAttributeConditionFor(productRaoVariable).withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
						.withOperator(RuleIrAttributeOperator.IN).withValue(products).build());
		for (final String product : products)
		{
			baseProductOrGroupCondition.getChildren().add(
					newAttributeConditionFor(productRaoVariable).withAttribute(BASE_PRODUCT_CODES_ATTRIBUTE)
							.withOperator(RuleIrAttributeOperator.CONTAINS).withValue(product).build());
		}
		irConditions.add(baseProductOrGroupCondition);

		irConditions.add(newAttributeRelationConditionFor(orderEntryRaoVariable)
				.withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.EQUAL)
				.withTargetVariable(productRaoVariable)
				.build());
		irConditions.add(newAttributeConditionFor(orderEntryRaoVariable)
				.withAttribute(QUANTITY_PARAM)
				.withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
				.withValue(quantity)
				.build());
		irConditions.add(newAttributeRelationConditionFor(cartRaoVariable)
				.withAttribute(CART_RAO_ENTRIES_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.CONTAINS)
				.withTargetVariable(orderEntryRaoVariable)
				.build());
		evaluateProductsOperator(context, operator, quantity, productsOperator, products, irQualifyingProductsCondition,
				irConditions, orderEntryRaoVariable);
	}

	// refactoring this method will make future backporting too painful
	protected void evaluateProductsOperator(final RuleCompilerContext context, final AmountOperator operator,
			final Integer quantity, final CollectionOperator productsOperator, final List<String> products,
			final RuleIrGroupCondition irQualifyingProductsCondition, final List<RuleIrCondition> irConditions,
			final String orderEntryRaoVariable) // NOSONAR
	{
		if (!CollectionOperator.CONTAINS_ALL.equals(productsOperator))
		{
			irConditions.addAll(createProductConsumedCondition(context, orderEntryRaoVariable));
		}
		if (CollectionOperator.NOT_CONTAINS.equals(productsOperator))
		{
			final RuleIrNotCondition irNotProductCondition = new RuleIrNotCondition();
			irNotProductCondition.setChildren(irConditions);
			irQualifyingProductsCondition.getChildren().add(irNotProductCondition);
		}
		else
		{
			irQualifyingProductsCondition.getChildren().addAll(irConditions);
			if (CollectionOperator.CONTAINS_ALL.equals(productsOperator))
			{
				addContainsAllProductsConditions(context, operator, quantity, products, irQualifyingProductsCondition);
			}
		}
	}

	protected void addContainsAllProductsConditions(final RuleCompilerContext context, final AmountOperator operator,
			final Integer quantity, final List<String> products, final RuleIrGroupCondition irQualifyingProductsCondition)
	{
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		for (final String product : products)
		{
			final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
			final String containsProductRaoVariable = context.generateLocalVariable(variablesContainer, ProductRAO.class);
			final String containsOrderEntryRaoVariable = context.generateLocalVariable(variablesContainer, OrderEntryRAO.class);

			final List<RuleIrCondition> irConditions = newArrayList();

			irConditions.add(newAttributeConditionFor(containsProductRaoVariable)
					.withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
					.withOperator(RuleIrAttributeOperator.EQUAL)
					.withValue(product)
					.build());
			irConditions.add(newAttributeRelationConditionFor(
					containsOrderEntryRaoVariable)
					.withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE)
					.withOperator(RuleIrAttributeOperator.EQUAL)
					.withTargetVariable(containsProductRaoVariable)
					.build());
			irConditions.add(newAttributeConditionFor(
					containsOrderEntryRaoVariable)
					.withAttribute(QUANTITY_PARAM)
					.withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
					.withValue(quantity)
					.build());
			irConditions.add(newAttributeRelationConditionFor(cartRaoVariable)
					.withAttribute(CART_RAO_ENTRIES_ATTRIBUTE)
					.withOperator(RuleIrAttributeOperator.CONTAINS)
					.withTargetVariable(containsOrderEntryRaoVariable)
					.build());

			irConditions.addAll(createProductConsumedCondition(context, containsOrderEntryRaoVariable));

			final RuleIrExistsCondition irExistsProductCondition = new RuleIrExistsCondition();
			irExistsProductCondition.setVariablesContainer(variablesContainer);
			irExistsProductCondition.setChildren(irConditions);

			irQualifyingProductsCondition.getChildren().add(irExistsProductCondition);
		}
	}
}
