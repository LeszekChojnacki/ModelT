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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.EDITOR_WIDGET_INSTANCE_MANAGER_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.FROM_SEARCH_CONFIGURATION_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.FROM_SEARCH_PROFILE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.INVALID_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.OVERRIDE_FROM_SEARCH_PROFILE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.OVERRIDE_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.VALID_SCLASS;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ZK_LISTBOX_INIT_ROD_SIZE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ZK_LISTBOX_PRELOAD_SIZE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.ZK_LISTBOX_ROD_ATTRIBUTE;

import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

import com.google.common.base.Splitter;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.common.configuration.EditorConfigurationUtil;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListView;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.util.BackofficeSpringUtil;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;


public class ConfigurableMultiReferenceEditor<V, D extends AbstractEditorData> extends AbstractComponentWidgetAdapterAware
		implements CockpitEditorRenderer<Collection<V>>, MultiReferenceEditorLogic<D, V>
{
	private static final Logger LOG = Logger.getLogger(ConfigurableMultiReferenceEditor.class);

	protected static final String ERROR_NOTIFICATION_ID = "reference.editor.cannot.instantiate.type.selected";
	protected static final String CREATE_BUTTON_LABEL = "configurableMultiReferenceEditor.create";

	protected static final String DATA_HANDLER_PARAM = "dataHandler";
	protected static final String DATA_HANDLER_PARAMETERS_PARAM = "dataHandlerParameters";
	protected static final String ITEM_MASTER_RENDERER_PARAM = "itemMasterRenderer";
	protected static final String ITEM_DETAIL_RENDERER_PARAM = "itemDetailRenderer";
	protected static final String CONTEXT_PARAM = "context";
	protected static final String SORTABLE_PARAM = "sortable";
	protected static final String EDITABLE_COLUMNS_PARAM = "inlineEditingParams";

	protected static final String DEFAULT_ITEM_MASTER_RENDERER = "asMultiReferenceItemMasterRenderer";
	protected static final String DEFAULT_ITEM_DETAIL_RENDERER = "asMultiReferenceItemDetailRenderer";

	protected static final String SEARCH_RESULT = "searchResult";

	protected static final Pattern SPEL_REGEXP = Pattern.compile("\\{((.*))\\}");

	protected static final String EDITOR_SCLASS = "yas-editor";
	protected static final String ITEM_SCLASS = "yas-item";
	protected static final String ITEM_MASTER_SCLASS = "yas-item-master";
	protected static final String ITEM_MASTER_INNER_SCLASS = "yas-item-master-inner";
	protected static final String ITEM_DETAIL_SCLASS = "yas-item-detail";
	protected static final String ITEM_DETAIL_INNER_SCLASS = "yas-item-detail-inner";

	protected static final String IN_SEARCH_RESULT_SCLASS = "yas-in-search-result";

	protected static final String FILTER_SCLASS = "yas-filter";

	protected static final String CREATE_SCLASS = "yas-create";
	protected static final String CREATE_ICON_SCLASS = "z-icon-plus";

	protected static final String SOCKET_OUT_CREATE_REQUEST = "createRequest";
	protected static final String SOCKET_IN_CREATE_RESULT = "createResult";
	protected static final String SOCKET_OUT_UPDATE_REQUEST = "updateRequest";
	protected static final String SOCKET_IN_UPDATE_RESULT = "updateResult";

	protected static final String ITEM_ATTRIBUTE = "asItem";
	protected static final String OPEN_ATTRIBUTE = "asOpen";

	protected static final String EDITOR_DATA_KEY = "editorData";

	protected static final int EDITOR_MAX_SIZE = 20;

	private EditorContext<Collection<V>> editorContext;
	private EditorListener<Collection<V>> editorListener;
	private Object parentObject;
	private String parentTypeCode;
	private String context;
	private boolean sortable;
	private Collection<String> columns;
	private Collection<String> editableColumns;
	private DataHandler<D, V> dataHandler;
	private ListModel<D> listModel;
	private EditorRenderer itemMasterRenderer;
	private EditorRenderer itemDetailRenderer;

	@Resource
	private TypeFacade typeFacade;

	@Resource
	private PermissionFacade permissionFacade;

	@Resource
	private PropertyValueService propertyValueService;

	@Override
	public void render(final Component parent, final EditorContext<Collection<V>> editorContext,
			final EditorListener<Collection<V>> editorListener)
	{
		setEditorContext(editorContext);
		setEditorListener(editorListener);
		setParentObject(resolveParentObject(parent));
		setParentTypeCode(resolveTypeCode(getParentObject()));
		setContext(resolveContext());
		setSortable(resolveSortable());
		setColumns(resolveColumns());
		setEditableColumns(resolveEditableColumns());
		setDataHandler(createDataHandler());
		setListModel(createListModel());
		setItemMasterRenderer(createItemMasterRenderer());
		setItemDetailRenderer(createItemDetailRenderer());

		renderList(parent);

		// register socket events
		addSocketInputEventListener(SOCKET_IN_CREATE_RESULT, event -> createReference(event.getData()));
		addSocketInputEventListener(SOCKET_IN_UPDATE_RESULT, event -> updateReference(event.getData()));
	}

	protected Object resolveParentObject(final Component parent)
	{
		return parent.getAttribute(PARENT_OBJECT_KEY);
	}

	protected String resolveTypeCode(final Object object)
	{
		return typeFacade.getType(object);
	}

	protected String resolveContext()
	{
		return editorContext.getParameterAs(CONTEXT_PARAM);
	}

	protected boolean resolveSortable()
	{
		return Boolean.parseBoolean(editorContext.getParameterAs(SORTABLE_PARAM));
	}

	protected List<String> resolveColumns()
	{
		final WidgetInstanceManager widgetInstanceManager = getWidgetInstanceManager();

		final ListView columnsConfiguration = EditorConfigurationUtil.loadConfiguration(null, widgetInstanceManager, context,
				ListView.class, true);
		if (columnsConfiguration == null)
		{
			return Collections.emptyList();
		}

		return columnsConfiguration.getColumn().stream().map(ListColumn::getQualifier).collect(Collectors.toList());
	}

	protected Collection<String> resolveEditableColumns()
	{
		final String editableColumnsConfiguration = ObjectUtils.toString(editorContext.getParameter(EDITABLE_COLUMNS_PARAM));
		if (StringUtils.isBlank(editableColumnsConfiguration))
		{
			return Collections.emptyList();
		}

		return Splitter.on(",").splitToList(editableColumnsConfiguration);
	}


	protected boolean canCreate(final String typeCode)
	{
		try
		{
			final DataType type = typeFacade.load(typeCode);
			return !type.isAbstract() && permissionFacade.canCreateTypeInstance(typeCode);
		}
		catch (final TypeNotFoundException e)
		{
			LOG.warn("Type not found", e);
		}
		return false;
	}

	protected SearchResultData resolveSearchResult()
	{
		final WidgetInstanceManager widgetInstanceManager = getWidgetInstanceManager();
		return widgetInstanceManager.getWidgetslot().getViewModel().getValue(SEARCH_RESULT, SearchResultData.class);
	}

	protected void renderList(final Component parent)
	{
		final Div editorDiv = new Div();
		editorDiv.setParent(parent);
		editorDiv.setSclass(EDITOR_SCLASS + " " + context);

		if (listModel.getSize() <= EDITOR_MAX_SIZE)
		{
			final Listbox listbox = new Listbox();
			listbox.setParent(editorDiv);
			listbox.setModel(listModel);
			listbox.setItemRenderer((item, data, index) -> renderItem(item, (AbstractEditorData) data));
		}
		else
		{
			final Div filterDiv = new Div();
			filterDiv.setParent(editorDiv);
			filterDiv.setSclass(FILTER_SCLASS);

			final Textbox filterTextbox = new Textbox();
			filterTextbox.setParent(filterDiv);

			final Listbox listbox = new Listbox();
			listbox.setParent(editorDiv);
			listbox.setModel(listModel);
			listbox.setItemRenderer((item, data, index) -> renderItem(item, (AbstractEditorData) data));
			listbox.setRows(EDITOR_MAX_SIZE);
			listbox.setAttribute(ZK_LISTBOX_ROD_ATTRIBUTE, Boolean.TRUE);
			listbox.setAttribute(ZK_LISTBOX_INIT_ROD_SIZE_ATTRIBUTE, EDITOR_MAX_SIZE);
			listbox.setAttribute(ZK_LISTBOX_PRELOAD_SIZE_ATTRIBUTE, EDITOR_MAX_SIZE * 2);

			filterTextbox.addEventListener(Events.ON_CHANGING, event -> {
				final String filter = ((InputEvent) event).getValue();

				if (StringUtils.isBlank(filter))
				{
					listbox.setModel(listModel);
					listbox.setRows(EDITOR_MAX_SIZE);
				}
				else
				{
					final ListModel<D> filteredListModel = createFilteredListModel(listModel, filter);
					listbox.setModel(filteredListModel);
					listbox.setRows(filteredListModel.getSize() <= EDITOR_MAX_SIZE ? 0 : EDITOR_MAX_SIZE);
				}
			});
		}

		final Div createDiv = new Div();
		createDiv.setParent(editorDiv);
		createDiv.setSclass(CREATE_SCLASS);

		final Button createButton = new Button();
		createButton.setParent(createDiv);
		createButton.setIconSclass(CREATE_ICON_SCLASS);
		createButton.setLabel(editorContext.getLabel(CREATE_BUTTON_LABEL));
		createButton.addEventListener(Events.ON_CLICK, event -> triggerCreateReference());
	}

	protected void renderItem(final Listitem listitem, final AbstractEditorData editorData)
	{
		listitem.setValue(editorData);

		final Listcell listcell = new Listcell();
		listcell.setParent(listitem);

		final Div itemDiv = new Div();
		itemDiv.setParent(listcell);
		itemDiv.setSclass(buildItemStyleClass(editorData));

		itemDiv.setAttribute(ITEM_ATTRIBUTE, Boolean.TRUE);
		setOpen(itemDiv, editorData.isOpen());

		renderItemMaster(itemDiv, editorData);
		renderItemDetail(itemDiv, editorData);

		if (editorData.isFromSearchConfiguration() && isSortable())
		{
			// drag and drop
			final String typeCode = dataHandler.getTypeCode();
			listitem.setDraggable(typeCode);
			listitem.setDroppable(typeCode);

			listitem.addEventListener(Events.ON_DROP, this::handleDropEvent);
		}
	}

	protected void renderItemMaster(final Component item, final AbstractEditorData editorData)
	{
		if (itemMasterRenderer != null && itemMasterRenderer.canRender(this, item, editorData))
		{
			final Div itemMasterDiv = new Div();
			itemMasterDiv.setParent(item);
			itemMasterDiv.setSclass(ITEM_MASTER_SCLASS);

			final Div itemMasterInnerDiv = new Div();
			itemMasterInnerDiv.setParent(itemMasterDiv);
			itemMasterInnerDiv.setSclass(ITEM_MASTER_INNER_SCLASS);

			itemMasterRenderer.beforeRender(this, itemMasterInnerDiv, editorData);
			itemMasterRenderer.render(this, itemMasterInnerDiv, editorData);

			// toggles the item status (open, closed)
			itemMasterDiv.addEventListener(Events.ON_CLICK, event -> setOpen(item, !isOpen(item)));
		}
	}

	protected void renderItemDetail(final Component item, final AbstractEditorData editorData)
	{
		if (itemDetailRenderer != null && itemDetailRenderer.canRender(this, item, editorData))
		{
			final boolean open = isOpen(item);

			final Div itemDetailDiv = new Div();
			itemDetailDiv.setParent(item);
			itemDetailDiv.setSclass(ITEM_DETAIL_SCLASS);
			itemDetailDiv.setVisible(open);

			final Div itemDetailInnerDiv = new Div();
			itemDetailInnerDiv.setParent(itemDetailDiv);
			itemDetailInnerDiv.setSclass(ITEM_DETAIL_INNER_SCLASS);

			itemDetailRenderer.beforeRender(this, itemDetailInnerDiv, editorData);
			renderItemDetailHelper(itemDetailInnerDiv, editorData, open);

			item.addEventListener(Events.ON_OPEN, event -> {
				final OpenEvent openEvent = (OpenEvent) event;
				itemDetailDiv.setVisible(openEvent.isOpen());
				renderItemDetailHelper(itemDetailInnerDiv, editorData, openEvent.isOpen());
			});
		}
	}

	protected void renderItemDetailHelper(final Component parent, final AbstractEditorData editorData, final boolean open)
	{
		// item detail is only rendered the first time it is required
		if (open && parent.getChildren().isEmpty())
		{
			itemDetailRenderer.render(this, parent, editorData);
		}
	}

	protected String buildItemStyleClass(final AbstractEditorData editorData)
	{
		final StringJoiner styleClass = new StringJoiner(" ");
		styleClass.add(ITEM_SCLASS);

		if (editorData.isValid())
		{
			styleClass.add(VALID_SCLASS);
		}
		else
		{
			styleClass.add(INVALID_SCLASS);
		}

		if (editorData.isFromSearchProfile())
		{
			styleClass.add(FROM_SEARCH_PROFILE_SCLASS);
		}

		if (editorData.isFromSearchConfiguration())
		{
			styleClass.add(FROM_SEARCH_CONFIGURATION_SCLASS);
		}

		if (editorData.isOverride())
		{
			styleClass.add(OVERRIDE_SCLASS);
		}

		if (editorData.isOverrideFromSearchProfile())
		{
			styleClass.add(OVERRIDE_FROM_SEARCH_PROFILE_SCLASS);
		}

		if (editorData.isInSearchResult())
		{
			styleClass.add(IN_SEARCH_RESULT_SCLASS);
		}

		return styleClass.toString();
	}

	@ViewEvent(eventName = Events.ON_DROP)
	protected void handleDropEvent(final Event event)
	{
		final DropEvent dropEvent = (DropEvent) event;
		final Component dragged = dropEvent.getDragged();
		final Component target = dropEvent.getTarget();

		if (!(dragged instanceof Listitem || target instanceof Listitem))
		{
			return;
		}

		final Listitem draggedItem = (Listitem) dragged;
		final Listitem droppedItem = (Listitem) target;
		final D draggedData = draggedItem.getValue();
		final D droppedData = droppedItem.getValue();

		final List<V> value = new ArrayList<>(dataHandler.getValue(listModel));
		final V draggedItemValue = dataHandler.getItemValue(draggedData);
		final V droppedItemValue = dataHandler.getItemValue(droppedData);

		final int droppedIndex = value.indexOf(droppedItemValue);

		value.remove(draggedItemValue);
		value.add(droppedIndex, draggedItemValue);

		updateValue(value);
	}

	protected ListModel<D> createListModel()
	{
		final SearchResultData searchResult = resolveSearchResult();
		final Map<String, Object> dataHandlerParameters = createDataHandlerParameters();
		final ListModel<D> data = dataHandler.loadData(editorContext.getInitialValue(), searchResult, dataHandlerParameters);

		if (data instanceof AbstractListModel)
		{
			((AbstractListModel) data).setSelectionControl(new AbstractListModel.DefaultSelectionControl((AbstractListModel) data)
			{
				@Override
				public boolean isSelectable(final Object object)
				{
					return false;
				}
			});
		}

		return data;
	}

	protected ListModel<D> createFilteredListModel(final ListModel<D> originalListModel, final String filter)
	{
		final List<D> data = new ArrayList<>();

		for (int index = 0; index < originalListModel.getSize(); index++)
		{
			final D itemData = originalListModel.getElementAt(index);
			if (StringUtils.containsIgnoreCase(itemData.getLabel(), filter))
			{
				data.add(itemData);
			}
		}

		return new ListModelList<>(data);
	}

	protected DataHandler createDataHandler()
	{
		final String dataHandlerId = ObjectUtils.toString(editorContext.getParameter(DATA_HANDLER_PARAM));
		if (StringUtils.isBlank(dataHandlerId))
		{
			return null;
		}

		return BackofficeSpringUtil.getBean(dataHandlerId);
	}

	protected Map<String, Object> createDataHandlerParameters()
	{
		final String configParameters = ObjectUtils.toString(editorContext.getParameter(DATA_HANDLER_PARAMETERS_PARAM));
		if (StringUtils.isBlank(configParameters))
		{
			return Collections.emptyMap();
		}

		final Map<String, String> parameters = Splitter.on(";").withKeyValueSeparator("=").split(configParameters);

		return parameters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> evaluate(entry.getValue())));
	}

	protected EditorRenderer createItemMasterRenderer()
	{
		String itemMasterRendererId = ObjectUtils.toString(editorContext.getParameter(ITEM_MASTER_RENDERER_PARAM));
		if (StringUtils.isBlank(itemMasterRendererId))
		{
			itemMasterRendererId = DEFAULT_ITEM_MASTER_RENDERER;
		}

		return BackofficeSpringUtil.getBean(itemMasterRendererId);
	}

	protected EditorRenderer createItemDetailRenderer()
	{
		String itemDetailRendererId = ObjectUtils.toString(editorContext.getParameter(ITEM_DETAIL_RENDERER_PARAM));
		if (StringUtils.isBlank(itemDetailRendererId))
		{
			itemDetailRendererId = DEFAULT_ITEM_DETAIL_RENDERER;
		}

		return BackofficeSpringUtil.getBean(itemDetailRendererId);
	}

	protected Object evaluate(final String value)
	{
		final Matcher matcher = SPEL_REGEXP.matcher(value);
		if (matcher.find())
		{
			final Map<String, Object> evaluationContext = createEvaluationContext();
			final String expression = matcher.group(1);

			final Object processedValue = propertyValueService.readValue(evaluationContext, expression);

			return processedValue == null ? value : processedValue;
		}
		else
		{
			return value;
		}
	}

	protected Map<String, Object> createEvaluationContext()
	{
		final Map<String, Object> evaluationContext = new HashMap<>();
		evaluationContext.put(PARENT_OBJECT_KEY, getParentObject());

		return evaluationContext;
	}

	public void triggerCreateReference()
	{
		final String typeCode = dataHandler.getTypeCode();

		if (canCreate(typeCode))
		{
			final Map<String, Object> wizardInput = new HashMap<>();
			wizardInput.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), typeCode);
			wizardInput.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT.getName(), getParentObject());
			wizardInput.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT_TYPE.getName(), getParentTypeCode());

			final String configurableFlowConfigCtx = MapUtils.getString(editorContext.getParameters(),
					ConfigurableFlowController.SETTING_CONFIGURABLE_FLOW_CONFIG_CTX);
			if (configurableFlowConfigCtx != null)
			{
				wizardInput.put(ConfigurableFlowController.SETTING_CONFIGURABLE_FLOW_CONFIG_CTX, configurableFlowConfigCtx);
			}

			sendOutput(SOCKET_OUT_CREATE_REQUEST, wizardInput);
		}
		else
		{
			Messagebox.show(Labels.getLabel(ERROR_NOTIFICATION_ID, new Object[]
			{ typeCode }), null, Messagebox.OK, Messagebox.EXCLAMATION);
		}
	}

	protected void createReference(final Object data)
	{
		final V newItem = (V) ((HashMap) data).get("newItem");
		final Collection<V> value = dataHandler.getValue(listModel);
		value.add(newItem);

		updateValue(value);
	}

	public void triggerUpdateReference(final AbstractEditorData editorData)
	{
		final TypeAwareSelectionContext<Object> selectionContext = new TypeAwareSelectionContext<>(dataHandler.getTypeCode(),
				editorData.getModel(), Collections.singletonList(editorData.getModel()));

		selectionContext.addParameter(EDITOR_DATA_KEY, editorData);

		sendOutput(SOCKET_OUT_UPDATE_REQUEST, selectionContext);
	}

	protected void updateReference(final Object data)
	{
		final Collection<V> value = dataHandler.getValue(listModel);
		updateValue(value);
	}

	@Override
	public WidgetInstanceManager getWidgetInstanceManager()
	{
		return (WidgetInstanceManager) editorContext.getParameter(EDITOR_WIDGET_INSTANCE_MANAGER_KEY);
	}

	@Override
	public boolean isOpen(final Component component)
	{
		return Objects.equals(component.getAttribute(OPEN_ATTRIBUTE), Boolean.TRUE);
	}

	@Override
	public void setOpen(final Component component, final boolean open)
	{
		if (isOpen(component) != open)
		{
			component.setAttribute(OPEN_ATTRIBUTE, Boolean.valueOf(open));
			Events.sendEvent(component, new OpenEvent(Events.ON_OPEN, component, open));
		}
	}

	@Override
	public Editor findEditor(final Component component)
	{
		for (Component current = component; current != null; current = current.getParent())
		{
			if (current instanceof Editor)
			{
				return (Editor) current;
			}
		}

		return null;
	}

	@Override
	public Component findEditorItem(final Component component)
	{
		for (Component current = component; current != null; current = current.getParent())
		{
			if (Objects.equals(current.getAttribute(ITEM_ATTRIBUTE), Boolean.TRUE))
			{
				return current;
			}
		}

		return null;
	}

	@Override
	public void updateValue(final Collection<V> value)
	{
		editorListener.onValueChanged(value);
	}

	@Override
	public void updateAttributeValue(final D data, final String attributeName, final Object attributeValue)
	{
		dataHandler.setAttributeValue(data, attributeName, attributeValue);
		updateValue(dataHandler.getValue(listModel));
	}

	@Override
	public EditorContext<Collection<V>> getEditorContext()
	{
		return editorContext;
	}

	protected void setEditorContext(final EditorContext<Collection<V>> editorContext)
	{
		this.editorContext = editorContext;
	}

	@Override
	public EditorListener<Collection<V>> getEditorListener()
	{
		return editorListener;
	}

	protected void setEditorListener(final EditorListener<Collection<V>> editorListener)
	{
		this.editorListener = editorListener;
	}

	protected Object getParentObject()
	{
		return parentObject;
	}

	protected void setParentObject(final Object parentObject)
	{
		this.parentObject = parentObject;
	}

	protected String getParentTypeCode()
	{
		return parentTypeCode;
	}

	protected void setParentTypeCode(final String parentTypeCode)
	{
		this.parentTypeCode = parentTypeCode;
	}

	@Override
	public String getContext()
	{
		return context;
	}

	protected void setContext(final String context)
	{
		this.context = context;
	}

	@Override
	public boolean isSortable()
	{
		return sortable;
	}

	protected void setSortable(final boolean sortable)
	{
		this.sortable = sortable;
	}

	@Override
	public Collection<String> getColumns()
	{
		return columns;
	}

	protected void setColumns(final Collection<String> columns)
	{
		this.columns = columns;
	}

	@Override
	public Collection<String> getEditableColumns()
	{
		return editableColumns;
	}

	protected void setEditableColumns(final Collection<String> editableColumns)
	{
		this.editableColumns = editableColumns;
	}

	@Override
	public DataHandler<D, V> getDataHandler()
	{
		return dataHandler;
	}

	protected void setDataHandler(final DataHandler<D, V> dataHandler)
	{
		this.dataHandler = dataHandler;
	}

	@Override
	public ListModel<D> getListModel()
	{
		return listModel;
	}

	protected void setListModel(final ListModel<D> listModel)
	{
		this.listModel = listModel;
	}

	@Override
	public EditorRenderer getItemMasterRenderer()
	{
		return itemMasterRenderer;
	}

	protected void setItemMasterRenderer(final EditorRenderer itemMasterRenderer)
	{
		this.itemMasterRenderer = itemMasterRenderer;
	}

	@Override
	public EditorRenderer getItemDetailRenderer()
	{
		return itemDetailRenderer;
	}

	protected void setItemDetailRenderer(final EditorRenderer itemDetailRenderer)
	{
		this.itemDetailRenderer = itemDetailRenderer;
	}
}
