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

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.List;
import java.util.Set;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.CustomPanel;
import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Parameter;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.editorarea.renderer.impl.DefaultEditorAreaPanelRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * This renderer renders the checkboxes which will either apply or remove the delivery cost to a {@link de.hybris.platform.returns.model.ReturnRequestModel}
 */
public class RefundDeliveryCostRenderer extends DefaultEditorAreaPanelRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(RefundDeliveryCostRenderer.class);

	private static final String RETURN_REQUEST = "ReturnRequest";
	private static final String QUALIFIER = "refundDeliveryCost";
	private static final String OPTIONAL_FIELD_NAME = "showOptionalField";
	private static final String OPTIONA_FIELD_VALUE = "false";

	private TypeFacade typeFacade;
	private ReturnService returnService;
	private Set<ReturnStatus> invalidReturnStatusForRefundDeliveryCost;
	private LabelService labelService;

	/**
	 * Renders the radiobuttons which will apply or remove the delivery cost
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
		if (abstractPanelConfiguration instanceof CustomPanel && object instanceof ReturnRequestModel)
		{
			try
			{
				final ReturnRequestModel requestModel = (ReturnRequestModel) object;
				final Attribute attribute = new Attribute();
				attribute.setQualifier(QUALIFIER);
				// set to Readonly only if the delivery cost hasn't been refunded yet
				attribute.setReadonly(!isDeliveryCostRefundable(requestModel.getOrder().getCode(), requestModel.getRMA()));

				final Parameter optionalParameter = new Parameter();
				optionalParameter.setName(OPTIONAL_FIELD_NAME);
				optionalParameter.setValue(OPTIONA_FIELD_VALUE);
				attribute.getEditorParameter().add(optionalParameter);
				final DataType returnRequest = getTypeFacade().load(RETURN_REQUEST);
				final boolean canReadObject = getPermissionFacade().canReadInstanceProperty(returnRequest.getClazz(), QUALIFIER);

				if (canReadObject)
				{
					createAttributeRenderer()
							.render(component, attribute, returnRequest.getClazz(), returnRequest, widgetInstanceManager);
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
	 * Verifies if the delivery cost is refundable for the given order and return request.
	 *
	 * @param orderCode
	 * 		order code
	 * @param returnRequestRMA
	 * 		the return request RMA
	 * @return true if the delivery cost can be refunded
	 */
	protected boolean isDeliveryCostRefundable(final String orderCode, final String returnRequestRMA)
	{
		validateParameterNotNullStandardMessage("orderCode", orderCode);
		validateParameterNotNullStandardMessage("returnRequestRMA", returnRequestRMA);

		final List<ReturnRequestModel> previousReturns = getReturnService().getReturnRequests(orderCode);
		final boolean isDeliveryCostAlreadyRefunded = previousReturns.stream()
				.filter(previousReturn -> !getInvalidReturnStatusForRefundDeliveryCost().contains(previousReturn.getStatus())).
						anyMatch(previousReturn -> !returnRequestRMA.equals(previousReturn.getRMA()) && (
								previousReturn.getRefundDeliveryCost() != null && previousReturn.getRefundDeliveryCost()));
		return !isDeliveryCostAlreadyRefunded;
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

	protected ReturnService getReturnService()
	{
		return returnService;
	}

	@Required
	public void setReturnService(final ReturnService returnService)
	{
		this.returnService = returnService;
	}

	protected Set<ReturnStatus> getInvalidReturnStatusForRefundDeliveryCost()
	{
		return invalidReturnStatusForRefundDeliveryCost;
	}

	@Required
	public void setInvalidReturnStatusForRefundDeliveryCost(final Set<ReturnStatus> invalidReturnStatusForRefundDeliveryCost)
	{
		this.invalidReturnStatusForRefundDeliveryCost = invalidReturnStatusForRefundDeliveryCost;
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
