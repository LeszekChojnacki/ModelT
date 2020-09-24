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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

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
import de.hybris.platform.ruleengineservices.compiler.RuleIrTypeCondition;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.UserGroupRAO;
import de.hybris.platform.ruleengineservices.rao.UserRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class RuleTargetCustomersConditionTranslator extends AbstractRuleConditionTranslator
{
	public static final String CUSTOMER_GROUPS_OPERATOR_PARAM = "customer_groups_operator";
	public static final String CUSTOMER_GROUPS_PARAM = "customer_groups";
	public static final String CUSTOMERS_PARAM = "customers";
	public static final String EXCLUDED_CUSTOMER_GROUPS_PARAM = "excluded_customer_groups";
	public static final String EXCLUDED_USERS_PARAM = "excluded_customers";
	public static final String USER_GROUP_RAO_ID_ATTRIBUTE = "id";
	public static final String USER_RAO_ID_ATTRIBUTE = "id";
	public static final String USER_RAO_PK_ATTRIBUTE = "pk";
	public static final String USER_RAO_GROUPS_ATTRIBUTE = "groups";
	public static final String CART_RAO_USER_ATTRIBUTE = "user";

	private boolean usePk;

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
				 final RuleConditionDefinitionData conditionDefinition)
	{
		final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
		final RuleParameterData customerGroupsOperatorParameter = conditionParameters.get(CUSTOMER_GROUPS_OPERATOR_PARAM);
		final RuleParameterData customerGroupsParameter = conditionParameters.get(CUSTOMER_GROUPS_PARAM);
		final RuleParameterData customersParameter = conditionParameters.get(CUSTOMERS_PARAM);
		final RuleParameterData excludedCustomerGroupsParameter = conditionParameters.get(EXCLUDED_CUSTOMER_GROUPS_PARAM);
		final RuleParameterData excludedCustomersParameter = conditionParameters.get(EXCLUDED_USERS_PARAM);

		if (verifyAnyPresent(customerGroupsParameter, customersParameter))
		{
			final CollectionOperator customerGroupsOperator = customerGroupsOperatorParameter.getValue();
			final List<String> customerGroups = isNull(customerGroupsParameter) ? emptyList()
						 : customerGroupsParameter.getValue();
			final List<String> customers = isNull(customersParameter) ? emptyList()
						 : customersParameter.getValue();

			if (verifyAllPresent(customerGroupsOperator) && verifyAnyPresent(customerGroups, customers))
			{
				final RuleIrGroupCondition irTargetCustomersCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();
				addTargetCustomersConditions(context, customerGroupsOperator, customerGroups, customers,
							 irTargetCustomersCondition);
				if (!CollectionOperator.NOT_CONTAINS.equals(customerGroupsOperator))
				{
					// add excluded customer conditions
					addExcludedCustomersAndCustomerGroupsConditions(context, excludedCustomerGroupsParameter,
								 excludedCustomersParameter,
								 irTargetCustomersCondition);
				}
				return irTargetCustomersCondition;
			}
		}
		return empty();
	}

	protected void addTargetCustomersConditions(final RuleCompilerContext context, final CollectionOperator customerGroupsOperator,
				 final List<String> customerGroups, final List<String> customers,
				 final RuleIrGroupCondition irTargetCustomersCondition)
	{
		final String userRaoVariable = context.generateVariable(UserRAO.class);
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		final List<RuleIrCondition> irConditions = newArrayList();

		final RuleIrTypeCondition irUserCondition = new RuleIrTypeCondition();
		irUserCondition.setVariable(userRaoVariable);

		final RuleIrAttributeRelCondition irCartUserRel = newAttributeRelationConditionFor(cartRaoVariable)
					 .withAttribute(CART_RAO_USER_ATTRIBUTE)
					 .withOperator(RuleIrAttributeOperator.EQUAL)
					 .withTargetVariable(userRaoVariable)
					 .build();
		irConditions.add(irUserCondition);
		irConditions.add(irCartUserRel);


		final RuleIrGroupCondition irCustomerGroupsCondition = getCustomerGroupConditions(context, customerGroupsOperator,
					 customerGroups);
		final RuleIrAttributeCondition irCustomersCondition = getCustomerConditions(context, customers);

		if (verifyAllPresent(irCustomerGroupsCondition, irCustomersCondition))
		{
			// if both conditions are defined the UserGroupRAO variable needs to be set (as empty) for the path of the customers condition as well
			// (otherwise the variable is not set and the action is not invoked)
			final RuleIrGroupCondition userAndUserGroupGroupCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();

			final RuleIrTypeCondition irUserGroupCondition = new RuleIrTypeCondition();
			irUserGroupCondition.setVariable(context.generateVariable(UserGroupRAO.class));

			userAndUserGroupGroupCondition.setChildren(asList(irCustomersCondition, irUserGroupCondition));

			final RuleIrGroupCondition groupCondition = newGroupConditionOf(RuleIrGroupOperator.OR).build();
			groupCondition.setChildren(asList(irCustomerGroupsCondition, userAndUserGroupGroupCondition));
			irConditions.add(groupCondition);
		}
		else if (nonNull(irCustomerGroupsCondition))
		{
			irConditions.add(irCustomerGroupsCondition);
		}
		else if (nonNull(irCustomersCondition))
		{
			irConditions.add(irCustomersCondition);
		}
		if (CollectionOperator.NOT_CONTAINS.equals(customerGroupsOperator))
		{
			//combine conditions with "and not"
			irTargetCustomersCondition.getChildren().add(newNotCondition().withChildren(irConditions).build());
		}
		else
		{
			//combine conditions with and
			irTargetCustomersCondition.getChildren().addAll(irConditions);
		}
	}

	protected RuleIrGroupCondition getCustomerGroupConditions(final RuleCompilerContext context,
				 final CollectionOperator customerGroupsOperator, final List<String> customerGroups)
	{
		RuleIrGroupCondition irCustomerGroupsCondition = null;
		if (isNotEmpty(customerGroups))
		{
			final String userRaoVariable = context.generateVariable(UserRAO.class);
			final String userGroupRaoVariable = context.generateVariable(UserGroupRAO.class);

			final List<RuleIrCondition> irCustomerGroupsConditions = newArrayList();

			final RuleIrAttributeCondition irUserGroupCondition = newAttributeConditionFor(userGroupRaoVariable)
						 .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE)
						 .withOperator(RuleIrAttributeOperator.IN)
						 .withValue(customerGroups)
						 .build();
			final RuleIrAttributeRelCondition irUserUserGroupRel = newAttributeRelationConditionFor(userRaoVariable)
						 .withAttribute(USER_RAO_GROUPS_ATTRIBUTE)
						 .withOperator(RuleIrAttributeOperator.CONTAINS)
						 .withTargetVariable(userGroupRaoVariable)
						 .build();
			irCustomerGroupsConditions.add(irUserGroupCondition);
			irCustomerGroupsConditions.add(irUserUserGroupRel);

			addContainsAllCustomerGroupConditions(context, customerGroupsOperator, customerGroups, irCustomerGroupsConditions);

			irCustomerGroupsCondition = new RuleIrGroupCondition();
			irCustomerGroupsCondition.setOperator(RuleIrGroupOperator.AND);
			irCustomerGroupsCondition.setChildren(irCustomerGroupsConditions);
		}
		return irCustomerGroupsCondition;
	}

	protected void addContainsAllCustomerGroupConditions(final RuleCompilerContext context,
				 final CollectionOperator customerGroupsOperator, final List<String> customerGroups,
				 final List<RuleIrCondition> irCustomerGroupsConditions)
	{
		if (CollectionOperator.CONTAINS_ALL.equals(customerGroupsOperator))
		{
			final String userRaoVariable = context.generateVariable(UserRAO.class);

			for (final String customerGroup : customerGroups)
			{
				final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
				final String containsUserGroupRaoVariable = context.generateLocalVariable(variablesContainer, UserGroupRAO.class);

				final RuleIrAttributeCondition irContainsUserGroupCondition = newAttributeConditionFor(containsUserGroupRaoVariable)
							 .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE)
							 .withOperator(RuleIrAttributeOperator.EQUAL)
							 .withValue(customerGroup)
							 .build();
				final RuleIrAttributeRelCondition irContainsUserUserGroupRel = newAttributeRelationConditionFor(userRaoVariable)
							 .withAttribute(USER_RAO_GROUPS_ATTRIBUTE)
							 .withOperator(RuleIrAttributeOperator.CONTAINS)
							 .withTargetVariable(containsUserGroupRaoVariable)
							 .build();
				final RuleIrExistsCondition irContainsCustomerGroupsCondition = new RuleIrExistsCondition();
				irContainsCustomerGroupsCondition.setVariablesContainer(variablesContainer);
				irContainsCustomerGroupsCondition
							 .setChildren(asList(irContainsUserGroupCondition, irContainsUserUserGroupRel));

				irCustomerGroupsConditions.add(irContainsCustomerGroupsCondition);
			}
		}
	}

	protected RuleIrAttributeCondition getCustomerConditions(final RuleCompilerContext context, final List<String> customers)
	{
		RuleIrAttributeCondition irCustomersCondition = null;
		if (isNotEmpty(customers))
		{
			irCustomersCondition = newAttributeConditionFor(context.generateVariable(UserRAO.class))
						 .withAttribute(getUserRAOAttribute())
						 .withOperator(RuleIrAttributeOperator.IN)
						 .withValue(customers)
						 .build();
		}

		return irCustomersCondition;
	}

	protected void addExcludedCustomersAndCustomerGroupsConditions(final RuleCompilerContext context,
				 final RuleParameterData excludedCustomerGroupsParameter, final RuleParameterData excludedCustomersParameter,
				 final RuleIrGroupCondition irTargetCustomersCondition)
	{
		final String userRaoVariable = context.generateVariable(UserRAO.class);

		if (verifyAllPresent(excludedCustomerGroupsParameter, excludedCustomerGroupsParameter) && isNotEmpty(
					 excludedCustomerGroupsParameter.getValue()))
		{
			final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
			final String excludedUserGroupRaoVariable = context.generateLocalVariable(variablesContainer, UserGroupRAO.class);

			final RuleIrAttributeCondition irExcludedUserGroupCondition = newAttributeConditionFor(excludedUserGroupRaoVariable)
						 .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE)
						 .withOperator(RuleIrAttributeOperator.IN)
						 .withValue(excludedCustomerGroupsParameter.getValue())
						 .build();
			final RuleIrAttributeRelCondition irExcludedUserUserGroupRel = newAttributeRelationConditionFor(userRaoVariable)
						 .withAttribute(USER_RAO_GROUPS_ATTRIBUTE)
						 .withOperator(RuleIrAttributeOperator.CONTAINS)
						 .withTargetVariable(excludedUserGroupRaoVariable)
						 .build();
			final RuleIrNotCondition irExcludedCustomerGroupsCondition = newNotCondition()
						 .withVariablesContainer(variablesContainer)
						 .withChildren(asList(irExcludedUserGroupCondition, irExcludedUserUserGroupRel))
						 .build();
			irTargetCustomersCondition.getChildren().add(irExcludedCustomerGroupsCondition);
		}

		if (verifyAllPresent(excludedCustomersParameter, excludedCustomersParameter.getValue()))
		{
			irTargetCustomersCondition.getChildren().add(newAttributeConditionFor(userRaoVariable)
						 .withAttribute(getUserRAOAttribute())
						 .withOperator(RuleIrAttributeOperator.NOT_IN)
						 .withValue(excludedCustomersParameter.getValue())
						 .build());
		}
	}

	protected String getUserRAOAttribute()
	{
		return isUsePk() ? USER_RAO_PK_ATTRIBUTE : USER_RAO_ID_ATTRIBUTE;
	}

	protected boolean isUsePk()
	{
		return usePk;
	}

	@Required
	public void setUsePk(final boolean usePk)
	{
		this.usePk = usePk;
	}
}
