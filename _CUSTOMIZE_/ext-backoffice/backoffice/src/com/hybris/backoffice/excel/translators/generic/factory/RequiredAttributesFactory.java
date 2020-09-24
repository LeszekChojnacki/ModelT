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
package com.hybris.backoffice.excel.translators.generic.factory;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;

import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Creates hierarchical structure of all required attributes for given composed type or attribute descriptor.
 */
public interface RequiredAttributesFactory
{

	/**
	 * Recursively creates {@link RequiredAttribute} structure for given attribute. The mechanism takes into account only
	 * unique attributes or not optional attributes without default value for partOf relation.
	 * 
	 * @param attributeDescriptorModel
	 *           start point of finding hierarchical structure of all required attributes
	 * @return hierarchical structure of all required attributes
	 */
	RequiredAttribute create(final AttributeDescriptorModel attributeDescriptorModel);

	/**
	 * Recursively creates {@link RequiredAttribute} structure for given composed type. The mechanism takes into account
	 * only unique attributes or not optional attributes without default value for partOf relation.
	 *
	 * @param composedTypeModel
	 *           start point of finding hierarchical structure of all required attributes
	 * @return hierarchical structure of all required attributes
	 */
	RequiredAttribute create(ComposedTypeModel composedTypeModel);
}
