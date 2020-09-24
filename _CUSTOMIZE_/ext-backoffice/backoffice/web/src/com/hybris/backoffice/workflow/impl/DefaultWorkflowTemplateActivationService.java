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
package com.hybris.backoffice.workflow.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.internal.i18n.LocalizationService;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.workflow.ScriptEvaluationService;
import de.hybris.platform.workflow.constants.WorkflowConstants;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowTemplateActivationAction;
import com.hybris.backoffice.workflow.WorkflowTemplateActivationCtx;
import com.hybris.backoffice.workflow.WorkflowTemplateActivationService;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;

import bsh.EvalError;


/**
 * default implementation of service which allows to create workflow based on workflow templates and its activation
 * scripts {@link WorkflowTemplateModel#getActivationScript()}
 */
public class DefaultWorkflowTemplateActivationService implements WorkflowTemplateActivationService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkflowTemplateActivationService.class);
	private static final String ITEM_NOT_NULL_VALIDATION_MESSAGE = "Item cannot be null";
	private static final String ITEM_MODEL_CTX_NOT_NULL_VALIDATION_MESSAGE = "itemModelCtx cannot be null";
	private TypeFacade typeFacade;
	private ScriptEvaluationService scriptEvaluationService;
	private TypeService typeService;
	private LocalizationService localizationService;
	private Set<String> workflowActivationSupportedTypes;

	@Override
	public void activateWorkflowTemplates(final Collection<WorkflowTemplateActivationCtx> activationCtxList)
	{
		if (CollectionUtils.isNotEmpty(activationCtxList))
		{
			activationCtxList.stream().filter(ctx -> ctx.getItem() != null && isSupportedType(ctx.getItem().getItemtype()))
					.forEach(ctx -> {
						try
						{
							scriptEvaluationService.evaluateActivationScripts(ctx.getItem(), ctx.getCurrentValues(),
									ctx.getInitialValues(), ctx.getWorkflowOperationType());
						}
						catch (final EvalError evalError)
						{
							LOG.warn("Cannot activate workflow template", evalError);
						}
					});
		}
	}

	@Override
	public List<WorkflowTemplateActivationCtx> prepareWorkflowTemplateActivationContexts(
			final Map<? extends ItemModel, WorkflowTemplateActivationAction> items, final Context invocationCtx)
	{
		final List<WorkflowTemplateActivationCtx> contexts = new ArrayList<>();
		if (MapUtils.isNotEmpty(items))
		{
			items.forEach((item, action) -> {
				if (item != null && isSupportedType(item.getItemtype()))
				{
					final WorkflowTemplateActivationCtx workflowCtx = prepareWorkflowTemplateActivationCtx(item, action,
							invocationCtx);
					CollectionUtils.addIgnoreNull(contexts, workflowCtx);
				}
			});
		}
		return contexts;
	}

	protected boolean isSupportedType(final String typeCode)
	{
		return StringUtils.isNotBlank(typeCode) && workflowActivationSupportedTypes.stream()
				.filter(supportedType -> supportedType.equals(typeCode) || typeService.isAssignableFrom(supportedType, typeCode))
				.findAny().isPresent();
	}

	protected WorkflowTemplateActivationCtx prepareWorkflowTemplateActivationCtx(final ItemModel item,
			final WorkflowTemplateActivationAction action, final Context invocationCtx)
	{
		Validate.notNull(ITEM_NOT_NULL_VALIDATION_MESSAGE, item);
		Validate.notNull("Action cannot be null", action);

		final DataType dataType = loadDataType(item.getItemtype());
		WorkflowTemplateActivationCtx ctx = null;
		if (dataType != null)
		{
			switch (action)
			{
				case CREATE:
					ctx = prepareContextForCreate(item, dataType, invocationCtx);
					break;
				case SAVE:
					ctx = prepareContextForSave(item, dataType, invocationCtx);
					break;
				default:
					ctx = prepareContextForRemove(item, dataType, invocationCtx);
			}
		}
		return ctx;
	}

	protected DataType loadDataType(final String typeCode)
	{
		DataType dataType = null;
		try
		{
			dataType = typeFacade.load(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.error(String.format("Cannot load type for code %s", typeCode), e);
		}
		return dataType;
	}

	protected WorkflowTemplateActivationCtx prepareContextForRemove(final ItemModel item, final DataType dataType,
			final Context invocationCtx)
	{
		LOG.debug("{} workflow template activation action is not supported", WorkflowTemplateActivationAction.REMOVE);
		return null;
	}

	protected WorkflowTemplateActivationCtx prepareContextForSave(final ItemModel item, final DataType dataType,
			final Context invocationCtx)
	{
		Validate.notNull(ITEM_NOT_NULL_VALIDATION_MESSAGE, item);
		Validate.notNull("DataType cannot be null", dataType);

		final ItemModelContext itemModelContext = getItemModelContext(item);

		final Set<String> modifiedAttributes = collectModifiedAttributes(itemModelContext, dataType);
		final Map<String, Set<Locale>> modifiedLocalizedAttributes = collectModifiedLocalizedAttributes(itemModelContext, dataType);

		final Map<String, Object> currentValues = collectCurrentValues(item, modifiedAttributes, modifiedLocalizedAttributes);
		final Map<String, Object> originalValues = collectOriginalValues(itemModelContext, modifiedAttributes,
				modifiedLocalizedAttributes);

		final WorkflowTemplateActivationCtx ctx = new WorkflowTemplateActivationCtx(item, currentValues, originalValues,
				WorkflowConstants.WorkflowActivationScriptActions.SAVE);
		copyInvocationContext(invocationCtx, ctx);
		return ctx;
	}

	protected WorkflowTemplateActivationCtx prepareContextForCreate(final ItemModel item, final DataType dataType,
			final Context invocationCtx)
	{
		Validate.notNull(ITEM_NOT_NULL_VALIDATION_MESSAGE, item);
		Validate.notNull("DataType cannot be null", dataType);

		final ItemModelContext itemModelContext = getItemModelContext(item);

		final Set<String> modifiedAttributes = collectModifiedAttributes(itemModelContext, dataType);
		final Map<String, Set<Locale>> modifiedLocalizedAttributes = collectModifiedLocalizedAttributes(itemModelContext, dataType);

		final Map<String, Object> currentValues = collectCurrentValues(item, modifiedAttributes, modifiedLocalizedAttributes);

		final WorkflowTemplateActivationCtx ctx = new WorkflowTemplateActivationCtx(item, currentValues,
				WorkflowConstants.WorkflowActivationScriptActions.CREATE);
		copyInvocationContext(invocationCtx, ctx);
		return ctx;
	}

	protected ItemModelContext getItemModelContext(final ItemModel item)
	{
		Validate.notNull(ITEM_NOT_NULL_VALIDATION_MESSAGE, item);
		return ModelContextUtils.getItemModelContext(item);
	}

	protected Map<String, Object> collectCurrentValues(final ItemModel item, final Set<String> attributes,
			final Map<String, Set<Locale>> localizedAttributes)
	{
		Validate.notNull(ITEM_NOT_NULL_VALIDATION_MESSAGE, item);

		final Map<String, Object> caseInsensitiveAttributesMap = new CaseInsensitiveMap<>();
		caseInsensitiveAttributesMap.putAll(collectValues(attributes, item::getProperty));
		caseInsensitiveAttributesMap.putAll(collectLocalizedValues(localizedAttributes, item::getProperty));
		return caseInsensitiveAttributesMap;
	}

	protected Map<String, Object> collectOriginalValues(final ItemModelContext itemModelCtx, final Set<String> attributes,
			final Map<String, Set<Locale>> localizedAttributes)
	{
		Validate.notNull(ITEM_MODEL_CTX_NOT_NULL_VALIDATION_MESSAGE, itemModelCtx);
		final Map<String, Object> caseInsensitiveAttributesMap = new CaseInsensitiveMap<>();
		caseInsensitiveAttributesMap.putAll(collectValues(attributes, itemModelCtx::getOriginalValue));
		caseInsensitiveAttributesMap.putAll(collectLocalizedValues(localizedAttributes, itemModelCtx::getOriginalValue));
		return caseInsensitiveAttributesMap;
	}

	protected Map<String, Object> collectValues(final Set<String> attributes, final Function<String, Object> attributeValue)
	{
		Validate.notNull("attributes cannot be null", attributes);
		Validate.notNull("attributeValue function cannot be null", attributeValue);

		final Map<String, Object> attrValMap = new HashMap<>();
		attributes.forEach(attribute -> attrValMap.put(attribute, attributeValue.apply(attribute)));
		return attrValMap;
	}

	protected Map<String, Object> collectLocalizedValues(final Map<String, Set<Locale>> localizedAttributes,
			final BiFunction<String, Locale, Object> localizedAttributeValue)
	{
		Validate.notNull("localizedAttributes cannot be null", localizedAttributes);
		Validate.notNull("localizedAttributeValue function cannot be null", localizedAttributeValue);

		final Map<String, Object> attrValMap = new HashMap<>();
		localizedAttributes
				.forEach((key, val) -> attrValMap.put(key, getLocalizedValuesForAttribute(key, val, localizedAttributeValue)));
		return attrValMap;
	}

	protected Map<String, Object> getLocalizedValuesForAttribute(final String attribute, final Set<Locale> locales,
			final BiFunction<String, Locale, Object> attributeValue)
	{
		Validate.notNull("locales cannot be null", locales);
		Validate.notNull("attributeValue function cannot be null", attributeValue);

		final Map<String, Object> localizedValue = new HashMap<>();
		locales.forEach(locale -> {
			final String langIsoCode = getLanguageIsoCodeForLocale(locale);
			if (StringUtils.isNotBlank(langIsoCode))
			{
				localizedValue.put(langIsoCode, attributeValue.apply(attribute, locale));
			}
		});
		return localizedValue;
	}

	protected Set<String> collectModifiedAttributes(final ItemModelContext itemModelCtx, final DataType dataType)
	{
		Validate.notNull(ITEM_MODEL_CTX_NOT_NULL_VALIDATION_MESSAGE, itemModelCtx);
		Validate.notNull("dataType cannot be null", dataType);

		return itemModelCtx.getDirtyAttributes().stream()//
				.filter(attr -> dataType.getAttribute(attr) != null)//
				.collect(Collectors.toSet());
	}

	protected Map<String, Set<Locale>> collectModifiedLocalizedAttributes(final ItemModelContext itemModelCtx,
			final DataType dataType)
	{
		Validate.notNull(ITEM_MODEL_CTX_NOT_NULL_VALIDATION_MESSAGE, itemModelCtx);
		Validate.notNull("dataType cannot be null", dataType);

		final Map<String, Set<Locale>> modifiedLocalizedAttributes = new HashMap<>();
		final Map<Locale, Set<String>> dirtyLocalizedAttributes = itemModelCtx.getDirtyLocalizedAttributes();
		dirtyLocalizedAttributes
				.forEach((locale, attributes) -> attributes.stream().filter(attr -> dataType.getAttribute(attr) != null).forEach(
						attribute -> modifiedLocalizedAttributes.computeIfAbsent(attribute, k -> new HashSet<>()).add(locale)));

		return modifiedLocalizedAttributes;
	}

	protected void copyInvocationContext(final Context invocationCtx, final Context toCtx)
	{
		if (invocationCtx != null && CollectionUtils.isNotEmpty(invocationCtx.getAttributeNames()) && toCtx != null)
		{
			invocationCtx.getAttributeNames()
					.forEach(attribute -> toCtx.addAttribute(attribute, invocationCtx.getAttribute(attribute)));
		}
	}

	protected String getLanguageIsoCodeForLocale(final Locale locale)
	{
		return localizationService.getDataLanguageIsoCode(locale);
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	public TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setScriptEvaluationService(final ScriptEvaluationService scriptEvaluationService)
	{
		this.scriptEvaluationService = scriptEvaluationService;
	}

	public ScriptEvaluationService getScriptEvaluationService()
	{
		return scriptEvaluationService;
	}

	@Required
	public void setLocalizationService(final LocalizationService localizationService)
	{
		this.localizationService = localizationService;
	}

	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}

	@Required
	public void setWorkflowActivationSupportedTypes(final Set<String> workflowActivationSupportedTypes)
	{
		this.workflowActivationSupportedTypes = workflowActivationSupportedTypes;
	}

	public Set<String> getWorkflowActivationSupportedTypes()
	{
		return workflowActivationSupportedTypes;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}
}
