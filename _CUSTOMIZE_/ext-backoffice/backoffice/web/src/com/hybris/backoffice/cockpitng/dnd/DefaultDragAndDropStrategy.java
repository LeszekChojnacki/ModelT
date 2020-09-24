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
package com.hybris.backoffice.cockpitng.dnd;

import com.hybris.cockpitng.util.notifications.event.NotificationEvent;
import com.hybris.cockpitng.util.notifications.event.NotificationEventTypes;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;

import com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation.BackofficeValidationService;
import com.hybris.backoffice.cockpitng.dnd.validators.DragAndDropValidator;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.core.context.CockpitContext;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeOperationResult;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectAccessException;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dnd.DefaultDragAndDropContext;
import com.hybris.cockpitng.dnd.DragAndDropContext;
import com.hybris.cockpitng.dnd.DragAndDropStrategy;
import com.hybris.cockpitng.dnd.DropHandler;
import com.hybris.cockpitng.dnd.DropOperationData;
import com.hybris.cockpitng.dnd.DropOperationValidationData;
import com.hybris.cockpitng.dnd.DropOperationValidationRenderer;
import com.hybris.cockpitng.dnd.SelectionSupplier;
import com.hybris.cockpitng.mouse.MouseKeys;
import com.hybris.cockpitng.validation.impl.DefaultValidationContext;
import com.hybris.cockpitng.validation.model.ValidationGroup;
import com.hybris.cockpitng.validation.model.ValidationInfo;
import com.hybris.cockpitng.validation.model.ValidationSeverity;


/**
 * Default strategy for drag and drop mechanism.
 */
public class DefaultDragAndDropStrategy implements DragAndDropStrategy
{
	public static final String CTX_PARAM_DROP_COMPONENT = "dropComponent";
	public static final String ATTRIBUTE_DND_DRAG_CONTEXT = "dragContext";
	public static final String RELOAD_AFTER_SAVE = "shouldReloadAfterUpdate";

	protected static final String NOTIFICATION_KEY_FAILURE_SUFFIX = "Failure";
	protected static final String NOTIFICATION_KEY_SUCCESS_SUFFIX = "Success";
	protected static final String NOTIFICATION_KEY_SINGLE_SUFFIX = "Single";
	protected static final String NOTIFICATION_KEY_MULTIPLE_SUFFIX = "Multiple";

	private static final Logger LOG = LoggerFactory.getLogger(DefaultDragAndDropStrategy.class);

	protected ModelService modelService;
	protected List<String> validationGroups = new ArrayList<>();
	protected boolean performSystemValidation = true;
	protected List<DragAndDropValidator> additionalValidators = new ArrayList<>();
	private TypeFacade typeFacade;
	private Map<String, DropHandler> handlerMap;
	private int subtypeLimit = 50;
	private BackofficeValidationService validationService;
	private DropOperationValidationRenderer dropOperationValidationRenderer;
	private ObjectFacade objectFacade;
	private Boolean reloadUiAfterSave = Boolean.FALSE;
	private NotificationService notificationService;

	protected void initialize()
	{
		handlerMap = resolveHandledSubtypes();
	}

	protected Map<String, DropHandler> resolveHandledSubtypes()
	{
		final Map<String, DropHandler> newMap = new HashMap<>();
		for (final Map.Entry<String, DropHandler> entry : handlerMap.entrySet())
		{
			final DropHandler typeDropHandler = entry.getValue();
			if (entry.getKey().endsWith("!"))
			{
				final String type = entry.getKey().substring(0, entry.getKey().length() - 1);
				newMap.put(type, typeDropHandler);
			}
			else
			{
				newMap.put(entry.getKey(), typeDropHandler);
				final List<String> subtypes = resolveAllConcreteSubtypes(entry.getKey());
				subtypes.forEach((final String subtype) -> newMap.putIfAbsent(subtype, typeDropHandler));
			}
		}
		return newMap;
	}

	private List<String> resolveAllConcreteSubtypes(final String type)
	{
		final List<String> types = new ArrayList<>();
		try
		{
			final DataType dataType = typeFacade.load(type);
			if (dataType != null)
			{
				if (!dataType.isAbstract())
				{
					types.add(dataType.getCode());
				}
				dataType.getSubtypes().forEach(subtype -> types.addAll(resolveAllConcreteSubtypes(subtype)));
			}
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn("Unable to load type", e);
		}
		return types;
	}

	@Override
	public void makeDraggable(final HtmlBasedComponent component, final Object businessObject, final CockpitContext dragContext)
	{
		makeDraggableInternal(component, businessObject, dragContext, null);
	}

