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
package de.hybris.platform.ruleengine.versioning.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.BooleanUtils.isNotTrue;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.AbstractValidationResult;
import de.hybris.platform.ruleengine.versioning.ComposableValidationResult;
import de.hybris.platform.ruleengine.versioning.RuleModelChecksumCalculator;
import de.hybris.platform.ruleengine.versioning.RuleModelValidator;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;

import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleEngineRuleModelValidator implements RuleModelValidator
{

	private RuleModelChecksumCalculator ruleModelChecksumCalculator;
	private EngineRuleDao engineRuleDao;

	@Override
	public AbstractValidationResult validate(final AbstractRuleEngineRuleModel rule, final InterceptorContext context)
	{
		checkArgument(rule instanceof DroolsRuleModel, "DroolsRuleModel type of rule is expected here");
		requireNonNull(rule);
		requireNonNull(context);

		final DroolsRuleModel ruleModel = (DroolsRuleModel) rule;

		if (context.isNew(ruleModel))
		{
			return validateNewContent(ruleModel);
		}
		else if (context.isModified(ruleModel))
		{
			return validateModifiedContent(ruleModel);
		}
		else if (context.isRemoved(ruleModel))
		{
			return validateRemovedContent(ruleModel);
		}
		throw new IllegalStateException("Unknown state of rule " + ruleModel);
	}

	protected ComposableValidationResult validateNewContent(final DroolsRuleModel droolsRule)
	{
		if (isGeneratedFromSourceRule(droolsRule))
		{
			return validateAutomaticallyGenerated(droolsRule);
		}
		else
		{
			return validateManuallyCreated(droolsRule);
		}
	}

	protected ComposableValidationResult validateAutomaticallyGenerated(final DroolsRuleModel droolsRule)
	{
		ComposableValidationResult validationResult = mustBeCurrentVersion(droolsRule).and(activeFlagMustBeSet(droolsRule));
		if (!validationResult.succeeded())
		{
			return validationResult;
		}
		if (droolsRule.getActive().booleanValue())
		{
			return validationResult.and(checksumMustMatch(droolsRule)).and(versionMustBeSet(droolsRule))
						 .and(codeMustBeSet(droolsRule));
		}
		validationResult = validationResult.and(versionMustBeSet(droolsRule)).and(codeMustBeSet(droolsRule));
		if (!validationResult.succeeded())
		{
			return validationResult;
		}
		if (hasKieModuleAssigned(droolsRule))
		{
			validationResult = validationResult.and(checksumVersionForNotActive(droolsRule));
			if (!validationResult.succeeded())
			{
				return validationResult;
			}
		}
		return validationResult.and(nonActiveChecksumMustMatch(droolsRule));
	}

	protected ComposableValidationResult validateManuallyCreated(final DroolsRuleModel droolsRule)
	{
		return mustBeCreatedUsingLatestVersion(droolsRule).and(nonActiveChecksumMustMatch(droolsRule));
	}

	protected boolean isGeneratedFromSourceRule(final DroolsRuleModel droolsRule)
	{
		return nonNull(droolsRule.getSourceRule());
	}

	protected ComposableValidationResult validateModifiedContent(final DroolsRuleModel droolsRule)
	{
		ComposableValidationResult validationResult = mustBeCurrentVersion(droolsRule).and(activeFlagMustBeSet(droolsRule));
		if (!validationResult.succeeded())
		{
			return validationResult;
		}
		return validationResult.and(checksumMustMatch(droolsRule)).and(versionMustBeSet(droolsRule))
					 .and(versionMustBeLast(droolsRule));
	}

	protected Supplier<ComposableValidationResult> activeFlagMustBeSet(final DroolsRuleModel droolsRule)
	{
		return () -> errorIf(isNull(droolsRule.getActive()), "Active flag must be set");
	}

	protected ComposableValidationResult validateRemovedContent(final DroolsRuleModel droolsRule)
	{
		return errorIf(isNotTrue(droolsRule.getCurrentVersion()), "Rule must be active.");
	}

	protected ComposableValidationResult mustBeCurrentVersion(final DroolsRuleModel droolsRule)
	{
		return errorIf(!hasLatestVersionOrNew(droolsRule), "Historical version of the rule cannot be modified");
	}

	protected ComposableValidationResult mustBeCreatedUsingLatestVersion(final DroolsRuleModel droolsRule)
	{
		return errorIf(!hasLatestVersionOrNew(droolsRule), "Rule must be created using latest rule module version.");
	}

	protected boolean hasLatestVersionOrNew(final AbstractRuleEngineRuleModel rule)
	{
		checkArgument(nonNull(rule), "rule must not be null");
		checkArgument(rule instanceof DroolsRuleModel, "rule must be an instance of DroolsRuleModel");

		final DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
		checkArgument(nonNull(droolsRule.getKieBase()), "rule must have correct associated KieBase");
		checkArgument(nonNull(droolsRule.getKieBase().getKieModule()), "rule must have correct associated KieModule");

		final DroolsKIEModuleModel module = droolsRule.getKieBase().getKieModule();
		final Long lastVersion = getEngineRuleDao().getRuleVersion(rule.getCode(), module.getName());
		final Long version = rule.getVersion();
		return isNull(lastVersion) || isNull(version) || version.longValue() >= lastVersion.longValue();
	}

	protected Supplier<ComposableValidationResult> kieModuleMustBeKnown(final DroolsRuleModel droolsRule)
	{
		return () -> errorIf(!hasKieModuleAssigned(droolsRule), "Kie base and kie module are required.");
	}

	protected boolean hasKieModuleAssigned(final DroolsRuleModel droolsRule)
	{
		return nonNull(droolsRule.getKieBase()) && nonNull(droolsRule.getKieBase().getKieModule());
	}

	protected Supplier<ComposableValidationResult> checksumVersionForNotActive(final DroolsRuleModel droolsRule)
	{
		return () ->
		{
			final Long currentRulesSnapshotVersion = getEngineRuleDao().getCurrentRulesSnapshotVersion(
						 droolsRule.getKieBase().getKieModule());
			return errorIf(droolsRule.getVersion() > currentRulesSnapshotVersion
									  || (isNull(droolsRule.getRuleContent()) && droolsRule.getVersion() < currentRulesSnapshotVersion),
						 "Non active rule version cannot increase overall knowledgebase snapshot version");
		};
	}

	protected Supplier<ComposableValidationResult> checksumMustMatch(final DroolsRuleModel droolsRule)
	{
		return () ->
		{
			final String expected = getRuleModelChecksumCalculator().calculateChecksumOf(droolsRule);
			return errorIf(
						 !StringUtils.equals(expected, droolsRule.getChecksum()),
						 String.format("Checksum doesn't match the rule content. Expected %s but was %s", expected,
									  droolsRule.getChecksum()));
		};
	}

	protected Supplier<ComposableValidationResult> nonActiveChecksumMustMatch(final DroolsRuleModel droolsRule)
	{
		return () ->
		{
			String expected = null;
			if (nonNull(droolsRule.getRuleContent()))
			{
				expected = getRuleModelChecksumCalculator().calculateChecksumOf(droolsRule);
			}
			return errorIf(nonNull(expected) && !expected.equals(droolsRule.getChecksum()), "Checksum doesn't match the content.");
		};
	}

	protected Supplier<ComposableValidationResult> versionMustBeSet(final DroolsRuleModel droolsRule)
	{
		return () -> errorIf(isNull(droolsRule.getVersion()), "Version must be set");
	}

	protected Supplier<ComposableValidationResult> codeMustBeSet(final DroolsRuleModel droolsRule)
	{
		return () -> errorIf(isNull(droolsRule.getCode()), "Code must be set.");
	}

	protected Supplier<ComposableValidationResult> versionMustBeLast(final DroolsRuleModel droolsRule)
	{
		return () -> errorIf(hasKieModuleAssigned(droolsRule) && !isVersionLast(droolsRule),
					 "Only update of the most recent rule version is possible");
	}

	protected boolean isVersionLast(final DroolsRuleModel droolsRule)
	{
		final DroolsKIEModuleModel module = droolsRule.getKieBase().getKieModule();
		final Long ruleVersion = getEngineRuleDao().getRuleVersion(droolsRule.getCode(), module.getName());
		return isNull(ruleVersion) || droolsRule.getVersion() >= ruleVersion;
	}

	protected final ComposableValidationResult errorIf(final boolean condition, final String errorMessage)
	{
		if (!condition)
		{
			return ComposableValidationResult.SUCCESS;
		}
		return ComposableValidationResult.makeError(errorMessage);
	}

	protected RuleModelChecksumCalculator getRuleModelChecksumCalculator()
	{
		return ruleModelChecksumCalculator;
	}

	@Required
	public void setRuleModelChecksumCalculator(final RuleModelChecksumCalculator ruleModelChecksumCalculator)
	{
		this.ruleModelChecksumCalculator = ruleModelChecksumCalculator;
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
