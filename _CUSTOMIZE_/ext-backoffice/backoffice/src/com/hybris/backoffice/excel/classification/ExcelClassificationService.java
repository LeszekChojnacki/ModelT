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
package com.hybris.backoffice.excel.classification;

import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Excel service dedicated for classification. It allows to retrieve the collection of {@link ClassificationClassModel}
 * which is grouped by {@link ClassificationSystemVersionModel}.
 */
public interface ExcelClassificationService
{

	/**
	 * {@link ClassificationClassModel}s are retrieved from given items and then intersected - it means that only common
	 * classification classes are returned.
	 *
	 * @param items
	 *           source of the classification classes
	 * @return collection of {@link ClassificationClassModel} grouped by {@link ClassificationSystemVersionModel}
	 */
	Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getItemsIntersectedClassificationClasses(
			final Collection<ItemModel> items);

	/**
	 * {@link ClassificationClassModel}s are retrieved from given items - it means that all classification classes from
	 * given items are returned.
	 *
	 * @param items
	 *           source of the classification classes
	 * @return collection of {@link ClassificationClassModel} grouped by {@link ClassificationSystemVersionModel}
	 */
	Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getItemsAddedClassificationClasses(
			final Collection<ItemModel> items);

	/**
	 * All {@link ClassificationClassModel}s from the system are returned.
	 *
	 * @return collection of {@link ClassificationClassModel} grouped by {@link ClassificationSystemVersionModel}
	 */
	Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getAllClassificationClasses();

}
