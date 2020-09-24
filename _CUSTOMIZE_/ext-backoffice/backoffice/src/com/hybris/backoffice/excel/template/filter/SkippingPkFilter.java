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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;

import org.apache.commons.lang3.StringUtils;

/**
 * Filter which checks whether given {@link AttributeDescriptorModel} is PK or not
 */
public class SkippingPkFilter implements ExcelFilter<AttributeDescriptorModel>
{

	@Override
	public boolean test(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return !StringUtils.equals(ItemModel.PK, attributeDescriptorModel.getQualifier());
	}
}
