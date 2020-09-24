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
package com.hybris.backoffice.excel.template.filter;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;


/**
 * Allows to filter only {@link AttributeDescriptorModel} which are writable
 */
public class WritableCheckingFilter implements ExcelFilter<AttributeDescriptorModel>
{

	@Override
	public boolean test(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return attributeDescriptorModel.getWritable();
	}
}
