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
 *
 */
package de.hybris.platform.warehousingbackoffice.renderers;

import de.hybris.platform.warehousing.model.AtpFormulaModel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectNotFoundException;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaController;
import com.hybris.cockpitng.widgets.editorarea.renderer.EditorAreaRendererUtils;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;


/**
 * This panel renderer allows displaying AtpFormula variables
 * <b>In order to guarantee proper rendering, you cannot reuse the same instance of this renderer across multiple
 * widgets.</b>
 */
public class AtpFormulaPanelRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(AtpFormulaPanelRenderer.class);

	protected static final String ADD_VARIABLE_IN_ATPFORMULA = "addvariableatpformula";
	protected static final String SUB_VARIABLE_IN_ATPFORMULA = "subvariableatpformula";
	protected static final String ATPFORMULA_BUILDER_HEADER_ACTIONS = "atpformulabuilderheaderactions";
	protected static final String ATPFORMULA_BUILDER_HEADER_VARIABLES = "atpformulabuilderheadervariables";
	protected static final String ATPFORMULA_TABLE_CLASS = "oms-widget-atpformula-table";
	protected static final String AVAILABILITY = "availability";
	protected static final String PLUS_OPERATOR = "+";
	protected static final String MINUS_OPERATOR = "-";

	private TypeFacade typeFacade;
	private ObjectFacade objectFacade;
	private Map<String, String> atpFormulaVar2ArithmeticOperatorMap;
	private Div attributeContainer;

	/**
	 * Renders the ATP Formula variables.
	 *
	 * @param component
	 * @param abstractPanelConfiguration
	 * @param object
	 * @param dataType
	 * @param widgetInstanceManager
	 */
	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (abstractPanelConfiguration instanceof CustomPanel && object instanceof AtpFormulaModel)
		{
			final AtpFormulaModel atpFormula = (AtpFormulaModel) object;
			try
			{
				final DataType atpFormulaType = getTypeFacade().load(AtpFormulaModel._TYPECODE);
				final boolean canReadObject = getPermissionFacade()
						.canReadInstanceProperty(atpFormulaType.getClazz(), AtpFormulaModel.AVAILABILITY);

				if (canReadObject)
				{
					attributeContainer = new Div();
					attributeContainer.setSclass(SCLASS_EDITOR_CONTAINER);
					attributeContainer.setParent(component);
					renderAtpFormulaVariables(attributeContainer, atpFormula, widgetInstanceManager);
					setAfterCancelListener(widgetInstanceManager);
				}
				else
				{
					component.appendChild(
							new Label(resolveLabel(widgetInstanceManager.getLabel("warehousingbackoffice.atpformula.no.access"))));
				}
			}
			catch (final TypeNotFoundException e) // NOSONAR
			{
				LOG.error("Could not find the ATP Formula Model");
			}
		}
		else
		{
			LOG.error(resolveLabel(widgetInstanceManager.getLabel("warehousingbackoffice.atpformula.render.error"))); //NOSONAR
		}
	}

	/**
	 * Renders the properties of {@link AtpFormulaModel} along with +/- option to either add or subtract the corresponding
	 * property in the {@link AtpFormulaModel}.
	 *
	 * @param attributeContainer
	 * @param atpFormula
	 * @param widgetInstanceManager
	 */
	protected void renderAtpFormulaVariables(final HtmlBasedComponent attributeContainer, final AtpFormulaModel atpFormula,
			final WidgetInstanceManager widgetInstanceManager)
	{
		final Vlayout vlayout = new Vlayout();

		vlayout.setClass(ATPFORMULA_TABLE_CLASS);

		//Rendering Builder Header
		renderAtpFormulaBuilderHeader(vlayout);

		//Rendering Formula Variables
		renderIndividualFormulaVariables(atpFormula, widgetInstanceManager, vlayout);

		attributeContainer.appendChild(vlayout);
	}

	/**
	 * Renders each individual formula variable with the +/- and remove options.
	 *
	 * @param atpFormula
	 * @param widgetInstanceManager
	 * @param vlayout
	 */
	protected void renderIndividualFormulaVariables(final AtpFormulaModel atpFormula,
			final WidgetInstanceManager widgetInstanceManager, final Vlayout vlayout)
	{
		final Set<PropertyDescriptor> formulaVarPropertyDescriptors = getAllAtpFormulaVariables(atpFormula);

		if (CollectionUtils.isNotEmpty(formulaVarPropertyDescriptors))
		{
			final PropertyDescriptor availabilityPropDescriptor = formulaVarPropertyDescriptors.stream()
					.filter(propertyDescriptor -> AVAILABILITY.equalsIgnoreCase(propertyDescriptor.getName())).findFirst().get();
			renderAtpFormulaVariableRow(atpFormula, widgetInstanceManager, vlayout, availabilityPropDescriptor);


			formulaVarPropertyDescriptors.stream()
					.filter(propertyDescriptor -> !propertyDescriptor.getName().equalsIgnoreCase(availabilityPropDescriptor.getName()))
					.forEach(formulaVarPropertyDescriptor -> renderAtpFormulaVariableRow(atpFormula, widgetInstanceManager, vlayout,
							formulaVarPropertyDescriptor));
		}

	}

	/**
	 * Renders the given Atp formula variable
	 *
	 * @param atpFormula
	 * 		the {@link AtpFormulaModel} to be rendered
	 * @param widgetInstanceManager
	 * 		the {@link WidgetInstanceManager}
	 * @param vlayout
	 * 		the {@link Vlayout} to contain the rendered atpformula variable
	 * @param formulaVarPropertyDescriptor
	 * 		the {@link PropertyDescriptor} for the AtpFormula variable being rendered
	 */
	protected void renderAtpFormulaVariableRow(final AtpFormulaModel atpFormula, final WidgetInstanceManager widgetInstanceManager,
			final Vlayout vlayout, final PropertyDescriptor formulaVarPropertyDescriptor)
	{
		final Hlayout hlayout = new Hlayout();
		hlayout.setClass("atpformulas-row");

		final Checkbox sliderCheckbox = new Checkbox();
		sliderCheckbox.setClass("ye-switch-checkbox");

		final Div signDiv = new Div();
		if (PLUS_OPERATOR
				.equals(getAtpFormulaVar2ArithmeticOperatorMap().get(formulaVarPropertyDescriptor.getName().toLowerCase())))
		{
			signDiv.setClass(ADD_VARIABLE_IN_ATPFORMULA);
		}
		else if (MINUS_OPERATOR
				.equals(getAtpFormulaVar2ArithmeticOperatorMap().get(formulaVarPropertyDescriptor.getName().toLowerCase())))
		{
			signDiv.setClass(SUB_VARIABLE_IN_ATPFORMULA);
		}

		try
		{
			final Boolean formulaVarValue = (Boolean) formulaVarPropertyDescriptor.getReadMethod().invoke(atpFormula);
			final Hlayout innerHlayout = new Hlayout();

			final String formulaVarLabel = getLabelService()
					.getObjectLabel(AtpFormulaModel._TYPECODE + "." + formulaVarPropertyDescriptor.getName());

			final Label formulaLabel = new Label(formulaVarLabel);
			formulaLabel.setClass("atpformulas-availability--bold");
			innerHlayout.appendChild(formulaLabel);

			if (AVAILABILITY.equalsIgnoreCase(formulaVarPropertyDescriptor.getName()))
			{
				final Label descLabel = new Label(
						"- " + resolveLabel("warehousingbackoffice.atpformula.formulabuilder.variable.availability.description"));
				descLabel.setClass("atpformulas-availability--description");
				innerHlayout.appendChild(descLabel);
				sliderCheckbox.setClass("atpformulas-availability");
			}

			if (formulaVarValue != null && formulaVarValue)
			{
				sliderCheckbox.setChecked(true);
			}
			else
			{
				sliderCheckbox.setChecked(false);
			}
			innerHlayout.setClass("atpformulas-variables");

			sliderCheckbox.addEventListener(Events.ON_CHECK,
					checkboxEvent -> handleOnCheckEvent(atpFormula, widgetInstanceManager, formulaVarPropertyDescriptor,
							sliderCheckbox));

			hlayout.appendChild(signDiv);
			hlayout.appendChild(innerHlayout);
			hlayout.appendChild(sliderCheckbox);
			vlayout.appendChild(hlayout);

		}
		catch (final IllegalAccessException | InvocationTargetException e)//NOSONAR
		{
			LOG.error("Failed to interpret the ATP formula. Please review your formula variable {}", formulaVarPropertyDescriptor
					.getName());
		}
	}

	/**
	 * Updates the current formula variable to the ATP formula.
	 *
	 * @param atpFormula
	 * @param widgetInstanceManager
	 * @param formulaVarPropertyDescriptor
	 */
	protected void handleOnCheckEvent(final AtpFormulaModel atpFormula, final WidgetInstanceManager widgetInstanceManager,
			final PropertyDescriptor formulaVarPropertyDescriptor, final Checkbox checkbox)
	{
		try
		{
			formulaVarPropertyDescriptor.getWriteMethod().invoke(atpFormula, checkbox.isChecked());
			widgetInstanceManager.getModel().setValue("currentObject", atpFormula);

		}
		catch (final IllegalAccessException | InvocationTargetException e)//NOSONAR
		{
			LOG.error("Failed to interpret the ATP formula. Please review your formula variable {}", formulaVarPropertyDescriptor
					.getName());
		}

	}

	/**
	 * Renders the Atp Formula Builder Header.
	 *
	 * @param vlayout
	 */
	protected void renderAtpFormulaBuilderHeader(final Vlayout vlayout)
	{
		final Hlayout headerLayout = new Hlayout();
		headerLayout.setClass("atpformulas-header");

		final Div variablesHeader = new Div();
		variablesHeader.appendChild(new Label(resolveLabel("warehousingbackoffice.atpformula.formulabuilder.header.variables")));
		variablesHeader.setClass(ATPFORMULA_BUILDER_HEADER_VARIABLES);
		headerLayout.appendChild(variablesHeader);

		final Div includeVarHeader = new Div();
		includeVarHeader
				.appendChild(new Label(resolveLabel("warehousingbackoffice.atpformula.formulabuilder.header.variable.include")));
		includeVarHeader.setClass(ATPFORMULA_BUILDER_HEADER_ACTIONS);
		headerLayout.appendChild(includeVarHeader);
		vlayout.appendChild(headerLayout);
	}

	/**
	 * Adds afterCancelListener, so as to reinitialize with original values when the Refresh button is pressed in the editor area
	 *
	 * @param widgetInstanceManager
	 */
	protected void setAfterCancelListener(final WidgetInstanceManager widgetInstanceManager)
	{
		EditorAreaRendererUtils.setAfterCancelListener(widgetInstanceManager.getModel(), "refreshAtpFormulaPanel", event ->
		{
			attributeContainer.getChildren().clear();
			try
			{
				final AtpFormulaModel freshAtpFormulaModel = (AtpFormulaModel) getObjectFacade().reload(
						widgetInstanceManager.getModel().getValue(DefaultEditorAreaController.MODEL_CURRENT_OBJECT, Object.class));

				widgetInstanceManager.getModel().setValue(DefaultEditorAreaController.MODEL_CURRENT_OBJECT, freshAtpFormulaModel);
				renderAtpFormulaVariables(attributeContainer, freshAtpFormulaModel, widgetInstanceManager);
			}
			catch (final ObjectNotFoundException e) //NOSONAR
			{
				LOG.error(
						resolveLabel(widgetInstanceManager.getLabel("warehousingbackoffice.atpforumla.not.found.on.reload"))); //NOSONAR
			}
		}, false);
	}

	/**
	 * Provides the Set of atpformula's properties
	 *
	 * @return all atpFormulaVariables
	 */
	protected Set<PropertyDescriptor> getAllAtpFormulaVariables(final AtpFormulaModel atpFormula)
	{
		final Set<PropertyDescriptor> formulaVariablePropertyDescriptors = new HashSet<>();
		if (atpFormula != null)
		{
			try
			{
				Arrays.stream(Introspector.getBeanInfo(atpFormula.getClass()).getPropertyDescriptors())
						.filter(descriptor -> descriptor.getPropertyType().equals(Boolean.class))
						.forEach(formulaVariablePropertyDescriptors::add);
			}
			catch (final IntrospectionException e) //NOSONAR
			{
				LOG.error("Failed to interpret the ATP formula.");
			}
		}
		return formulaVariablePropertyDescriptors;
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

	protected ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	protected Map<String, String> getAtpFormulaVar2ArithmeticOperatorMap()
	{
		return atpFormulaVar2ArithmeticOperatorMap;
	}

	@Required
	public void setAtpFormulaVar2ArithmeticOperatorMap(final Map<String, String> atpFormulaVar2ArithmeticOperatorMap)
	{
		this.atpFormulaVar2ArithmeticOperatorMap = atpFormulaVar2ArithmeticOperatorMap;
	}
}
