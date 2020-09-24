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

import de.hybris.platform.adaptivesearchbackoffice.data.CatalogVersionData;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Objects;


/**
 * View model for catalog versions.
 */
public class CatalogVersionModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private CatalogVersionData catalogVersion;
	private boolean active;
	private String name;

	public CatalogVersionData getCatalogVersion()
	{
		return catalogVersion;
	}

	public void setCatalogVersion(final CatalogVersionData catalogVersion)
	{
		this.catalogVersion = catalogVersion;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
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

		final CatalogVersionModel that = (CatalogVersionModel) obj;
		return new EqualsBuilder()
				.append(this.getCatalogVersion(), that.getCatalogVersion())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.catalogVersion);
	}
}
