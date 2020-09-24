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
package de.hybris.platform.adaptivesearchbackoffice.widgets.navigationcontext;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Objects;


/**
 * View model for index configurations.
 */
public class IndexConfigurationModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String code;
	private String name;

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

		final IndexConfigurationModel that = (IndexConfigurationModel) obj;
		return new EqualsBuilder()
				.append(this.code, that.code)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.code);
	}
}
