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
import static de.hybris.platform.ruleengine.util.RuleEngineUtils.getCleanedContent;
import static java.lang.Long.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.HistoricalRuleContentProvider;
import de.hybris.platform.ruleengine.versioning.RuleModelHistoricalContentCreator;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Helper class, that manages the historical version of the DroolsRule object creation
 */
public class RuleEngineRuleModelHistoricalContentCreator implements RuleModelHistoricalContentCreator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEngineRuleModelHistoricalContentCreator.class);
	private static final String RULE_CONTENT_CHECK_ENABLED = "ruleengine.rule.content.check.enabled";

	private List<HistoricalRuleContentProvider> historicalRuleContentProviders;
	private EngineRuleDao engineRuleDao;
	private ConfigurationService configurationService;

	@Override
	public void createHistoricalVersion(final AbstractRuleEngineRuleModel rule, final InterceptorContext context)
	{
		checkArgument(nonNull(rule), "Model should not be null here");
		checkArgument(nonNull(context), "InterceptorContext should not be null here");
		checkArgument(rule instanceof DroolsRuleModel, "The model must be an instance of DroolsRuleModel type");

		final DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
		createHistoricalVersionIfNeeded(droolsRule, context);
	}

	protected void createHistoricalVersionIfNeeded(final DroolsRuleModel droolsRule, final InterceptorContext ctx)
	{
		if (hasAssociatedKieBase(droolsRule) && historicalVersionMustBeCreated(droolsRule, ctx))
		{
			checkIfKieModuleIsTheSame(droolsRule, ctx);
			incrementActiveModelVersion(droolsRule);
			droolsRule.setCurrentVersion(Boolean.TRUE);
			final DroolsRuleModel historicalDroolsRule = createHistoricalVersionForDroolsRule(droolsRule, ctx);
			LOGGER.debug("Registering historical model: PK={}, code={}, uuid={}, version={}, active={}, currentVersion={}",
					historicalDroolsRule.getPk(), historicalDroolsRule.getCode(), historicalDroolsRule.getUuid(),
					historicalDroolsRule.getVersion(), historicalDroolsRule.getActive(), historicalDroolsRule.getCurrentVersion());
			ctx.registerElementFor(historicalDroolsRule, PersistenceOperation.SAVE);
		}
		LOGGER.debug("Registering modified model: PK={}, code={}, uuid={}, version={}, active={}, currentVersion={}",
				droolsRule.getPk(), droolsRule.getCode(), droolsRule.getUuid(), droolsRule.getVersion(), droolsRule.getActive(),
				droolsRule.getCurrentVersion());
	}

	protected void checkIfKieModuleIsTheSame(final DroolsRuleModel droolsRule, final InterceptorContext ctx)
	{
		final DroolsKIEBaseModel origKieBase = this.getOriginal(droolsRule, ctx, DroolsRuleModel.KIEBASE);
		if (nonNull(origKieBase) && nonNull(origKieBase.getKieModule()) && !origKieBase.getKieModule().getName()
				.equals(droolsRule.getKieBase().getKieModule().getName()))
		{
			throw new IllegalStateException(
					"KieModule of the modified drools rule should not change. Consider associating the rule KieBase to the same KieModule first");
		}
	}

	protected void incrementActiveModelVersion(final AbstractRuleEngineRuleModel ruleModel)
	{
		final AbstractRulesModuleModel rulesModule = getKieModule(ruleModel);
		final Long maxRuleVersion = getEngineRuleDao().getCurrentRulesSnapshotVersion(rulesModule);
		requireNonNull(maxRuleVersion, "Maximum rule version cannot be found");

		final long nextVersion = 1 + maxRuleVersion.longValue();
		ruleModel.setVersion(valueOf(nextVersion));
	}

	protected boolean historicalVersionMustBeCreated(final AbstractRuleEngineRuleModel droolsRule,
			final InterceptorContext context)
	{
		return hasLastVersion(droolsRule) && modelIsBeeingModified(droolsRule, context) && modelIsValid(droolsRule)
				&& drivingAttributesModified(droolsRule, context);
	}

	protected boolean drivingAttributesModified(final AbstractRuleEngineRuleModel droolsRule, final InterceptorContext context)
	{
		return (isActive(droolsRule, context) && contentHasChanged(droolsRule, context)) || activeFlagChanged(droolsRule, context);
	}

	protected boolean hasLastVersion(final AbstractRuleEngineRuleModel ruleModel)
	{
		final AbstractRulesModuleModel rulesModule = getKieModule(ruleModel);
		final Long lastVersion = getEngineRuleDao().getRuleVersion(ruleModel.getCode(), rulesModule.getName());
		return isNull(lastVersion) || ruleModel.getVersion() >= lastVersion;
	}

	protected boolean activeFlagChanged(final AbstractRuleEngineRuleModel model, final InterceptorContext context)
	{
		return !model.getActive().equals(this.<Boolean>getOriginal(model, context, AbstractRuleEngineRuleModel.ACTIVE));
	}

	protected DroolsRuleModel createHistoricalVersionForDroolsRule(final DroolsRuleModel droolsRule,
			final InterceptorContext context)
	{
		final DroolsRuleModel historicalDroolsRule = context.getModelService().clone(droolsRule);

		putOriginalValuesIntoHistoricalVersion(droolsRule, historicalDroolsRule, context);
		deactivateHistoricalVersion(historicalDroolsRule);
		return historicalDroolsRule;
	}

	protected void putOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel droolsRule,
			final AbstractRuleEngineRuleModel historicalDroolsRule, final InterceptorContext ctx)
	{
		getHistoricalRuleContentProviders()
				.forEach(p -> p.copyOriginalValuesIntoHistoricalVersion(droolsRule, historicalDroolsRule, ctx));
	}

	protected void deactivateHistoricalVersion(final DroolsRuleModel historicalDroolsRule)
	{
		historicalDroolsRule.setCurrentVersion(Boolean.FALSE);
	}

	protected boolean modelIsValid(final AbstractRuleEngineRuleModel ruleModel)
	{
		return nonNull(ruleModel.getRuleContent()) && nonNull(ruleModel.getCode());
	}

	protected boolean modelIsBeeingModified(final AbstractRuleEngineRuleModel ruleModel, final InterceptorContext ctx)
	{
		return !ctx.isNew(ruleModel) && !ctx.isRemoved(ruleModel) && ctx.isModified(ruleModel);
	}

	protected boolean isActive(final AbstractRuleEngineRuleModel ruleModel, final InterceptorContext ctx)
	{
		final Boolean result = getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.ACTIVE);
		return nonNull(result) && result.booleanValue();
	}

	protected boolean hasAssociatedKieBase(final DroolsRuleModel droolsRule)
	{
		return nonNull(droolsRule.getKieBase());
	}

	protected AbstractRulesModuleModel getKieModule(final AbstractRuleEngineRuleModel ruleModel)
	{
		if (ruleModel instanceof DroolsRuleModel)
		{
			return requireNonNull(((DroolsRuleModel) ruleModel).getKieBase().getKieModule());
		}
		return null;
	}

	protected <T> T getOriginal(final AbstractRuleEngineRuleModel droolsRule, final InterceptorContext context,
			final String attributeQualifier)
	{
		if (context.isModified(droolsRule, attributeQualifier))
		{
			final ItemModelContext modelContext = ModelContextUtils.getItemModelContext(droolsRule);
			return modelContext.getOriginalValue(attributeQualifier);
		}
		final ModelService modelService = requireNonNull(context.getModelService());
		return modelService.getAttributeValue(droolsRule, attributeQualifier);
	}

	protected boolean contentHasChanged(final AbstractRuleEngineRuleModel ruleModel, final InterceptorContext ctx)
	{

		if (getConfigurationService().getConfiguration().getBoolean(RULE_CONTENT_CHECK_ENABLED, false))
		{
			final String cleanedContent = getCleanedContent(ruleModel.getRuleContent(), ruleModel.getUuid());
			final String origCleanedContent = getCleanedContent(
					this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.RULECONTENT),
					this.getOriginal(ruleModel, ctx, AbstractRuleEngineRuleModel.UUID));
			if (nonNull(cleanedContent))
			{
				return !cleanedContent.equals(origCleanedContent);
			}
			return nonNull(origCleanedContent);
		}

		return true;
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

	protected List<HistoricalRuleContentProvider> getHistoricalRuleContentProviders()
	{
		return historicalRuleContentProviders;
	}

	@Required
	public void setHistoricalRuleContentProviders(final List<HistoricalRuleContentProvider> historicalRuleContentProviders)
	{
		this.historicalRuleContentProviders = historicalRuleContentProviders;
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

}
