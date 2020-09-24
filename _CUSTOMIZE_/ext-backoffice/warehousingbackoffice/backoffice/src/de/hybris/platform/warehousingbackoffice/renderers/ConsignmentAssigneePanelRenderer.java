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
 *
 */
package de.hybris.platform.warehousingbackoffice.renderers;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import javax.annotation.Resource;

import java.util.Optional;

import com.hybris.cockpitng.common.EditorBuilder;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;

import static com.hybris.cockpitng.util.YTestTools.modifyYTestId;


/**
 * This renderer renders the employee assigned to a consigment in the Consignment Details editor area.
 */
public class ConsignmentAssigneePanelRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsignmentAssigneePanelRenderer.class);

	protected static final String CONSIGNMENT = "Consignment";
	protected static final String QUALIFIER = "taskAssignmentWorkflow";

	@Resource
	protected WorkflowService newestWorkflowService;
	@Resource
	protected TypeFacade typeFacade;

	private String assignee;

	/**
	 * Renders the employee assigned to a {@link ConsignmentModel} into a textbox.
	 *
	 * @param component
	 * @param abstractPanelConfiguration
	 * 		as a CustomPanel
	 * @param object
	 * 		as a {@link ConsignmentModel}
	 * @param dataType
	 * @param widgetInstanceManager
	 */
	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (abstractPanelConfiguration instanceof CustomPanel && object instanceof ConsignmentModel
				&& ((ConsignmentModel) object).getTaskAssignmentWorkflow() != null)
		{
			final WorkflowModel taskAssignmentWorkflow = getNewestWorkflowService()
					.getWorkflowForCode(((ConsignmentModel) object).getTaskAssignmentWorkflow());

			final Optional<WorkflowActionModel> taskInProgress = taskAssignmentWorkflow.getActions().stream()
					.filter(action -> action.getStatus().equals(WorkflowActionStatus.IN_PROGRESS)).findAny();
			if (taskInProgress.isPresent())
			{
				assignee = taskInProgress.get().getPrincipalAssigned().getDisplayName();

				final Attribute attribute = new Attribute();
				attribute.setLabel("warehousingbackoffice.consignment.assignee");
				attribute.setQualifier(QUALIFIER);
				attribute.setDescription("warehousingbackoffice.taskassignment.consignment.assignee.description");

				try
				{
					final DataType consignment = getTypeFacade().load(CONSIGNMENT);
					createAttributeRenderer().render(component, attribute, consignment.getClazz(), consignment, widgetInstanceManager);
				}
				catch (final TypeNotFoundException e)
				{
					if (LOGGER.isWarnEnabled())
					{
						LOGGER.warn(e.getMessage());
					}
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	protected Editor createEditor(final DataType genericType, final WidgetInstanceManager widgetInstanceManager,
			final Attribute attribute, final Object object)
	{
		final DataAttribute genericAttribute = genericType.getAttribute(attribute.getQualifier());
		if (genericAttribute == null)
		{
			return null;
		}

		final String referencedModelProperty = CONSIGNMENT + "." + QUALIFIER;
		final Editor editor = new EditorBuilder(widgetInstanceManager, genericAttribute, referencedModelProperty)
				.setReadOnly(Boolean.TRUE).setValueType(resolveEditorType(genericAttribute))
				.setOptional(!genericAttribute.isMandatory()).setValue(assignee).build();

		editor.setLocalized(Boolean.FALSE);
		modifyYTestId(editor, "editor_" + CONSIGNMENT + "." + QUALIFIER);
		editor.setAttribute("parentObject", object);
		editor.setWritableLocales(getPermissionFacade().getWritableLocalesForInstance(object));
		editor.setReadableLocales(getPermissionFacade().getReadableLocalesForInstance(object));
		editor.setProperty(referencedModelProperty);
		if (StringUtils.isNotBlank(attribute.getEditor()))
		{
			editor.setDefaultEditor(attribute.getEditor());
		}
		editor.setPartOf(genericAttribute.isPartOf());
		editor.setOrdered(genericAttribute.isOrdered());
		editor.afterCompose();
		editor.setSclass(SCLASS_READONLY_EDITOR);

		return editor;
	}

	protected WorkflowService getNewestWorkflowService()
	{
		return newestWorkflowService;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}
}
