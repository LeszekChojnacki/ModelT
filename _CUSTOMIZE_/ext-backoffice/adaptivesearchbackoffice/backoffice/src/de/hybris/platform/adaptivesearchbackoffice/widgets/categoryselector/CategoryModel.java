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
package de.hybris.platform.adaptivesearchbackoffice.widgets.categoryselector;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 * View model for categories.
 */
public class CategoryModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String code;
	private String name;
	private boolean hasSearchConfiguration;
	private int numberOfConfigurations;

	public String getCode()
	{
		return code;
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setHasSearchConfiguration(final boolean hasSearchConfiguration)
	{
		this.hasSearchConfiguration = hasSearchConfiguration;
	}

	public boolean isHasSearchConfiguration()
	{
		return hasSearchConfiguration;
	}

	public void setNumberOfConfigurations(final int numberOfConfigurations)
	{
		this.numberOfConfigurations = numberOfConfigurations;
	}

	public int getNumberOfConfigurations()
	{
		return numberOfConfigurations;
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

		final CategoryModel that = (CategoryModel) obj;
		return new EqualsBuilder().append(this.code, that.code).isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.code);
	}
}
