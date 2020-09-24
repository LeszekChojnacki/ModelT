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

import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;

import java.util.Collection;

/**
 * Dao to perform validation operations
 * @spring.bean backofficeValidationDao
 */
public interface BackofficeValidationDao
{

    /**
     * Queries for constraint groups with specified ids
     *
     * @param groupsIds identities of groups to be queried
     * @return groups with provided identities
     */
    Collection<ConstraintGroupModel> getConstraintGroups(final Collection<String> groupsIds);

}
