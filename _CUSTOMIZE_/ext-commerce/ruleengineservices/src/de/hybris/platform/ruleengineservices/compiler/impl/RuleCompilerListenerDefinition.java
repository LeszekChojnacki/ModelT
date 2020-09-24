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


/**
 * Interface that allows to register global listeners.
 */
public class RuleCompilerListenerDefinition implements Comparable<RuleCompilerListenerDefinition>
{
	private int priority;
	private Object listener;

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
	 * @return the listener
	 */
	public Object getListener()
	{
		return listener;
	}

	/**
	 * @param listener
	 * 		the listener to set
	 */
	public void setListener(final Object listener)
	{
		this.listener = listener;
	}

	@Override
	public int compareTo(final RuleCompilerListenerDefinition other)
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

		if (!(o.getClass().equals(RuleCompilerListenerDefinition.class)))
		{
			return false;
		}

		final RuleCompilerListenerDefinition that = (RuleCompilerListenerDefinition) o;

		if (getPriority() != that.getPriority())
		{
			return false;
		}
		return getListener() != null ? getListener().equals(that.getListener()) : that.getListener() == null;
	}

	@Override
	public int hashCode()
	{
		return this.getPriority() + (this.getListener() == null ? 0 : this.getListener().hashCode());
	}
}
