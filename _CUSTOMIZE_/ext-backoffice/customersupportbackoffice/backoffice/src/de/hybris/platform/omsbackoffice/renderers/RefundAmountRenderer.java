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

import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.math.BigDecimal;

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


/**
 * This renderer renders the total refund amount for a returned order
 */
public class RefundAmountRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(RefundAmountRenderer.class);

	protected static final String REFUND_AMOUNT_OBSERVER_ID = "refundAmountObserver";
	protected static final String REFUND_ENTRY = "RefundEntry";
	protected static final String QUALIFIER = "amount";
	protected static final String CURRENT_OBJECT = "currentObject";

	private Editor editor;
	private TypeFacade typeFacade;
	private BigDecimal totalRefundAmount;
	private LabelService labelService;

	/**
	 * Renders the refund amount of the order into a textbox.
	 *
	 * @param component
	 * @param abstractPanelConfiguration
	 * 		as CustomPanel
	 * @param object
	 * 		as ReturnRequestModel
	 * @param dataType
	 * @param widgetInstanceManager
	 */
	@Override
	public void render(final Component component, final AbstractPanel abstractPanelConfiguration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (!(abstractPanelConfiguration instanceof CustomPanel && object instanceof ReturnRequestModel))
		{
			return;
		}
		
		totalRefundAmount = getOrderRefundAmount((ReturnRequestModel) object);

		try
		{
			final Attribute attribute = new Attribute();
			attribute.setLabel("customersupportbackoffice.returnentry.totalrefundamount");
			attribute.setQualifier(QUALIFIER);
			attribute.setReadonly(Boolean.TRUE);
			final DataType refundEntry = getTypeFacade().load(REFUND_ENTRY);
			final boolean canReadObject = getPermissionFacade().canReadInstanceProperty(refundEntry.getClazz(), QUALIFIER);

			if (!canReadObject)
			{
				final Div attributeContainer = new Div();
				attributeContainer.setSclass(SCLASS_EDITOR_CONTAINER);
				renderNotReadableLabel(attributeContainer, attribute, dataType, getLabelService().getAccessDeniedLabel(attribute));
				attributeContainer.setParent(component);
				return;
			}

			createAttributeRenderer().render(component, attribute, refundEntry.getClazz(), refundEntry, widgetInstanceManager);

			final WidgetModel widgetInstanceModel = widgetInstanceManager.getModel();
			widgetInstanceModel.removeObserver(REFUND_AMOUNT_OBSERVER_ID);
			widgetInstanceModel.addObserver(CURRENT_OBJECT, new ModelObserver()
			{
				@Override
				public void modelChanged()
				{
					if (!ReturnRequestModel.class.equals(widgetInstanceModel.getValueType(CURRENT_OBJECT)))
					{
						return;
					}
					final ReturnRequestModel currentReturnRequest = widgetInstanceModel.getValue(CURRENT_OBJECT,
							ReturnRequestModel.class);
					if (currentReturnRequest != null)
					{
						totalRefundAmount = getOrderRefundAmount(currentReturnRequest);
						editor.setInitialValue(totalRefundAmount);
					}
				}

				@Override
				public String getId()
				{
					return REFUND_AMOUNT_OBSERVER_ID;
				}
			});

		}
		catch (final TypeNotFoundException e)
		{
			if (LOG.isWarnEnabled())
			{
				LOG.warn(e.getMessage(), e);
			}
		}

	}

	/**
	 * Render method executes this overridden method
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

		String editorSClass = SCLASS_EDITOR;
		final boolean editable = !attribute.isReadonly() && canChangeProperty(genericAttribute, object);
		if (!editable)
		{
			editorSClass = SCLASS_READONLY_EDITOR;
		}

		final String qualifier = genericAttribute.getQualifier();
		final String referencedModelProperty = REFUND_ENTRY + "." + attribute.getQualifier();
		final Editor newEditor = createEditor(genericAttribute, widgetInstanceManager.getModel(), referencedModelProperty);
		newEditor.setReadOnly(!editable);
		newEditor.setLocalized(Boolean.FALSE);
		newEditor.setWidgetInstanceManager(widgetInstanceManager);
		newEditor.setType(resolveEditorType(genericAttribute));
		newEditor.setOptional(!genericAttribute.isMandatory());
		YTestTools.modifyYTestId(newEditor, "editor_" + REFUND_ENTRY + "." + qualifier);
		newEditor.setAttribute("parentObject", object);
		newEditor.setWritableLocales(getPermissionFacade().getWritableLocalesForInstance(object));
		newEditor.setReadableLocales(getPermissionFacade().getReadableLocalesForInstance(object));
		if (genericAttribute.isLocalized())
		{
			newEditor.addParameter(LocalizedEditor.EDITOR_PARAM_ATTRIBUTE_DESCRIPTION, getAttributeDescription(genericType, attribute));
		}
		newEditor.setProperty(referencedModelProperty);
		if (StringUtils.isNotBlank(attribute.getEditor()))
		{
			newEditor.setDefaultEditor(attribute.getEditor());
		}
		newEditor.setPartOf(genericAttribute.isPartOf());
		newEditor.setOrdered(genericAttribute.isOrdered());
		newEditor.afterCompose();
		newEditor.setSclass(editorSClass);
		newEditor.setInitialValue(totalRefundAmount);
		this.editor = newEditor;
		return newEditor;
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
	 * Sums up all the refund amount for all entries and then adds the delivery cost if it is to be refunded
	 *
	 * @param returnRequest
	 * 		the model containing the list of {@link ReturnEntryModel}
	 * @return the total refund amount
	 */
	protected BigDecimal getOrderRefundAmount(final ReturnRequestModel returnRequest)
	{
		BigDecimal refundAmount = returnRequest.getReturnEntries().stream().map(returnEntry -> getRefundEntryAmount(returnEntry))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (returnRequest.getRefundDeliveryCost() != null && returnRequest.getRefundDeliveryCost().booleanValue())
		{
			refundAmount = refundAmount.add(BigDecimal.valueOf(returnRequest.getOrder().getDeliveryCost().doubleValue()));
		}

		return refundAmount.setScale(returnRequest.getOrder().getCurrency().getDigits().intValue(), BigDecimal.ROUND_FLOOR);
	}

	/**
	 * Calculates the refund amount for this entry considering the number which was returned
	 *
	 * @param returnEntryModel
	 * @return the refund amount for a single entry
	 */
	protected BigDecimal getRefundEntryAmount(final ReturnEntryModel returnEntryModel)
	{
		final ReturnRequestModel returnRequest = returnEntryModel.getReturnRequest();
		BigDecimal refundEntryAmount = BigDecimal.ZERO;

		if (returnEntryModel instanceof RefundEntryModel)
		{
			final RefundEntryModel refundEntry = (RefundEntryModel) returnEntryModel;

			if (refundEntry.getAmount() != null)
			{
				refundEntryAmount = refundEntry.getAmount();
				refundEntryAmount = refundEntryAmount
						.setScale(returnRequest.getOrder().getCurrency().getDigits().intValue(), BigDecimal.ROUND_HALF_DOWN);
			}
		}
		return refundEntryAmount;
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
