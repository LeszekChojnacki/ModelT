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
package de.hybris.platform.solrfacetsearch.common.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Objects;

/**
 * Interface that allows to register global listeners.
 */
public class ListenerDefinition implements Comparable<ListenerDefinition>
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
	 *           the priority to set
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
	 *           the listener to set
	 */
	public void setListener(final Object listener)
	{
		this.listener = listener;
	}

	@Override
	public int compareTo(final ListenerDefinition other)
	{
		return Integer.compare(other.getPriority(), this.getPriority());
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final ListenerDefinition that = (ListenerDefinition) obj;
		return new EqualsBuilder()
				.append(this.listener, that.listener)
				.append(this.priority, that.priority)
				.isEquals();
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.listener, this.priority);
	}
}
