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
package de.hybris.platform.commerceservices.backoffice.widgets.organization.create.orgunit.employee;

import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitService;
import de.hybris.platform.commerceservices.organization.utils.OrgUtils;
import de.hybris.platform.commerceservices.util.CommerceSearchUtils;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.core.impl.NotificationStack;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Default handler to create Organization Unit Employee. Implementation of {@link FlowActionHandler}
 */
public class DefaultCreateOrgUnitEmployeeWizardHandler implements FlowActionHandler
{
	private static final Logger LOG = Logger.getLogger(DefaultCreateOrgUnitEmployeeWizardHandler.class);

	private NotificationStack notificationStack;
	private OrgUnitService orgUnitService;
	private ModelService modelService;
	private NotificationService notificationService;

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		try
		{
			// create employee
			final EmployeeModel newOrgUnitEmployee = adapter.getWidgetInstanceManager().getModel().getValue("neworgunitempl",
					EmployeeModel.class);
			final Set<PrincipalGroupModel> orgRoles = adapter.getWidgetInstanceManager().getModel().getValue("orgRoles", Set.class);
			final Set<OrgUnitModel> orgUnits = adapter.getWidgetInstanceManager().getModel().getValue("orgUnits", Set.class);
			newOrgUnitEmployee.setGroups(orgRoles);
			getModelService().save(newOrgUnitEmployee);

			// add employee to org unit
			for (final OrgUnitModel orgUnitModel : orgUnits)
			{
				getOrgUnitService().addMembers(
						OrgUtils.createOrgUnitMemberParameter(orgUnitModel.getUid(), Collections.singleton(newOrgUnitEmployee),
								EmployeeModel.class, CommerceSearchUtils.getAllOnOnePagePageableData()));
			}
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Error for creating org unit employee.", e);
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
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
