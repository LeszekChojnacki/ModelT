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

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;

import org.apache.commons.lang3.BooleanUtils;


/**
 * Default implementation of {@link ExcelAttribute} supports classification attributes
 */
public class ExcelClassificationAttribute implements ExcelAttribute
{

	private ClassAttributeAssignmentModel attributeAssignment;
	private String isoCode;
	private String name;

	public void setAttributeAssignment(final ClassAttributeAssignmentModel attributeAssignment)
	{
		this.attributeAssignment = attributeAssignment;
	}

	public void setIsoCode(final String isoCode)
	{
		this.isoCode = isoCode;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isLocalized()
	{
		return BooleanUtils.isTrue(attributeAssignment.getLocalized());
	}

	@Override
	public String getIsoCode()
	{
		return isoCode;
	}

	@Override
	public String getQualifier()
	{
		return attributeAssignment.getClassificationAttribute().getCode();
	}

	@Override
	public boolean isMandatory()
	{
		return BooleanUtils.isTrue(attributeAssignment.getMandatory());
	}

	@Override
	public String getType()
	{
		return attributeAssignment.getAttributeType().getCode();
	}

	@Override
	public boolean isMultiValue()
	{
		return BooleanUtils.isTrue(attributeAssignment.getMultiValued());
	}

	public ClassAttributeAssignmentModel getAttributeAssignment()
	{
		return attributeAssignment;
	}
}
