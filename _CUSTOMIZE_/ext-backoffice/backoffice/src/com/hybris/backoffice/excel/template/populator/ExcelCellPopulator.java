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

import java.util.function.Function;

import com.hybris.backoffice.excel.data.ExcelAttribute;


/**
 * Interface for excel's single cell population
 *
 * @param <T>
 *           type of {@link ExcelAttribute}
 */
@FunctionalInterface
public interface ExcelCellPopulator<T extends ExcelAttribute> extends Function<ExcelAttributeContext<T>, String>
{
}
