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
package com.hybris.backoffice.daos;

import de.hybris.platform.core.model.media.MediaModel;

import java.util.Collection;


public interface BackofficeConfigurationDao
{
	/**
	 * Find all medias for given code.
	 *
	 * @param code medias code
	 */
	Collection<MediaModel> findMedias(final String code);
}
