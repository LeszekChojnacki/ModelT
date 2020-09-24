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
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruledefinitions.AmountOperator;
import de.hybris.platform.ruledefinitions.CollectionOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeRelCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExistsCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.CategoryRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;

public class RuleQualifyingCategoriesConditionTranslator extends AbstractRuleConditionTranslator
{
	public static final String EXCLUDED_CATEGORIES_PARAM = "excluded_categories";
	public static final String EXCLUDED_PRODUCTS_PARAM = "excluded_products";

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
		final RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
		final RuleParameterData quantityParameter = conditionParameters.get(QUANTITY_PARAM);
		final RuleParameterData categoriesOperatorParameter = conditionParameters.get(CATEGORIES_OPERATOR_PARAM);
		final RuleParameterData categoriesParameter = conditionParameters.get(CATEGORIES_PARAM);
		final RuleParameterData excludedCategoriesParameter = conditionParameters.get(EXCLUDED_CATEGORIES_PARAM);
		final RuleParameterData excludedProductsParameter = conditionParameters.get(EXCLUDED_PRODUCTS_PARAM);

		if (verifyAllPresent(operatorParameter, quantityParameter, categoriesOperatorParameter, categoriesParameter))
		{
			final AmountOperator operator = operatorParameter.getValue();
			final Integer quantity = quantityParameter.getValue();
			final CollectionOperator categoriesOperator = categoriesOperatorParameter.getValue();
			final List<String> categories = categoriesParameter.getValue();
			if (verifyAllPresent(operator, quantity, categoriesOperator, categories))
			{
				final RuleIrGroupCondition irQualifyingCategoriesCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();
				addQualifyingCategoriesConditions(context, operator, quantity, categoriesOperator, categories,
						irQualifyingCategoriesCondition);
				if (!CollectionOperator.NOT_CONTAINS.equals(categoriesOperator))
				{
					addExcludedProductsAndCategoriesConditions(context, excludedCategoriesParameter, excludedProductsParameter,
							irQualifyingCategoriesCondition);
				}
				return irQualifyingCategoriesCondition;
			}
		}
		return empty();
	}

	protected void addQualifyingCategoriesConditions(final RuleCompilerContext context, final AmountOperator operator,
			final Integer quantity, final CollectionOperator categoriesOperator, final List<String> categories,
			final RuleIrGroupCondition irQualifyingCategoriesCondition)
	{

		final String categoryRaoVariable = context.generateVariable(CategoryRAO.class);
		final String productRaoVariable = context.generateVariable(ProductRAO.class);
		final String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		final List<RuleIrCondition> irConditions = newArrayList();

		irConditions.add(newAttributeConditionFor(categoryRaoVariable).withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE) //
				.withOperator(RuleIrAttributeOperator.IN).withValue(categories) //
				.build());
		irConditions.add(newAttributeRelationConditionFor(productRaoVariable).withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE) //
				.withOperator(RuleIrAttributeOperator.CONTAINS) //
				.withTargetVariable(categoryRaoVariable) //
				.build());
		irConditions.add(newAttributeRelationConditionFor(orderEntryRaoVariable).withAttribute(ORDER_ENTRY_RAO_PRODUCT_ATTRIBUTE) //
				.withOperator(RuleIrAttributeOperator.EQUAL) //
				.withTargetVariable(productRaoVariable) //
				.build());
		irConditions.add(newAttributeConditionFor(orderEntryRaoVariable).withAttribute(QUANTITY_PARAM) //
				.withOperator(RuleIrAttributeOperator.valueOf(operator.name())) //
				.withValue(quantity).build());
		irConditions.add(newAttributeRelationConditionFor(cartRaoVariable).withAttribute(CART_RAO_ENTRIES_ATTRIBUTE) //
				.withOperator(RuleIrAttributeOperator.CONTAINS) //
				.withTargetVariable(orderEntryRaoVariable) //
				.build());

		irConditions.addAll(createProductConsumedCondition(context, orderEntryRaoVariable));

		evaluateCategoriesOperator(context, categoriesOperator, categories, irQualifyingCategoriesCondition, irConditions);
	}

	protected void evaluateCategoriesOperator(final RuleCompilerContext context, final CollectionOperator categoriesOperator,
			final List<String> categories, final RuleIrGroupCondition irQualifyingCategoriesCondition,
			final List<RuleIrCondition> irConditions)
	{
		if (CollectionOperator.NOT_CONTAINS.equals(categoriesOperator))
		{
			//combine conditions with "and not"
			final RuleIrNotCondition irNotProductCondition = new RuleIrNotCondition();
			irNotProductCondition.setChildren(irConditions);
			irQualifyingCategoriesCondition.getChildren().add(irNotProductCondition);
		}
		else
		{
			//combine conditions with "and"
			irQualifyingCategoriesCondition.getChildren().addAll(irConditions);

			//add exists conditions
			if (CollectionOperator.CONTAINS_ALL.equals(categoriesOperator))
			{
				addContainsAllCategoriesConditions(context, categories, irQualifyingCategoriesCondition);
			}
		}
	}


	protected void addContainsAllCategoriesConditions(final RuleCompilerContext context, final List<String> categories,
			final RuleIrGroupCondition irQualifyingCategoriesCondition)
	{
		final String productRaoVariable = context.generateVariable(ProductRAO.class);

		for (final String category : categories)
		{
			final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
			final String containsCategoryRaoVariable = context.generateLocalVariable(variablesContainer, CategoryRAO.class);

			final RuleIrAttributeCondition irContainsCategoryCondition = newAttributeConditionFor(containsCategoryRaoVariable)
					.withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE) //
					.withOperator(RuleIrAttributeOperator.EQUAL) //
					.withValue(category) //
					.build();

			final RuleIrAttributeRelCondition irContainsProductCategoryRel = newAttributeRelationConditionFor(productRaoVariable)
					.withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE)//
					.withOperator(RuleIrAttributeOperator.CONTAINS) //
					.withTargetVariable(containsCategoryRaoVariable)//
					.build();

			final RuleIrExistsCondition irExistsCategoryCondition = new RuleIrExistsCondition();
			irExistsCategoryCondition.setVariablesContainer(variablesContainer);
			irExistsCategoryCondition.setChildren(asList(irContainsCategoryCondition, irContainsProductCategoryRel));

			irQualifyingCategoriesCondition.getChildren().add(irExistsCategoryCondition);
		}
	}

	protected void addExcludedProductsAndCategoriesConditions(final RuleCompilerContext context,
			final RuleParameterData excludedCategoriesParameter, final RuleParameterData excludedProductsParameter,
			final RuleIrGroupCondition irQualifyingCategoriesCondition)
	{
		final String productRaoVariable = context.generateVariable(ProductRAO.class);

		if (verifyAllPresent(excludedCategoriesParameter, excludedCategoriesParameter)
				&& isNotEmpty(excludedCategoriesParameter.getValue()))
		{
			final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
			final String excludedCategoryRaoVariable = context.generateLocalVariable(variablesContainer, CategoryRAO.class);

			final RuleIrAttributeCondition irExcludedCategoryCondition = newAttributeConditionFor(excludedCategoryRaoVariable)
					.withAttribute(CATEGORY_RAO_CODE_ATTRIBUTE) //
					.withOperator(RuleIrAttributeOperator.IN) //
					.withValue(excludedCategoriesParameter.getValue())//
					.build();
			final RuleIrAttributeRelCondition irExcludedProductCategoryRel = newAttributeRelationConditionFor(productRaoVariable)
					.withAttribute(PRODUCT_RAO_CATEGORIES_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS)
					.withTargetVariable(excludedCategoryRaoVariable).build();

			final RuleIrNotCondition irExcludedCategoriesCondition = new RuleIrNotCondition();
			irExcludedCategoriesCondition.setVariablesContainer(variablesContainer);
			irExcludedCategoriesCondition.setChildren(asList(irExcludedCategoryCondition, irExcludedProductCategoryRel));

			irQualifyingCategoriesCondition.getChildren().add(irExcludedCategoriesCondition);
		}

		if (excludedProductsParameter != null && isNotEmpty(excludedProductsParameter.getValue()))
		{
			final RuleIrGroupCondition baseProductNotOrGroupCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();
			final List<String> products = excludedProductsParameter.getValue();
			baseProductNotOrGroupCondition.getChildren().add(
					newAttributeConditionFor(productRaoVariable).withAttribute(PRODUCT_RAO_CODE_ATTRIBUTE)
							.withOperator(RuleIrAttributeOperator.NOT_IN).withValue(products).build());
			for (final String product : products)
			{
				baseProductNotOrGroupCondition.getChildren().add(
						newAttributeConditionFor(productRaoVariable).withAttribute(BASE_PRODUCT_CODES_ATTRIBUTE)
								.withOperator(RuleIrAttributeOperator.NOT_CONTAINS).withValue(product).build());
			}

			irQualifyingCategoriesCondition.getChildren().add(baseProductNotOrGroupCondition);
		}
	}
}
