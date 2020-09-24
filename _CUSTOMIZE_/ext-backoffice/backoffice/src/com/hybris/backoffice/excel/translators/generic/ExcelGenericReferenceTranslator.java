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
package com.hybris.backoffice.excel.translators.generic;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.RelationDescriptorModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.exceptions.ModelTypeNotSupportedException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;

import com.hybris.backoffice.excel.data.Impex;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.AbstractValidationAwareTranslator;
import com.hybris.backoffice.excel.translators.generic.factory.ExportDataFactory;
import com.hybris.backoffice.excel.translators.generic.factory.ImportImpexFactory;
import com.hybris.backoffice.excel.translators.generic.factory.ReferenceFormatFactory;
import com.hybris.backoffice.excel.translators.generic.factory.RequiredAttributesFactory;


/**
 * Default generic translator for all references
 */
public class ExcelGenericReferenceTranslator extends AbstractValidationAwareTranslator
{

	private static final Logger LOG = LoggerFactory.getLogger(ExcelGenericReferenceTranslator.class);

	private int order = Ordered.LOWEST_PRECEDENCE - 100;
	private RequiredAttributesFactory requiredAttributesFactory;
	private ReferenceFormatFactory referenceFormatFactory;
	private ExportDataFactory exportDataFactory;
	private ImportImpexFactory importImpexFactory;
	private TypeService typeService;
	private List<String> excludedFields;

	@Override
	public boolean canHandle(final AttributeDescriptorModel attributeDescriptor)
	{
		final TypeModel attributeType = attributeDescriptor.getAttributeType();
		final boolean isNotPartOf = BooleanUtils.isNotTrue(attributeDescriptor.getPartOf());
		final boolean isRelation = attributeDescriptor instanceof RelationDescriptorModel;
		final boolean isCollection = attributeType instanceof CollectionTypeModel;
		final boolean isReference = attributeType instanceof ComposedTypeModel
				&& !ComposedTypeModel._TYPECODE.equals(attributeType.getCode());
		final boolean isTypeAllowed = isRelation || isCollection || isReference;
		return isNotPartOf && isTypeAllowed && !isAttributeExcluded(attributeDescriptor)
				&& hasAtLeastOneUniqueAttribute(attributeDescriptor);
	}

	private boolean hasAtLeastOneUniqueAttribute(final AttributeDescriptorModel attributeDescriptor)
	{
		final RequiredAttribute rootUniqueAttribute = getRequiredAttributes(attributeDescriptor);
		return CollectionUtils.isNotEmpty(rootUniqueAttribute.getChildren());
	}

	private boolean isAttributeExcluded(final AttributeDescriptorModel attributeDescriptor)
	{
		if (CollectionUtils.isEmpty(getExcludedFields()))
		{
			return false;
		}
		final String enclosingType = attributeDescriptor.getEnclosingType().getCode();
		final String expectedQualifier = attributeDescriptor.getQualifier();
		for (final String excludedField : getExcludedFields())
		{
			final String[] splitValue = excludedField.split("\\.");
			if (splitValue.length != 2)
			{
				LOG.warn("%s has incorrect format. Expected format is 'type.attribute'", excludedField);
				continue;
			}
			if (expectedQualifier.equals(splitValue[1]))
			{
				final boolean assignableFrom = tryToCheckIfIsAssignableFrom(splitValue[0], enclosingType);
				if (assignableFrom)
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean tryToCheckIfIsAssignableFrom(final String superModelTypeCode, final String enclosingType)
	{
		try
		{
			return getTypeService().isAssignableFrom(superModelTypeCode, enclosingType);
		}
		catch (final UnknownIdentifierException ex)
		{
			LOG.warn("Unknown type: %s", superModelTypeCode);
			return false;
		}
	}

	@Override
	public String referenceFormat(final AttributeDescriptorModel attributeDescriptor)
	{
		final RequiredAttribute rootUniqueAttribute = getRequiredAttributes(attributeDescriptor);
		return getReferenceFormatFactory().create(rootUniqueAttribute);
	}

	@Override
	public Optional<Object> exportData(final Object objectToExport)
	{
		return Optional.empty();
	}

	@Override
	public Optional<?> exportData(final AttributeDescriptorModel attributeDescriptor, final Object objectToExport)
	{
		final RequiredAttribute rootUniqueAttribute = getRequiredAttributes(attributeDescriptor);
		try
		{
			return getExportDataFactory().create(rootUniqueAttribute, objectToExport);
		}
		catch (final ModelTypeNotSupportedException e)
		{
			LOG.error(String.format("Cannot export given object %s", objectToExport), e);
			return Optional.empty();
		}
	}

	@Override
	public Impex importData(final AttributeDescriptorModel attributeDescriptor, final ImportParameters importParameters)
	{
		final RequiredAttribute rootUniqueAttribute = getRequiredAttributes(attributeDescriptor);
		return getImportImpexFactory().create(rootUniqueAttribute, importParameters);
	}

	private RequiredAttribute getRequiredAttributes(final AttributeDescriptorModel attributeDescriptorModel)
	{
		return getRequiredAttributesFactory().create(attributeDescriptorModel);
	}

	public RequiredAttributesFactory getRequiredAttributesFactory()
	{
		return requiredAttributesFactory;
	}

	@Required
	public void setRequiredAttributesFactory(final RequiredAttributesFactory requiredAttributesFactory)
	{
		this.requiredAttributesFactory = requiredAttributesFactory;
	}

	public ReferenceFormatFactory getReferenceFormatFactory()
	{
		return referenceFormatFactory;
	}

	@Required
	public void setReferenceFormatFactory(final ReferenceFormatFactory referenceFormatFactory)
	{
		this.referenceFormatFactory = referenceFormatFactory;
	}

	public ExportDataFactory getExportDataFactory()
	{
		return exportDataFactory;
	}

	@Required
	public void setExportDataFactory(final ExportDataFactory exportDataFactory)
	{
		this.exportDataFactory = exportDataFactory;
	}

	public ImportImpexFactory getImportImpexFactory()
	{
		return importImpexFactory;
	}

	@Required
	public void setImportImpexFactory(final ImportImpexFactory importImpexFactory)
	{
		this.importImpexFactory = importImpexFactory;
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

	public List<String> getExcludedFields()
	{
		return excludedFields;
	}

	@Required
	public void setExcludedFields(final List<String> excludedFields)
	{
		this.excludedFields = excludedFields;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

}

