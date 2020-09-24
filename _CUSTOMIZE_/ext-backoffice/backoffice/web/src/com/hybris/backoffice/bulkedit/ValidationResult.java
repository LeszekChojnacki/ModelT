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
package com.hybris.backoffice.bulkedit;

import java.util.List;

import com.hybris.cockpitng.validation.model.ValidationInfo;


public class ValidationResult
{

	private Object item;
	private List<ValidationInfo> validationInfos;

	public ValidationResult(final Object item, final List<ValidationInfo> validationInfos)
	{
		this.item = item;
		this.validationInfos = validationInfos;
	}

	public Object getItem()
	{
		return item;
	}

	public void setItem(final Object item)
	{
		this.item = item;
	}

	public List<ValidationInfo> getValidationInfos()
	{
		return validationInfos;
	}

	public void setValidationInfos(final List<ValidationInfo> validationInfos)
	{
		this.validationInfos = validationInfos;
	}
}
