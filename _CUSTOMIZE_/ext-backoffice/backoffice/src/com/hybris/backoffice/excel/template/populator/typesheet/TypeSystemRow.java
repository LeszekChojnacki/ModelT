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
package com.hybris.backoffice.excel.template.populator.typesheet;

/**
 * Represents single row of TypeSystem sheet.
 */
public class TypeSystemRow
{
	private String typeCode;
	private String typeName;
	private String attrQualifier;
	private String attrName;
	private Boolean attrOptional;
	private String attrTypeCode;
	private String attrTypeItemType;
	private Boolean attrLocalized;
	private String attrLocLang;
	private String attrDisplayName;
	private Boolean attrUnique;
	private String attrReferenceFormat;

	public String getTypeCode()
	{
		return typeCode;
	}

	public void setTypeCode(final String typeCode)
	{
		this.typeCode = typeCode;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public void setTypeName(final String typeName)
	{
		this.typeName = typeName;
	}

	public String getAttrQualifier()
	{
		return attrQualifier;
	}

	public void setAttrQualifier(final String attrQualifier)
	{
		this.attrQualifier = attrQualifier;
	}

	public String getAttrName()
	{
		return attrName;
	}

	public void setAttrName(final String attrName)
	{
		this.attrName = attrName;
	}

	public Boolean getAttrOptional()
	{
		return attrOptional;
	}

	public void setAttrOptional(final Boolean attrOptional)
	{
		this.attrOptional = attrOptional;
	}

	public String getAttrTypeCode()
	{
		return attrTypeCode;
	}

	public void setAttrTypeCode(final String attrTypeCode)
	{
		this.attrTypeCode = attrTypeCode;
	}

	public String getAttrTypeItemType()
	{
		return attrTypeItemType;
	}

	public void setAttrTypeItemType(final String attrTypeItemType)
	{
		this.attrTypeItemType = attrTypeItemType;
	}

	public Boolean getAttrLocalized()
	{
		return attrLocalized;
	}

	public void setAttrLocalized(final Boolean attrLocalized)
	{
		this.attrLocalized = attrLocalized;
	}

	public String getAttrLocLang()
	{
		return attrLocLang;
	}

	public void setAttrLocLang(final String attrLocLang)
	{
		this.attrLocLang = attrLocLang;
	}

	public String getAttrDisplayName()
	{
		return attrDisplayName;
	}

	public void setAttrDisplayName(final String attrDisplayName)
	{
		this.attrDisplayName = attrDisplayName;
	}

	public Boolean getAttrUnique()
	{
		return attrUnique;
	}

	public void setAttrUnique(final Boolean attrUnique)
	{
		this.attrUnique = attrUnique;
	}

	public String getAttrReferenceFormat()
	{
		return attrReferenceFormat;
	}

	public void setAttrReferenceFormat(final String attrReferenceFormat)
	{
		this.attrReferenceFormat = attrReferenceFormat;
	}
}