	@Override
	public void makeDraggable(final HtmlBasedComponent component, final Object businessObject, final CockpitContext dragContext,
			final SelectionSupplier selectionSupplier)
	{
		makeDraggableInternal(component, businessObject, dragContext, selectionSupplier);
	}

	@Override
	public void makeDroppable(final HtmlBasedComponent component, final Object businessObject, final CockpitContext context)
	{
		if (MapUtils.isEmpty(handlerMap))
		{
			return;
		}
		component.setAttribute(ATTRIBUTE_DND_DATA, businessObject);
		String droppables = "";
		if (businessObject != null)
		{
			final String type = typeFacade.getType(businessObject);
			final DropHandler dropHandler = handlerMap.get(type);
			if (dropHandler != null)
			{
				droppables = resolveDroppables(dropHandler);
				context.setParameter(CTX_PARAM_DROP_COMPONENT, component);
				component.addEventListener(Events.ON_DROP, event -> handleDrop((DropEvent) event, dropHandler, context));
			}
		}
		component.setDroppable(droppables);
	}

	private void makeDraggableInternal(final HtmlBasedComponent component, final Object businessObject,
			final CockpitContext dragContext, final SelectionSupplier selectionSupplier)
	{
		String draggableId = StringUtils.EMPTY;
		if (businessObject != null)
		{
			draggableId = typeFacade.getType(businessObject);
		}
		component.setDraggable(draggableId);
		component.setAttribute(ATTRIBUTE_DND_DATA, businessObject);
		component.setAttribute(ATTRIBUTE_DND_DRAG_CONTEXT, dragContext);
		if (selectionSupplier != null)
		{
			component.setAttribute(ATTRIBUTE_DND_SELECTION_SUPPLIER, selectionSupplier);
		}
	}

	protected String resolveDroppables(final DropHandler dropHandler)
	{
		final List<String> definedDroppableList = Optional.ofNullable(dropHandler.findSupportedTypes())
				.orElse(Collections.emptyList());
		final Set<String> droppableTypes = new HashSet<>();
		for (final String definedType : definedDroppableList)
		{
			if (definedType.endsWith("!"))
			{
				final String type = definedType.substring(0, definedType.length() - 1);
				droppableTypes.add(type);
			}
			else
			{
				droppableTypes.addAll(resolveAllConcreteSubtypes(definedType));
			}
			if (droppableTypes.size() >= subtypeLimit)
			{
				break;
			}
		}
		return droppableTypes.stream().limit(subtypeLimit).reduce((s1, s2) -> s1 + "," + s2).orElse("true");
	}

	protected void handleDrop(final DropEvent event, final DropHandler dropHandler, final CockpitContext targetContext)
	{
		final SelectionSupplier<Object> selectionSupplier = (SelectionSupplier<Object>) event.getDragged()
				.getAttribute(ATTRIBUTE_DND_SELECTION_SUPPLIER, true);

		final List<Object> draggedObjects = new ArrayList<>();
		if (selectionSupplier != null)
		{
			draggedObjects.addAll(selectionSupplier.findSelection());
		}
		if (draggedObjects.isEmpty())
		{
			draggedObjects.add(event.getDragged().getAttribute(ATTRIBUTE_DND_DATA, true));
		}

		final Object targetObject = event.getTarget().getAttribute(ATTRIBUTE_DND_DATA, true);
		final CockpitContext dragContext = (CockpitContext) event.getDragged().getAttribute(ATTRIBUTE_DND_DRAG_CONTEXT);

		final DefaultDragAndDropContext dragAndDropContext = new DefaultDragAndDropContext.Builder()
				.withKeys(MouseKeys.fromMouseEvent(event)).withDraggedContext(dragContext).withTargetContext(targetContext).build();

		final List operationsData = dropHandler.handleDrop(draggedObjects, targetObject, dragAndDropContext);

		if (!operationsData.isEmpty())
		{
			final List<DropOperationValidationData> validateResults = validateData(operationsData, dragAndDropContext);

			final Consumer<Collection<DropOperationData>> confirmedAction = (
					final Collection<DropOperationData> confirmedData) -> applyModifications(dragAndDropContext, confirmedData);

			if (!validateResults.isEmpty())
			{
				final Object dropComponent = dragAndDropContext.getTargetContext()
						.getParameter(DefaultDragAndDropStrategy.CTX_PARAM_DROP_COMPONENT);
				if (dropComponent instanceof Component)
				{
					dropOperationValidationRenderer.askForConfirmation((Component) dropComponent, validateResults, confirmedAction,
							this::refreshModels);
				}
				final List itemsWithoutErrorsAndWarnings = findItemsWithoutErrorsAndWarnings(operationsData, validateResults);
				confirmedAction.accept(itemsWithoutErrorsAndWarnings);
			}
			else
			{
				confirmedAction.accept(operationsData);
			}
		}
	}

