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
package de.hybris.platform.commerceservices.backoffice.widgets.organization.create.salesunit;

import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitParameter;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.impl.NotificationStack;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowController;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Organization Sales Unit implementation of {@link FlowActionHandler}
 */
public class CreateSalesUnitWizardHandler implements FlowActionHandler
{
	private static final Logger LOG = Logger.getLogger(CreateSalesUnitWizardHandler.class);

	private NotificationStack notificationStack;
	private OrgUnitService orgUnitService;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		try
		{
			final OrgUnitParameter param = new OrgUnitParameter();
			final OrgUnitModel newSalesUnit = adapter.getWidgetInstanceManager().getModel().getValue("newsalesunit",
					OrgUnitModel.class);
			final OrgUnitModel parentUnit = adapter.getWidgetInstanceManager().getModel().getValue("parentUnit", OrgUnitModel.class);
			param.setUid(newSalesUnit.getUid());
			param.setName(newSalesUnit.getName());
			param.setActive(newSalesUnit.getActive());
			param.setParentUnit(parentUnit);
			param.setDescription(newSalesUnit.getDescription());
			param.setLineOfBusiness(newSalesUnit.getLineOfBuisness());
			param.setSupplier(Boolean.TRUE);
			getOrgUnitService().createUnit(param);
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Error for creating sales unit.", e);
			}

			final ConfigurableFlowController controller = ((ConfigurableFlowController) adapter.getWidgetInstanceManager()
					.getWidgetslot().getAttribute("widgetController"));

			if ("step2".equals(controller.getCurrentStep().getId()))
			{
				adapter.back();
			}

			if (e instanceof ModelSavingException)
			{
				ModelSavingException ex = (ModelSavingException) e;
				while (ex != null)
				{
					final Throwable ie = ex.getCause();

					getNotificationService().notifyUser(ie.getMessage(), NotificationEventTypes.EVENT_TYPE_OBJECT_CREATION,
							NotificationEvent.Level.FAILURE, ex);
					ex = ex.getNextException();
				}
			}
			else
			{
				getNotificationService().notifyUser(e.getMessage(), NotificationEventTypes.EVENT_TYPE_OBJECT_CREATION,
						NotificationEvent.Level.FAILURE, e);
			}

			return;
		}
		adapter.done();
	}


	protected NotificationStack getNotificationStack()
	{
		return notificationStack;
	}

	@Required
	public void setNotificationStack(final NotificationStack notificationStack)
	{
		this.notificationStack = notificationStack;
	}

	protected OrgUnitService getOrgUnitService()
	{
		return orgUnitService;
	}

	@Required
	public void setOrgUnitService(final OrgUnitService orgUnitService)
	{
		this.orgUnitService = orgUnitService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
