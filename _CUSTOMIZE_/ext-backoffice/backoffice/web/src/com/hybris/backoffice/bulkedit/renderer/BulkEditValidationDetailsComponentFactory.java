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
package com.hybris.backoffice.bulkedit.renderer;

import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.validation.model.ValidationInfo;


/**
 * Allows to create element which displays information about validation issue
 * 
 * @param <T>
 */
public interface BulkEditValidationDetailsComponentFactory<T extends Component>
{

	T createValidationDetails(final ValidationInfo validationMessage);

}
