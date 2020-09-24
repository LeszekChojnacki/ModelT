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
package de.hybris.platform.warehousing.asn.service.impl;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.warehousing.asn.service.AsnWorkflowService;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.WorkflowTemplateService;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;
import static java.lang.String.format;


/**
 * Default implementation of {@link AsnWorkflowService}
 */
public class DefaultAsnWorkflowService implements AsnWorkflowService
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsnWorkflowService.class);
	protected static final String CONSIGNMENT_TEMPLATE_NAME = "warehousing.asn.workflow.template";
	protected static final String WORKFLOW_OF_ASN = "asn_workflow_";

	private ModelService modelService;
	private WorkflowService workflowService;
	private WorkflowTemplateService workflowTemplateService;
	private WorkflowProcessingService workflowProcessingService;
	private UserService userService;
	private ConfigurationService configurationService;

	@Override
	public void startAsnCancellationWorkflow(final AdvancedShippingNoticeModel advancedShippingNotice)
	{
		final String asnWorkflowName = getConfigurationService().getConfiguration().getString(CONSIGNMENT_TEMPLATE_NAME);

		try
		{
			final WorkflowTemplateModel workflowTemplate = getWorkflowTemplateService().getWorkflowTemplateForCode(asnWorkflowName);
			if (workflowTemplate != null)
			{
				final WorkflowModel workflow = getWorkflowService()
						.createWorkflow(WORKFLOW_OF_ASN + advancedShippingNotice.getInternalId(), workflowTemplate,
								Collections.singletonList(advancedShippingNotice), getUserService().getAdminUser());
				getModelService().save(workflow);

				workflow.getActions().forEach(action -> getModelService().save(action));
				getWorkflowProcessingService().startWorkflow(workflow);
				workflow.setOwner(getUserService().getAdminUser());
				workflow.getActions().forEach(action ->
				{
					action.setPrincipalAssigned(getUserService().getAdminUser());
					getModelService().save(action);
				});
				getModelService().save(workflow);
			}
			else
			{
				LOGGER.debug(getLocalizedString("warehousing.asncancellation.workflow.no.template.found"));
			}
		}
		catch (final UnknownIdentifierException | IllegalArgumentException e)  //NOSONAR
		{
			LOGGER.debug(
					format(getLocalizedString("warehousing.asncancellation.workflow.no.template.found.for.code"), asnWorkflowName));
		}
		catch (final ModelSavingException e)  //NOSONAR
		{
			LOGGER.debug(
					format(getLocalizedString("warehousing.asncancellation.workflow.error"), advancedShippingNotice.getInternalId()));
		}
	}


	protected WorkflowService getWorkflowService()
	{
		return workflowService;
	}

	@Required
	public void setWorkflowService(final WorkflowService workflowService)
	{
		this.workflowService = workflowService;
	}

	protected WorkflowTemplateService getWorkflowTemplateService()
	{
		return workflowTemplateService;
	}

	@Required
	public void setWorkflowTemplateService(final WorkflowTemplateService workflowTemplateService)
	{
		this.workflowTemplateService = workflowTemplateService;
	}

	protected WorkflowProcessingService getWorkflowProcessingService()
	{
		return workflowProcessingService;
	}

	@Required
	public void setWorkflowProcessingService(final WorkflowProcessingService workflowProcessingService)
	{
		this.workflowProcessingService = workflowProcessingService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
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

