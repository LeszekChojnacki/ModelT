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
package com.hybris.backoffice.excel.data;

public class SelectedAttributeQualifier
{

	private final String name;
	private final String qualifier;

	public SelectedAttributeQualifier(final String name, final String qualifier)
	{
		this.name = name;
		this.qualifier = qualifier;
	}

	public String getName()
	{
		return name;
	}

	public String getQualifier()
	{
		return qualifier;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null)
		{
			return false;
		}
		if (o.getClass() != this.getClass())
		{
			return false;
		}
		final SelectedAttributeQualifier that = (SelectedAttributeQualifier) o;

		if (getName() != null ? !getName().equals(that.getName()) : (that.getName() != null))
		{
			return false;
		}
		return getQualifier() != null ? getQualifier().equals(that.getQualifier()) : (that.getQualifier() == null);
	}

	@Override
	public int hashCode()
	{
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + (getQualifier() != null ? getQualifier().hashCode() : 0);
		return result;
	}
}
