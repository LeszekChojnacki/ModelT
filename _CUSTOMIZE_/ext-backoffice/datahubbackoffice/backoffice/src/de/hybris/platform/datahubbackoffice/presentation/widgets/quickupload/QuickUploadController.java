/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

package de.hybris.platform.datahubbackoffice.presentation.widgets.quickupload;

import static de.hybris.platform.datahubbackoffice.WidgetConstants.DATAHUB_BACKOFFICE_MAIN_WIDGET;
import static de.hybris.platform.datahubbackoffice.dataaccess.pool.PoolTypeFacadeStrategy.DATAHUB_POOL_TYPECODE;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.cockpitng.util.UITools;
import com.hybris.datahub.dto.feed.FeedData;
import com.hybris.datahub.dto.pool.PoolData;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;

import com.google.common.collect.Maps;

public class QuickUploadController extends DefaultWidgetController
{
	protected static final String FEED_MODEL_PARAMETER = "selectedFeed";
	protected static final String TYPE_MODEL_PARAMETER = "selectedType";
	protected static final String COMPOSE_POOL_MODEL_PARAMETER = "selectedPoolForCompose";
	protected static final String PUBLISH_POOL_MODEL_PARAMETER = "selectedPoolForPublish";
	protected static final String TARGET_SYSTEMS_MODEL_PARAMETER = "selectedTargetSystems";
	protected static final String UPLOADED_MEDIA_MODEL_PARAMETER = "uploadedMedia";
	protected static final String DATAHUB_INFO_MODEL_PARAMETER = "datahubInfo";
	protected static final String TEXTAREA_CONTENT_MODEL_PARAMETER = "textareaContent";

	protected static final String DATAHUB_FEED_TYPECODE = "Datahub_Feed";
	protected static final String DATAHUB_TARGET_SYSTEM_TYPECODE = "Datahub_TargetSystem";
	protected static final String DATAHUB_CANONICAL_ITEM_TYPECODE = "Datahub_CanonicalItem";

	protected static final String COMPONENT_CLEAR_BTN = "clear";
	protected static final String COMPONENT_LOAD_BTN = "loadBtn";
	protected static final String COMPONENT_COMPOSE_BTN = "composeBtn";
	protected static final String COMPONENT_PUBLISH_BTN = "publishBtn";
	protected static final String COMPONENT_UPLOAD_BTN = "uploadFile";
	protected static final String COMPONENT_TEXTAREA = "textarea";
	protected static final String COMPONENT_PROCESS = "process";

	protected static final String NOTIFIER_STACK = "notifierStack";

	private static final String EDITOR_PLACEHOLDER_SUFFIX = "selector.placeholder";
	private static final String EDITOR_PLACEHOLDER_KEY_ATTR = "placeholderKey";
	private static final String EDITOR_PAGE_SIZE_ATTR = "pageSize";
	private static final String EDITOR_AVAILABLE_VALUES_PROVIDER_ATTR = "availableValuesProvider";
	private static final String EDITOR_SCLASS = "ye-quickupload-editor";
	private static final String CONFIGURABLE_DROPDOWN_EDITOR_ID = "de.hybris.platform.datahubbackoffice.datahubdropdown";

	private static final String VALUE_CANNOT_BE_EMPTY = "editor.value.notempty";

	private static final String SETTING_QUICK_UPLOAD_TITLE_KEY = "quickUploadTitle";
	private static final String SETTING_EDITOR_PAGE_SIZE = "editorPageSize";
	private static final String DATAHUB_CONNECTION_ERROR = "datahub.error.connecting.to.server";

	protected Div composeContent;
	protected Div publishContent;
	protected Div loadSelectors;
	protected Textbox url;
	protected Textbox filename;
	protected Textbox textarea;
	protected Button uploadFile;
	protected Div uploadedFileContent;
	protected Radio loadBtn;
	protected Radiogroup radioGroup;
	protected Div main;
	protected Label widgetTitle;

	protected Editor feedSelector;
	protected Editor typeSelector;
	protected Editor composePoolSelector;
	protected Editor publishPoolSelector;
	protected Editor targetSystemSelector;

	private final Map<String, Component> editorsMap = Maps.newHashMap();

