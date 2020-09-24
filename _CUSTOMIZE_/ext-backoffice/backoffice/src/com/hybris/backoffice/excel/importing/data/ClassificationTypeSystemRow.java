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
package com.hybris.backoffice.excel.importing.data;

/**
 * Represents single row of ClassificationTypeSystem sheet
 */
public class ClassificationTypeSystemRow
{

	private String fullName;
	private String classificationSystem;
	private String classificationVersion;
	private String classificationClass;
	private String classificationAttribute;
	private boolean localized;
	private String isoCode;
	private boolean mandatory;

	public String getFullName()
	{
		return fullName;
	}

	public void setFullName(final String fullName)
	{
		this.fullName = fullName;
	}

	public String getClassificationSystem()
	{
		return classificationSystem;
	}

	public void setClassificationSystem(final String classificationSystem)
	{
		this.classificationSystem = classificationSystem;
	}

	public String getClassificationVersion()
	{
		return classificationVersion;
	}

	public void setClassificationVersion(final String classificationVersion)
	{
		this.classificationVersion = classificationVersion;
	}

	public String getClassificationClass()
	{
		return classificationClass;
	}

	public void setClassificationClass(final String classificationClass)
	{
		this.classificationClass = classificationClass;
	}

	public String getClassificationAttribute()
	{
		return classificationAttribute;
	}

	public void setClassificationAttribute(final String classificationAttribute)
	{
		this.classificationAttribute = classificationAttribute;
	}

	public boolean isLocalized()
	{
		return localized;
	}

	public void setLocalized(final boolean localized)
	{
		this.localized = localized;
	}

	public String getIsoCode()
	{
		return isoCode;
	}

	public void setIsoCode(final String isoCode)
	{
		this.isoCode = isoCode;
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void setMandatory(final boolean mandatory)
	{
		this.mandatory = mandatory;
	}
}
