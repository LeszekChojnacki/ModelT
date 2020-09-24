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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.ExpressionEvaluator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Resolver that gets the values from attributes on the model. By default, if parameter attribute is not specified, it
 * tries to get the attribute with the same name as the one configured on the indexed property.
 * <p>
 * <h4>Supported parameters:</h4>
 * <p>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 summary="Table showing supported parameters.">
 * <tr bgcolor="#ccccff">
 * <th>Parameter
 * <th>Default value
 * <th>Description
 * <tr valign=top>
 * <td>optional
 * <td>true
 * <td>If false, indicates that the resolved values should not be null and not an empty string (for every qualifier). If
 * these conditions are not met, an exception of type {@link FieldValueProviderException} is thrown.
 * <tr valign=top bgcolor="#eeeeff">
 * <td>attribute
 * <td>
 * <td>If specified, this is the name of the attribute.
 * <tr valign=top>
 * <td>split
 * <td>false
 * <td>If true, splits any resolved value around matches of a regular expression (only if the value is of type String).
 * <tr valign=top bgcolor="#eeeeff">
 * <td>splitRegex
 * <td>\s+
 * <td>If split is true this is the regular expression to use.
 * <tr valign=top>
 * <td>format
 * <td>null
 * <td>The ID of the Format Bean that is going to be used to format the attribute value object before applying the split
 * <tr valign=top>
 * <td>evaluateExpression
 * <td>false
 * <td>If true the attribute name is assumed to be a spring expression language that need to be evaluated
 * </table>
 * </blockquote>
 */
public class ModelAttributesValueResolver<T extends ItemModel> extends AbstractValueResolver<T, Object, Object>
{
	public static final String OPTIONAL_PARAM = "optional";
	public static final boolean OPTIONAL_PARAM_DEFAULT_VALUE = true;

	public static final String ATTRIBUTE_PARAM = "attribute";
	public static final String ATTRIBUTE_PARAM_DEFAULT_VALUE = null;

	public static final String EVALUATE_EXPRESSION_PARAM = "evaluateExpression";
	public static final boolean EVALUATE_EXPRESSION_PARAM_DEFAULT_VALUE = false;

	private ModelService modelService;
	private TypeService typeService;

	private ExpressionEvaluator expressionEvaluator;

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
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

	public ExpressionEvaluator getExpressionEvaluator()
	{
		return expressionEvaluator;
	}

	@Required
	public void setExpressionEvaluator(final ExpressionEvaluator expressionEvaluator)
	{
		this.expressionEvaluator = expressionEvaluator;
	}

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final T model, final ValueResolverContext<Object, Object> resolverContext)
			throws FieldValueProviderException
	{
		boolean hasValue = false;
		final String attributeName = getAttributeName(indexedProperty);
		final Object attributeValue = getAttributeValue(indexedProperty, model, attributeName);

		hasValue = filterAndAddFieldValues(document, batchContext, indexedProperty, attributeValue,
				resolverContext.getFieldQualifier());

		if (!hasValue)
		{
			final boolean isOptional = ValueProviderParameterUtils.getBoolean(indexedProperty, OPTIONAL_PARAM,
					OPTIONAL_PARAM_DEFAULT_VALUE);
			if (!isOptional)
			{
				throw new FieldValueProviderException("No value resolved for indexed property " + indexedProperty.getName());
			}
		}
	}

	protected String getAttributeName(final IndexedProperty indexedProperty)
	{
		String attributeName = ValueProviderParameterUtils.getString(indexedProperty, ATTRIBUTE_PARAM,
				ATTRIBUTE_PARAM_DEFAULT_VALUE);

		if (attributeName == null)
		{
			attributeName = indexedProperty.getName();
		}

		return attributeName;
	}

	protected Object getAttributeValue(final IndexedProperty indexedProperty, final T model, final String attributeName)
			throws FieldValueProviderException
	{
		Object value = null;

		if (StringUtils.isNotEmpty(attributeName))
		{
			final boolean evaluateExpression = ValueProviderParameterUtils.getBoolean(indexedProperty, EVALUATE_EXPRESSION_PARAM,
					EVALUATE_EXPRESSION_PARAM_DEFAULT_VALUE);
			if (evaluateExpression)
			{
				value = expressionEvaluator.evaluate(attributeName, model);
			}
			else
			{
				final ComposedTypeModel composedType = typeService.getComposedTypeForClass(model.getClass());

				if (typeService.hasAttribute(composedType, attributeName))
				{
					value = modelService.getAttributeValue(model, attributeName);
				}
			}
		}

		return value;
	}
}
