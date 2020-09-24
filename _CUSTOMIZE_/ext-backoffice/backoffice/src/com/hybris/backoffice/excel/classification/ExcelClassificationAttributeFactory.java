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
package com.hybris.backoffice.excel.classification;

import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;
import com.hybris.backoffice.excel.template.populator.extractor.ClassificationFullNameExtractor;


public class ExcelClassificationAttributeFactory
{
	private ClassificationFullNameExtractor classificationFullNameExtractor;

	public ExcelClassificationAttribute create(final ClassAttributeAssignmentModel attributeAssignment, final String isoCode)
	{
		final ExcelClassificationAttribute attribute = new ExcelClassificationAttribute();
		attribute.setIsoCode(isoCode);
		attribute.setAttributeAssignment(attributeAssignment);
		attribute.setName(classificationFullNameExtractor.extract(attribute));
		return attribute;
	}

	public ExcelClassificationAttribute create(final ClassAttributeAssignmentModel attributeAssignment)
	{
		return create(attributeAssignment, null);
	}

	@Required
	public void setClassificationFullNameExtractor(final ClassificationFullNameExtractor classificationFullNameExtractor)
	{
		this.classificationFullNameExtractor = classificationFullNameExtractor;
	}
}
