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
package de.hybris.platform.b2bcommerce.backoffice.editor;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.backoffice.editor.OrgUnitLogicHandler;
import de.hybris.platform.commerceservices.model.OrgUnitModel;
import de.hybris.platform.commerceservices.organization.services.OrgUnitMemberParameter;
import de.hybris.platform.commerceservices.organization.utils.OrgUtils;
import de.hybris.platform.commerceservices.util.CommerceSearchUtils;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


/**
 * B2B Unit extension of {@link OrgUnitLogicHandler}
 */
public class OrgUnitCustomersLogicHandler extends OrgUnitLogicHandler
{
	private static final Logger LOG = Logger.getLogger(OrgUnitCustomersLogicHandler.class);

	@Override
	public void beforeEditorAreaRender(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
	{
		super.beforeEditorAreaRender(widgetInstanceManager, currentObject);

		final OrgUnitMemberParameter<B2BUnitModel> param = OrgUtils.createOrgUnitMemberParameter(
				((OrgUnitModel) currentObject).getUid(), null, B2BUnitModel.class, CommerceSearchUtils.getAllOnOnePagePageableData());

		// Assign customers to model
		widgetInstanceManager.getModel().setValue("customersChanged", getOrgUnitService().getMembers(param).getResults());
	}

	@Override
	protected void handleSaveMembers(final WidgetInstanceManager widgetInstanceManager, final OrgUnitModel unit)
			throws ObjectSavingException
	{
		try
		{
			super.handleSaveMembers(widgetInstanceManager, unit);

			// Get original customers
			final OrgUnitMemberParameter<B2BUnitModel> param = OrgUtils.createOrgUnitMemberParameter(unit.getUid(), null,
					B2BUnitModel.class, CommerceSearchUtils.getAllOnOnePagePageableData());
			final List<B2BUnitModel> customersToRemove = getOrgUnitService().getMembers(param).getResults();

			// Remove original customers
			if (CollectionUtils.isNotEmpty(customersToRemove))
			{
				param.setMembers(new HashSet<B2BUnitModel>(customersToRemove));
				getOrgUnitService().removeMembers(param);
			}

			// Add new customers
			final List<B2BUnitModel> customersChanged = widgetInstanceManager.getModel().getValue("customersChanged", List.class);
			if (CollectionUtils.isNotEmpty(customersChanged))
			{
				param.setMembers(new HashSet<B2BUnitModel>(customersChanged));
				getOrgUnitService().addMembers(param);
			}
		}
		catch (final Exception e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getMessage(), e);
			}
			if (e instanceof ObjectSavingException)
			{
				throw e;
			}
			throw new ObjectSavingException(unit.getUid(), e.getMessage(), e);
		}
	}
}
