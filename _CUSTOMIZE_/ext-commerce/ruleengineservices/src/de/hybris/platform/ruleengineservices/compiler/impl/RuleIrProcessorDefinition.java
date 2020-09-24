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

import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessor;


public class RuleIrProcessorDefinition implements Comparable<RuleIrProcessorDefinition>
{
	private int priority;
	private RuleIrProcessor processor;

	/**
	 * @return the priority
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * @param priority
	 * 		the priority to set
	 */
	public void setPriority(final int priority)
	{
		this.priority = priority;
	}

	/**
	 * @return the processor
	 */
	public RuleIrProcessor getRuleIrProcessor()
	{
		return processor;
	}

	/**
	 * @param processor
	 * 		the processor to set
	 */
	public void setRuleIrProcessor(final RuleIrProcessor processor)
	{
		this.processor = processor;
	}

	@Override
	public int compareTo(final RuleIrProcessorDefinition other)
	{
		return Integer.compare(other.getPriority(), this.getPriority());
	}
	

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null) {
			return false;
		}

		if (!(o.getClass().equals(RuleIrProcessorDefinition.class)))
		{
			return false;
		}

		final RuleIrProcessorDefinition that = (RuleIrProcessorDefinition) o;

		if (getPriority() != that.getPriority())
		{
			return false;
		}
		return getRuleIrProcessor() != null ?
				getRuleIrProcessor().equals(that.getRuleIrProcessor()) :
				that.getRuleIrProcessor() == null;
	}

	@Override
	public int hashCode()
	{
		return this.getPriority() + (this.getRuleIrProcessor() == null ? 0 : this.getRuleIrProcessor().hashCode());
	}
}
