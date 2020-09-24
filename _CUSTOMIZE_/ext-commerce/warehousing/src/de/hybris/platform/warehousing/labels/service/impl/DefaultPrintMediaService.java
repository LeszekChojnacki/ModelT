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
package de.hybris.platform.warehousing.labels.service.impl;

import de.hybris.platform.acceleratorservices.document.service.DocumentGenerationService;
import de.hybris.platform.commerceservices.impersonation.ImpersonationContext;
import de.hybris.platform.commerceservices.impersonation.ImpersonationService;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.labels.service.PrintMediaService;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


/**
 * Default implementation of {@link PrintMediaService}
 */
public class DefaultPrintMediaService implements PrintMediaService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPrintMediaService.class);

	public static final String DOCUMENT_BODY_ENCODING = "UTF-8";

	private ImpersonationService impersonationService;
	private DocumentGenerationService documentGenerationService;
	private MediaService mediaService;
	private ModelService modelService;

	@Override
	public MediaModel getMediaForTemplate(final String frontendTemplateName, final BusinessProcessModel businessProcessModel)
	{
		validateParameterNotNullStandardMessage("frontendTemplateName", frontendTemplateName);
		validateParameterNotNullStandardMessage("businessProcessModel", businessProcessModel);

		LOG.info("Generating media for template: [{}] and item associated with business process: [{}]", frontendTemplateName,
				businessProcessModel.getClass().getSimpleName());

		final OrderModel order = getOrder(businessProcessModel);
		final ImpersonationContext context = new ImpersonationContext();
		context.setOrder(order);
		context.setSite(order.getSite());
		context.setUser(order.getUser());
		context.setCatalogVersions(Collections.emptyList());
		return getImpersonationService()
				.executeInContext(context, () -> getDocumentGenerationService().generate(frontendTemplateName, businessProcessModel));
	}

	/**
	 * Generates the script of the popup for the given {@link MediaModel}
	 *
	 * @param mediaModel
	 * 		the {@link MediaModel} for which popup script needs to be generated
	 * @param width
	 * 		the width of the popup
	 * @param height
	 * 		the height of the popup
	 * @param blockedPopupMessage
	 * 		the localised message if the popups are blocked
	 * @return the script of the popup
	 */
	@Override
	public String generatePopupScriptForMedia(final MediaModel mediaModel, final String width, final String height,
			final String blockedPopupMessage)
	{
		validateParameterNotNullStandardMessage("width", width);
		validateParameterNotNullStandardMessage("height", height);
		validateParameterNotNullStandardMessage("blockedPopupMessage", blockedPopupMessage);

		final String formattedHTML = StringEscapeUtils.escapeEcmaScript(generateHtmlMediaTemplate(mediaModel));

		return "var myWindow = window.open('','','width=" + width + ",height=" + height + ",scrollbars=yes'); try { "
				+ "myWindow.document.write(\"" + formattedHTML + "\") } catch (e) { alert(\"" + blockedPopupMessage + "\") }";
	}

	@Override
	public String generateHtmlMediaTemplate(final MediaModel mediaModel)
	{
		validateParameterNotNullStandardMessage("mediaModel", mediaModel);

		String htmlTemplate;
		try
		{
			htmlTemplate = new String(getMediaService().getDataFromMedia(mediaModel), DOCUMENT_BODY_ENCODING);
		}
		catch (final UnsupportedEncodingException e)//NOSONAR
		{
			htmlTemplate = new String(getMediaService().getDataFromMedia(mediaModel));
			LOG.warn("document content - UnsupportedEncodingException");
		}

		return htmlTemplate;
	}

	/**
	 * Extracts {@link OrderModel} from the business process
	 *
	 * @param businessProcessModel
	 * 		the {@link BusinessProcessModel} from which {@link OrderModel} needs to be extracted
	 * @return the {@link OrderModel}
	 */
	protected OrderModel getOrder(final BusinessProcessModel businessProcessModel)
	{
		validateParameterNotNullStandardMessage("businessProcessModel", businessProcessModel);

		if (businessProcessModel instanceof OrderProcessModel)
		{
			return ((OrderProcessModel) businessProcessModel).getOrder();
		}

		else if (businessProcessModel instanceof ConsignmentProcessModel)
		{
			return (OrderModel) ((ConsignmentProcessModel) businessProcessModel).getConsignment().getOrder();
		}

		else if (businessProcessModel instanceof ReturnProcessModel)
		{
			return ((ReturnProcessModel) businessProcessModel).getReturnRequest().getOrder();
		}
		else
		{
			if (businessProcessModel.getContextParameters().iterator().hasNext())
			{
				final BusinessProcessParameterModel param = businessProcessModel.getContextParameters().iterator().next();
				final List<ConsignmentModel> consignmentList = (List<ConsignmentModel>) param.getValue();

				if (CollectionUtils.isNotEmpty(consignmentList))
				{
					return (OrderModel) consignmentList.iterator().next().getOrder();
				}
			}
		}

		LOG.info("Unsupported BusinessProcess type [{}] for item [{}]", businessProcessModel.getClass().getSimpleName(),
				businessProcessModel);

		return null;
	}

	protected ImpersonationService getImpersonationService()
	{
		return impersonationService;
	}

	@Required
	public void setImpersonationService(final ImpersonationService impersonationService)
	{
		this.impersonationService = impersonationService;
	}

	protected DocumentGenerationService getDocumentGenerationService()
	{
		return documentGenerationService;
	}

	@Required
	public void setDocumentGenerationService(final DocumentGenerationService documentGenerationService)
	{
		this.documentGenerationService = documentGenerationService;
	}

	protected MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
