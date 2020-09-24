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
package com.hybris.backoffice.excel.translators.generic.factory;

import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.template.filter.ExcelFilter;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;


/**
 * Default implementation of {@link RequiredAttributesFactory}. The services creates hierarchical structure of all
 * required attributes for given composed type or attribute descriptor.
 */
public class DefaultRequiredAttributesFactory implements RequiredAttributesFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultRequiredAttributesFactory.class);
	private int maxDepth = 50;
	private TypeService typeService;
	private ExcelFilter<AttributeDescriptorModel> uniqueFilter;
	private ExcelFilter<AttributeDescriptorModel> mandatoryFilter;
	private ExcelFilter<AttributeDescriptorModel> filter;


	@Override
	public RequiredAttribute create(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return traverseRecursively(attributeDescriptorModel, null, 0);
	}

	@Override
	public RequiredAttribute create(final ComposedTypeModel composedTypeModel)
	{
		final RequiredAttribute rootAttribute = new RequiredAttribute(composedTypeModel, composedTypeModel.getCode(),
				StringUtils.EMPTY, false, false, false);
		return traverse(composedTypeModel, rootAttribute, 0);
	}

	private RequiredAttribute traverseRecursively(final AttributeDescriptorModel attributeDescriptorModel,
			final RequiredAttribute parentAttribute, final int depth)
	{
		if (depth > getMaxDepth())
		{
			return parentAttribute;
		}
		final TypeModel attributeType = attributeDescriptorModel.getAttributeType();
		final boolean isCollectionOfAtomicTypes = attributeType instanceof CollectionTypeModel
				&& ((CollectionTypeModel) attributeType).getElementType() instanceof AtomicTypeModel;
		final boolean isAtomicType = attributeType instanceof AtomicTypeModel;
		if (isAtomicType || isCollectionOfAtomicTypes)
		{
			return handleAtomicType(attributeDescriptorModel, parentAttribute, attributeType);
		}

		final RequiredAttribute currentUniqueAttribute = new RequiredAttribute(attributeType,
				attributeDescriptorModel.getEnclosingType().getCode(), attributeDescriptorModel.getQualifier(),
				BooleanUtils.isTrue(uniqueFilter.test(attributeDescriptorModel)),
				BooleanUtils.isTrue(mandatoryFilter.test(attributeDescriptorModel)),
				BooleanUtils.isTrue(attributeDescriptorModel.getPartOf()));
		if (parentAttribute != null)
		{
			parentAttribute.addChild(currentUniqueAttribute);
		}

		final ComposedTypeModel composedType = findComposedType(attributeDescriptorModel);
		traverse(composedType, currentUniqueAttribute, depth + 1);
		return parentAttribute != null ? parentAttribute : currentUniqueAttribute;
	}

	private ComposedTypeModel findComposedType(final AttributeDescriptorModel attributeDescriptorModel)
	{
		final TypeModel attributeType = attributeDescriptorModel.getAttributeType();
		if (attributeDescriptorModel instanceof RelationDescriptorModel)
		{
			return handleRelationType((RelationDescriptorModel) attributeDescriptorModel);
		}
		else if (attributeType instanceof CollectionTypeModel)
		{
			return handleCollectionType((CollectionTypeModel) attributeType);
		}
		return loadComposedType(attributeDescriptorModel);
	}

	private ComposedTypeModel loadComposedType(final AttributeDescriptorModel attributeDescriptorModel)
	{
		try
		{
			return typeService.getComposedTypeForCode(attributeDescriptorModel.getAttributeType().getCode());
		}
		catch (final RuntimeException ex)
		{
			LOG.debug(String.format("Cannot load composed type for %s.%s", attributeDescriptorModel.getEnclosingType().getCode(),
					attributeDescriptorModel.getQualifier()), ex);
			return null;
		}
	}

	private static ComposedTypeModel handleRelationType(final RelationDescriptorModel attributeDescriptorModel)
	{
		if (BooleanUtils.isFalse(attributeDescriptorModel.getIsSource()))
		{
			return attributeDescriptorModel.getRelationType().getSourceType();
		}
		return attributeDescriptorModel.getRelationType().getTargetType();
	}

	private ComposedTypeModel handleCollectionType(final CollectionTypeModel attributeType)
	{
		return typeService.getComposedTypeForCode(attributeType.getElementType().getCode());
	}

	private RequiredAttribute handleAtomicType(final AttributeDescriptorModel attributeDescriptorModel,
			final RequiredAttribute parentAttribute, final TypeModel attributeType)
	{
		final RequiredAttribute uniqueAttribute = new RequiredAttribute(attributeType,
				attributeDescriptorModel.getEnclosingType().getCode(), attributeDescriptorModel.getQualifier(),
				BooleanUtils.isTrue(uniqueFilter.test(attributeDescriptorModel)),
				BooleanUtils.isTrue(mandatoryFilter.test(attributeDescriptorModel)),
				BooleanUtils.isTrue(attributeDescriptorModel.getPartOf()));
		if (parentAttribute != null)
		{
			parentAttribute.addChild(uniqueAttribute);
		}
		return parentAttribute != null ? parentAttribute : uniqueAttribute;
	}

	private RequiredAttribute traverse(final ComposedTypeModel composedTypeModel, final RequiredAttribute rootAttribute,
			final int depth)
	{
		final Collection<AttributeDescriptorModel> allAttributes = new HashSet<>();
		if (composedTypeModel != null)
		{
			allAttributes.addAll(composedTypeModel.getDeclaredattributedescriptors());
			allAttributes.addAll(composedTypeModel.getInheritedattributedescriptors());
		}
		final List<AttributeDescriptorModel> uniqueAttributesModels = filterAttributes(allAttributes);

		for (final AttributeDescriptorModel uniqueAttributesModel : uniqueAttributesModels)
		{
			traverseRecursively(uniqueAttributesModel, rootAttribute, depth + 1);
		}
		return rootAttribute;
	}

	private List<AttributeDescriptorModel> filterAttributes(final Collection<AttributeDescriptorModel> allAttributes)
	{
		return allAttributes.stream() //
				.filter(attribute -> filter.test(attribute)).collect(Collectors.toList());
	}

	public int getMaxDepth()
	{
		return maxDepth;
	}

	public void setMaxDepth(final int maxDepth)
	{
		this.maxDepth = maxDepth;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public ExcelFilter<AttributeDescriptorModel> getFilter()
	{
		return filter;
	}

	@Required
	public void setFilter(final ExcelFilter<AttributeDescriptorModel> filter)
	{
		this.filter = filter;
	}

	public ExcelFilter<AttributeDescriptorModel> getUniqueFilter()
	{
		return uniqueFilter;
	}

	@Required
	public void setUniqueFilter(final ExcelFilter<AttributeDescriptorModel> uniqueFilter)
	{
		this.uniqueFilter = uniqueFilter;
	}

	public ExcelFilter<AttributeDescriptorModel> getMandatoryFilter() {
		return mandatoryFilter;
	}

	@Required
	public void setMandatoryFilter(final ExcelFilter<AttributeDescriptorModel> mandatoryFilter) {
		this.mandatoryFilter = mandatoryFilter;
	}
}
