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
package de.hybris.platform.datahubbackoffice.presentation.renderer;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Parameter;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.editor.localized.LocalizedEditor;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.i18n.CockpitLocaleService;
import com.hybris.cockpitng.util.YTestTools;
import com.hybris.cockpitng.widgets.common.ProxyRenderer;
import com.hybris.cockpitng.widgets.editorarea.EditorAreaParameterNames;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaSectionRenderer;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

import com.google.common.collect.Sets;

public class CanonicalItemCustomSection extends DefaultEditorAreaSectionRenderer
{
	private AttributesRetrievalStrategy attributesRetrievalStrategy;
	private CockpitLocaleService cockpitLocaleService;

	@Override
	public void render(final Component parent, final AbstractSection abstractSectionConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetMgr)
	{
		final List<Attribute> attributes = attributesRetrievalStrategy.retrieveAttributes(object);
		if (attributes.isEmpty())
		{
			renderMessageForNoAttributes(parent);
		}
		else
		{
			renderAttributes(parent, abstractSectionConfiguration, object, dataType, widgetMgr, attributes);
		}
	}

	private static void renderMessageForNoAttributes(final Component parent)
	{
		final String msg = Labels.getLabel("datahub.canonical.item.label.noAttributes");
		parent.appendChild(new Label(msg));
	}

	private void renderAttributes(
			final Component parent,
			final AbstractSection cfg,
			final Object object,
			final DataType type,
			final WidgetInstanceManager widgetMgr,
			final List<Attribute> attributes)
	{
		final int columns = 2;
		final String width = calculateWidthPercentage(columns);
		renderAttributes(attributes, new ProxyRenderer(this, parent, cfg, object), columns, width, type, widgetMgr, object);
	}

	@Override
	protected Editor createEditor(final DataType type, final WidgetInstanceManager widgetMgr, final Attribute attr, final Object obj)
	{
		final DataAttribute genericAttribute = type.getAttribute(attr.getQualifier());
		if (genericAttribute == null)
		{
			return null;
		}

		final Editor editor = new Editor();

		processParameters(attr.getEditorParameter(), editor);
		final boolean editable = !attr.isReadonly() && canChangeProperty(genericAttribute, obj);
		final String editorSClass = editable ? SCLASS_EDITOR : SCLASS_READONLY_EDITOR;

		editor.setReadOnly(!editable);
		editor.setLocalized(genericAttribute.isLocalized());
		editor.setWidgetInstanceManager(widgetMgr);
		editor.setEditorLabel(resolveAttributeLabel(attr, type));
		editor.setType(resolveEditorType(genericAttribute));
		editor.setOptional(!genericAttribute.isMandatory());
		final String qualifier = genericAttribute.getQualifier();
		YTestTools.modifyYTestId(editor, "editor_" + qualifier);

		final WidgetModel model = widgetMgr.getModel();
		final Object currentObject = model.getValue("currentObject", Object.class);
		editor.setAttribute("parentObject", currentObject);
		editor.setWritableLocales(Sets.newHashSet(cockpitLocaleService.getAllLocales()));
		editor.setReadableLocales(Sets.newHashSet(cockpitLocaleService.getAllLocales()));
		if (genericAttribute.isLocalized())
		{
			editor.addParameter(LocalizedEditor.HEADER_LABEL_TOOLTIP, attr.getQualifier());
			editor.addParameter(LocalizedEditor.EDITOR_PARAM_ATTRIBUTE_DESCRIPTION, getAttributeDescription(type, attr));
		}
		final String referencedModelProperty = CURRENT_OBJECT + qualifier;
		editor.setProperty(referencedModelProperty);
		if (StringUtils.isNotBlank(attr.getEditor()))
		{
			editor.setDefaultEditor(attr.getEditor());
		}
		editor.setOrdered(genericAttribute.isOrdered());
		editor.afterCompose();
		editor.setSclass(editorSClass);

		return editor;
	}

	private void processParameters(final List<Parameter> parameters, final Editor editor)
	{
		for (final Parameter parameter : parameters)
		{
			if (EditorAreaParameterNames.MULTILINE_EDITOR_ROWS.getName().equals(parameter.getName())
					|| EditorAreaParameterNames.ROWS.getName().equals(parameter.getName()))
			{
				editor.addParameter("rows", parameter.getValue());
			}
			else if (EditorAreaParameterNames.NESTED_OBJECT_WIZARD_NON_PERSISTABLE_PROPERTIES_LIST.getName()
					.equals(parameter.getName()))
			{
				final List<String> nonPersistablePropertiesList = extractPropertiesList(parameter.getValue());
				editor.addParameter(EditorAreaParameterNames.NESTED_OBJECT_WIZARD_NON_PERSISTABLE_PROPERTIES_LIST.getName(),
						nonPersistablePropertiesList);
			}
			else
			{
				editor.addParameter(parameter.getName(), parameter.getValue());
			}
		}
	}

	@Override
	public void setCockpitLocaleService(final CockpitLocaleService cockpitLocaleService)
	{
		this.cockpitLocaleService = cockpitLocaleService;
	}

	@Required
	public void setAttributesRetrievalStrategy(final AttributesRetrievalStrategy strategy)
	{
		attributesRetrievalStrategy = strategy;
	}
}
