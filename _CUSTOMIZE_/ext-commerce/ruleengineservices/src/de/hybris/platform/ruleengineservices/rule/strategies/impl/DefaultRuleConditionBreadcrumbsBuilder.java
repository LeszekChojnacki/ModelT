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
package de.hybris.platform.ruleengineservices.rule.strategies.impl;

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.definitions.conditions.RuleGroupOperator;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConditionBreadcrumbsBuilder;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleConditionBreadcrumbsBuilder extends AbstractRuleBreadcrumbsBuilder
		implements RuleConditionBreadcrumbsBuilder
{
	protected static final String GROUP_CONDITION_DEFINITION_ID = "y_group";
	protected static final String GROUP_CONDITION_OPERATOR_PARAM = "operator";
	protected static final String GROUP_CONDITION_OPERATOR_TYPE = "Enum(de.hybris.platform.ruleengineservices.definitions.conditions.RuleGroupOperator)";

	protected static final RuleGroupOperator DEFAULT_GROUP_OPERATOR = RuleGroupOperator.AND;

	protected static final String PARENT_CONDITION_CLASS_PREFIX = "rule-parent-condition rule-parent-condition-";

	private I18NService i18NService;

	@Override
	public String buildConditionBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return buildBreadcrumbs(conditions, conditionDefinitions, false);
	}

	@Override
	public String buildStyledConditionBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions)
	{
		return buildBreadcrumbs(conditions, conditionDefinitions, true);
	}

	protected String buildBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions, final boolean styled)
	{
		ServicesUtil.validateParameterNotNull(conditions, "conditions cannot be null");
		ServicesUtil.validateParameterNotNull(conditionDefinitions, "condition definitions cannot be null");

		final StringBuilder breadcrumbBuilder = new StringBuilder();
		final Locale locale = i18NService.getCurrentLocale();
		final String separator = buildSeparator(null, conditionDefinitions, locale, styled);

		buildBreadcrumbsHelper(conditions, conditionDefinitions, styled, StringUtils.EMPTY, StringUtils.EMPTY, separator, true,
				locale, breadcrumbBuilder);

		return breadcrumbBuilder.toString();
	}

	protected void buildBreadcrumbsHelper(final List<RuleConditionData> conditions,                           // NOSONAR
			final Map<String, RuleConditionDefinitionData> conditionDefinitions, final boolean styled, final String prefix,
			final String suffix, final String separator, final boolean isRootParent, final Locale locale,
			final StringBuilder breadcrumbBuilder)
	{
		if (CollectionUtils.isEmpty(conditions))
		{
			return;
		}

		final List<RuleConditionBreadcrumbData> conditionBreadcrumbs = extractConditionBreadcrumbs(conditions, conditionDefinitions,
				locale, styled);
		final int conditionBreadcrumbsSize = conditionBreadcrumbs.size();

		final boolean requiresPrefixAndSuffix = isRootParent || conditionBreadcrumbsSize != 1;
		if (requiresPrefixAndSuffix)
		{
			breadcrumbBuilder.append(prefix);
		}

		int index = 0;
		for (final RuleConditionBreadcrumbData conditionBreadcrumb : conditionBreadcrumbs)
		{
			final RuleConditionData condition = conditionBreadcrumb.getCondition();
			final RuleConditionDefinitionData conditionDefinition = conditionBreadcrumb.getConditionDefinition();
			final String breadcrumb = conditionBreadcrumb.getBreadcrumb();
			final boolean isGroupCondition = conditionBreadcrumb.isGroupCondition();

			if (index != 0)
			{
				breadcrumbBuilder.append(separator);
			}

			if (BooleanUtils.isTrue(conditionDefinition.getAllowsChildren()))
			{
				final String styleClass = PARENT_CONDITION_CLASS_PREFIX + condition.getDefinitionId();
				final String childrenSeparator = buildSeparator(condition, conditionDefinitions, locale, styled);

				if (isGroupCondition && isRootParent && conditionBreadcrumbsSize == 1)
				{
					final String childrenPrefix = StringUtils.EMPTY;
					final String childrenSuffix = StringUtils.EMPTY;

					buildBreadcrumbsHelper(condition.getChildren(), conditionDefinitions, styled, childrenPrefix, childrenSuffix,
							childrenSeparator, false, locale, breadcrumbBuilder);
				}
				else if (isGroupCondition)
				{
					final String childrenPrefix = decorateValue("(", styleClass, styled);
					final String childrenSuffix = decorateValue(")", styleClass, styled);

					buildBreadcrumbsHelper(condition.getChildren(), conditionDefinitions, styled, childrenPrefix, childrenSuffix,
							childrenSeparator, false, locale, breadcrumbBuilder);
				}
				else
				{
					final String childrenPrefix = decorateValue(breadcrumb + " (", styleClass, styled);
					final String childrenSuffix = decorateValue(")", styleClass, styled);

					buildBreadcrumbsHelper(condition.getChildren(), conditionDefinitions, styled, childrenPrefix, childrenSuffix,
							childrenSeparator, true, locale, breadcrumbBuilder);
				}
			}
			else
			{
				breadcrumbBuilder.append(breadcrumb);
			}

			index++;
		}

		if (requiresPrefixAndSuffix)
		{
			breadcrumbBuilder.append(suffix);
		}
	}

	protected List<RuleConditionBreadcrumbData> extractConditionBreadcrumbs(final List<RuleConditionData> conditions,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions, final Locale locale, final boolean styled)
	{
		final List<RuleConditionBreadcrumbData> conditionBreadcrumbs = new ArrayList<>();

		for (final RuleConditionData condition : conditions)
		{
			final RuleConditionDefinitionData conditionDefinition = conditionDefinitions.get(condition.getDefinitionId());
			if (conditionDefinition == null)
			{
				throw new RuleEngineServiceException("No condition definition found for id " + condition.getDefinitionId());
			}

			final boolean decorated = styled && BooleanUtils.isFalse(conditionDefinition.getAllowsChildren());
			final String breadcrumb = formatBreadcrumb(conditionDefinition.getBreadcrumb(), condition.getParameters(), locale,
					styled, decorated);
			final boolean isGroupCondition = isGroupCondition(condition);

			if (StringUtils.isNotBlank(breadcrumb) || isGroupCondition)
			{
				final RuleConditionBreadcrumbData conditionBreadcrumb = new RuleConditionBreadcrumbData();
				conditionBreadcrumb.setCondition(condition);
				conditionBreadcrumb.setConditionDefinition(conditionDefinition);
				conditionBreadcrumb.setBreadcrumb(breadcrumb);
				conditionBreadcrumb.setGroupCondition(isGroupCondition);

				conditionBreadcrumbs.add(conditionBreadcrumb);
			}
		}

		return conditionBreadcrumbs;
	}

	protected String buildSeparator(final RuleConditionData parentCondition,
			final Map<String, RuleConditionDefinitionData> conditionDefinitions, final Locale locale, final boolean styled)
	{
		final String separator;
		final RuleConditionDefinitionData groupConditionDefinition = conditionDefinitions.get(GROUP_CONDITION_DEFINITION_ID);

		if (isGroupCondition(parentCondition))
		{
			separator = formatBreadcrumb(groupConditionDefinition.getBreadcrumb(), parentCondition.getParameters(), locale, false,
					true);
		}
		else
		{
			final RuleParameterData operatorParameter = new RuleParameterData();
			operatorParameter.setValue(DEFAULT_GROUP_OPERATOR);
			operatorParameter.setType(GROUP_CONDITION_OPERATOR_TYPE);

			final Map<String, RuleParameterData> parameters = Collections.singletonMap(GROUP_CONDITION_OPERATOR_PARAM,
					operatorParameter);

			separator = formatBreadcrumb(groupConditionDefinition.getBreadcrumb(), parameters, locale, false, true);
		}

		final String styleClass = PARENT_CONDITION_CLASS_PREFIX + GROUP_CONDITION_DEFINITION_ID;
		return decorateValue(" " + separator + " ", styleClass, styled);
	}

	protected boolean isGroupCondition(final RuleConditionData condition)
	{
		if (condition == null)
		{
			return false;
		}

		return GROUP_CONDITION_DEFINITION_ID.equals(condition.getDefinitionId());
	}

	protected static class RuleConditionBreadcrumbData
	{
		private RuleConditionData condition;
		private RuleConditionDefinitionData conditionDefinition;
		private String breadcrumb;
		private boolean groupCondition;

		public RuleConditionData getCondition()
		{
			return condition;
		}

		public void setCondition(final RuleConditionData condition)
		{
			this.condition = condition;
		}

		public RuleConditionDefinitionData getConditionDefinition()
		{
			return conditionDefinition;
		}

		public void setConditionDefinition(final RuleConditionDefinitionData conditionDefinition)
		{
			this.conditionDefinition = conditionDefinition;
		}

		public String getBreadcrumb()
		{
			return breadcrumb;
		}

		public void setBreadcrumb(final String breadcrumb)
		{
			this.breadcrumb = breadcrumb;
		}

		public boolean isGroupCondition()
		{
			return groupCondition;
		}

		public void setGroupCondition(final boolean groupCondition)
		{
			this.groupCondition = groupCondition;
		}
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
