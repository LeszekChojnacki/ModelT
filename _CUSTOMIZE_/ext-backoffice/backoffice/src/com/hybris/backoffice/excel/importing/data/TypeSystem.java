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
package com.hybris.backoffice.excel.importing.data;

import java.util.Optional;


/**
 * Interface which represents methods for POJO which contains data read from Excel's TypeSystem
 *
 * @param <SYSTEMROW>
 */
public interface TypeSystem<SYSTEMROW>
{
	/**
	 * Allows to retrieve the whole row of TypeSystem's sheet based on passed attributeDisplayName
	 *
	 * @param attributeDisplayName
	 *           value of "attributeDisplayName" column
	 * @return the whole row
	 */
	Optional<SYSTEMROW> findRow(final String attributeDisplayName);
}
