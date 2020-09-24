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
package com.hybris.backoffice.widgets.selectivesync;

import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncJobModel;

import java.util.Collections;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.backoffice.widgets.selectivesync.legend.TreeLegend;
import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.model.StandardModelKeys;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.testing.annotation.InextensibleMethod;
import com.hybris.cockpitng.util.DefaultWidgetController;


/**
 * Controller for Selective Synchronization management
 */
public class SelectiveSyncController extends DefaultWidgetController
{
	public static final String MODEL_CURRENT_OBJECT = StandardModelKeys.CONTEXT_OBJECT;
	public static final String MODEL_VALUE_CHANGED = "valueChanged";

	protected static final String COMP_ID_CANCEL_BTN = "cancelButton";
	protected static final String COMP_ID_SAVE_BTN = "saveButton";
	protected static final String SOCKET_INPUT_OBJECT = "inputObject";
	protected static final String SOCKET_OUTPUT_OBJECT_SAVED = "objectSaved";
	protected static final String SOCKET_OUTPUT_CANCEL = "cancel";

	private static final String TITLE_LABEL = "edit.catalogsyncjob.syncattributedescriptorconfigcollectioneditor.header";
	private static final Logger LOG = LoggerFactory.getLogger(SelectiveSyncController.class);

	@Wire
	private Div contentDiv;
	@Wire
	private Div legendDiv;
	@Wire
	private Button cancelButton;
	@Wire
	private Button saveButton;

	@WireVariable
	private transient ObjectFacade objectFacade;
	@WireVariable
	private transient SelectiveSyncRenderer selectiveSyncRenderer;
	@WireVariable
	private transient NotificationService notificationService;

	@Override
	public void initialize(final Component comp)
	{
		getWidgetInstanceManager().setTitle(Labels.getLabel(TITLE_LABEL));
		createTreeLegend();
		updateSaveButtonState();
		getModel().addObserver(MODEL_VALUE_CHANGED, this::updateSaveButtonState);
		renderCurrentObject();
	}

	protected void createTreeLegend()
	{
		legendDiv.appendChild(new TreeLegend());
	}

	@InextensibleMethod
	private void updateSaveButtonState()
	{
		saveButton.setDisabled(BooleanUtils.isNotTrue(getValue(MODEL_VALUE_CHANGED, Boolean.class)));
	}

	protected void renderCurrentObject()
	{
		if (isCurrentObjectAvailable())
		{
			selectiveSyncRenderer.render(contentDiv, getCurrentObject(), getWidgetInstanceManager());
		}
	}

	@SocketEvent(socketId = SOCKET_INPUT_OBJECT)
	public void handleInputObject(final CatalogVersionSyncJobModel inputObject)
	{
		try
		{
			final CatalogVersionSyncJobModel reloadedModel = objectFacade.reload(inputObject);
			setCurrentObject(reloadedModel);
			renderCurrentObject();
		}
		catch (final ObjectNotFoundException exception)
		{
			handleNotFoundException(inputObject, exception);
		}
	}

	protected void handleNotFoundException(final CatalogVersionSyncJobModel model, final ObjectNotFoundException e)
	{
		getNotificationService().notifyUser(getNotificationSource(), NotificationEventTypes.EVENT_TYPE_OBJECT_LOAD,
				NotificationEvent.Level.FAILURE, Collections.singletonMap(model, e));
	}

	@ViewEvent(componentID = COMP_ID_CANCEL_BTN, eventName = Events.ON_CLICK)
	public void handleCancelButtonClick()
	{
		sendOutput(SOCKET_OUTPUT_CANCEL, null);
	}

	@ViewEvent(componentID = COMP_ID_SAVE_BTN, eventName = Events.ON_CLICK)
	public void handleSaveButtonClick()
	{
		if (isCurrentObjectAvailable())
		{
			try
			{
				final CatalogVersionSyncJobModel savedObject = objectFacade.save(getCurrentObject());
				handleObjectSavingSuccess(savedObject);
				setValue(MODEL_VALUE_CHANGED, Boolean.FALSE);
				sendOutput(SOCKET_OUTPUT_OBJECT_SAVED, savedObject);
			}
			catch (final ObjectSavingException objectSavingException)
			{
				LOG.error(objectSavingException.getMessage(), objectSavingException);
			}
		}
	}

	protected void handleObjectSavingSuccess(final Object savedObject)
	{
		getNotificationService().notifyUser(getNotificationSource(), NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE,
				NotificationEvent.Level.SUCCESS, Collections.singletonList(savedObject));
	}

	protected CatalogVersionSyncJobModel getCurrentObject()
	{
		return getValue(MODEL_CURRENT_OBJECT, CatalogVersionSyncJobModel.class);
	}

	protected void setCurrentObject(final CatalogVersionSyncJobModel model)
	{
		setValue(MODEL_CURRENT_OBJECT, model);
	}

	protected boolean isCurrentObjectAvailable()
	{
		return getCurrentObject() != null;
	}

	/**
	 * @deprecated since 6.7, use
	 *             {@link NotificationService#getWidgetNotificationSource(com.hybris.cockpitng.engine.WidgetInstanceManager)}
	 *             instead.
	 */
	@Deprecated
	protected String getNotificationSource()
	{
		return getNotificationService().getWidgetNotificationSource(getWidgetInstanceManager());
	}

	public Div getContentDiv()
	{
		return contentDiv;
	}

	public Div getLegendDiv()
	{
		return legendDiv;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	public SelectiveSyncRenderer getSelectiveSyncRenderer()
	{
		return selectiveSyncRenderer;
	}

	public void setSelectiveSyncRenderer(final SelectiveSyncRenderer selectiveSyncRenderer)
	{
		this.selectiveSyncRenderer = selectiveSyncRenderer;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}


}
