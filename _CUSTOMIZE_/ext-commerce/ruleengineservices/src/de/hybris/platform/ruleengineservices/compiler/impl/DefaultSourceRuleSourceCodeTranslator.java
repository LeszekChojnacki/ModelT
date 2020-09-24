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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.compiler.RuleActionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAction;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslator;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizerStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;


/**
 * Default implementation of {@link RuleSourceCodeTranslator} for source rules.
 */
public class DefaultSourceRuleSourceCodeTranslator implements RuleSourceCodeTranslator
{
	private RuleConditionsService ruleConditionsService;
	private RuleActionsService ruleActionsService;
	private RuleConditionsTranslator ruleConditionsTranslator;
	private RuleActionsTranslator ruleActionsTranslator;
	private RuleCompilerProblemFactory ruleCompilerProblemFactory;
	private RuleParameterValueNormalizerStrategy ruleParameterValueNormalizerStrategy;

	@Override
	public RuleIr translate(final RuleCompilerContext context)
	{
		try
		{
			if (!(context.getRule() instanceof SourceRuleModel))
			{
				throw new RuleCompilerException("Rule is not of type SourceRule");
			}

			final SourceRuleModel rule = (SourceRuleModel) context.getRule();
			final RuleIr ruleIr = new RuleIr();
			ruleIr.setVariablesContainer(context.getVariablesGenerator().getRootContainer());

			final List<RuleConditionData> ruleConditions = getRuleConditionsService().convertConditionsFromString(rule.getConditions(),
					context.getConditionDefinitions());
			populateRuleParametersFromConditions(context, ruleConditions);
			addRuleConditionsToContext(context, ruleConditions);

			final List<RuleActionData> ruleActions = getRuleActionsService().convertActionsFromString(rule.getActions(),
					context.getActionDefinitions());
			populateRuleParametersFromActions(context, ruleActions);

			final List<RuleIrCondition> ruleIrConditions = getRuleConditionsTranslator().translate(context, ruleConditions);
			ruleIr.setConditions(ruleIrConditions);
			final List<RuleIrAction> ruleIrActions = getRuleActionsTranslator().translate(context, ruleActions);
			ruleIr.setActions(ruleIrActions);

			validate(context, ruleConditions, ruleActions);

			return ruleIr;
		}
		catch (final RuleEngineServiceException e)
		{
			throw new RuleCompilerException(e);
		}
	}

	protected void addRuleConditionsToContext(final RuleCompilerContext context, final List<RuleConditionData> ruleConditions)
	{
		if (CollectionUtils.isEmpty(ruleConditions))
		{
			return;
		}

		for (final RuleConditionData condition : ruleConditions)
		{
			context.getRuleConditions().add(condition);
			addRuleConditionsToContext(context, condition.getChildren());
		}
	}

	protected void populateRuleParametersFromConditions(final RuleCompilerContext context, final List<RuleConditionData> conditions)
	{
		if (CollectionUtils.isEmpty(conditions))
		{
			return;
		}

		for (final RuleConditionData condition : conditions)
		{
			if (MapUtils.isNotEmpty(condition.getParameters()))
			{
				for (final RuleParameterData parameter : condition.getParameters().values())
				{
					normalizeRuleParameter(parameter);
					context.getRuleParameters().add(parameter);
				}
			}

			populateRuleParametersFromConditions(context, condition.getChildren());
		}
	}

	protected void populateRuleParametersFromActions(final RuleCompilerContext context, final List<RuleActionData> actions)
	{
		if (CollectionUtils.isEmpty(actions))
		{
			return;
		}

		for (final RuleActionData action : actions)
		{
			if (MapUtils.isNotEmpty(action.getParameters()))
			{
				for (final RuleParameterData parameter : action.getParameters().values())
				{
					normalizeRuleParameter(parameter);
					context.getRuleParameters().add(parameter);
				}
			}
		}
	}

	protected void normalizeRuleParameter(final RuleParameterData parameter)
	{
		parameter.setValue(getRuleParameterValueNormalizerStrategy().normalize(parameter.getValue(), parameter.getType()));
	}

	protected void validate(final RuleCompilerContext context, final List<RuleConditionData> conditions,
			final List<RuleActionData> actions)
	{
		if (CollectionUtils.isEmpty(actions))
		{
			final RuleCompilerProblem problem = getRuleCompilerProblemFactory().createProblem(Severity.ERROR,
					"rule.compiler.error.actions.empty");
			context.addProblem(problem);
			return;
		}

		getRuleConditionsTranslator().validate(context, conditions);
		getRuleActionsTranslator().validate(context, actions);
	}

	protected RuleConditionsService getRuleConditionsService()
	{
		return ruleConditionsService;
	}

	@Required
	public void setRuleConditionsService(final RuleConditionsService ruleConditionsService)
	{
		this.ruleConditionsService = ruleConditionsService;
	}

	public RuleActionsService getRuleActionsService()
	{
		return ruleActionsService;
	}

	@Required
	public void setRuleActionsService(final RuleActionsService ruleActionsService)
	{
		this.ruleActionsService = ruleActionsService;
	}

	protected RuleConditionsTranslator getRuleConditionsTranslator()
	{
		return ruleConditionsTranslator;
	}

	@Required
	public void setRuleConditionsTranslator(final RuleConditionsTranslator ruleConditionsTranslator)
	{
		this.ruleConditionsTranslator = ruleConditionsTranslator;
	}

	protected RuleActionsTranslator getRuleActionsTranslator()
	{
		return ruleActionsTranslator;
	}

	@Required
	public void setRuleActionsTranslator(final RuleActionsTranslator ruleActionsTranslator)
	{
		this.ruleActionsTranslator = ruleActionsTranslator;
	}

	protected RuleCompilerProblemFactory getRuleCompilerProblemFactory()
	{
		return ruleCompilerProblemFactory;
	}

	@Required
	public void setRuleCompilerProblemFactory(final RuleCompilerProblemFactory ruleCompilerProblemFactory)
	{
		this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
	}

	protected RuleParameterValueNormalizerStrategy getRuleParameterValueNormalizerStrategy()
	{
		return ruleParameterValueNormalizerStrategy;
	}

	@Required
	public void setRuleParameterValueNormalizerStrategy(
			final RuleParameterValueNormalizerStrategy ruleParameterValueNormalizerStrategy)
	{
		this.ruleParameterValueNormalizerStrategy = ruleParameterValueNormalizerStrategy;
	}
}
