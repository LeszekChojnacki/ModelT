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
package com.hybris.backoffice.excel.template.mapper;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;


/**
 * Allows for mapping given value to collection of {@link AttributeDescriptorModel}
 *
 * @param <INPUT>
 *           input - can be ComposedType, typeCode, Workbook etc.
 */
@FunctionalInterface
public interface ToAttributeDescriptorsMapper<INPUT> extends ExcelMapper<INPUT, AttributeDescriptorModel>
{
}
