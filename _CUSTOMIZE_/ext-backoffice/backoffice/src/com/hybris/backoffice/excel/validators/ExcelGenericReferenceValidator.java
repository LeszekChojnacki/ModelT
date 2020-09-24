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
package com.hybris.backoffice.excel.validators;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.CollectionTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.translators.generic.RequiredAttribute;
import com.hybris.backoffice.excel.translators.generic.factory.RequiredAttributesFactory;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Validator for {@link com.hybris.backoffice.excel.translators.generic.ExcelGenericReferenceTranslator} The validator
 * tries to load all references and checks whether the references exist
 */
public class ExcelGenericReferenceValidator implements ExcelValidator
{

	protected static final String VALIDATION_MESSAGE_KEY = "excel.import.validation.generic.translator.not.existing.item";
	private RequiredAttributesFactory requiredAttributesFactory;
	private FlexibleSearchService flexibleSearchService;

	/**
	 * Validator can handle request only if call value is not empty
	 * 
	 * @param importParameters
	 * @param attributeDescriptor
	 * @return boolean whether validator can handle the cell.
	 */
	@Override
	public boolean canHandle(final ImportParameters importParameters, final AttributeDescriptorModel attributeDescriptor)
	{
		return importParameters.isCellValueNotBlank();
	}

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters,
			final AttributeDescriptorModel attributeDescriptor, final Map<String, Object> context)
	{
		final RequiredAttribute requiredAttribute = getRequiredAttributesFactory().create(attributeDescriptor);
		return validateRequiredAttribute(requiredAttribute, importParameters, context);
	}

	/**
	 * Validates given cell and returns validation result. If cell doesn't have validation issues then
	 * {@link ExcelValidationResult} with no validation errors should be returned.
	 *
	 * @param requiredAttribute
	 *           hierarchical structure of required attributes
	 * @param importParameters
	 *           parsed parameters for given cell
	 * @param context
	 *           map which can be used as a cache. The map is shared between all request for given excel sheet.
	 * @return {@link ExcelValidationResult}
	 */
	public ExcelValidationResult validateRequiredAttribute(final RequiredAttribute requiredAttribute,
			final ImportParameters importParameters, final Map<String, Object> context)
	{
		return new ExcelValidationResult(recursivelyValidateAllLevels(requiredAttribute, importParameters, context));
	}

	/**
	 * Recursively checks whether reference exist. The method starts checking references from the lowest descendant. If
	 * reference from lower level doesn't exist then process is stopped. For example, if given Catalog.id doesn't exist then
	 * reference for catalog version is not checked.
	 * 
	 * @param rootUniqueAttribute
	 * @param importParameters
	 * @param context
	 * @return
	 */
	protected List<ValidationMessage> recursivelyValidateAllLevels(final RequiredAttribute rootUniqueAttribute,
			final ImportParameters importParameters, final Map<String, Object> context)
	{
		final List<ValidationMessage> messages = new ArrayList<>();
		for (final RequiredAttribute child : rootUniqueAttribute.getChildren())
		{
			messages.addAll(recursivelyValidateAllLevels(child, importParameters, context));
		}
		if (messages.isEmpty())
		{
			messages.addAll(validateSingleLevel(rootUniqueAttribute, importParameters, context));
		}
		return messages;
	}

	/**
	 * Validates just single level without checking children. This method is taking into account multi-values.
	 * 
	 * @param rootUniqueAttribute
	 * @param importParameters
	 * @param context
	 * @return
	 */
	protected List<ValidationMessage> validateSingleLevel(final RequiredAttribute rootUniqueAttribute,
			final ImportParameters importParameters, final Map<String, Object> context)
	{
		final List<ValidationMessage> validationMessages = new ArrayList<>();
		for (final Map<String, String> params : importParameters.getMultiValueParameters())
		{
			validateSingleValue(rootUniqueAttribute, params, context).ifPresent(validationMessages::add);
		}
		return validationMessages;
	}

	/**
	 * This method validates just single value from single level. For example, it checks whether given catalog version
	 * exist. To check catalog version, the method assumes that all children's references are checked and stored in context.
	 * 
	 * @param rootUniqueAttribute
	 * @param importParameters
	 * @param context
	 * @return
	 */
	protected Optional<ValidationMessage> validateSingleValue(final RequiredAttribute rootUniqueAttribute,
			final Map<String, String> importParameters, final Map<String, Object> context)
	{
		final String cacheKey = createCacheKey(rootUniqueAttribute, importParameters);
		if (context.containsKey(cacheKey))
		{
			return Optional.empty();
		}
		final Optional<FlexibleSearchQuery> flexibleSearchQuery = buildFlexibleSearchQuery(rootUniqueAttribute, importParameters,
				context);
		if (flexibleSearchQuery.isPresent())
		{
			final SearchResult<ItemModel> searchResult = flexibleSearchService.search(flexibleSearchQuery.get());
			final int count = searchResult.getCount();
			if (count != 1)
			{
				return Optional.of(prepareValidationMessage(rootUniqueAttribute, importParameters));
			}
			else
			{
				final ItemModel foundUniqueValue = searchResult.getResult().get(0);
				context.put(cacheKey, foundUniqueValue);
			}
		}
		return Optional.empty();
	}

	protected ValidationMessage prepareValidationMessage(final RequiredAttribute rootUniqueAttribute,
			final Map<String, String> importParameters)
	{
		final Map<String, String> allAtomicParams = findAllAtomicParams(rootUniqueAttribute, importParameters);
		return new ValidationMessage(VALIDATION_MESSAGE_KEY, getTypeCode(rootUniqueAttribute.getTypeModel()),
				allAtomicParams.toString());
	}

	/**
	 * Create cache key. The key is created as TYPE_allUniqueValues, for example CatalogVersion_Default_Online
	 * 
	 * @param rootUniqueAttribute
	 * @param params
	 * @return
	 */
	private static String createCacheKey(final RequiredAttribute rootUniqueAttribute, final Map<String, String> params)
	{
		final TypeModel typeModel = rootUniqueAttribute.getTypeModel();
		final Collection<String> allAtomicParams = findAllAtomicParams(rootUniqueAttribute, params).values();
		return String.format("%s_%s", getTypeCode(typeModel), String.join("_", allAtomicParams));
	}

	/**
	 * Recursively finds atomic values.
	 * 
	 * @param rootUniqueAttribute
	 * @param params
	 * @return
	 */
	private static Map<String, String> findAllAtomicParams(final RequiredAttribute rootUniqueAttribute,
			final Map<String, String> params)
	{
		final Map<String, String> atomicParams = new LinkedHashMap<>();

		if (rootUniqueAttribute.getTypeModel() instanceof AtomicTypeModel)
		{
			final String key = String.format("%s.%s", rootUniqueAttribute.getEnclosingType(), rootUniqueAttribute.getQualifier());
			atomicParams.put(key, params.get(key));
		}
		for (final RequiredAttribute child : rootUniqueAttribute.getChildren())
		{
			atomicParams.putAll(findAllAtomicParams(child, params));
		}
		return atomicParams;
	}

	protected Optional<FlexibleSearchQuery> buildFlexibleSearchQuery(final RequiredAttribute rootUniqueAttribute,
			final Map<String, String> importParameters, final Map context)
	{
		final TypeModel typeModel = rootUniqueAttribute.getTypeModel();
		final String typeCode = getTypeCode(typeModel);
		if (rootUniqueAttribute.getChildren().isEmpty())
		{
			return Optional.empty();
		}
		final QueryBuilder queryBuilder = new QueryBuilder(typeCode);
		for (int i = 0; i < rootUniqueAttribute.getChildren().size(); i++)
		{
			final RequiredAttribute child = rootUniqueAttribute.getChildren().get(i);
			final String fullQualifier = String.format("%s.%s", child.getEnclosingType(), child.getQualifier());
			if (child.getTypeModel() instanceof AtomicTypeModel)
			{
				final String value = StringUtils.defaultIfEmpty(importParameters.get(fullQualifier), StringUtils.EMPTY);
				queryBuilder.withParam(child.getQualifier(), value);
			}
			else
			{
				final String cacheKey = createCacheKey(child, importParameters);
				final Object foundObject = context.get(cacheKey);
				if (foundObject instanceof ItemModel)
				{
					queryBuilder.withParam(child.getQualifier(), ((ItemModel) foundObject).getPk().getLongValue());
				}
			}
		}
		return Optional.of(new FlexibleSearchQuery(queryBuilder.build(), queryBuilder.params));
	}

	/**
	 * Checks whether typeModel is {@link CollectionTypeModel}. If so, the method returns code of element type. Otherwise it
	 * returns code of provided type model
	 * 
	 * @param typeModel
	 * @return
	 */
	private static String getTypeCode(final TypeModel typeModel)
	{
		if (typeModel instanceof CollectionTypeModel)
		{
			return ((CollectionTypeModel) typeModel).getElementType().getCode();
		}
		return typeModel.getCode();
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

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	private static class QueryBuilder
	{
		private final String type;
		private final Map<String, Object> params = new LinkedHashMap<>();

		QueryBuilder(final String type)
		{
			this.type = type;
		}

		void withParam(final String key, final Object value)
		{
			params.put(key, value);
		}

		Map<String, Object> getParams()
		{
			return params;
		}

		String build()
		{
			final StringBuilder query = new StringBuilder();
			query.append("SELECT {pk} FROM {").append(type).append("} WHERE ");
			final int maxIndex = params.size() - 1;
			int index = 0;
			for (final Map.Entry<String, Object> entry : params.entrySet())
			{
				query.append("{").append(entry.getKey()).append("} = ?").append(entry.getKey());
				if (index < maxIndex)
				{
					query.append(" AND ");
				}
				index++;
			}
			return query.toString();
		}
	}

}
