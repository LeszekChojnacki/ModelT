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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContextFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerListener;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerListenersFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult.Result;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResultFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerService;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessor;
import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessorFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGeneratorFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleSourceCodeTranslatorFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleTargetCodeGenerator;
import de.hybris.platform.ruleengineservices.compiler.RuleTargetCodeGeneratorFactory;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContextProvider;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default implementation of {@link RuleCompilerService}.
 */
public class DefaultRuleCompilerService implements RuleCompilerService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultRuleCompilerService.class);

	private RuleCompilerListenersFactory ruleCompilerListenersFactory;
	private RuleIrVariablesGeneratorFactory ruleIrVariablesGeneratorFactory;
	private RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory;
	private RuleSourceCodeTranslatorFactory ruleSourceCodeTranslatorFactory;
	private RuleIrProcessorFactory ruleIrProcessorFactory;
	private RuleTargetCodeGeneratorFactory ruleTargetCodeGeneratorFactory;
	private RuleCompilerResultFactory ruleCompilerResultFactory;
	private ModelService modelService;
	private RuleCompilationContextProvider ruleCompilationContextProvider;

	@Override
	public RuleCompilerResult compile(final AbstractRuleModel rule, final String moduleName)
	{
		return compile(getRuleCompilationContextProvider().getRuleCompilationContext(), rule, moduleName);
	}

	@Override
	public RuleCompilerResult compile(final RuleCompilationContext ruleCompilationContext, final AbstractRuleModel rule,
			final String moduleName)
	{
		final List<RuleCompilerListener> listeners = getRuleCompilerListenersFactory().getListeners(RuleCompilerListener.class);
		final RuleIrVariablesGenerator variablesGenerator = getRuleIrVariablesGeneratorFactory().createVariablesGenerator();
		final DefaultRuleCompilerContext context = getRuleCompilerContextFactory()
				.createContext(ruleCompilationContext, rule, moduleName, variablesGenerator);

		try
		{
			executeBeforeCompileListeners(context, listeners);

			// convert the rule to the intermediate representation
			final RuleSourceCodeTranslator sourceCodeTranslator = getRuleSourceCodeTranslatorFactory()
					.getSourceCodeTranslator(context);
			final RuleIr ruleIr = sourceCodeTranslator.translate(context);

			final RuleCompilerResult result = getRuleCompilerResultFactory().create(rule, context.getProblems());
			if (result.getResult() == Result.ERROR)
			{
				executeAfterCompileErrorListeners(context, listeners);
				return result;
			}

			// process the intermediate representation
			final List<RuleIrProcessor> irProcessors = getRuleIrProcessorFactory().getRuleIrProcessors();
			for (final RuleIrProcessor irProcessor : irProcessors)
			{
				irProcessor.process(context, ruleIr);
			}

			// convert the intermediate representation to the rule engine format
			final RuleTargetCodeGenerator targetCodeGenerator = getRuleTargetCodeGeneratorFactory().getTargetCodeGenerator(context);
			targetCodeGenerator.generate(context, ruleIr);

			executeAfterCompileListeners(context, listeners);

			return getRuleCompilerResultFactory().create(result, context.getRuleVersion());

		}
		catch (final Exception e)
		{
			LOG.error("Exception caught", e);
			executeAfterCompileErrorListeners(context, listeners);
			final String errorMessage = String.format("Exception caught - %s: %s", e.getClass().getName(), e.getMessage());
			final List<RuleCompilerProblem> ruleCompilerProblems = Lists
					.newArrayList(new DefaultRuleCompilerProblem(RuleCompilerProblem.Severity.ERROR, errorMessage));
			return getRuleCompilerResultFactory().create(rule, ruleCompilerProblems);
		}
	}

	protected void executeBeforeCompileListeners(final DefaultRuleCompilerContext context,
			final List<RuleCompilerListener> listeners)
	{
		for (final RuleCompilerListener listener : listeners)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running beforeCompile listener for: {}", listener.getClass().getCanonicalName());
			}

			listener.beforeCompile(context);
		}
	}

	protected void executeAfterCompileListeners(final DefaultRuleCompilerContext context,
			final List<RuleCompilerListener> listeners)
	{
		for (final RuleCompilerListener listener : Lists.reverse(listeners))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Running afterCompile listener for: {}", listener.getClass().getCanonicalName());
			}

			listener.afterCompile(context);
		}
	}

	protected void executeAfterCompileErrorListeners(final DefaultRuleCompilerContext context,
			final List<RuleCompilerListener> listeners)
	{
		for (final RuleCompilerListener listener : Lists.reverse(listeners))
		{
			try
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Running afterCompileError listener for: {}", listener.getClass().getCanonicalName());
				}

				listener.afterCompileError(context);
			}
			catch (final Exception exception)
			{
				context.addFailureException(exception);
			}
		}
	}

	protected RuleCompilerListenersFactory getRuleCompilerListenersFactory()
	{
		return ruleCompilerListenersFactory;
	}

	@Required
	public void setRuleCompilerListenersFactory(final RuleCompilerListenersFactory ruleCompilerListenersFactory)
	{
		this.ruleCompilerListenersFactory = ruleCompilerListenersFactory;
	}

	protected RuleSourceCodeTranslatorFactory getRuleSourceCodeTranslatorFactory()
	{
		return ruleSourceCodeTranslatorFactory;
	}

	protected RuleIrVariablesGeneratorFactory getRuleIrVariablesGeneratorFactory()
	{
		return ruleIrVariablesGeneratorFactory;
	}

	@Required
	public void setRuleIrVariablesGeneratorFactory(final RuleIrVariablesGeneratorFactory ruleIrVariablesGeneratorFactory)
	{
		this.ruleIrVariablesGeneratorFactory = ruleIrVariablesGeneratorFactory;
	}

	protected RuleCompilerContextFactory<DefaultRuleCompilerContext> getRuleCompilerContextFactory()
	{
		return ruleCompilerContextFactory;
	}

	@Required
	public void setRuleCompilerContextFactory(
			final RuleCompilerContextFactory<DefaultRuleCompilerContext> ruleCompilerContextFactory)
	{
		this.ruleCompilerContextFactory = ruleCompilerContextFactory;
	}

	@Required
	public void setRuleSourceCodeTranslatorFactory(final RuleSourceCodeTranslatorFactory ruleSourceCodeTranslatorFactory)
	{
		this.ruleSourceCodeTranslatorFactory = ruleSourceCodeTranslatorFactory;
	}

	protected RuleIrProcessorFactory getRuleIrProcessorFactory()
	{
		return ruleIrProcessorFactory;
	}

	@Required
	public void setRuleIrProcessorFactory(final RuleIrProcessorFactory ruleIrProcessorFactory)
	{
		this.ruleIrProcessorFactory = ruleIrProcessorFactory;
	}

	protected RuleTargetCodeGeneratorFactory getRuleTargetCodeGeneratorFactory()
	{
		return ruleTargetCodeGeneratorFactory;
	}

	@Required
	public void setRuleTargetCodeGeneratorFactory(final RuleTargetCodeGeneratorFactory ruleTargetCodeGeneratorFactory)
	{
		this.ruleTargetCodeGeneratorFactory = ruleTargetCodeGeneratorFactory;
	}

	protected RuleCompilerResultFactory getRuleCompilerResultFactory()
	{
		return ruleCompilerResultFactory;
	}

	@Required
	public void setRuleCompilerResultFactory(final RuleCompilerResultFactory ruleCompilerResultFactory)
	{
		this.ruleCompilerResultFactory = ruleCompilerResultFactory;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected RuleCompilationContextProvider getRuleCompilationContextProvider()
	{
		return ruleCompilationContextProvider;
	}

	@Required
	public void setRuleCompilationContextProvider(
			final RuleCompilationContextProvider ruleCompilationContextProvider)
	{
		this.ruleCompilationContextProvider = ruleCompilationContextProvider;
	}
}
