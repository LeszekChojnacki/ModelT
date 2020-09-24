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
package de.hybris.platform.ruleengine.dao.interceptors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;
import de.hybris.platform.ruleengine.versioning.RuleModelChecksumCalculator;
import de.hybris.platform.ruleengine.versioning.RuleModelHistoricalContentCreator;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Prepare interceptor for AbstractRuleEngineRuleModel. It sets uuid on creation of AbstractRuleEngineRuleModel.
 */
public class RuleEngineRulePrepareInterceptor implements PrepareInterceptor<DroolsRuleModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(RuleEngineRulePrepareInterceptor.class);

	private static final Long DEFAULT_VERSION = 0L;

	private RuleModelChecksumCalculator ruleModelChecksumCalculator;
	private EngineRuleDao engineRuleDao;
	private RuleModelHistoricalContentCreator historicalContentCreator;
	private ModuleVersioningService moduleVersioningService;

	@Override
	public void onPrepare(final DroolsRuleModel droolsRule, final InterceptorContext context) throws InterceptorException
	{
		final DroolsKIEBaseModel kieBase = droolsRule.getKieBase();
		if(isNull(kieBase))
		{
			throw new InterceptorException("The Kie base should be assigned to the drools rule instance");
		}

		final AbstractRulesModuleModel rulesModule = kieBase.getKieModule();
		if (isNull(droolsRule.getVersion()))
		{
			if (nonNull(rulesModule) && nonNull(rulesModule.getVersion()) && rulesModule.getVersion() > -1)
			{
				final long moduleVersion = rulesModule.getVersion();
				final long ruleModelVersion = droolsRule.getActive() ? moduleVersion + 1 : moduleVersion;
				droolsRule.setVersion(ruleModelVersion);
			}
			else
			{
				droolsRule.setVersion(DEFAULT_VERSION);
			}
		}
		if (!context.isNew(droolsRule))
		{
			getHistoricalContentCreator().createHistoricalVersion(droolsRule, context);
		}
		else if (isNull(droolsRule.getUuid()))
		{
			final UUID uuid = UUID.randomUUID();
			droolsRule.setUuid(uuid.toString());
		}
		droolsRule.setChecksum(calculateChecksum(droolsRule));
		getModuleVersioningService().assertRuleModuleVersion(droolsRule, rulesModule);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Prepared for writing DroolsRule instance: uuid [{}], version [{}], current version [{}], active [{}]", droolsRule.getUuid(), droolsRule.getVersion(), droolsRule.getCurrentVersion(), droolsRule.getActive());
		}
	}

	protected String calculateChecksum(final DroolsRuleModel droolsRule)
	{
		return isNull(droolsRule.getRuleContent()) ? null : getRuleModelChecksumCalculator().calculateChecksumOf(droolsRule);
	}

	@Required
	public void setRuleModelChecksumCalculator(final RuleModelChecksumCalculator ruleModelChecksumCalculator)
	{
		this.ruleModelChecksumCalculator = ruleModelChecksumCalculator;
	}

	protected RuleModelChecksumCalculator getRuleModelChecksumCalculator()
	{
		return ruleModelChecksumCalculator;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	protected RuleModelHistoricalContentCreator getHistoricalContentCreator()
	{
		return historicalContentCreator;
	}

	@Required
	public void setHistoricalContentCreator(final RuleModelHistoricalContentCreator historicalContentCreator)
	{
		this.historicalContentCreator = historicalContentCreator;
	}

	protected ModuleVersioningService getModuleVersioningService()
	{
		return moduleVersioningService;
	}

	@Required
	public void setModuleVersioningService(final ModuleVersioningService moduleVersioningService)
	{
		this.moduleVersioningService = moduleVersioningService;
	}

}
