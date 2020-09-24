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
package com.hybris.backoffice.excel.template.cell;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.core.Ordered;


/**
 * Defines how to retrieve cell value for specific excel type
 */
@FunctionalInterface
interface CellValue extends Ordered
{

	Optional<String> getValue(@Nullable final Cell cell);

	default boolean canHandle(final CellType cellType)
	{
		return false;
	}

	@Override
	default int getOrder()
	{
		return 0;
	}

}
