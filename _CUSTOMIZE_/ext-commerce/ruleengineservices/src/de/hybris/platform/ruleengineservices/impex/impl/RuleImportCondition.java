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

import de.hybris.platform.core.Registry;
import de.hybris.platform.impex.jalo.header.AbstractColumnDescriptor;
import de.hybris.platform.impex.jalo.imp.ValueLine;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static de.hybris.platform.ruleengineservices.model.AbstractRuleModel.CODE;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;


/**
 * Predicate used for {@link AbstractRuleModel} conditional impex import process. Relies on existence of 'code' column value.
 * Decision is based on the existence of rules with the same code that have status other than {@link RuleStatus#UNPUBLISHED}
 */
public class RuleImportCondition implements Predicate<ValueLine>
{
	private static final String RULE_DAO = "ruleDao";

	private static final RuleStatus[] LOOKUP_RULE_STATUSES = ArrayUtils.removeElement(RuleStatus.values(), RuleStatus.UNPUBLISHED);

	@Override
	public boolean test(final ValueLine valueLine)
	{
		final ValueLine.ValueEntry valueEntry = getCodeValueEntry(valueLine);

		final List<AbstractRuleModel> rules = getRuleDao()
				.findAllRuleVersionsByCodeAndStatuses(valueEntry.getCellValue(), getLookupRuleStatuses());

		return CollectionUtils.isEmpty(rules);
	}

	protected ValueLine.ValueEntry getCodeValueEntry(final ValueLine valueLine)
	{
		final Collection<AbstractColumnDescriptor> columns = valueLine.getHeader().getColumnsByQualifier(CODE);

		validateIfSingleResult(columns, "No column with given code[" + CODE + "] was found",
				"More than one column with given code [" + CODE + "] was found");

		return valueLine.getValueEntry(columns.iterator().next().getValuePosition());
	}

	protected RuleDao getRuleDao()
	{
		return Registry.getApplicationContext().getBean(RULE_DAO, RuleDao.class);
	}

	protected RuleStatus[] getLookupRuleStatuses()
	{
		return LOOKUP_RULE_STATUSES;
	}
}
