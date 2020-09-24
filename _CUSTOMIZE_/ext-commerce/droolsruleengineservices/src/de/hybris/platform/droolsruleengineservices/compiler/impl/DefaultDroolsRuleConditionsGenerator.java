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
package de.hybris.platform.droolsruleengineservices.compiler.impl;

import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleConditionsGenerator;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatter;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleValueFormatterException;
import de.hybris.platform.ruleengineservices.compiler.AbstractRuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.AbstractRuleIrBooleanCondition;
import de.hybris.platform.ruleengineservices.compiler.AbstractRuleIrPatternCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerRuntimeException;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeRelCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrConditionWithChildren;
import de.hybris.platform.ruleengineservices.compiler.RuleIrEmptyCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExistsCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrFalseCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrTrueCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrTypeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultDroolsRuleConditionsGenerator implements DroolsRuleConditionsGenerator
{
	public static final String NON_SUPPORTED_CONDITION = "Not supported RuleIrCondition";
	public static final int BUFFER_SIZE = 4096;

	private DroolsRuleValueFormatter droolsRuleValueFormatter;
	private List<String> excludedQueryVariableClassNames;
	private List<Class<?>> typesToSkipOperatorEvaluation;

	@Override
	public String generateConditions(final DroolsRuleGeneratorContext context, final String indentation)
	{
		try
		{
			final RuleIr ruleIr = context.getRuleIr();
			return generateConditions(context, ruleIr.getConditions(), RuleIrGroupOperator.AND, StringUtils.EMPTY, indentation);
		}
		catch (final DroolsRuleValueFormatterException e)
		{
			throw new RuleCompilerException(e);
		}
	}

	@Override
	public String generateRequiredFactsCheckPattern(final DroolsRuleGeneratorContext context)
	{
		final RuleIr ruleIr = context.getRuleIr();
		return generateWhenConditions(context, ruleIr.getConditions(), RuleIrGroupOperator.AND, StringUtils.EMPTY,
				context.getIndentationSize());
	}

	@Override
	public String generateRequiredTypeVariables(final DroolsRuleGeneratorContext context)
	{
		final RuleIr ruleIr = context.getRuleIr();
		final Map<String, RuleIrTypeCondition> typeConditions = new HashMap<>();
		collectTypeConditions(context, ruleIr.getConditions(), typeConditions);

		final StringJoiner conditionsJoiner = new StringJoiner(StringUtils.EMPTY);

		for (final Entry<String, RuleIrTypeCondition> entry : typeConditions.entrySet())
		{
			final String variableName = entry.getKey();
			final RuleIrVariable variable = findVariable(context, variableName);
			if (variable == null)
			{
				throw new RuleCompilerException("Variable with name '" + variableName + "' not found");
			}
			final String variableClassName = context.generateClassName(variable.getType());

			final StringBuilder conditionsBuffer = new StringBuilder(BUFFER_SIZE);
			conditionsBuffer.append(context.getIndentationSize()).append(context.getVariablePrefix()).append(variableName)
					.append(" := ").append(variableClassName).append("()\n");

			conditionsJoiner.add(conditionsBuffer);
		}

		return conditionsJoiner.toString();
	}

	protected void collectTypeConditions(final DroolsRuleGeneratorContext context, final List<RuleIrCondition> conditions,
			final Map<String, RuleIrTypeCondition> typeConditions)
	{
		final RuleIrConditionsByType ruleIrConditionsByType = evaluateRuleConditionType(conditions);
		final Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions = ruleIrConditionsByType
				.getPatternConditions().asMap();

		for (final Entry<String, Collection<AbstractRuleIrPatternCondition>> entry : patternConditions.entrySet())
		{
			if (isVariableTerminal(entry.getKey(), context))
			{
				entry.getValue().stream().filter(RuleIrTypeCondition.class::isInstance).findFirst()
						.ifPresent(t -> typeConditions.put(entry.getKey(), (RuleIrTypeCondition) t));
			}
		}

		if (CollectionUtils.isNotEmpty(ruleIrConditionsByType.getGroupConditions()))
		{
			ruleIrConditionsByType.getGroupConditions()
					.forEach(c -> collectTypeConditions(context, c.getChildren(), typeConditions));
		}
	}

	protected String generateWhenConditions(final DroolsRuleGeneratorContext context, final List<RuleIrCondition> conditions,
			final RuleIrGroupOperator operator, final String conditionPrefix, final String indentation)
	{
		if (isEmpty(conditions))
		{
			return StringUtils.EMPTY;
		}
		final RuleIrConditionsByType ruleIrConditionsByType = evaluateRuleConditionType(conditions);
		final ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = ruleIrConditionsByType
				.getPatternConditions();
		final List<RuleIrGroupCondition> groupConditions = ruleIrConditionsByType.getGroupConditions();

		// exclude the group conditions with OR operation
		final List<RuleIrGroupCondition> normalizedGroupConditions = groupConditions
				.stream()
				.filter(
						c -> filterOutNonGroupConditions(c.getChildren()).size() <= 1
								|| c.getOperator().compareTo(RuleIrGroupOperator.OR) != 0).collect(Collectors.toList());

		final List<RuleIrNotCondition> notConditions = ruleIrConditionsByType.getNotConditions();

		StringJoiner conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
		final int conditionsCount = patternConditions.size() + normalizedGroupConditions.size() + notConditions.size();

		if (conditionsCount > 0)
		{
			String conditionsIndentation;
			if ((operator == null || conditionsCount == 1) && StringUtils.isEmpty(conditionPrefix))
			{
				conditionsIndentation = indentation;
			}
			else
			{
				final String operatorAsString = conditionsCount > 1 && operator != null ? operator.toString().toLowerCase() : "";
				final String delimiter = new StringBuilder(indentation).append(context.getIndentationSize()).append(operatorAsString)
						.append("\n").toString();
				final String prefix = new StringBuilder(indentation).append(conditionPrefix == null ? "" : conditionPrefix)
						.append("(\n").toString();
				final String suffix = indentation + ")\n";

				conditionsIndentation = indentation + context.getIndentationSize();
				conditionsJoiner = new StringJoiner(delimiter, prefix, suffix);
				conditionsJoiner.setEmptyValue(StringUtils.EMPTY);
			}

			// order conditional items in generated Drools rule to avoid variables being used in patternConditions
			// before they are declared in groupConditions
			if (patternConditions.asMap().values().stream().flatMap(l -> l.stream())
					.anyMatch(condition -> isConditionDependentOnOthers(condition, normalizedGroupConditions,
							patternConditions.asMap().keySet())))
			{
				generateWhenGroupConditions(context, normalizedGroupConditions, conditionsJoiner, conditionsIndentation);
				generateWhenPatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
			}
			else
			{
				generateWhenPatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
				generateWhenGroupConditions(context, normalizedGroupConditions, conditionsJoiner, conditionsIndentation);
			}
			generateWhenNotConditions(context, notConditions, conditionsJoiner, conditionsIndentation);
		}

		return conditionsJoiner.toString();
	}

	protected boolean isConditionDependentOnOthers(final RuleIrCondition condition,
			final Collection<? extends RuleIrCondition> others, final Collection<String> definedVariables)
	{
		final Collection<String> variableNamesToCheck = new HashSet<>();
		if (condition instanceof RuleIrAttributeRelCondition)
		{
			variableNamesToCheck.add(((RuleIrAttributeRelCondition) condition).getTargetVariable());
		}
		else if (condition instanceof RuleIrGroupCondition)
		{
			findVariablesOfPatternConditions(variableNamesToCheck, ((RuleIrGroupCondition) condition).getChildren());
		}
		else
		{
			return false;
		}
		variableNamesToCheck.removeAll(definedVariables);
		return isAnyVariableReferredInConditions(variableNamesToCheck, others);
	}

	protected boolean isAnyVariableReferredInConditions(final Collection<String> variableNamesToCheck,
			final Collection<? extends RuleIrCondition> others)
	{
		if (isAnyVariableReferredInAttrRelConditions(variableNamesToCheck, others)
				|| isAnyVariableReferredInPatternConditions(variableNamesToCheck, others))
		{
			return true;
		}
		else
		{
			final Collection<RuleIrCondition> conditionsInGroups = getConditionsInGroups(others);
			if (conditionsInGroups.isEmpty())
			{
				return false;
			}
			else
			{
				return isAnyVariableReferredInConditions(variableNamesToCheck, conditionsInGroups);
			}
		}
	}

	protected boolean isAnyVariableReferredInPatternConditions(final Collection<String> variableNamesToCheck, final Collection<? extends RuleIrCondition> others)
	{
		return others.stream().filter(c -> c instanceof AbstractRuleIrPatternCondition)
				.map(c -> ((AbstractRuleIrPatternCondition) c).getVariable()).filter(Objects::nonNull)
				.anyMatch(variableNamesToCheck::contains);
	}

	protected boolean isAnyVariableReferredInAttrRelConditions(final Collection<String> variableNamesToCheck, final Collection<? extends RuleIrCondition> others)
	{
		return others.stream().filter(c -> c instanceof RuleIrAttributeRelCondition)
				.map(c -> ((RuleIrAttributeRelCondition) c).getTargetVariable()).filter(Objects::nonNull)
				.anyMatch(variableNamesToCheck::contains);
	}

	protected void findVariablesOfPatternConditions(final Collection<String> variableNames,
			final Collection<RuleIrCondition> conditions)
	{
		variableNames.addAll(conditions.stream().filter(c -> c instanceof AbstractRuleIrPatternCondition)
				.map(c -> ((AbstractRuleIrPatternCondition) c).getVariable()).collect(Collectors.toSet()));
		final Collection<RuleIrCondition> conditionsInGroups = getConditionsInGroups(conditions);
		if (!conditionsInGroups.isEmpty())
		{
			findVariablesOfPatternConditions(variableNames, conditionsInGroups);
		}
	}

	protected Collection<RuleIrCondition> getConditionsInGroups(final Collection<? extends RuleIrCondition> conditions)
	{
		return conditions.stream().filter(c -> c instanceof RuleIrConditionWithChildren)
				.map(c -> (RuleIrConditionWithChildren) c).filter(c -> c.getChildren() != null).flatMap(c -> c.getChildren().stream())
				.collect(Collectors.toSet());
	}

	protected Collection<RuleIrCondition> filterOutNonGroupConditions(final Collection<RuleIrCondition> conditions)
	{
		if (CollectionUtils.isNotEmpty(conditions))
		{
			return conditions.stream().filter(c -> c instanceof RuleIrConditionWithChildren).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	protected String generateConditions(final DroolsRuleGeneratorContext context, final List<RuleIrCondition> conditions,
			final RuleIrGroupOperator operator, final String conditionPrefix, final String indentation)

	{
		if (isEmpty(conditions))
		{
			return StringUtils.EMPTY;
		}

		final RuleIrConditionsByType ruleIrConditionsList = evaluateRuleConditionType(conditions);

		final ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions = ruleIrConditionsList
				.getBooleanConditions();
		final ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = ruleIrConditionsList.getPatternConditions();
		final List<RuleIrGroupCondition> groupConditions = ruleIrConditionsList.getGroupConditions();
		final List<RuleIrExecutableCondition> executableConditions = ruleIrConditionsList.getExecutableConditions();
		final List<RuleIrExistsCondition> existsConditions = ruleIrConditionsList.getExistsConditions();
		final List<RuleIrNotCondition> notConditions = ruleIrConditionsList.getNotConditions();

		final int conditionsCount = booleanConditions.size() + patternConditions.size() + groupConditions.size()
				+ existsConditions.size() + notConditions.size() + executableConditions.size();

		if (conditionsCount == 0)
		{
			return StringUtils.EMPTY;
		}

		String conditionsIndentation;
		final StringJoiner conditionsJoiner;

		// if there is only one condition or no operator and
		// if no prefix (like "not" or "exists") is used
		// we can simplify the syntax
		if ((operator == null || conditionsCount == 1) && StringUtils.isEmpty(conditionPrefix))
		{
			conditionsIndentation = indentation;
			conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
		}
		else
		{
			final String operatorAsString = operator != null ? operator.toString().toLowerCase() : "";
			final String delimiter = new StringBuilder(indentation).append(context.getIndentationSize()).append(operatorAsString)
					.append("\n").toString();
			final String prefix = new StringBuilder(indentation).append(conditionPrefix == null ? "" : conditionPrefix)
					.append("(\n").toString();
			final String suffix = indentation + ")\n";

			conditionsIndentation = indentation + context.getIndentationSize();
			conditionsJoiner = new StringJoiner(delimiter, prefix, suffix);
			conditionsJoiner.setEmptyValue(StringUtils.EMPTY);
		}

		generateBooleanConditions(booleanConditions.asMap(), conditionsJoiner, conditionsIndentation);
		generatePatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
		generateGroupConditions(context, groupConditions, conditionsJoiner, conditionsIndentation);
		generateExecutableConditions(context, executableConditions, conditionsJoiner, conditionsIndentation);
		generateExistsConditions(context, existsConditions, conditionsJoiner, conditionsIndentation);
		generateNotConditions(context, notConditions, conditionsJoiner, conditionsIndentation);

		return conditionsJoiner.toString();
	}

	protected RuleIrConditionsByType evaluateRuleConditionType(final List<RuleIrCondition> conditions)
	{
		final RuleIrConditionsByType ruleIrConditionsList = new RuleIrConditionsByType();

		final ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions = new ArrayListValuedHashMap<>();
		final ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = new ArrayListValuedHashMap<>();
		final List<RuleIrGroupCondition> groupConditions = new ArrayList<>();
		final List<RuleIrExecutableCondition> executableConditions = new ArrayList<>();
		final List<RuleIrExistsCondition> existsConditions = new ArrayList<>();
		final List<RuleIrNotCondition> notConditions = new ArrayList<>();

		for (final RuleIrCondition ruleIrCondition : conditions)
		{
			if (ruleIrCondition instanceof RuleIrTrueCondition)
			{
				booleanConditions.put(Boolean.TRUE, (RuleIrTrueCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof RuleIrFalseCondition)
			{
				booleanConditions.put(Boolean.FALSE, (RuleIrFalseCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof AbstractRuleIrPatternCondition)
			{
				final AbstractRuleIrPatternCondition ruleIrPatternCondition = (AbstractRuleIrPatternCondition) ruleIrCondition;
				patternConditions.put(ruleIrPatternCondition.getVariable(), ruleIrPatternCondition);
			}
			else if (ruleIrCondition instanceof RuleIrGroupCondition)
			{
				groupConditions.add((RuleIrGroupCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof RuleIrExistsCondition)
			{
				existsConditions.add((RuleIrExistsCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof RuleIrNotCondition)
			{
				notConditions.add((RuleIrNotCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof RuleIrExecutableCondition)
			{
				executableConditions.add((RuleIrExecutableCondition) ruleIrCondition);
			}
			else if (ruleIrCondition instanceof RuleIrEmptyCondition)
			{
				// empty
			}
			else
			{
				throw new RuleCompilerException(NON_SUPPORTED_CONDITION);
			}
		}

		ruleIrConditionsList.setBooleanConditions(booleanConditions);
		ruleIrConditionsList.setPatternConditions(patternConditions);
		ruleIrConditionsList.setGroupConditions(groupConditions);
		ruleIrConditionsList.setExecutableConditions(executableConditions);
		ruleIrConditionsList.setExistsConditions(existsConditions);
		ruleIrConditionsList.setNotConditions(notConditions);

		return ruleIrConditionsList;

	}

	protected void generateBooleanConditions(final Map<Boolean, Collection<AbstractRuleIrBooleanCondition>> booleanConditions,
			final StringJoiner conditionsJoiner, final String indentation)
	{
		for (final Entry<Boolean, Collection<AbstractRuleIrBooleanCondition>> entry : booleanConditions.entrySet())
		{
			if (Boolean.TRUE.equals(entry.getKey()))
			{
				conditionsJoiner.add(indentation + "eval(true)\n");
			}
			else if (Boolean.FALSE.equals(entry.getKey()))
			{
				conditionsJoiner.add(indentation + "eval(false)\n");
			}
			else
			{
				throw new RuleCompilerException(NON_SUPPORTED_CONDITION);
			}
		}
	}

	protected void generateWhenPatternConditions(final DroolsRuleGeneratorContext context,
			final Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions,
			final RuleIrGroupOperator groupOperator, final StringJoiner conditionsJoiner, final String indentation)
	{
		// sort the conditions to avoid variables being used before they are declared
		final Set<Dependency> dependencies = buildDependencies(patternConditions);
		final Map<String, Collection<AbstractRuleIrPatternCondition>> sortedPatternConditions = new TreeMap<>(
				new DependencyComparator(dependencies));
		sortedPatternConditions.putAll(patternConditions);

		for (final Entry<String, Collection<AbstractRuleIrPatternCondition>> entry : sortedPatternConditions.entrySet())
		{
			final String separator = (groupOperator == RuleIrGroupOperator.AND) ? ", " : " || ";
			final StringBuilder conditionsBuffer = new StringBuilder(BUFFER_SIZE);

			final String variableName = entry.getKey();
			final RuleIrVariable variable = findVariable(context, variableName);

			if (variable == null)
			{
				throw new RuleCompilerException("Variable with name '" + variableName + "' not found");
			}

			final String variableClassName = context.generateClassName(variable.getType());

			final Supplier<String> variablePrefixSupplier = () -> context.getVariablePrefix() + "rao_";
			final boolean variableIsTerminal = isVariableTerminal(variableName, context);

			if (variableIsTerminal && entry.getValue().stream().noneMatch(c -> !(c instanceof RuleIrTypeCondition)))
			{
				continue;
			}

			if (variableIsTerminal)
			{
				conditionsBuffer.append(indentation).append("exists (").append(variableClassName).append('(');
			}
			else
			{
				conditionsBuffer.append(indentation).append(variablePrefixSupplier.get()).append(variableName).append(" := ")
						.append(variableClassName).append('(');
			}

			entry.getValue()
					.stream()
					.filter(c -> c instanceof AbstractRuleIrAttributeCondition)
					.map(c -> (AbstractRuleIrAttributeCondition) c)
					.forEach(c -> conditionsBuffer.append(evaluateAttributeNameAndOperator(c))
							.append(evaluatePatternConditionType(context, c, variablePrefixSupplier)).append(separator));

			String conditionsBufferStr = conditionsBuffer.toString();
			conditionsBufferStr = conditionsBufferStr.endsWith(separator) ? conditionsBufferStr.substring(0,
					conditionsBufferStr.length() - separator.length()) : conditionsBufferStr;

			String conditionTerminator = ")\n";
			if (variableIsTerminal)
			{
				conditionTerminator = "))\n";
			}

			conditionsJoiner.add(conditionsBufferStr + conditionTerminator);
		}
	}

	/**
	 * verifies if the specified variable is terminal (non referenced by any other condition)
	 *
	 * @deprecated since 6.7. Use method isVariableTerminal(String, DroolsRuleGeneratorContext) instead.
	 *
	 * @param variableName
	 *           name of the variable to check
	 * @param patternConditions
	 *           the map of all available conditions
	 * @return true if the variable is terminal
	 */
	@Deprecated
	protected boolean isVariableTerminal(final String variableName,
			final Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions)
	{
		return patternConditions.entrySet().stream().flatMap(m -> m.getValue().stream())
				.filter(c -> c instanceof RuleIrAttributeRelCondition).map(c -> (RuleIrAttributeRelCondition) c)
				.map(RuleIrAttributeRelCondition::getTargetVariable).filter(Objects::nonNull).noneMatch(variableName::equals);
	}

	protected boolean isVariableTerminal(final String variableName, final DroolsRuleGeneratorContext context)
	{
		return isVariableTerminal(variableName, context.getRuleIr().getConditions());
	}

	protected boolean isVariableTerminal(final String variableName, final Collection<RuleIrCondition> conditions)
	{
		if (conditions.stream().filter(c -> c instanceof RuleIrAttributeRelCondition)
				.map(c -> ((RuleIrAttributeRelCondition) c).getTargetVariable()).filter(Objects::nonNull)
				.anyMatch(variableName::equals))
		{
			return false;
		}
		else
		{
			final Collection<RuleIrCondition> conditionsInGroups = getConditionsInGroups(conditions);
			if (conditionsInGroups.isEmpty())
			{
				return true;
			}
			else
			{
				return isVariableTerminal(variableName, conditionsInGroups);
			}
		}
	}

	protected void generatePatternConditions(final DroolsRuleGeneratorContext context,
			final Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions,
			final RuleIrGroupOperator groupOperator, final StringJoiner conditionsJoiner, final String indentation)
	{
		// sort the conditions to avoid variables being used before they are declared
		final Set<Dependency> dependencies = buildDependencies(patternConditions);
		final Map<String, Collection<AbstractRuleIrPatternCondition>> sortedPatternConditions = new TreeMap<>(
				new DependencyComparator(dependencies));
		sortedPatternConditions.putAll(patternConditions);

		for (final Entry<String, Collection<AbstractRuleIrPatternCondition>> entry : sortedPatternConditions.entrySet())
		{
			if (isVariableTerminal(entry.getKey(), context)
					&& entry.getValue().stream().noneMatch(c -> !(c instanceof RuleIrTypeCondition)))
			{
				continue;
			}

			final String variableName = entry.getKey();
			final RuleIrVariable variable = findVariable(context, variableName);
			if (variable == null)
			{
				throw new RuleCompilerException("Variable with name '" + variableName + "' not found");
			}
			final String variableClassName = context.generateClassName(variable.getType());
			if (getExcludedQueryVariableClassNames().contains(variableClassName))
			{
				continue;
			}

			final String separator = (groupOperator == RuleIrGroupOperator.AND) ? ", " : " || ";
			final StringBuilder conditionsBuffer = new StringBuilder(BUFFER_SIZE);

			conditionsBuffer.append(indentation).append(context.getVariablePrefix()).append(variableName).append(" := ")
					.append(variableClassName).append('(');

			entry.getValue()
					.stream()
					.filter(c -> c instanceof AbstractRuleIrAttributeCondition)
					.map(c -> (AbstractRuleIrAttributeCondition) c)
					.forEach(c -> conditionsBuffer.append(evaluateAttributeNameAndOperator(c))
							.append(evaluatePatternConditionType(context, c)).append(separator));

			String conditionsBufferStr = conditionsBuffer.toString();
			conditionsBufferStr = conditionsBufferStr.endsWith(separator) ? conditionsBufferStr.substring(0,
					conditionsBufferStr.length() - separator.length()) : conditionsBufferStr;

			conditionsJoiner.add(conditionsBufferStr + ")\n");
		}
	}

	protected String evaluateAttributeNameAndOperator(final AbstractRuleIrAttributeCondition condition)
	{
		if (condition instanceof RuleIrAttributeCondition)
		{
			final RuleIrAttributeCondition attributeCondition = (RuleIrAttributeCondition) condition;
			if (getTypesToSkipOperatorEvaluation().stream().anyMatch(t -> t.isInstance(attributeCondition.getValue())))
			{
				return StringUtils.EMPTY;
			}
		}

		return condition.getAttribute().concat(" ").concat(condition.getOperator().getOriginalCode()).concat(" ");
	}


	protected String evaluatePatternConditionType(final DroolsRuleGeneratorContext context,
			final AbstractRuleIrPatternCondition patternCondition)
	{
		return evaluatePatternConditionType(context, patternCondition, context::getVariablePrefix);
	}

	protected String evaluatePatternConditionType(final DroolsRuleGeneratorContext context,
			final AbstractRuleIrPatternCondition patternCondition, final Supplier<String> variablePrefixSupplier)
	{
		if (patternCondition instanceof RuleIrAttributeCondition)
		{
			return getDroolsRuleValueFormatter().formatValue(context, patternCondition);
		}
		if (patternCondition instanceof RuleIrAttributeRelCondition)
		{
			final RuleIrAttributeRelCondition attributeRelCondition = (RuleIrAttributeRelCondition) patternCondition;

			final String targetVariableName = attributeRelCondition.getTargetVariable();
			if (isNull(findVariable(context, targetVariableName)))
			{
				throw new RuleCompilerRuntimeException("Variable with name '" + targetVariableName + "' not found");
			}

			String result = variablePrefixSupplier.get() + targetVariableName;

			if (isNotEmpty(attributeRelCondition.getTargetAttribute()))
			{
				result += context.getAttributeDelimiter() + attributeRelCondition.getTargetAttribute();
			}

			return result;
		}
		throw new RuleCompilerRuntimeException(NON_SUPPORTED_CONDITION);
	}

	protected void generateWhenGroupConditions(final DroolsRuleGeneratorContext context,
			final List<RuleIrGroupCondition> groupConditions, final StringJoiner conditionsJoiner, final String indentation)
	{
		generateGroupConditions(groupConditions,
				c -> generateWhenConditions(context, c.getChildren(), c.getOperator(), StringUtils.EMPTY, indentation),
				conditionsJoiner);
	}

	protected void generateGroupConditions(final DroolsRuleGeneratorContext context,
			final List<RuleIrGroupCondition> groupConditions, final StringJoiner conditionsJoiner, final String indentation)
	{
		generateGroupConditions(groupConditions,
				c -> generateConditions(context, c.getChildren(), c.getOperator(), StringUtils.EMPTY, indentation), conditionsJoiner);
	}

	protected void generateGroupConditions(final List<RuleIrGroupCondition> groupConditions,
			final Function<RuleIrGroupCondition, String> generateConditionsFunction, final StringJoiner conditionsJoiner)
	{
		for (final RuleIrGroupCondition groupCondition : groupConditions)
		{
			if (groupCondition.getOperator() == null)
			{
				throw new RuleCompilerException("Group operator cannot be null");
			}

			if (CollectionUtils.isNotEmpty(groupCondition.getChildren()))
			{
				final String generatedConditions = generateConditionsFunction.apply(groupCondition);
				if (StringUtils.isNotEmpty(generatedConditions))
				{
					conditionsJoiner.add(generatedConditions);
				}
			}
		}
	}

	protected void generateExistsConditions(final DroolsRuleGeneratorContext context,
			final List<RuleIrExistsCondition> existsConditions, final StringJoiner conditionsJoiner, final String indentation)
	{
		for (final RuleIrExistsCondition exitsCondition : existsConditions)
		{
			if (CollectionUtils.isNotEmpty(exitsCondition.getChildren()))
			{
				if (exitsCondition.getVariablesContainer() != null)
				{
					context.addLocalVariables(exitsCondition.getVariablesContainer().getVariables());
				}

				final String generatedConditions = generateConditions(context, exitsCondition.getChildren(), RuleIrGroupOperator.AND,
						"exists ", indentation);
				conditionsJoiner.add(generatedConditions);

				if (exitsCondition.getVariablesContainer() != null)
				{
					context.getLocalVariables().pollFirst();
				}
			}
		}
	}

	protected void generateNotConditions(final DroolsRuleGeneratorContext context, final List<RuleIrNotCondition> notConditions,
			final StringJoiner conditionsJoiner, final String indentation)
	{
		generateNotConditions(
				context,
				notConditions,
				(final RuleIrNotCondition notCondition) -> generateConditions(context, notCondition.getChildren(),
						RuleIrGroupOperator.AND, "not ", indentation), conditionsJoiner);
	}

	protected void generateWhenNotConditions(final DroolsRuleGeneratorContext context,
			final List<RuleIrNotCondition> notConditions, final StringJoiner conditionsJoiner, final String indentation)
	{
		generateNotConditions(
				context,
				notConditions,
				(final RuleIrNotCondition notCondition) -> generateWhenConditions(context, notCondition.getChildren(),
						RuleIrGroupOperator.AND, "not ", indentation), conditionsJoiner);
	}

	protected void generateNotConditions(final DroolsRuleGeneratorContext context, final List<RuleIrNotCondition> notConditions,
			final Function<RuleIrNotCondition, String> generateConditionsSupplier, final StringJoiner conditionsJoiner)
	{
		for (final RuleIrNotCondition notCondition : notConditions)
		{
			if (CollectionUtils.isNotEmpty(notCondition.getChildren()))
			{
				if (notCondition.getVariablesContainer() != null)
				{
					context.addLocalVariables(notCondition.getVariablesContainer().getVariables());
				}

				final String generatedConditions = generateConditionsSupplier.apply(notCondition);
				if (StringUtils.isNotEmpty(generatedConditions))
				{
					conditionsJoiner.add(generatedConditions);
				}

				if (notCondition.getVariablesContainer() != null)
				{
					context.getLocalVariables().pollFirst();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	protected void generateExecutableConditions(final DroolsRuleGeneratorContext context,
			final List<RuleIrExecutableCondition> executableConditions, final StringJoiner conditionsJoiner, final String indentation)
	{
		// not implemented yet
	}

	protected RuleIrVariable findVariable(final DroolsRuleGeneratorContext context, final String variableName)
	{
		for (final Map<String, RuleIrVariable> variables : context.getLocalVariables())
		{
			final RuleIrVariable variable = variables.get(variableName);
			if (variable != null)
			{
				return variable;
			}
		}

		return context.getVariables().get(variableName);
	}

	protected Set<Dependency> buildDependencies(final Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions)
	{
		final Set<Dependency> dependencies = new HashSet<>();

		for (final Collection<AbstractRuleIrPatternCondition> conditions : patternConditions.values())
		{
			for (final AbstractRuleIrPatternCondition condition : conditions)
			{
				if (condition instanceof RuleIrAttributeRelCondition)
				{
					final RuleIrAttributeRelCondition attributeRelCondition = (RuleIrAttributeRelCondition) condition;
					final Dependency dependency = new Dependency(attributeRelCondition.getVariable(),
							attributeRelCondition.getTargetVariable());
					dependencies.add(dependency);
				}
			}
		}

		expandDependencies(dependencies);

		return dependencies;
	}

	protected void expandDependencies(final Set<Dependency> dependencies)
	{
		final Set<Dependency> newDependencies = new HashSet<>();

		for (final Dependency dependency1 : dependencies)
		{
			final String source = dependency1.source;

			for (final Dependency dependency2 : dependencies)
			{
				if (dependency2.source.equals(dependency1.target))
				{
					final String target = dependency2.target;
					newDependencies.add(new Dependency(source, target));
				}
			}
		}

		if (dependencies.addAll(newDependencies))
		{
			expandDependencies(dependencies);
		}
	}

	public DroolsRuleValueFormatter getDroolsRuleValueFormatter()
	{
		return droolsRuleValueFormatter;
	}

	@Required
	public void setDroolsRuleValueFormatter(final DroolsRuleValueFormatter droolsRuleValueFormatter)
	{
		this.droolsRuleValueFormatter = droolsRuleValueFormatter;
	}

	protected List<String> getExcludedQueryVariableClassNames()
	{
		return excludedQueryVariableClassNames;
	}

	@Required
	public void setExcludedQueryVariableClassNames(final List<String> excludedQueryVariableClassNames)
	{
		this.excludedQueryVariableClassNames = excludedQueryVariableClassNames;
	}

	protected List<Class<?>> getTypesToSkipOperatorEvaluation()
	{
		return typesToSkipOperatorEvaluation;
	}

	@Required
	public void setTypesToSkipOperatorEvaluation(final List<Class<?>> typesToSkipOperatorEvaluation)
	{
		this.typesToSkipOperatorEvaluation = typesToSkipOperatorEvaluation;
	}

	protected static class Dependency
	{
		private final String source;
		private final String target;

		public Dependency(final String source, final String target)
		{
			this.source = source;
			this.target = target;
		}

		public String getVariable1()
		{
			return source;
		}

		public String getVariable2()
		{
			return target;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj == null)
			{
				return false;
			}

			if (!(obj.getClass().equals(Dependency.class)))
			{
				return false;
			}

			final Dependency other = (Dependency) obj;

			return Objects.equals(source, other.source) && Objects.equals(target, other.target);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(source, target);
		}

		@Override
		public String toString()
		{
			return source + " -> " + target;
		}
	}

	protected static class DependencyComparator implements Comparator<String>
	{
		private final Set<Dependency> dependencies;

		public DependencyComparator(final Set<Dependency> dependencies)
		{
			this.dependencies = dependencies;
		}

		@Override
		public int compare(final String variable1, final String variable2)
		{
			if (dependencies.contains(new Dependency(variable1, variable2)))
			{
				return 1;
			}
			else if (dependencies.contains(new Dependency(variable2, variable1)))
			{
				return -1;
			}
			else
			{
				return variable1.compareTo(variable2);
			}
		}
	}
}
