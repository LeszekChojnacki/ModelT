/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.renderers;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.core.model.ModelObserver;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.editor.localized.LocalizedEditor;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.YTestTools;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;


public class TotalDiscountRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(TotalDiscountRenderer.class);

	private static final String ORDER = "Order";
	private static final String QUALIFIER = "totalDiscounts";

	private TypeFacade typeFacade;
	private Double totalDiscountAmount;
	private LabelService labelService;

	/**
	 * Renders the total discount amount of the order into a textbox.
	 *
	 * @param component
	 * @param abstractPanelConfiguration
	 * 		as {@link CustomPanel}
	 * @param object
	 * 		as {@link ReturnRequestModel}
	 * @param dataType
	 * @param widgetInstanceManager
	 */
	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (abstractPanelConfiguration instanceof CustomPanel)
		{
			totalDiscountAmount = getOrderTotalDiscount((ReturnRequestModel) object);

			try
			{
				final Attribute attribute = new Attribute();
				attribute.setLabel("customersupportbackoffice.returnentry.totaldiscount");
				attribute.setQualifier(QUALIFIER);
				attribute.setReadonly(Boolean.TRUE);
				final DataType order = getTypeFacade().load(ORDER);
				final boolean canReadObject = getPermissionFacade().canReadInstanceProperty(order.getClazz(), QUALIFIER);

				if (canReadObject)
				{
					createAttributeRenderer().render(component, attribute, order.getClazz(), order, widgetInstanceManager);
				}
				else
				{
					final Div attributeContainer = new Div();
					attributeContainer.setSclass(SCLASS_EDITOR_CONTAINER);
					renderNotReadableLabel(attributeContainer, attribute, dataType, getLabelService().getAccessDeniedLabel(attribute));
					attributeContainer.setParent(component);
				}
			}
			catch (final TypeNotFoundException e)
			{
				if (LOG.isWarnEnabled())
				{
					LOG.warn(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Render method executes this overriden method
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

		final String qualifier = genericAttribute.getQualifier();
		final String referencedModelProperty = ORDER + "." + attribute.getQualifier();
		final Editor editor = createEditor(genericAttribute, widgetInstanceManager.getModel(), referencedModelProperty);
		editor.setReadOnly(Boolean.TRUE);
		editor.setLocalized(Boolean.FALSE);
		editor.setWidgetInstanceManager(widgetInstanceManager);
		editor.setType(resolveEditorType(genericAttribute));
		editor.setOptional(!genericAttribute.isMandatory());
		YTestTools.modifyYTestId(editor, "editor_" + ORDER + "." + qualifier);
		editor.setAttribute("parentObject", object);
		editor.setWritableLocales(getPermissionFacade().getWritableLocalesForInstance(object));
		editor.setReadableLocales(getPermissionFacade().getReadableLocalesForInstance(object));
		if (genericAttribute.isLocalized())
		{
			editor.addParameter(LocalizedEditor.EDITOR_PARAM_ATTRIBUTE_DESCRIPTION, getAttributeDescription(genericType, attribute));
		}
		editor.setProperty(referencedModelProperty);
		if (StringUtils.isNotBlank(attribute.getEditor()))
		{
			editor.setDefaultEditor(attribute.getEditor());
		}
		editor.setPartOf(genericAttribute.isPartOf());
		editor.setOrdered(genericAttribute.isOrdered());
		editor.afterCompose();
		editor.setSclass(SCLASS_READONLY_EDITOR);
		editor.setInitialValue(totalDiscountAmount);
		return editor;
	}

	/**
	 * Creates the actual {@link Editor} object which gets populated later.
	 *
	 * @param genericAttribute
	 * @param model
	 * @param referencedModelProperty
	 * @returnthe {@link Editor}
	 */
	protected Editor createEditor(final DataAttribute genericAttribute, final WidgetModel model,
			final String referencedModelProperty)
	{
		if (isReferenceEditor(genericAttribute))
		{
			final ModelObserver referenceObserver = new ModelObserver()
			{
				@Override
				public void modelChanged()
				{
					// empty
				}
			};
			model.addObserver(referencedModelProperty, referenceObserver);
			return new Editor()
			{
				@Override
				public void destroy()
				{
					super.destroy();
					model.removeObserver(referencedModelProperty, referenceObserver);
				}
			};
		}

		return new Editor();
	}

	/**
	 * Calculates the order total discount by adding the global discount with the sum of all discounts on entries.
	 *
	 * @param returnRequest
	 * @return the total discounts applied to the order
	 */
	protected Double getOrderTotalDiscount(final ReturnRequestModel returnRequest)
	{
		final OrderModel order = returnRequest.getOrder();
		Double totalDiscount = order.getTotalDiscounts() != null ? order.getTotalDiscounts() : 0.0;

		totalDiscount += order.getEntries().stream()
				.mapToDouble(entry -> entry.getDiscountValues().stream().mapToDouble(discount -> discount.getAppliedValue()).sum())
				.sum();

		return totalDiscount;
	}

	protected boolean isReferenceEditor(final DataAttribute genericAttribute)
	{
		return genericAttribute.getValueType() != null && !genericAttribute.getValueType().isAtomic();
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
