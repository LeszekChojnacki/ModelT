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
package de.hybris.platform.marketplacebackoffice.renderes;

import de.hybris.platform.marketplacebackoffice.exceptions.InvalidNestedAttributeException;
import de.hybris.platform.marketplacebackoffice.utils.NestedAttributeUtils;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;

import com.hybris.cockpitng.common.EditorBuilder;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomElement;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Panel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Parameter;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.commonconfig.Positioned;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.editorarea.EditorAreaParameterNames;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;


/**
 * This panel renderer allows displaying read-only nested attributes.
 * <p>
 * <b>In order to guarantee proper rendering, you cannot reuse the same instance of this rendered across multiple
 * widgets.</b>
 * <p>
 */
@NotThreadSafe
public class NestedAttributePanelRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(NestedAttributePanelRenderer.class);
	private static final String NESTED_OBJECT_IDENTIFIER = "InCurrentObject";

	private String nestedObjectKey = "";
	private TypeFacade typeFacade;
	private NestedAttributeUtils nestedAttributeUtils;
	private LabelService labelService;

	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (abstractPanelConfiguration instanceof Panel)
		{
			final Panel panel = (Panel) abstractPanelConfiguration;

			panel.getAttributeOrCustom().stream()
					.forEach(element -> renderAttributeOrCustom(component, object, dataType, widgetInstanceManager, element));
		}
	}

	protected void renderAttributeOrCustom(final Component component, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager, final Positioned element)
	{
		if (element instanceof Attribute)
		{
			final Attribute attribute = (Attribute) element;
			renderNestedAttribute(component, attribute, object, dataType, widgetInstanceManager);
		}
		else if (element instanceof CustomElement)
		{
			final CustomElement definition = (CustomElement) element;
			createCustomHtmlRenderer().render(component, definition, object, dataType, widgetInstanceManager);
		}
	}

	protected void renderNestedAttribute(final Component component, final Attribute attribute, Object rootObject,
			final DataType rootDataType, final WidgetInstanceManager widgetInstanceManager)
	{
		Object nestedObject = rootObject;
		String nestedObjectName = StringUtils.EMPTY;
		Attribute nestedPropertyAttribute = null;

		try
		{
			final List<String> splitQualifier = getNestedAttributeUtils().splitQualifier(attribute.getQualifier());

			for (int i = 0; i < splitQualifier.size() - 1; i++)
			{
				nestedObjectName = splitQualifier.get(i);
				nestedObject = getNestedAttributeUtils().getNestedObject(nestedObject, nestedObjectName);
				nestedPropertyAttribute = generateAttributeForNestedProperty(attribute, splitQualifier.get(i + 1));
				if (i < splitQualifier.size() - 2)
				{
					rootObject = nestedObject;
				}
			}

			if (nestedObject == null)
			{
				final String pattern = "Property {0} is null, skipping render of {1}";
				final String message = MessageFormat.format(pattern, nestedObjectName, attribute.getQualifier());
				LOG.info(message);

				return;
			}

			nestedObjectKey = nestedObjectName + NESTED_OBJECT_IDENTIFIER;
			widgetInstanceManager.getModel().put(nestedObjectKey, nestedObject);
			final String nestedObjectClass = getNestedAttributeUtils().getNameOfClassWithoutModel(nestedObject);
			final DataType nestedDataType = getTypeFacade().load(nestedObjectClass);

			final boolean canReadNestedObject = getPermissionFacade().canReadInstanceProperty(rootObject, nestedObjectName);
			final boolean canChangeNestedObject = getPermissionFacade().canChangeInstanceProperty(rootObject, nestedObjectName);

			if (canReadNestedObject && canChangeNestedObject)
			{
				createAttributeRenderer().render(component, nestedPropertyAttribute, nestedObject, nestedDataType,
						widgetInstanceManager);
			}
			else if (!canReadNestedObject)
			{
				final Div attributeContainer = new Div();
				attributeContainer.setSclass(SCLASS_EDITOR_CONTAINER);
				renderNotReadableLabel(attributeContainer, nestedPropertyAttribute, rootDataType,
						getLabelService().getAccessDeniedLabel(nestedPropertyAttribute));
				attributeContainer.setParent(component);
			}
			else
			{
				if (nestedPropertyAttribute != null)
				{
					nestedPropertyAttribute.setReadonly(Boolean.TRUE);
				}
				createAttributeRenderer().render(component, nestedPropertyAttribute, nestedObject, nestedDataType,
						widgetInstanceManager);
			}
		}
		catch (final TypeNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException //NOSONAR
				| NoSuchMethodException | SecurityException | InvalidNestedAttributeException e)//NOSONAR
		{
			if (LOG.isErrorEnabled())
			{
				final String pattern = "Error occured when render property {0}.{1}";
				final String message = MessageFormat.format(pattern, nestedObjectName, attribute.getQualifier());
				LOG.error(message);
			}
		}
	}

	protected Attribute generateAttributeForNestedProperty(final Attribute attribute, final String nestedQualifier)
	{
		final Attribute nestedAttribute = new Attribute();
		nestedAttribute.setQualifier(nestedQualifier);
		nestedAttribute.setReadonly(Boolean.valueOf(attribute.isReadonly()));
		nestedAttribute.setDescription(attribute.getDescription());
		nestedAttribute.setEditor(attribute.getEditor());
		nestedAttribute.setLabel(attribute.getLabel());
		nestedAttribute.setVisible(Boolean.valueOf(attribute.isVisible()));
		nestedAttribute.setMergeMode(attribute.getMergeMode());
		nestedAttribute.setPosition(attribute.getPosition());
		return nestedAttribute;
	}

	/*
	 * overriden methods from DefaultEditorAreaPanelRenderer
	 */

	/**
	 * renderAttribute method execute this overridden method
	 *
	 * @param genericType
	 * @param widgetInstanceManager
	 * @param attribute
	 * @param object
	 * @return the {@link Editor}
	 */
	@Override
	protected Editor createEditor(final DataType genericType, final WidgetInstanceManager widgetInstanceManager,
			final Attribute attribute, final Object object)
	{
		final DataAttribute genericAttribute = genericType.getAttribute(attribute.getQualifier());
		if (genericAttribute == null)
		{
			return null;
		}
		final boolean editable = !attribute.isReadonly() && canChangeProperty(genericAttribute, object);

		final String editorSClass = editable ? SCLASS_EDITOR : SCLASS_READONLY_EDITOR;

		final EditorBuilder editorBuilder = new EditorBuilder(widgetInstanceManager)
				.configure(nestedObjectKey, genericAttribute).setReadOnly(!editable)
				.setLabel(resolveAttributeLabel(attribute, genericType))
				.setDescription(getAttributeDescription(genericType, attribute))
				.addParameters(attribute.getEditorParameter().stream(), this::extractParameterName, this::extractParameterValue)
				.setValueType(resolveEditorType(genericAttribute)).useEditor(attribute.getEditor())
				.apply(editor -> processEditorBeforeComposition(editor, genericType, widgetInstanceManager, attribute, object));

		final Editor editor = buildEditor(editorBuilder, widgetInstanceManager);

		UITools.addSClass(editor, editorSClass);
		return editor;
	}


	protected void processParameters(final List<Parameter> parameters, final Editor editor)
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

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	protected NestedAttributeUtils getNestedAttributeUtils()
	{
		return nestedAttributeUtils;
	}

	@Required
	public void setNestedAttributeUtils(final NestedAttributeUtils nestedAttributeUtils)
	{
		this.nestedAttributeUtils = nestedAttributeUtils;
	}

	@Override
	protected LabelService getLabelService()
	{
		return labelService;
	}

	@Override
	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

}
