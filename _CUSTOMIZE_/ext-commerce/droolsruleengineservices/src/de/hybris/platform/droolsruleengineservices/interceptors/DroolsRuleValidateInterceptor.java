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
package de.hybris.platform.droolsruleengineservices.interceptors;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_MODULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULEMETADATA_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.VALIDATE_DROOLSRULE_DEFAULT_FLAG;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.VALIDATE_DROOLSRULE_MODULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.VALIDATE_DROOLSRULE_RULECODE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.VALIDATE_DROOLSRULE_RULENAME;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.VALIDATE_DROOLSRULE_RULEPACKAGE;
import static java.util.stream.Collectors.toList;


/**
 * Validates the DroolsRule item type. Validations are described below and can be disabled by setting the respective
 * property to {@code false}:
 * Check that {@code DroolsRule.code} doesn't contain double-quote (") and that {@code @ruleCode} meta-data is present
 * in {@code DroolsRule.drl} and matches {@code DroolsRule.code}:
 * {@code droolsruleengineservices.validate.droolsrule.rulecode}
 * Check that {@code @moduleName} meta-data is present
 * in {@code DroolsRule.drl} and matches {@code AbstractRulesModule.name}:
 * {@code droolsruleengineservices.validate.droolsrule.rulecode}
 * Check for {@code DroolsRule.uuid} doesn't contain double-quote(") and that it matches the {@code DroolsRule.drl} rule
 * declaration ( using double-quotes around the rule, e.g. {@code rule "this is the rule uuid"}):
 * {@code droolsruleengineservices.validate.droolsrule.rulename}
 * Check that {@code DroolsRule.rulePackage} attribute (if set) matches the package declaration in the
 * {@code DroolsRule.drl} content: {@code droolsruleengineservices.validate.droolsrule.rulepackage}
 */
@SuppressWarnings("squid:S1192")
public class DroolsRuleValidateInterceptor implements ValidateInterceptor<DroolsRuleModel>
{
	// the test predicate to check if two rules have the same name and package
	private BiPredicate<DroolsRuleModel, DroolsRuleModel> sameNameAndPackageBiPredicate;

	private ConfigurationService configurationService;
	private EngineRuleDao engineRuleDao;

	@Override
	public void onValidate(final DroolsRuleModel model, final InterceptorContext ctx) throws InterceptorException
	{
		if(Boolean.FALSE.equals(model.getCurrentVersion()))
		{
			return;
		}
		validateRuleCode(model);
		validateRuleName(model);

		if (model.getRuleContent() != null)
		{
			validateContentForRuleCode(model);
			validateContentForRulePackage(model);
			validateContentForRuleName(model);
			validateContentForModuleName(model);
		}

		if (model.getKieBase() != null && ctx.getDirtyAttributes(model).containsKey(DroolsRuleModel.KIEBASE))
		{
			// check if any of the kie bases already contains a rule with the same name and package
			final DroolsKIEBaseModel base = model.getKieBase();

			final List<DroolsRuleModel> activeRules = getActiveRules(base);

			for (final DroolsRuleModel rule : activeRules)
			{
				if (getSameNameAndPackageBiPredicate().test(rule, model))
				{
					throw new InterceptorException("cannot add DroolsRule with code: " + model.getCode() + " to DroolsKIEBase: "
								 + base.getName() + "! Rule with code: " + rule.getCode()
								 + " has the same name and package declaration.");
				}

				final RuleType moduleRuleType = base.getKieModule().getRuleType();
				final RuleType ruleType = rule.getRuleType();
				if (!moduleRuleType.equals(ruleType))
				{
					throw new InterceptorException("Cannot add DroolsRule with code: " + model.getCode() + " to DroolsKIEBase: "
								 + base.getName() + "! Rule type of the rule must match the RuleModule ruleType: " + moduleRuleType + " Rule Type was: " + ruleType);
				}
			}
		}
	}

	protected List<DroolsRuleModel> getActiveRules(final DroolsKIEBaseModel base)
	{
		return getEngineRuleDao().getActiveRules(base.getKieModule()).stream()
				.filter(DroolsRuleModel.class::isInstance).map(DroolsRuleModel.class::cast).collect(toList());
	}

	protected void validateContentForRuleName(final DroolsRuleModel model) throws InterceptorException
	{
		final boolean validateRuleName = getConfigurationService().getConfiguration().getBoolean(VALIDATE_DROOLSRULE_RULENAME,
					 VALIDATE_DROOLSRULE_DEFAULT_FLAG);
		if (validateRuleName && model.getUuid() != null)
		{
			final String drl = model.getRuleContent();
			final Pattern regexRuleName = Pattern.compile("rule\\s+\"" + Pattern.quote(model.getUuid()) + "\"", Pattern.MULTILINE);
			if (!regexRuleName.matcher(drl).find())
			{
				throw new InterceptorException("rule(code:" + model.getCode()
							 + ") The drl content does not contain the matching rule declaration with the value of your hybris rule's uuid attribute. "
							 + "Please adjust the uuid of your hybris rule and/or add: rule \"" + model.getUuid()
							 + "\" (i.e. putting the rule uuid in double-quotes) in your drl content.");
			}
		}
	}

