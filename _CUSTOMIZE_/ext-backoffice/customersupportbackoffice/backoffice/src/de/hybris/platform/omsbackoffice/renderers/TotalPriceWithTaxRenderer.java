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

import de.hybris.platform.core.model.order.AbstractOrderModel;

import java.math.BigDecimal;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
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


/**
 * Custom renderer extending a {@link DefaultEditorAreaPanelRenderer} to render an editor field that contains the order total with taxes.
 */
public class TotalPriceWithTaxRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(TotalPriceWithTaxRenderer.class);

	protected static final String ORDER = "Order";
	protected static final String QUALIFIER = "totalPrice";
	protected static final String LABEL = "customersupportbackoffice.order.details.total";

	private TypeFacade typeFacade;
	private LabelService labelService;

	private Double totalPriceWithTax;

	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (abstractPanelConfiguration instanceof CustomPanel && object instanceof AbstractOrderModel)
		{
			totalPriceWithTax = getOrderTotalWithTax((AbstractOrderModel) object);
			try
			{
				final Attribute attribute = new Attribute();
				attribute.setLabel(LABEL);
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
			catch (final TypeNotFoundException e) //NOSONAR
			{
				LOG.warn(e.getMessage());
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

		final String qualifier = genericAttribute.getQualifier();
		final String referencedModelProperty = ORDER + "." + attribute.getQualifier();
		final Editor editor = new Editor();

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
		editor.setInitialValue(totalPriceWithTax);
		return editor;
	}

	/**
	 * Sum of the {@link AbstractOrderModel#TOTALPRICE} with {@link AbstractOrderModel#TOTALTAX)}
	 *
	 * @param abstractOrderModel
	 * 		the order
	 * @return the amount of the order with taxes.
	 */
	protected Double getOrderTotalWithTax(final AbstractOrderModel abstractOrderModel)
	{
		return BigDecimal.valueOf(abstractOrderModel.getTotalPrice()).add(BigDecimal.valueOf(abstractOrderModel.getTotalTax()))
				.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
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
