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
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrNotConditionBuilder.newNotCondition;
import static java.util.Collections.singletonList;

import de.hybris.platform.core.enums.GroupType;
import de.hybris.platform.ruledefinitions.MembershipOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrTypeCondition;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryGroupRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;

/**
 * Creates the intermediate representation of the OrderEntryGroupRAO.groupType condition
 */
public class RuleEntryGroupTypeConditionTranslator extends AbstractRuleConditionTranslator
{
	protected static final String GROUP_TYPES_PARAM = "groupTypes";
	protected static final String ORDER_ENTRY_RAO_ENTRY_GROUP_NUMBERS_ATTRIBUTE = "entryGroupNumbers";
	protected static final String ORDER_ENTRY_GROUP_RAO_ENTRY_GROUP_ID_ATTRIBUTE = "entryGroupId";
	protected static final String ORDER_ENTRY_GROUP_RAO_GROUP_TYPE_ATTRIBUTE = "groupType";

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
		final RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
		final RuleParameterData groupTypeParameter = conditionParameters.get(GROUP_TYPES_PARAM);

		if (verifyAllPresent(operatorParameter, groupTypeParameter))
		{
			final MembershipOperator operator = operatorParameter.getValue();
			final List<GroupType> value = groupTypeParameter.getValue();
			if (verifyAllPresent(operator, value))
			{
				return getEntryGroupTypeConditions(context, operator, value);
			}
		}
		return empty();
	}

	protected RuleIrCondition getEntryGroupTypeConditions(final RuleCompilerContext context, final MembershipOperator operator,
				 final List<GroupType> value)
	{
		final String cartRaoVariable = context.generateVariable(CartRAO.class);
		final String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
		final String orderEntryGroupRaoVariable = context.generateVariable(OrderEntryGroupRAO.class);

		final List<RuleIrCondition> conditions = newArrayList();

		final RuleIrTypeCondition orderEntryRAOTypeCondition = new RuleIrTypeCondition();
		orderEntryRAOTypeCondition.setVariable(orderEntryRaoVariable);

		conditions.add(orderEntryRAOTypeCondition);
		conditions.add(newAttributeRelationConditionFor(orderEntryGroupRaoVariable)
				.withAttribute(ORDER_ENTRY_GROUP_RAO_ENTRY_GROUP_ID_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.MEMBER_OF)
				.withTargetVariable(orderEntryRaoVariable)
				.withTargetAttribute(ORDER_ENTRY_RAO_ENTRY_GROUP_NUMBERS_ATTRIBUTE)
				.build());
		conditions.add(newAttributeConditionFor(orderEntryGroupRaoVariable)
				.withAttribute(ORDER_ENTRY_GROUP_RAO_GROUP_TYPE_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.IN)
				.withValue(value)
				.build());
		conditions.add(newAttributeRelationConditionFor(cartRaoVariable)
				.withAttribute(CART_RAO_ENTRIES_ATTRIBUTE)
				.withOperator(RuleIrAttributeOperator.CONTAINS)
				.withTargetVariable(orderEntryRaoVariable)
				.build());

		return groupCondition(operator, conditions);
	}

	protected RuleIrCondition groupCondition(final MembershipOperator operator, final List<RuleIrCondition> conditions)
	{
		final List<RuleIrCondition> childrenConditions = operator == MembershipOperator.NOT_IN ? singletonList(newNotCondition()
				.withChildren(conditions).build()) : conditions;

		return newGroupConditionOf(RuleIrGroupOperator.AND).withChildren(childrenConditions).build();
	}

}
