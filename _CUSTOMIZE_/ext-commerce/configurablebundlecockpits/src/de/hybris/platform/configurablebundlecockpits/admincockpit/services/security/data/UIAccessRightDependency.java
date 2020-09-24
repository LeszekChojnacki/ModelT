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

package de.hybris.platform.configurablebundlecockpits.admincockpit.services.security.data;

public class UIAccessRightDependency
{
	private String typeCode;
	private String attributeName;
	private String dependentOnAttributeName;
	private Boolean isNull;

	public String getTypeCode()
	{
		return typeCode;
	}

	public void setTypeCode(final String typeCode)
	{
		this.typeCode = typeCode;
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	public void setAttributeName(final String attributeName)
	{
		this.attributeName = attributeName;
	}

	public String getDependentOnAttributeName()
	{
		return dependentOnAttributeName;
	}

	public void setDependentOnAttributeName(final String dependentOnAttributeName)
	{
		this.dependentOnAttributeName = dependentOnAttributeName;
	}

	public Boolean getIsNull()
	{
		return isNull;
	}

	public void setIsNull(final Boolean isNull)
	{
		this.isNull = isNull;
	}
}
