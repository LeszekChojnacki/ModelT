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

import de.hybris.platform.ruleengineservices.compiler.AbstractRuleIrBooleanCondition;
import de.hybris.platform.ruleengineservices.compiler.AbstractRuleIrPatternCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExecutableCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrExistsCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;

import java.util.List;

import org.apache.commons.collections4.ListValuedMap;


/**
 * class to hold together rule conditions by type
 */
public class RuleIrConditionsByType
{

	private ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions;
	private ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions;
	private List<RuleIrGroupCondition> groupConditions;
	private List<RuleIrExecutableCondition> executableConditions;
	private List<RuleIrExistsCondition> existsConditions;
	private List<RuleIrNotCondition> notConditions;

	public void setBooleanConditions(final ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions)
	{
		this.booleanConditions = booleanConditions;
	}

	public ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> getBooleanConditions()
	{
		return booleanConditions;
	}

	public void setPatternConditions(final ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions)
	{
		this.patternConditions = patternConditions;
	}

	public ListValuedMap<String, AbstractRuleIrPatternCondition> getPatternConditions()
	{
		return patternConditions;
	}

	public void setGroupConditions(final List<RuleIrGroupCondition> groupConditions)
	{
		this.groupConditions = groupConditions;
	}

	public List<RuleIrGroupCondition> getGroupConditions()
	{
		return groupConditions;
	}

	public void setExecutableConditions(final List<RuleIrExecutableCondition> executableConditions)
	{
		this.executableConditions = executableConditions;
	}

	public List<RuleIrExecutableCondition> getExecutableConditions()
	{
		return executableConditions;
	}

	public void setExistsConditions(final List<RuleIrExistsCondition> existsConditions)
	{
		this.existsConditions = existsConditions;
	}

	public List<RuleIrExistsCondition> getExistsConditions()
	{
		return existsConditions;
	}

	public void setNotConditions(final List<RuleIrNotCondition> notConditions)
	{
		this.notConditions = notConditions;
	}

	public List<RuleIrNotCondition> getNotConditions()
	{
		return notConditions;
	}
}
