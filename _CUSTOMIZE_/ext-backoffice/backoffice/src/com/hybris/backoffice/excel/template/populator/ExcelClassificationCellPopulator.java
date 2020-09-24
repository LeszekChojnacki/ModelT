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
package com.hybris.backoffice.excel.template.populator;

import javax.annotation.Nonnull;

import com.hybris.backoffice.excel.data.ExcelClassificationAttribute;


/**
 * Interface for excel's single cell population dedicated for classification
 */
@FunctionalInterface
public interface ExcelClassificationCellPopulator extends ExcelCellPopulator<ExcelClassificationAttribute>
{
	@Override
	String apply(@Nonnull ExcelAttributeContext<ExcelClassificationAttribute> context);
}
