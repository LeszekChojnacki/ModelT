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
package com.hybris.backoffice.excel.imp.wizard.renderer;

import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Allows to create element which displays information about validation issue
 * 
 * @param <T>
 */
interface ExcelValidationDetailsComponentFactory<T extends Component>
{

	T createValidationDetails(final Object columnHeader, final ValidationMessage validationMessage);

}
