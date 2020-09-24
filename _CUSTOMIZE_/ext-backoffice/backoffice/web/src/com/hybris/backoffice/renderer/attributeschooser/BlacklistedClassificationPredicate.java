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
package com.hybris.backoffice.renderer.attributeschooser;

import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;

import java.util.function.Predicate;


/**
 * Default implementation of predicated which checks whether given classification system is on a blacklist. By default
 * all classification systems are allowed, therefore this implementation returns false, which means that given system is
 * not on blacklist.
 */
public class BlacklistedClassificationPredicate implements Predicate<ClassificationSystemModel>
{
	@Override
	public boolean test(final ClassificationSystemModel classificationSystemModel)
	{
		return false;
	}
}
