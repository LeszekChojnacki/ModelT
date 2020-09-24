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
package de.hybris.platform.ruleengineservices.impex.impl;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hybris.platform.campaigns.jalo.Campaign;
import de.hybris.platform.core.Registry;
import de.hybris.platform.impex.jalo.header.StandardColumnDescriptor;
import de.hybris.platform.impex.jalo.translators.SingleValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.ruleengineservices.jalo.RuleengineservicesManager;
import de.hybris.platform.ruleengineservices.jalo.SourceRule;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * The translator that updates source rules against Campaign using the source rule with provided code and version resolved by one
 * of the available strategies
 * <ul>
 *    <li>ruleSelection=earliest - selects source rule with minimum version </li>
 *    <li>ruleSelection=latest - select source rule with maximum version</li>
 * </ul>
 * The default ruleSelection\ is configured to latest.
 *
 * It can do processing using one of two modes:
 * <ul>
 * <li>mode=append - appends rule to the end of the list</li>
 * <li>mode=replace - replaces the list</li>
 * </ul>
 * The default mode is configured to replace.
 *
 * Whenever it's desired to pass more than one rule you must separate it using either {@link #DEFAULT_SEPARATOR} or
 * define your own separator by adding extra meta data separator=[char].
 *
 * eg. INSERT_UPDATE
 * Campaign[disable.interceptor.types=validate];code[unique=true];sourceRules[translator=de.hybris.platform.
 * ruleengineservices.impex.impl.CampaignSourceRulesTranslator,mode=append,ruleSelection=latest,separator=","]; ;campaign-name;ruleA,ruleB;
 */
public class CampaignSourceRulesTranslator extends SingleValueTranslator
{
	private static final Logger LOG = LoggerFactory.getLogger(CampaignSourceRulesTranslator.class);
	private static final String DEFAULT_SEPARATOR = ",";
	private static final String APPEND_MODE = "append";
	private static final String REPLACE_MODE = "replace";
	private static final String MODE_META_KEY = "mode";
	private static final String SEPARATOR_META_KEY = "separator";
	private static final String RULE_SELECTION_META_KEY = "ruleSelection";
	private static final String RULE_SELECTION_EARLIEST = "earliest";
	private static final String RULE_SELECTION_LATEST = "latest";
	private static final String MODEL_SERVICE = "modelService";
	private static final String RULE_DAO = "ruleDao";

	private String separator = DEFAULT_SEPARATOR;
	private String mode = REPLACE_MODE;
	private String ruleSelection = "latest";

	@Override
	public void init(final StandardColumnDescriptor descriptor)
	{
		super.init(descriptor);
		configureSeparator(descriptor);
		configureMode(descriptor);
		configureSelectionStrategy(descriptor);
	}

	protected void configureSelectionStrategy(final StandardColumnDescriptor descriptor)
	{
		final String customRuleSelection = descriptor.getDescriptorData().getModifier(RULE_SELECTION_META_KEY);
		if (RULE_SELECTION_EARLIEST.equalsIgnoreCase(customRuleSelection)
				|| RULE_SELECTION_LATEST.equalsIgnoreCase(customRuleSelection))
		{
			this.ruleSelection = customRuleSelection;
		}
	}

	protected void configureMode(final StandardColumnDescriptor descriptor)
	{
		final String customMode = descriptor.getDescriptorData().getModifier(MODE_META_KEY);
		if (APPEND_MODE.equalsIgnoreCase(customMode) || REPLACE_MODE.equalsIgnoreCase(customMode))
		{
			this.mode = customMode;
		}
	}

	protected void configureSeparator(final StandardColumnDescriptor descriptor)
	{
		final String customSeparator = descriptor.getDescriptorData().getModifier(SEPARATOR_META_KEY);
		if (null != customSeparator)
		{
			this.separator = customSeparator;
		}
	}

	@Override
	protected Object convertToJalo(final String expression, final Item item)
	{
		if (isNotEmpty(expression) && item instanceof Campaign)
		{
			final Campaign campaign = (Campaign) item;
			final Set<SourceRule> sourceRulesFromExpression = findSourceRules(expression);

			if (isAppendMode())
			{
				final Set<SourceRule> originalSourceRules = RuleengineservicesManager.getInstance().getSourceRules(campaign);
				final Set<SourceRule> newSourceRules = new HashSet<>(originalSourceRules);
				newSourceRules.addAll(sourceRulesFromExpression);
				return newSourceRules;
			}
			else
			{
				return sourceRulesFromExpression;
			}
		}
		return null;
	}

	protected Set<SourceRule> findSourceRules(final String expression)
	{
		final String[] codes = expression.split(this.separator);
		return isRuleSelectionEarliest() ? findSourceRulesHavingEarliestVersion(codes) : findSourceRulesHavingLatestVersion(codes);
	}

	protected Set<SourceRule> findSourceRulesHavingLatestVersion(final String[] codes)
	{
		return Stream.of(codes).map(getRuleDao()::findRuleByCode).map(getModelService()::getSource).map(SourceRule.class::cast)
				.collect(toSet());
	}

	private Set<SourceRule> findSourceRulesHavingEarliestVersion(final String[] codes)
	{
		return Stream.of(codes).map(code -> getRuleDao().findAllRuleVersionsByCode(code)).map(this::selectOldestRule)
				.map(getModelService()::getSource).map(SourceRule.class::cast).collect(toSet());
	}

	protected boolean isRuleSelectionEarliest()
	{
		return RULE_SELECTION_EARLIEST.equalsIgnoreCase(this.ruleSelection);
	}

	protected AbstractRuleModel selectOldestRule(final List<AbstractRuleModel> rules)
	{
		return rules.stream().min((r1, r2) -> r1.getVersion().compareTo(r2.getVersion())).get();
	}

	@Override
	protected String convertToString(final Object o)
	{
		LOG.debug("Export operation for this translator is not supported");
		return null;
	}

	protected RuleDao getRuleDao()
	{
		return (RuleDao) Registry.getApplicationContext().getBean(RULE_DAO);
	}

	protected ModelService getModelService()
	{
		return (ModelService) Registry.getApplicationContext().getBean(MODEL_SERVICE);
	}

	protected boolean isAppendMode()
	{
		return APPEND_MODE.equalsIgnoreCase(this.mode);
	}
}