	protected List<DropOperationData> findItemsWithoutErrorsAndWarnings(final List<DropOperationData> allElements,
			final List<DropOperationValidationData> validationData)
	{
		final List<DropOperationData> elementWithErrorsOrWarnings = validationData.stream()
				.map(DropOperationValidationData::getDropOperationData).collect(Collectors.toList());
		return allElements.stream().filter(element -> !elementWithErrorsOrWarnings.contains(element)).collect(Collectors.toList());
	}

	protected void applyModifications(final DefaultDragAndDropContext context, final Collection<DropOperationData> operationsData)
	{
		if (operationsData.isEmpty())
		{
			return;
		}
		saveAndNotify(context, operationsData);
	}

	/**
	 * @deprecated since 6.7, use {@link #applyModifications(DefaultDragAndDropContext, Collection)}
	 */
	@Deprecated
	protected void saveAndNotify(final DefaultDragAndDropContext context, final Collection<DropOperationData> confirmedData)
	{
		final List<DropOperationData> data = new ArrayList<>(confirmedData);
		final ObjectFacadeOperationResult result = save(data, context);
		refreshFailedModels(data, result);
		notifyUser(result, data, context);
	}

	protected void refreshFailedModels(final List<DropOperationData> operationsData, final ObjectFacadeOperationResult<?> result)
	{
		final Collection<?> failedObjects = result.getFailedObjects();
		if (CollectionUtils.isNotEmpty(failedObjects))
		{
			refreshModels(operationsData.stream().filter(element -> failedObjects.contains(element.getModified()))
					.collect(Collectors.toSet()));
		}
	}

	protected void refreshModels(final Collection<DropOperationData> dataToCancel)
	{
		dataToCancel.forEach(operationData -> modelService.refresh(operationData.getModified()));
	}

	protected List<DropOperationValidationData> validateData(final List<DropOperationData> operationsData,
			final DefaultDragAndDropContext dragAndDropContext)
	{
		final List<DropOperationValidationData> resultList = new LinkedList<>();
		for (final DropOperationData data : operationsData)
		{
			if (data.getModified() != null)
			{
				final List<ValidationInfo> additionalValidationResult = additionalValidators.stream()
						.filter(validator -> validator.isApplicable(data, dragAndDropContext))
						.flatMap(validator -> validator.validate(data, dragAndDropContext).stream()).collect(Collectors.toList());

				final List<ValidationInfo> standardValidationInfoList = isPerformSystemValidation()
						? validationService.validate(data.getModified(), prepareValidationContext())
						: Collections.emptyList();

				final List<ValidationInfo> info = new ArrayList<>(standardValidationInfoList);
				info.addAll(additionalValidationResult);

				if (CollectionUtils.isNotEmpty(info))
				{
					resultList.add(aggregateValidationResults(data, info));
				}
			}
		}
		return resultList;
	}

	protected ObjectFacadeOperationResult save(final List<DropOperationData> operationsData, final DragAndDropContext context)
	{
		if (operationsData.isEmpty())
		{
			return new ObjectFacadeOperationResult();
		}

		final List objectsToSave = operationsData.stream().map(DropOperationData::getModified).filter(Objects::nonNull)
				.collect(Collectors.toList());
		context.setParameter(RELOAD_AFTER_SAVE, reloadUiAfterSave);
		return objectFacade.save(objectsToSave, new DefaultContext.Builder().attributes(context.getParameters()).build());
	}

	protected DefaultValidationContext prepareValidationContext()
	{
		final DefaultValidationContext validationContext = new DefaultValidationContext();
		if (CollectionUtils.isNotEmpty(validationGroups))
		{
			final List<ValidationGroup> constraintGroups = new ArrayList<>();
			for (final String group : validationGroups)
			{
				constraintGroups.add(new ValidationGroup(group));
			}
			validationContext.setConstraintGroups(constraintGroups);
		}
		return validationContext;
	}

	private static DropOperationValidationData aggregateValidationResults(final DropOperationData data,
			final List<ValidationInfo> validationInfoList)
	{
		final Map<ValidationSeverity, List<ValidationInfo>> infoBySeverity = validationInfoList.stream()
				.collect(Collectors.groupingBy(ValidationInfo::getValidationSeverity));
		return new DropOperationValidationData(data, infoBySeverity);
	}

