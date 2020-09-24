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
package de.hybris.platform.ruleengineservices.util;

import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.Optional;
import java.util.function.Predicate;


/**
 * The predicate checks if the Rule is allowed to edit.
 */
public class DefaultRuleAllowedToEditPredicate implements Predicate<AbstractRuleModel>
{
	private RuleDao ruleDao;

	@Override
	public boolean test(final AbstractRuleModel ruleInstance)
	{
		if (ruleInstance.getStatus() == RuleStatus.UNPUBLISHED)
		{
			return true;
		}

		final Optional<AbstractRuleModel> latestUnpublishedRule = getRuleDao().findRuleByCodeAndStatus(ruleInstance.getCode(),
				RuleStatus.UNPUBLISHED);
		return !latestUnpublishedRule.isPresent();
	}

	protected RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
	}
}