	@WireVariable("processingStrategies")
	private transient List<ProcessingStrategy> processingStrategies = new ArrayList<>();
	@WireVariable("notificationService")
	private transient NotificationService notificationService;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		createWidgetTitle();
		renderContent();
	}

	@SocketEvent(socketId = DATAHUB_INFO_MODEL_PARAMETER)
	public void refreshWhenDatahubInstanceChanged(final DataHubServer dataHub)
	{
		Validate.notNull(dataHub, "DatahubServerInfo cannot be null!");
		prepareWidgetModel(dataHub);
		renderContent();
	}

	protected void prepareWidgetModel(final DataHubServer dataHub)
	{
		getModel().setValue(DATAHUB_INFO_MODEL_PARAMETER, dataHub);
		editorsMap.clear();
	}

	protected DataHubServer contextDataHubServer()
	{
		return getValue(DATAHUB_INFO_MODEL_PARAMETER, DataHubServer.class);
	}

	public String getWidgetId()
	{
		return notificationService.getWidgetNotificationSource(getWidgetInstanceManager());
	}

	@ViewEvent(componentID = COMPONENT_CLEAR_BTN, eventName = Events.ON_CLICK)
	public void clearButtonPressed()
	{
		clearLoadRow();
		clearComposeRow();
		clearPublishRow();

		loadBtn.setChecked(true);
		UITools.modifySClass(textarea, "yw-textarea-active", false);

		recalculateUrl();
	}

	@ViewEvent(componentID = COMPONENT_LOAD_BTN, eventName = Events.ON_CHECK)
	public void activateLoadRow()
	{
		try
		{
			appendLocalNotifier();

			enableLoadRow(true);
			enableComposeRow(false);
			enablePublishRow(false);
		}
		catch(final Exception e)
		{
			notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
			notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, DATAHUB_CONNECTION_ERROR,
					NotificationEvent.Level.FAILURE, e);
		}
	}

	@ViewEvent(componentID = COMPONENT_COMPOSE_BTN, eventName = Events.ON_CHECK)
	public void activateComposeRow()
	{
		try
		{
			removeLocalNotifier();

			enableLoadRow(false);
			enableComposeRow(true);
			enablePublishRow(false);
		}
		catch(final Exception e)
		{
			notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
			notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, DATAHUB_CONNECTION_ERROR,
					NotificationEvent.Level.FAILURE, e);
		}
	}

	@ViewEvent(componentID = COMPONENT_PUBLISH_BTN, eventName = Events.ON_CHECK)
	public void activatePublishRow()
	{
		try
		{
			removeLocalNotifier();

			enableLoadRow(false);
			enableComposeRow(false);
			enablePublishRow(true);
		}
		catch(final Exception e)
		{
			notificationService.clearNotifications(DATAHUB_BACKOFFICE_MAIN_WIDGET);
			notificationService.notifyUser(DATAHUB_BACKOFFICE_MAIN_WIDGET, DATAHUB_CONNECTION_ERROR,
					NotificationEvent.Level.FAILURE, e);
		}
	}

	@ViewEvent(componentID = COMPONENT_UPLOAD_BTN, eventName = Events.ON_UPLOAD)
	public void uploadFile(final UploadEvent uploadEvent)
	{
		getModel().put(UPLOADED_MEDIA_MODEL_PARAMETER, uploadEvent.getMedia());
		filename.setValue(uploadEvent.getMedia().getName());
		filename.setVisible(true);
		textarea.setValue(StringUtils.EMPTY);
		textarea.setDisabled(true);
		uploadedFileContent.setVisible(true);
	}

	@ViewEvent(componentID = COMPONENT_TEXTAREA, eventName = Events.ON_CHANGING)
	public void onTextAreaChange(final InputEvent event)
	{
		clearBinaryMedia();
		filename.setValue(StringUtils.EMPTY);
		uploadFile.setDisabled(true);

		UITools.modifySClass(textarea, "yw-textarea-active", true);
		getModel().setValue(TEXTAREA_CONTENT_MODEL_PARAMETER, event.getValue());
	}

	@ViewEvent(componentID = COMPONENT_PROCESS, eventName = Events.ON_CLICK)
	public void processSelected()
	{
		if (isProcessSelected())
		{
			final String selectedRowValue = getSelectedProcessKey();

			final ProcessingStrategy strategy = lookupMatchingStrategy(selectedRowValue);
			assert strategy != null : "Fallback strategy should be returned to handle null object special case";
			strategy.process(this, selectedRowValue, createCtx());
		}
	}

	protected String getSelectedProcessKey()
	{
		return isProcessSelected()
				? ObjectUtils.toString(radioGroup.getSelectedItem().getValue())
				: StringUtils.EMPTY;
	}

	protected boolean isProcessSelected()
	{
		return radioGroup.getSelectedItem() != null;
	}

	protected void createWidgetTitle()
	{
		final String widgetTitleKey = getWidgetSettings().getString(SETTING_QUICK_UPLOAD_TITLE_KEY);
		String titleLabel = getLabel(widgetTitleKey);
		if (StringUtils.isBlank(titleLabel))
		{
			titleLabel = Labels.getLabel(widgetTitleKey);
		}

		widgetTitle.setValue(titleLabel);
	}

	protected Map<String, Object> createCtx()
	{
		final Map<String, Object> ctx = Maps.newHashMap();
		ctx.put(FEED_MODEL_PARAMETER, getValue(FEED_MODEL_PARAMETER, FeedData.class));
		ctx.put(TYPE_MODEL_PARAMETER, getValue(TYPE_MODEL_PARAMETER, String.class));
		ctx.put(COMPOSE_POOL_MODEL_PARAMETER, getValue(COMPOSE_POOL_MODEL_PARAMETER, PoolData.class));
		ctx.put(PUBLISH_POOL_MODEL_PARAMETER, getValue(PUBLISH_POOL_MODEL_PARAMETER, PoolData.class));
		ctx.put(TARGET_SYSTEMS_MODEL_PARAMETER, getValue(TARGET_SYSTEMS_MODEL_PARAMETER, Set.class));
		ctx.put(DATAHUB_INFO_MODEL_PARAMETER, contextDataHubServer());
		return ctx;
	}

	protected void renderContent()
	{
		recalculateUrl();

		populateLoadRowSelectors(loadSelectors);
		buildComposeRow(composeContent);
		buildPublishRow(publishContent);

		activateLoadRow();
	}

	protected void clearLoadRowData()
	{
		getModel().remove(FEED_MODEL_PARAMETER);
		getModel().remove(TYPE_MODEL_PARAMETER);
		getModel().remove(UPLOADED_MEDIA_MODEL_PARAMETER);
	}

	protected void enableLoadRow(final boolean enabled)
	{
		feedSelector.setReadOnly(!enabled);
		feedSelector.reload();

		typeSelector.setReadOnly(!enabled);
		typeSelector.reload();

		clearLoadRowData();

		textarea.setDisabled(!enabled);
		textarea.setValue(StringUtils.EMPTY);
		uploadFile.setDisabled(!enabled);
		uploadedFileContent.setVisible(false);
		filename.setVisible(!enabled);

		recalculateUrl();
	}

	protected void enableComposeRow(final boolean enable)
	{

		composePoolSelector.setReadOnly(!enable);
		composePoolSelector.reload();

		clearComposeRow();
	}

	protected void enablePublishRow(final boolean enable)
	{
		publishPoolSelector.setReadOnly(!enable);
		publishPoolSelector.reload();

		targetSystemSelector.setReadOnly(!enable);
		targetSystemSelector.reload();

		clearPublishRow();
	}

	protected void clearLoadRow()
	{
		clearLoadRowData();

		textarea.setDisabled(false);
		textarea.setValue(StringUtils.EMPTY);
		uploadedFileContent.setVisible(false);
		uploadFile.setDisabled(false);
	}

	protected void clearComposeRow()
	{
		getModel().remove(COMPOSE_POOL_MODEL_PARAMETER);
	}

	protected void clearPublishRow()
	{
		getModel().remove(PUBLISH_POOL_MODEL_PARAMETER);
		getModel().remove(TARGET_SYSTEMS_MODEL_PARAMETER);
	}

	protected EventListener<Event> createRecalculateUrlEventListener()
	{
		return event -> recalculateUrl();
	}

	protected EventListener<Event> createClearWrongValueEditorListener()
	{
		return event -> {
			final Component editor = event.getTarget();
			if (editor instanceof Editor && editor.getFirstChild() != null)
			{
				Clients.clearWrongValue(editor.getFirstChild());
			}
		};
	}

	protected void populateLoadRowSelectors(final Component parent)
	{
		if (parent != null && CollectionUtils.isNotEmpty(parent.getChildren()))
		{
			parent.getChildren().clear();
		}

		final EventListener<Event> recalculateUrlEventListener = createRecalculateUrlEventListener();
		final EventListener<Event> clearEditorWrongValueListener = createClearWrongValueEditorListener();

		feedSelector = createEditor(DATAHUB_FEED_TYPECODE, FEED_MODEL_PARAMETER, recalculateUrlEventListener, false);
		feedSelector.addEventListener(Editor.ON_VALUE_CHANGED, clearEditorWrongValueListener);
		typeSelector = createTypeEditor(recalculateUrlEventListener);
		typeSelector.addEventListener(Editor.ON_VALUE_CHANGED, clearEditorWrongValueListener);

		if( parent != null )
		{
			parent.appendChild(feedSelector);
			parent.appendChild(typeSelector);
		}
	}

	protected void buildComposeRow(final Component parent)
	{
		if (parent != null && CollectionUtils.isNotEmpty(parent.getChildren()))
		{
			parent.getChildren().clear();
		}
		composePoolSelector = createEditor(DATAHUB_POOL_TYPECODE, COMPOSE_POOL_MODEL_PARAMETER, null, false);
		composePoolSelector.addEventListener(Editor.ON_VALUE_CHANGED, createClearWrongValueEditorListener());
		if( parent != null )
		{
			parent.appendChild(composePoolSelector);
		}
	}

	protected void buildPublishRow(final Component parent)
	{
		if (parent != null && CollectionUtils.isNotEmpty(parent.getChildren()))
		{
			parent.getChildren().clear();
		}

		final EventListener<Event> clearWrongValueEditorListener = createClearWrongValueEditorListener();
		publishPoolSelector = createEditor(DATAHUB_POOL_TYPECODE, PUBLISH_POOL_MODEL_PARAMETER, null, false);
		publishPoolSelector.addEventListener(Editor.ON_VALUE_CHANGED, clearWrongValueEditorListener);

		targetSystemSelector = createEditor(DATAHUB_TARGET_SYSTEM_TYPECODE, TARGET_SYSTEMS_MODEL_PARAMETER, null, true);
		targetSystemSelector.addEventListener(Editor.ON_VALUE_CHANGED, clearWrongValueEditorListener);
		targetSystemSelector.setSclass("ye-quickupload-wide-editor");

		if( parent != null )
		{
			parent.appendChild(publishPoolSelector);
			parent.appendChild(targetSystemSelector);
		}
	}

	protected void recalculateUrl()
	{
		final DataHubServer dataHubServer = contextDataHubServer();
		if (dataHubServer != null)
		{
			final StringBuilder recalculatedUrl = new StringBuilder(dataHubServer.getLocation());

			final FeedData feedData = getModel().getValue(FEED_MODEL_PARAMETER, FeedData.class);
			if (feedData != null)
			{
				recalculatedUrl.append(String.format("/data-feeds/%s", feedData.getName()));
			}

			final String canonicalItemType = getModel().getValue(TYPE_MODEL_PARAMETER, String.class);
			if (StringUtils.isNotBlank(canonicalItemType))
			{
				recalculatedUrl.append(String.format("/items/%s", canonicalItemType));
			}
			url.setValue(recalculatedUrl.toString());
		}
	}

	protected void clearBinaryMedia()
	{
		getModel().remove(UPLOADED_MEDIA_MODEL_PARAMETER);
	}

	protected ProcessingStrategy lookupMatchingStrategy(final String key)
	{
		for (final ProcessingStrategy strategy : processingStrategies)
		{
			if (strategy != null && strategy.supports(key))
			{
				return strategy;
			}
		}
		return new FallbackProcessingStrategy();
	}

	protected void appendLocalNotifier()
	{
		final Div localNotifier = new Div();
		localNotifier.setSclass("yw-quickupload-busynotifier");
		localNotifier.setId(NOTIFIER_STACK);
		localNotifier.setAttribute("verticalLayout", "true");

		if (main.getFellowIfAny(NOTIFIER_STACK) == null)
		{
			main.appendChild(localNotifier);
		}
	}

	protected void removeLocalNotifier()
	{
		final Component localNotifier = main.getFellowIfAny(NOTIFIER_STACK);
		if (localNotifier != null)
		{
			localNotifier.detach();
		}
	}

	protected Editor createTypeEditor(final EventListener<Event> eventListener)
	{
		final Editor editor = new Editor();
		editor.setOptional(false);
		editor.setSclass(EDITOR_SCLASS);
		editor.setProperty(TYPE_MODEL_PARAMETER);
		editor.setType(DataType.STRING.getCode());
		editor.setDefaultEditor(CONFIGURABLE_DROPDOWN_EDITOR_ID);
		editor.setWidgetInstanceManager(getWidgetInstanceManager());
		editor.setNestedObjectCreationDisabled(true);
		if (eventListener != null)
		{
			editor.addEventListener(Editor.ON_VALUE_CHANGED, eventListener);
		}

		final Map<String, Object> editorParams = Maps.newHashMap();
		editorParams.put(EDITOR_PLACEHOLDER_KEY_ATTR,
				String.format("%s.%s.%s", DATAHUB_CANONICAL_ITEM_TYPECODE, TYPE_MODEL_PARAMETER, EDITOR_PLACEHOLDER_SUFFIX));
		editorParams.put(EDITOR_AVAILABLE_VALUES_PROVIDER_ATTR, "rawItemTypesProvider");

		editor.setParameters(editorParams);
		editor.afterCompose();
		editorsMap.put(TYPE_MODEL_PARAMETER, editor);
		return editor;

	}

	public void markEditorError(final String id)
	{
		final Component comp = editorsMap.get(id);

		if (comp != null)
		{
			Clients.wrongValue(comp.getFirstChild(), getLabel(VALUE_CANNOT_BE_EMPTY));
		}
	}

	protected Editor createEditor(final String type, final String property, final EventListener<Event> eventListener, final boolean multi)
	{
		final Editor editor = new Editor();
		editor.setEditorLabel(type + "." + property);
		editor.setSclass(EDITOR_SCLASS);
		editor.setProperty(property);
		if (multi)
		{
			editor.setType(String.format("MultiReference-SET(%s)", type));
		}
		else
		{
			editor.setType(String.format("Reference(%s)", type));
		}

		editor.setWidgetInstanceManager(getWidgetInstanceManager());
		editor.setNestedObjectCreationDisabled(true);
		if (eventListener != null)
		{
			editor.addEventListener(Editor.ON_VALUE_CHANGED, eventListener);
		}

		final Map<String, Object> editorParams = Maps.newHashMap();
		editorParams.put(EDITOR_PLACEHOLDER_KEY_ATTR, String.format("%s.%s.%s", type, property, EDITOR_PLACEHOLDER_SUFFIX));
		editorParams.put(EDITOR_PAGE_SIZE_ATTR, getWidgetSettings().getInt(SETTING_EDITOR_PAGE_SIZE));
		editor.setParameters(editorParams);
		editor.afterCompose();
		editorsMap.put(type + "." + property, editor);
		return editor;
	}

	public void setProcessingStrategies(final List<ProcessingStrategy> strategies)
	{
		if (strategies != null)
		{
			processingStrategies.addAll(strategies);
		}
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public NotificationService getNotificationService()
	{
		return this.notificationService;
	}

	/**
	 * This is a special case for object used when no matching processing strategy is found.
	 */
	private static class FallbackProcessingStrategy implements ProcessingStrategy
	{
		@Override
		public void process(final QuickUploadController controller, final String id, final Map<String, Object> ctx)
		{
			// do nothing, if no matching strategy found
		}

		@Override
		public boolean supports(final String key)
		{
			return false;
		}
	}
}