	protected void notifyUser(final ObjectFacadeOperationResult result, final List<DropOperationData> operationsData,
			final DragAndDropContext context)
	{
		final Map<DropOperationData, ObjectAccessException> failureOperations = new LinkedHashMap<>();
		for (final Object failedObject : result.getFailedObjects())
		{
			final ObjectAccessException exception = result.getErrorForObject(failedObject);
			LOG.warn("Error occurred while DnD operation", exception);
			final DropOperationData operationData = findOperationDataAssociatedWithModifiedObject(operationsData, failedObject);
			failureOperations.put(operationData, exception);
		}
		notifyUserAboutFailure(failureOperations, context);

		final List<DropOperationData> successfulOperations = new LinkedList<>();
		for (final Object successfulObject : result.getSuccessfulObjects())
		{
			final DropOperationData operationData = findOperationDataAssociatedWithModifiedObject(operationsData, successfulObject);
			successfulOperations.add(operationData);
		}
		notifyUserAboutSuccess(successfulOperations, context);
	}

	private static DropOperationData findOperationDataAssociatedWithModifiedObject(final List<DropOperationData> operationsData,
			final Object modified)
	{
		return operationsData.stream().filter(obj -> obj.getModified() != null && obj.getModified().equals(modified)).findAny()
				.get();
	}

	protected void notifyUserAboutFailure(final Map<DropOperationData, ObjectAccessException> operationsDataWithExceptions,
			final DragAndDropContext context)
	{
		if (operationsDataWithExceptions.isEmpty())
		{
			return;
		}

		final Entry<DropOperationData, ObjectAccessException> firstOperationWithException = operationsDataWithExceptions.entrySet()
				.iterator().next();
		final DropOperationData firstOperation = firstOperationWithException.getKey();

		final String notificationKey = firstOperationWithException.getKey().getNotificationKey() + NOTIFICATION_KEY_FAILURE_SUFFIX;
		if (operationsDataWithExceptions.size() == 1)
		{
			getNotificationService().notifyUser(notificationKey + NOTIFICATION_KEY_SINGLE_SUFFIX,
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.FAILURE, firstOperation.getDragged(),
					firstOperation.getTarget(), firstOperation.getModified(), firstOperationWithException.getValue());
		}
		else
		{
			getNotificationService().notifyUser(notificationKey + NOTIFICATION_KEY_MULTIPLE_SUFFIX,
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.FAILURE, operationsDataWithExceptions,
					firstOperation.getTarget());
		}
	}

	protected void notifyUserAboutSuccess(final List<DropOperationData> operationsData, final DragAndDropContext context)
	{
		if (operationsData.isEmpty())
		{
			return;
		}
		final DropOperationData firstOperation = operationsData.get(0);
		final String notificationKey = firstOperation.getNotificationKey() + NOTIFICATION_KEY_SUCCESS_SUFFIX;
		if (operationsData.size() == 1)
		{
			getNotificationService().notifyUser(notificationKey + NOTIFICATION_KEY_SINGLE_SUFFIX,
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS, firstOperation.getDragged(),
					firstOperation.getTarget(), firstOperation.getModified());
		}
		else
		{
			getNotificationService().notifyUser(notificationKey + NOTIFICATION_KEY_MULTIPLE_SUFFIX,
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS, operationsData,
					firstOperation.getTarget());
		}
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	@Required
	public void setHandlerMap(final Map<String, DropHandler> handlerMap)
	{
		this.handlerMap = handlerMap;
	}

	public void setSubtypeLimit(final int subtypeLimit)
	{
		this.subtypeLimit = subtypeLimit;
	}

	@Required
	public void setValidationService(final BackofficeValidationService validationService)
	{
		this.validationService = validationService;
	}

	public void setValidationGroups(final List<String> validationGroups)
	{
		this.validationGroups = validationGroups;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	@Required
	public void setDropOperationValidationRenderer(final DropOperationValidationRenderer dropOperationValidationRenderer)
	{
		this.dropOperationValidationRenderer = dropOperationValidationRenderer;
	}

	public void setReloadUiAfterSave(final Boolean reloadUiAfterSave)
	{
		this.reloadUiAfterSave = reloadUiAfterSave;
	}

	public void setAdditionalValidators(final List<DragAndDropValidator> additionalValidators)
	{
		this.additionalValidators = additionalValidators;
	}

	public boolean isPerformSystemValidation()
	{
		return performSystemValidation;
	}

	public void setPerformSystemValidation(final boolean performSystemValidation)
	{
		this.performSystemValidation = performSystemValidation;
	}

	protected NotificationService getNotificationService() {
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
