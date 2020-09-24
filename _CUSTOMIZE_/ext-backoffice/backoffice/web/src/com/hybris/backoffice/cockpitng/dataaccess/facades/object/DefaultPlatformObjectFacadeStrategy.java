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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object;


import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import de.hybris.platform.servicelayer.model.ModelContextUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.ViewResultItem;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.backoffice.cockpitng.dataaccess.facades.common.PlatformFacadeStrategyHandleCache;
import com.hybris.backoffice.cockpitng.dataaccess.facades.object.savedvalues.ItemModificationHistoryService;
import com.hybris.backoffice.cockpitng.dataaccess.facades.object.savedvalues.ItemModificationInfo;
import com.hybris.backoffice.workflow.WorkflowTemplateActivationAction;
import com.hybris.backoffice.workflow.WorkflowTemplateActivationCtx;
import com.hybris.backoffice.workflow.WorkflowTemplateActivationService;
import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.clone.CloneStrategy;
import com.hybris.cockpitng.dataaccess.facades.clone.CloneStrategyRegistry;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectAccessException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectCloningException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectCreationException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectDeletionException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.validation.ValidationHandler;


public class DefaultPlatformObjectFacadeStrategy implements ObjectFacadeStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPlatformObjectFacadeStrategy.class);

	private ModelService modelService;
	private LabelService labelService;
	private TypeService typeService;
	private TypeFacade typeFacade;
	private PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache;
	private ItemModificationHistoryService itemModificationHistoryService;
	private WorkflowTemplateActivationService workflowTemplateActivationService;
	private ValidationHandler validationHandler;
	private CloneStrategyRegistry cloneStrategyRegistry;

	@Override
	public <T> T load(final String objectId, final Context ctx) throws ObjectNotFoundException
	{
		try
		{
			return modelService.get(PK.parse(objectId));
		}
		catch (final Exception ex)
		{
			throw new ObjectNotFoundException(objectId, ex);
		}
	}

	@Override
	public <T> void delete(final T object, final Context ctx) throws ObjectDeletionException
	{
		final ObjectFacadeOperationResult<T> delete = delete(Collections.singletonList(object), ctx);
		if (delete.hasError())
		{
			final ObjectAccessException error = delete.getErrorForObject(object);
			if (error instanceof ObjectDeletionException)
			{
				throw (ObjectDeletionException) error;
			}
		}
	}

	@Override
	public <T> ObjectFacadeOperationResult<T> delete(final Collection<T> objects, final Context ctx)
	{
		final ObjectFacadeOperationResult<T> result = new ObjectFacadeOperationResult<>();
		try
		{
			executeWithWorkflowTemplateActivation(modelService::removeAll, objects, ctx,
					item -> WorkflowTemplateActivationAction.REMOVE);
			objects.forEach(result::addSuccessfulObject);

			logItemDeletion(objects);
		}
		catch (final ModelRemovalException e)
		{
			objects.forEach(object -> result.addFailedObject(object, new ObjectDeletionException(String.valueOf(object), e)));
		}
		return result;
	}

	protected void logItemDeletion(final Collection<?> deletedObjects)
	{

		deletedObjects.forEach(deletedObject -> {
			final ItemModificationInfo modificationInfo = itemModificationHistoryService
					.createModificationInfo((ItemModel) deletedObject);

			itemModificationHistoryService.logItemModification(modificationInfo);
		});
	}

	@Override
	public <T> T create(final String typeId, final Context ctx) throws ObjectCreationException
	{
		try
		{
			return modelService.create(typeId);
		}
		catch (final Exception ex)
		{
			throw new ObjectCreationException(typeId, ex);
		}
	}

	@Override
	public <T> T save(final T objectToSave, final Context ctx) throws ObjectSavingException
	{
		try
		{
			final ItemModificationInfo modificationInfo = itemModificationHistoryService
					.createModificationInfo((ItemModel) objectToSave);

			final Collection<AbstractItemModel> modified = collectModifiedEntities((AbstractItemModel) objectToSave);

			executeWithWorkflowTemplateActivation(modelService::saveAll, modified, ctx,
					item -> isNew(item) ? WorkflowTemplateActivationAction.CREATE : WorkflowTemplateActivationAction.SAVE);

			itemModificationHistoryService.logItemModification(modificationInfo);
			return objectToSave;
		}
		catch (final ModelSavingException ex)
		{
			throw new ObjectSavingException(getObjectId(objectToSave), ex);
		}
	}

	/**
	 * Persist objects by given collection data. Since 1808 this method is transactional.
	 * 
	 * @param objects
	 *           the data for updating objects in system. Each object is saved separately in a dedicated transaction.
	 * @param ctx
	 *           context specifying the loaded attributes of the returned object as well as application settings
	 * @return ObjectFacadeOperationResult object
	 */
	@Override
	public <T> ObjectFacadeOperationResult<T> save(final Collection<T> objects, final Context ctx)
	{
		final List<ItemModificationInfo> modificationInfos = objects.stream().map(ItemModel.class::cast)
				.map(itemModificationHistoryService::createModificationInfo).collect(Collectors.toList());

		final Collection<AbstractItemModel> modified = objects.stream().map(AbstractItemModel.class::cast)
				.map(this::collectModifiedEntities).flatMap(Collection::stream).collect(Collectors.toSet());
		final ObjectFacadeOperationResult<T> result = new ObjectFacadeOperationResult<>();
		try
		{
			executeWithWorkflowTemplateActivation(modelService::saveAll, modified, ctx,
					item -> isNew(item) ? WorkflowTemplateActivationAction.CREATE : WorkflowTemplateActivationAction.SAVE);

			modificationInfos.forEach(itemModificationHistoryService::logItemModification);
			objects.forEach(result::addSuccessfulObject);
		}
		catch (final ModelSavingException ex)
		{
			LOG.debug("Cannot save items", ex);
			objects.forEach(obj -> result.addFailedObject(obj, new ObjectAccessException(ex.getMessage(), ex)));
		}
		return result;
	}

	@Override
	public <T> T clone(final T objectToClone, final Context ctx) throws ObjectCloningException
	{
		final CloneStrategy cloneStrategy = cloneStrategyRegistry.getStrategy(objectToClone);
		if (cloneStrategy == null)
		{
			throw new IllegalStateException("No clone strategy found. Not even default one.");
		}
		return cloneStrategy.clone(objectToClone);
	}

	protected <T> void executeWithWorkflowTemplateActivation(final Consumer<Collection<T>> consumer, final Collection<T> toConsume,
			final Context invocationCtx, final Function<ItemModel, WorkflowTemplateActivationAction> actionMapper)
	{
		final Map<ItemModel, WorkflowTemplateActivationAction> toActivate = toConsume.stream().filter(ItemModel.class::isInstance)
				.map(ItemModel.class::cast).collect(Collectors.toMap(item -> item, actionMapper));

		final List<WorkflowTemplateActivationCtx> workflowActivationContexts = workflowTemplateActivationService
				.prepareWorkflowTemplateActivationContexts(toActivate, invocationCtx);

		consumer.accept(toConsume);

		workflowTemplateActivationService.activateWorkflowTemplates(workflowActivationContexts);
	}

	private Collection<AbstractItemModel> collectModifiedEntities(final AbstractItemModel model)
	{
		final Collection<AbstractItemModel> result = Sets.newLinkedHashSet();
		result.add(model);
		collectModifiedEntities(model, result);
		return result;
	}

	private void collectModifiedEntities(final AbstractItemModel model, final Collection<AbstractItemModel> result)
	{
		final ItemModelContext context = ModelContextUtils.getItemModelContext(model);
		final String modelType = modelService.getModelType(model);
		try
		{
			final DataType dataType = typeFacade.load(modelType);
			context.getDirtyAttributes().stream().forEach(qualifier -> {

				final DataAttribute attribute = dataType.getAttribute(qualifier);
				if (attribute != null && !attribute.isPrimitive())
				{
					final Object value = modelService.getAttributeValue(model, qualifier);
					collectModifiedEntriesInternal(value, result);
				}
			});
		}
		catch (final TypeNotFoundException e)
		{
			final String message = String.format("Cannot load type %s.", modelType);
			if (LOG.isDebugEnabled())
			{
				LOG.warn(message, e);
			}
			else
			{
				LOG.warn(message);
			}
		}
	}

	private void collectModifiedEntriesInternal(final Object value, final Collection<AbstractItemModel> result)
	{
		final boolean isItem = value instanceof AbstractItemModel;
		final boolean isCollection = !isItem && value instanceof Collection;
		final boolean isMap = !isCollection && value instanceof Map;
		if (isItem && canPersistSingleElement(value))
		{
			collectModifiedEntriesInternalForSingleElement(value, result);
		}
		else if (isCollection)
		{
			collectModifiedEntriesInternalForCollection((Collection) value, result);
		}
		else if (isMap)
		{
			collectModifiedEntriesInternalForMap((Map) value, result);
		}
	}

	private void collectModifiedEntriesInternalForSingleElement(final Object value, final Collection<AbstractItemModel> result)
	{
		if (!result.contains(value))
		{
			result.add((AbstractItemModel) value);
			collectModifiedEntities((AbstractItemModel) value, result);
		}
	}

	private void collectModifiedEntriesInternalForCollection(final Collection<?> value, final Collection<AbstractItemModel> result)
	{
		value.stream().filter(element -> element instanceof AbstractItemModel && !result.contains(element)).forEach(element -> {
			result.add((AbstractItemModel) element);
			collectModifiedEntities((AbstractItemModel) element, result);
		});
	}

	private void collectModifiedEntriesInternalForMap(final Map value, final Collection<AbstractItemModel> result)
	{
		final Set<Map.Entry> set = value.entrySet();
		for (final Map.Entry entry : set)
		{
			final Object keyValue = entry.getKey();
			if (canPersistSingleElement(keyValue))
			{
				if (!result.contains(keyValue))
				{
					result.add((AbstractItemModel) keyValue);
					collectModifiedEntities((AbstractItemModel) keyValue, result);
				}
			}
			else
			{
				collectModifiedEntriesInternal(keyValue, result);
			}
			final Object mapValue = entry.getValue();
			if (mapValue instanceof AbstractItemModel)
			{
				collectModifiedEntities((AbstractItemModel) mapValue, result);
			}
			else
			{
				collectModifiedEntriesInternal(mapValue, result);
			}
		}
	}

	protected boolean canPersistSingleElement(final Object value)
	{
		return value instanceof AbstractItemModel && (modelService.isNew(value) || modelService.isModified(value));
	}

	@Override
	public <T> boolean isNew(final T object)
	{
		return !(object instanceof ViewResultItem) && modelService.isNew(object);
	}

	@Override
	public <T> boolean isModified(final T object)
	{
		return !(object instanceof ViewResultItem) && modelService.isModified(object);
	}

	@Override
	public <T> T reload(final T object, final Context ctx) throws ObjectNotFoundException
	{
		if (object instanceof ItemModel)
		{
			modelService.detach(object);
			final T obj = load(String.valueOf(((ItemModel) object).getPk()), ctx);
			if (object instanceof EnumerationValueModel && obj instanceof HybrisEnumValue)
			{
				final T enumerationValueModel = (T) convert((HybrisEnumValue) obj);
				if (enumerationValueModel != null)
				{
					return enumerationValueModel;
				}
			}
			else
			{
				return obj;
			}
		}
		else if (object instanceof ViewResultItem)
		{
			return object;
		}
		throw new ObjectNotFoundException(String.valueOf(object));
	}

	protected EnumerationValueModel convert(final HybrisEnumValue value)
	{
		final EnumerationMetaTypeModel enumMetaTypeModel = typeService.getEnumerationTypeForCode(value.getType());
		final Collection<ItemModel> valueModels = enumMetaTypeModel.getValues();

		for (final ItemModel itemModel : valueModels)
		{
			if (itemModel instanceof EnumerationValueModel)
			{
				final EnumerationValueModel enumCode = (EnumerationValueModel) itemModel;
				if (value.getCode().equals(enumCode.getCode()))
				{
					return enumCode;
				}
			}
		}
		return null;
	}

	@Override
	public boolean canHandle(final Object object)
	{
		if (object instanceof String)
		{
			final boolean canHandle = platformFacadeStrategyHandleCache.canHandle((String) object);
			if (canHandle)
			{
				return true;
			}
			else
			{
				try
				{
					PK.parse((String) object);
					return true;
				}
				catch (final PK.PKException e)
				{
					LOG.debug("Exception during parsing object.", e);
					return false;
				}
			}
		}
		return object instanceof ItemModel || object instanceof ViewResultItem || object instanceof Collection;
	}

	@Override
	public String getObjectId(final Object object)
	{
		if (object instanceof ItemModel)
		{
			final PK pk = ((ItemModel) object).getPk();
			if (pk != null)
			{
				return pk.getLongValueAsString();
			}
		}
		return StringUtils.EMPTY;
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setPlatformFacadeStrategyHandleCache(final PlatformFacadeStrategyHandleCache platformFacadeStrategyHandleCache)
	{
		this.platformFacadeStrategyHandleCache = platformFacadeStrategyHandleCache;
	}

	@Required
	public void setItemModificationHistoryService(final ItemModificationHistoryService itemModificationHistoryService)
	{
		this.itemModificationHistoryService = itemModificationHistoryService;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	@Required
	public void setWorkflowTemplateActivationService(final WorkflowTemplateActivationService workflowTemplateActivationService)
	{
		this.workflowTemplateActivationService = workflowTemplateActivationService;
	}

	public ValidationHandler getValidationHandler()
	{
		return validationHandler;
	}

	@Required
	public void setValidationHandler(final ValidationHandler validationHandler)
	{
		this.validationHandler = validationHandler;
	}

	public CloneStrategyRegistry getCloneStrategyRegistry()
	{
		return cloneStrategyRegistry;
	}

	@Required
	public void setCloneStrategyRegistry(final CloneStrategyRegistry cloneStrategyRegistry)
	{
		this.cloneStrategyRegistry = cloneStrategyRegistry;
	}
}