	protected void validateContentForRulePackage(final DroolsRuleModel model) throws InterceptorException
	{
		final boolean validateRulePackage = getConfigurationService().getConfiguration().getBoolean(VALIDATE_DROOLSRULE_RULEPACKAGE,
					 VALIDATE_DROOLSRULE_DEFAULT_FLAG);
		if (validateRulePackage && model.getRulePackage() != null)
		{
			final String drl = model.getRuleContent();
			final Pattern regexRulePackage = Pattern.compile("^package\\s+" + Pattern.quote(model.getRulePackage()),
						 Pattern.MULTILINE);
			if (!regexRulePackage.matcher(drl).find())
			{
				throw new InterceptorException("rule(code:" + model.getCode()
							 + ") The drl content does not contain the matching package declaration with the value of your hybris rule's rule "
							 + "package attribute. Please adjust the rule package of your hybris rule and/or add 'package "
							 + model.getRulePackage() + "' (without the single quotes) as a first line in your drl content.");
			}
		}
	}

	protected void validateContentForModuleName(final DroolsRuleModel model) throws InterceptorException
	{
		final boolean validateRulesModuleName = getConfigurationService().getConfiguration()
					 .getBoolean(VALIDATE_DROOLSRULE_MODULENAME,
								  VALIDATE_DROOLSRULE_DEFAULT_FLAG);
		if (validateRulesModuleName)
		{
			final String moduleName;
			try
			{
				moduleName = model.getKieBase().getKieModule().getName();
			}
			catch (final Exception e)
			{
				throw new InterceptorException(
							 "Fatal error processing the module-awareness validation for rule(code:" + model.getCode() + "):", e);
			}

			final String drl = model.getRuleContent();
			final Pattern regexRuleCode = Pattern.compile(
						 "@" + RULEMETADATA_MODULENAME + "\\s*\\(\\s*\"" + Pattern.quote(moduleName) + "\"\\s*\\)",
						 Pattern.MULTILINE);
			if (!regexRuleCode.matcher(drl).find())
			{
				throw new InterceptorException("rule(code:" + model.getCode()
							 + ") The drl content does not contain the required meta data key @moduleName with the matching value of "
							 + "your rule's code attribute in hybris. Please adjust the module attribute in hybris to match the @moduleName and/or add @"
							 + RULEMETADATA_MODULENAME + "(\"" + moduleName + "\") right under your rule statement.");
			}
		}
	}

	protected void validateContentForRuleCode(final DroolsRuleModel model) throws InterceptorException
	{
		final boolean validateRuleCode = getConfigurationService().getConfiguration().getBoolean(VALIDATE_DROOLSRULE_RULECODE,
					 VALIDATE_DROOLSRULE_DEFAULT_FLAG);
		if (validateRuleCode)
		{
			final String drl = model.getRuleContent();
			final Pattern regexRuleCode = Pattern.compile(
						 "@" + RULEMETADATA_RULECODE + "\\s*\\(\\s*\"" + Pattern.quote(model.getCode()) + "\"\\s*\\)",
						 Pattern.MULTILINE);
			if (!regexRuleCode.matcher(drl).find())
			{
				throw new InterceptorException("rule(code:" + model.getCode()
							 + ") The drl content does not contain the required meta data key @ruleCode with the matching value of "
							 + "your rule's code attribute in hybris. Please adjust the code attribute in hybris to match the @ruleCode and/or add @"
							 + RULEMETADATA_RULECODE + "(\"" + model.getCode() + "\") right under your rule statement.");
			}
		}
	}

	protected void validateRuleName(final DroolsRuleModel model) throws InterceptorException
	{
		if (model.getUuid() != null && model.getUuid().contains("\""))
		{
			final boolean validate = getConfigurationService().getConfiguration().getBoolean(VALIDATE_DROOLSRULE_RULENAME,
						 VALIDATE_DROOLSRULE_DEFAULT_FLAG);
			if (validate)
			{
				throw new InterceptorException(
							 "rule(uuid:\"" + model.getUuid() + "\") DroolsRule.uuid must not contain double quotes character: \"!");
			}
		}
	}

	protected void validateRuleCode(final DroolsRuleModel model) throws InterceptorException
	{
		if (model.getCode() != null && model.getCode().contains("\""))
		{
			final boolean validate = getConfigurationService().getConfiguration().getBoolean(VALIDATE_DROOLSRULE_RULECODE,
						 VALIDATE_DROOLSRULE_DEFAULT_FLAG);
			if (validate)
			{
				throw new InterceptorException(
							 "rule(code:\"" + model.getCode() + "\") DroolsRule.code must not contain double quotes character: \"!");
			}
		}
	}

	protected BiPredicate<DroolsRuleModel, DroolsRuleModel> getSameNameAndPackageBiPredicate()
	{
		return sameNameAndPackageBiPredicate;
	}

	@Required
	public void setSameNameAndPackageBiPredicate(final BiPredicate<DroolsRuleModel, DroolsRuleModel> sameNameAndPackageBiPredicate)
	{
		this.sameNameAndPackageBiPredicate = sameNameAndPackageBiPredicate;
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

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}
}
