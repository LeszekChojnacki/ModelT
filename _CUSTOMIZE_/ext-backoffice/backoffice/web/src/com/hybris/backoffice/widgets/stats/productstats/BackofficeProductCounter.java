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
package com.hybris.backoffice.widgets.stats.productstats;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;

import javax.annotation.Nonnull;


public interface BackofficeProductCounter
{
	long countProducts();

	long countProducts(@Nonnull final ArticleApprovalStatus status);
}
