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

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.classification.ClassificationService;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.security.permissions.PermissionCRUDService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.backoffice.excel.template.filter.ExcelFilter;


/**
 * Default implementation of {@link ExcelClassificationService}
 */
public class DefaultExcelClassificationService implements ExcelClassificationService
{
	private CatalogService catalogService;
	private ClassificationService classificationService;
	private PermissionCRUDService permissionCRUDService;

	private Collection<ExcelFilter<ClassificationSystemVersionModel>> filters;

	@Override
	public Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getItemsIntersectedClassificationClasses(
			final Collection<ItemModel> items)
	{
		if (!hasPermissionsToClassification())
		{
			return Collections.emptyMap();
		}

		return getFilteredMap(getItemsClassificationClassesInternal(items, true));
	}

	@Override
	public Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getItemsAddedClassificationClasses(
			final Collection<ItemModel> items)
	{
		if (!hasPermissionsToClassification())
		{
			return Collections.emptyMap();
		}

		return getFilteredMap(getItemsClassificationClassesInternal(items, false));
	}

	@Override
	public Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getAllClassificationClasses()
	{
		if (!hasPermissionsToClassification())
		{
			return Collections.emptyMap();
		}

		return getFilteredMap(getAllClassificationClassesInternal());
	}

	protected Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getFilteredMap(
			final Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> map)
	{
		return map //
				.entrySet() //
				.stream() //
				.filter(e -> filter(e.getKey())) //
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	protected boolean hasPermissionsToClassification()
	{
		return permissionCRUDService.canReadType(ClassificationClassModel._TYPECODE)
				&& permissionCRUDService.canReadType(ClassificationAttributeModel._TYPECODE)
				&& permissionCRUDService.canReadType(ClassAttributeAssignmentModel._TYPECODE);
	}

	protected boolean filter(final ClassificationSystemVersionModel classificationSystemVersionModel)
	{
		return CollectionUtils.emptyIfNull(filters).stream().allMatch(filter -> filter.test(classificationSystemVersionModel));
	}

	private Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getAllClassificationClassesInternal()
	{
		final List<CategoryModel> rootCategories = catalogService.getAllCatalogsOfType(ClassificationSystemModel.class).stream() //
				.map(ClassificationSystemModel::getCatalogVersions) //
				.flatMap(Collection::stream) //
				.map(CatalogVersionModel::getRootCategories) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toList());

		final List<CategoryModel> allCategories = new ArrayList<>();
		allCategories.addAll(rootCategories);
		allCategories.addAll(rootCategories.stream().map(CategoryModel::getAllSubcategories).flatMap(Collection::stream)
				.collect(Collectors.toList()));
		return allCategories.stream() //
				.filter(ClassificationClassModel.class::isInstance) //
				.map(ClassificationClassModel.class::cast) //
				.collect( //
						Collectors.groupingBy(ClassificationClassModel::getCatalogVersion, //
								Collectors.collectingAndThen( //
										Collectors.toMap(CategoryModel::getCode, Function.identity(), (left, right) -> left //
										), //
										map -> new ArrayList<>(map.values()))));
	}

	private Map<ClassificationSystemVersionModel, List<ClassificationClassModel>> getItemsClassificationClassesInternal(
			final Collection<ItemModel> items, final boolean useIntersection)
	{
		final BinaryOperator<Set<ClassificationClassModel>> reduceOperator = (set1, set2) -> useIntersection //
				? Sets.intersection(set1, set2) //
				: Sets.union(set1, set2).stream().collect( //
						Collectors.collectingAndThen( //
								Collectors.toMap( //
										CategoryModel::getCode, Function.identity(), (left, right) -> left //
								), //
								map -> new HashSet<>(map.values()) //
						));

		return items.stream()//
				.map(ProductModel.class::cast)//
				.map(classificationService::getFeatures)//
				.map(FeatureList::getClassificationClasses)//
				.reduce(reduceOperator)//
				.map(classes -> classes.stream()
						.collect(Collectors.groupingBy(ClassificationClassModel::getCatalogVersion, Collectors.toList())))
				.orElse(Collections.emptyMap());
	}

	@Required
	public void setCatalogService(final CatalogService catalogService)
	{
		this.catalogService = catalogService;
	}

	@Required
	public void setClassificationService(final ClassificationService classificationService)
	{
		this.classificationService = classificationService;
	}

	@Required
	public void setPermissionCRUDService(final PermissionCRUDService permissionCRUDService)
	{
		this.permissionCRUDService = permissionCRUDService;
	}

	// optional
	public void setFilters(final Collection<ExcelFilter<ClassificationSystemVersionModel>> filters)
	{
		this.filters = filters;
	}
}
