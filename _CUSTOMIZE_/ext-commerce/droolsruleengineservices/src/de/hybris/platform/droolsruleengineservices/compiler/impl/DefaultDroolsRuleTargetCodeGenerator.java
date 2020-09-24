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

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.DEFAULT_DROOLS_DATE_FORMAT;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.DROOLS_DATE_FORMAT_KEY;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MAXIMUM_RULE_EXECUTIONS;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MODULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_CODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULEGROUP_EXCLUSIVE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfAnyResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleActionsGenerator;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleConditionsGenerator;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleGeneratorContext;
import de.hybris.platform.droolsruleengineservices.compiler.DroolsRuleMetadataGenerator;
import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.strategies.DroolsKIEBaseFinderStrategy;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerException;
import de.hybris.platform.ruleengineservices.compiler.RuleIr;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariable;
import de.hybris.platform.ruleengineservices.compiler.RuleTargetCodeGenerator;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.RuleGroupModel;
import de.hybris.platform.ruleengineservices.rrd.EvaluationTimeRRD;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;
import de.hybris.platform.ruleengineservices.rule.evaluation.impl.RuleAndRuleGroupExecutionTracker;
import de.hybris.platform.ruleengineservices.rule.services.RuleParametersService;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class DefaultDroolsRuleTargetCodeGenerator implements RuleTargetCodeGenerator
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDroolsRuleTargetCodeGenerator.class);

	public static final int BUFFER_SIZE = 4096;
	public static final int RULE_CONFIG_BUFFER_SIZE = 128;
	public static final String DROOLS_RULES_PACKAGE = "de.hybris.platform.droolsruleengine";
	public static final Locale DEFAULT_LOCALE = Locale.UK;

	private RuleParametersService ruleParametersService;
	private ModelService modelService;
	private RuleEngineService platformRuleEngineService;
	private DroolsRuleConditionsGenerator droolsRuleConditionsGenerator;
	private DroolsRuleActionsGenerator droolsRuleActionsGenerator;
	private DroolsRuleMetadataGenerator droolsRuleMetadataGenerator;
	private CommonI18NService commonI18NService;
	private ConfigurationService configurationService;
	private RuleService ruleService;
	private DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy;
	private RulesModuleDao rulesModuleDao;
	private boolean useDeprecatedRRDsInRules = true;

	@Override
	public void generate(final RuleCompilerContext context, @Nonnull final RuleIr ruleIr)
	{
		validateParameterNotNull(ruleIr, "RuleIr object cannot be null");
		validateIfAnyResult(ruleIr.getActions(), "Actions cannot be null or empty");
		validateParameterNotNull(context, "RuleCompilerContext must be provided");
		final String moduleName = context.getModuleName();
		validateParameterNotNull(moduleName, "Rules module name must be correctly set in the compiler context");
		final AbstractRuleModel rule = context.getRule();
		final String ruleCode = rule.getCode();
		DroolsRuleModel droolsRule;
		final AbstractRuleEngineRuleModel engineRule = getPlatformRuleEngineService().getRuleForCodeAndModule(ruleCode, moduleName);

		if (isNull(engineRule))
		{
			droolsRule = getModelService().create(DroolsRuleModel.class);
			final RuleType ruleType = getRuleService().getEngineRuleTypeForRuleType(rule.getClass());
			droolsRule.setRuleType(ruleType);
			droolsRule.setCode(ruleCode);
			LOGGER.debug("creating new drools rule for type and code: {} {}", ruleCode, ruleType);
		}
		else
		{
			if (engineRule instanceof DroolsRuleModel)
			{
				droolsRule = (DroolsRuleModel) engineRule;
				LOGGER.debug("using existing drools rule for code: {}", ruleCode);
			}
			else
			{
				throw new RuleCompilerException(String.format("Given rule with the code: %s is not of the type DroolsRuleModel.",
						ruleCode));
			}
		}
		droolsRule.setUuid(UUID.randomUUID().toString());
		droolsRule.setSourceRule(rule);
		droolsRule.setActive(Boolean.TRUE);

		final String ruleGroupCode = getRuleGroupCode(rule);
		droolsRule.setRuleGroupCode(ruleGroupCode);

		for (final LanguageModel language : getCommonI18NService().getAllLanguages())
		{
			final Locale locale = getCommonI18NService().getLocaleForLanguage(language);
			droolsRule.setMessageFired(rule.getMessageFired(locale), locale);
		}

		try
		{
			final String ruleParameters = getRuleParametersService().convertParametersToString(context.getRuleParameters());
			droolsRule.setRuleParameters(ruleParameters);
		}
		catch (final RuleConverterException e)
		{
			throw new RuleCompilerException("RuleConverterException caught: ", e);
		}

		final DroolsRuleGeneratorContext generatorContext = createGeneratorContext(context, ruleIr, droolsRule);

		final String ruleContent = generateRuleContent(generatorContext);

		final Map<String, String> globals = generateGlobals(generatorContext);

		droolsRule.setRuleContent(ruleContent);
		droolsRule.setGlobals(globals);
		droolsRule.setMaxAllowedRuns(rule.getMaxAllowedRuns());
		droolsRule.setRulePackage(DROOLS_RULES_PACKAGE);

		final DroolsKIEModuleModel rulesModule = getRulesModuleDao().findByName(moduleName);
		final DroolsKIEBaseModel baseForKIEModule = getDroolsKIEBaseFinderStrategy().getKIEBaseForKIEModule(rulesModule);

		droolsRule.setKieBase(baseForKIEModule);
		setVersionIfAbsent(context.getRuleCompilationContext(), droolsRule, moduleName);
		getModelService().save(droolsRule);

		LOGGER.debug("rule for code '{}' compiled:", ruleCode);
		LOGGER.debug("source rule code and version: {} {}", ruleCode, rule.getVersion());
		LOGGER.debug("rule group code: {}", ruleGroupCode);
		LOGGER.debug("max allowed runs: {}", rule.getMaxAllowedRuns());
		LOGGER.debug("rule module: {}", moduleName);
		LOGGER.debug("drl content:");
		LOGGER.debug("------------");
		LOGGER.debug("{}", ruleContent);
		LOGGER.debug("------------");

	}

	protected void setVersionIfAbsent(final RuleCompilationContext ruleCompilationContext,
			final AbstractRuleEngineRuleModel ruleModel, final String moduleName)
	{
		if (isNull(ruleModel.getVersion()))
		{
			final Long nextRuleEngineRuleVersion = ruleCompilationContext.getNextRuleEngineRuleVersion(moduleName);
			ruleModel.setVersion(nextRuleEngineRuleVersion);
		}
	}

	protected String generateRuleContent(final DroolsRuleGeneratorContext context)
	{
		final StringBuilder ruleContent = new StringBuilder(BUFFER_SIZE);

		final String indentation = context.getIndentationSize();
		final String generatedConditions = getDroolsRuleConditionsGenerator().generateConditions(context, indentation);
		final String generatedActions = getDroolsRuleActionsGenerator().generateActions(context, indentation);
		final String generatedQuery = generateRuleContentQuery(context, generatedConditions);
		final String metadata = getDroolsRuleMetadataGenerator().generateMetadata(context, indentation);
		final String generatedRule = generateRuleContentRule(context, generatedActions, metadata);

		ruleContent.append("package ").append(DROOLS_RULES_PACKAGE).append(";\n\n");

		for (final Class<?> importType : context.getImports())
		{
			ruleContent.append("import ").append(importType.getName()).append(";\n");
		}

		ruleContent.append('\n');

		for (final Map.Entry<String, Class<?>> globalEntry : context.getGlobals().entrySet())
		{
			ruleContent.append("global ").append(globalEntry.getValue().getName()).append(' ').append(globalEntry.getKey())
					.append(";\n");
		}

		ruleContent.append('\n');
		ruleContent.append(generatedQuery);
		ruleContent.append('\n');
		ruleContent.append(generatedRule);

		return ruleContent.toString();
	}

	@SuppressWarnings("unused")
	protected String generateRuleContentQuery(final DroolsRuleGeneratorContext context, final String conditions)
	{
		final StringJoiner queryParameters = new StringJoiner(", ");

		final Map<String, RuleIrVariable> variables = context.getVariables();
		if (MapUtils.isNotEmpty(variables))
		{
			for (final RuleIrVariable variable : variables.values())
			{
				final String variableClassName = context.generateClassName(variable.getType());
				queryParameters.add(variableClassName + " " + context.getVariablePrefix() + variable.getName());
			}
		}

		final StringBuilder buffer = new StringBuilder(BUFFER_SIZE);
		final DroolsRuleModel droolsRule = context.getDroolsRule();
		final String uuid = droolsRule.getUuid().replaceAll("-", "");

		buffer.append("query rule_").append(uuid).append("_query(").append(queryParameters.toString()).append(")\n");
		buffer.append(conditions);
		buffer.append("end\n");

		return buffer.toString();
	}

	protected String generateRuleContentRule(final DroolsRuleGeneratorContext context, final String actions, final String metadata)
	{
		final AbstractRuleModel rule = context.getRuleCompilerContext().getRule();
		final DroolsRuleModel droolsRule = context.getDroolsRule();

		final StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

		buffer.append("rule \"").append(droolsRule.getUuid()).append("\"\n");
		buffer.append("@" + RULEMETADATA_RULECODE + "(\"").append(rule.getCode()).append("\")\n"); // NOSONAR
		buffer.append("@" + RULEMETADATA_MODULENAME + "(\"").append(context.getRuleCompilerContext().getModuleName())
				.append("\")\n");
		if (nonNull(rule.getMaxAllowedRuns()))
		{
			buffer.append("@" + RULEMETADATA_MAXIMUM_RULE_EXECUTIONS + "(\"").append(rule.getMaxAllowedRuns()).append("\")\n");
		}
		if (nonNull(rule.getRuleGroup()))
		{
			buffer.append("@" + RULEMETADATA_RULEGROUP_CODE + "(\"").append(rule.getRuleGroup().getCode()).append("\")\n");
			buffer.append("@" + RULEMETADATA_RULEGROUP_EXCLUSIVE + "(\"").append(rule.getRuleGroup().isExclusive()).append("\")\n");
		}
		buffer.append(metadata);
		buffer.append("dialect \"mvel\" \n");
		buffer.append("salience ").append(rule.getPriority()).append('\n');
		buffer.append("when\n");

		final String requiredFactsCheckPattern = getDroolsRuleConditionsGenerator().generateRequiredFactsCheckPattern(context);
		buffer.append(generateRequiredFactsCheck(context, requiredFactsCheckPattern));
		buffer.append(generateDateRangeCondition(context, rule));
		buffer.append(generateTypeVariables(context));
		buffer.append(generateAccumulateFunction(context, droolsRule));
		if (isUseDeprecatedRRDsInRules())
		{
			buffer.append(generateConfigVariable(context, rule));
			buffer.append(generateGroupExecutionVariable(context, rule));
		}
		else
		{
			buffer.append(generateTrackerVariable(context, rule));
		}
		buffer.append(generateResultCountCondition(context));

		buffer.append("then\n");
		buffer.append(actions);
		buffer.append("end\n");

		return buffer.toString();
	}

	protected StringBuilder generateTypeVariables(final DroolsRuleGeneratorContext context)
	{
		final String typeVariables = getDroolsRuleConditionsGenerator().generateRequiredTypeVariables(context);

		return StringUtils.isNotEmpty(typeVariables) ? new StringBuilder(RULE_CONFIG_BUFFER_SIZE).append(typeVariables)
				: new StringBuilder();
	}

	protected StringBuilder generateResultCountCondition(final DroolsRuleGeneratorContext context)
	{
		final String l1Indentation = context.getIndentationSize();
		if (isUseDeprecatedRRDsInRules())
		{
			// for backwards compatibility
			return new StringBuilder(l1Indentation).append("eval($result_count > 0 && $groupExecution.allowedToExecute($config))\n");
		}
		return new StringBuilder(l1Indentation).append("eval($result_count > 0)\n");
	}

	/**
	 * @deprecated since 18.11 to enable {@link RuleAndRuleGroupExecutionTracker} set
	 *             <code>ruleengineservices.use.deprecated.rrd.objects<code> to false and use
	 *             {@link #generateTrackerVariable(DroolsRuleGeneratorContext, AbstractRuleModel)} instead
	 */
	@Deprecated
	protected StringBuilder generateConfigVariable(final DroolsRuleGeneratorContext context, final AbstractRuleModel rule)
	{
		final String l1Indentation = context.getIndentationSize();
		final String ruleConfigurationClassName = context.generateClassName(RuleConfigurationRRD.class);

		return new StringBuilder(RULE_CONFIG_BUFFER_SIZE).append(l1Indentation).append("$config := ")
				.append(ruleConfigurationClassName).append("(ruleCode == \"").append(rule.getCode()).append("\")\n");
	}

	/**
	 * @deprecated since 18.11 to enable {@link RuleAndRuleGroupExecutionTracker} set
	 *             <code>ruleengineservices.use.deprecated.rrd.objects<code> to false and use
	 *             {@link #generateTrackerVariable(DroolsRuleGeneratorContext, AbstractRuleModel)} instead
	 */
	@Deprecated
	protected StringBuilder generateGroupExecutionVariable(final DroolsRuleGeneratorContext context, final AbstractRuleModel rule)
	{
		final String l1Indentation = context.getIndentationSize();
		final String ruleGroupExecutionClassName = context.generateClassName(RuleGroupExecutionRRD.class);

		return new StringBuilder(RULE_CONFIG_BUFFER_SIZE).append(l1Indentation).append("$groupExecution := ")
				.append(ruleGroupExecutionClassName).append("(code == $config.ruleGroupCode)\n");
	}

	protected StringBuilder generateTrackerVariable(final DroolsRuleGeneratorContext context, final AbstractRuleModel rule)
	{
		final String l1Indentation = context.getIndentationSize();
		final String executionTrackerClassName = context.generateClassName(RuleAndRuleGroupExecutionTracker.class);

		return new StringBuilder(RULE_CONFIG_BUFFER_SIZE).append(l1Indentation).append("$executionTracker := ")
				.append(executionTrackerClassName).append("()\n");
	}

	protected StringBuilder generateRequiredFactsCheck(final DroolsRuleGeneratorContext context, final String conditions)
	{
		if (StringUtils.isNotEmpty(conditions))
		{
			final String indentation = context.getIndentationSize();
			return new StringBuilder(RULE_CONFIG_BUFFER_SIZE).append(indentation).append(conditions);
		}
		else
		{
			return new StringBuilder();
		}
	}

	protected StringBuilder generateAccumulateFunction(final DroolsRuleGeneratorContext context, final DroolsRuleModel droolsRule)
	{
		final String l1Indentation = context.getIndentationSize();
		final String l2Indentation = l1Indentation + context.getIndentationSize();
		final Map<String, RuleIrVariable> variables = context.getVariables();
		final StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

		buffer.append(l1Indentation).append("accumulate (\n");

		final StringJoiner queryParameters = new StringJoiner(", ");
		for (final RuleIrVariable variable : variables.values())
		{
			queryParameters.add(context.getVariablePrefix() + variable.getName());
		}

		final String uuid = droolsRule.getUuid().replaceAll("-", "");
		buffer.append(l2Indentation).append("rule_").append(uuid).append("_query(").append(queryParameters).append(";)\n");
		buffer.append(l1Indentation).append(";\n");

		final StringJoiner accumulateFunctions = new StringJoiner(",\n");
		for (final RuleIrVariable variable : variables.values())
		{
			accumulateFunctions.add(l2Indentation + context.getVariablePrefix() + variable.getName() + "_set : collectSet("
					+ context.getVariablePrefix() + variable.getName() + ")");
		}

		accumulateFunctions.add(l2Indentation + "$result_count : count(1)");

		buffer.append(accumulateFunctions).append('\n');
		buffer.append(l1Indentation).append(")\n");
		return buffer;
	}

	/**
	 * @deprecated since 6.6. It's deprecated because of the changes in the rule action execution logic
	 */
	@Deprecated
	protected StringBuilder generateRuleGroupCondition(final DroolsRuleGeneratorContext context, // NOSONAR
			final AbstractRuleModel rule)
	{
		final StringBuilder bufferRuleGroupCondition = new StringBuilder(BUFFER_SIZE);
		final String ruleGroupCode = getRuleGroupCode(rule);
		if (!rule.getStackable().booleanValue() && StringUtils.isNotEmpty(ruleGroupCode))
		{
			final String ruleGroupExecutionClassName = context.generateClassName(RuleGroupExecutionRRD.class);
			bufferRuleGroupCondition.append(context.getIndentationSize()).append("exists (").append(ruleGroupExecutionClassName)
					.append("(code == \"").append(ruleGroupCode).append("\", allowedToExecute($config) == true))\n");
		}
		return bufferRuleGroupCondition;
	}

	protected StringBuilder generateDateRangeCondition(final DroolsRuleGeneratorContext context, final AbstractRuleModel rule)
	{
		final StringBuilder builder = new StringBuilder(BUFFER_SIZE);
		final Date startDate = rule.getStartDate();
		final Date endDate = rule.getEndDate();
		if (startDate != null || endDate != null)
		{
			final String evaluationTimeClassName = context.generateClassName(EvaluationTimeRRD.class);
			final String indentation = context.getIndentationSize();
			final String startDateCondition = startDate != null ? String.format("evaluationTime >= %d", startDate.getTime()) : "";
			final String endDateCondition = endDate != null ? String.format("evaluationTime <= %d", endDate.getTime()) : "";
			final String dateConditionDelimiter = StringUtils.isNotEmpty(startDateCondition)
					&& StringUtils.isNotEmpty(endDateCondition) ? " && " : "";
			builder.append(indentation).append("$evaluationTimeRRD := ").append(evaluationTimeClassName).append("(")
					.append(startDateCondition).append(dateConditionDelimiter).append(endDateCondition).append(")\n");
		}
		return builder;
	}

	protected String getFormattedDateString(final Date date)
	{
		final String dateFormatString = getConfigurationService().getConfiguration().getString(DROOLS_DATE_FORMAT_KEY,
				DEFAULT_DROOLS_DATE_FORMAT);
		final DateFormat dateFormat = new SimpleDateFormat(dateFormatString, DEFAULT_LOCALE);
		return dateFormat.format(date);
	}

	protected Map<String, String> generateGlobals(final DroolsRuleGeneratorContext context)
	{
		final Map<String, String> globals = new HashMap<>();

		for (final String global : context.getGlobals().keySet())
		{
			globals.put(global, global);
		}

		return globals;
	}

	protected DroolsRuleGeneratorContext createGeneratorContext(final RuleCompilerContext context, final RuleIr ruleIr,
			final DroolsRuleModel droolsRule)
	{
		return new DefaultDroolsGeneratorContext(context, ruleIr, droolsRule);
	}

	protected void updateEngineRule(final AbstractRuleEngineRuleModel engineRule, final AbstractRulesModuleModel rulesModule)

	{
		final RuleEngineActionResult result = getPlatformRuleEngineService().updateEngineRule(engineRule, rulesModule);
		if (result.isActionFailed())
		{
			throw new RuleCompilerException("Rule compilation failed when updating the compiled rule:"
					+ result.getMessagesAsString(MessageLevel.ERROR));
		}
	}

	protected String getRuleGroupCode(final AbstractRuleModel rule)
	{
		final RuleGroupModel ruleGroup = rule.getRuleGroup();
		if (nonNull(ruleGroup) && isNotEmpty(ruleGroup.getCode()))
		{
			return ruleGroup.getCode();
		}
		return null;
	}

	protected RuleParametersService getRuleParametersService()
	{
		return ruleParametersService;
	}

	@Required
	public void setRuleParametersService(final RuleParametersService ruleParametersService)
	{
		this.ruleParametersService = ruleParametersService;
	}

	protected RuleEngineService getPlatformRuleEngineService()
	{
		return platformRuleEngineService;
	}

	@Required
	public void setPlatformRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.platformRuleEngineService = ruleEngineService;
	}

	protected DroolsRuleConditionsGenerator getDroolsRuleConditionsGenerator()
	{
		return droolsRuleConditionsGenerator;
	}

	@Required
	public void setDroolsRuleConditionsGenerator(final DroolsRuleConditionsGenerator droolsRuleConditionsGenerator)
	{
		this.droolsRuleConditionsGenerator = droolsRuleConditionsGenerator;
	}

	protected DroolsRuleActionsGenerator getDroolsRuleActionsGenerator()
	{
		return droolsRuleActionsGenerator;
	}

	@Required
	public void setDroolsRuleActionsGenerator(final DroolsRuleActionsGenerator droolsRuleActionsGenerator)
	{
		this.droolsRuleActionsGenerator = droolsRuleActionsGenerator;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected RuleService getRuleService()
	{
		return ruleService;
	}

	@Required
	public void setRuleService(final RuleService ruleService)
	{
		this.ruleService = ruleService;
	}

	protected DroolsRuleMetadataGenerator getDroolsRuleMetadataGenerator()
	{
		return droolsRuleMetadataGenerator;
	}

	@Required
	public void setDroolsRuleMetadataGenerator(final DroolsRuleMetadataGenerator droolsRuleMetadataGenerator)
	{
		this.droolsRuleMetadataGenerator = droolsRuleMetadataGenerator;
	}

	protected DroolsKIEBaseFinderStrategy getDroolsKIEBaseFinderStrategy()
	{
		return droolsKIEBaseFinderStrategy;
	}

	@Required
	public void setDroolsKIEBaseFinderStrategy(final DroolsKIEBaseFinderStrategy droolsKIEBaseFinderStrategy)
	{
		this.droolsKIEBaseFinderStrategy = droolsKIEBaseFinderStrategy;
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

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	public void setUseDeprecatedRRDsInRules(final boolean useDeprecatedRRDsInRules)
	{
		this.useDeprecatedRRDsInRules = useDeprecatedRRDsInRules;
	}

	/**
	 * @deprecated since 18.11 flag to toggle between RRD usage and rule tracker (backwards compatibility)
	 */
	@Deprecated
	protected boolean isUseDeprecatedRRDsInRules()
	{
		return useDeprecatedRRDsInRules;
	}
}
