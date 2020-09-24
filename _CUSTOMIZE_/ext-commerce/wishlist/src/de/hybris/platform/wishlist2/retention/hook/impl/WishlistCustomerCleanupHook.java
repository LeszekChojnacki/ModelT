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
package de.hybris.platform.wishlist2.retention.hook.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.directpersistence.audit.dao.WriteAuditRecordsDAO;
import de.hybris.platform.retention.hook.ItemCleanupHook;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.wishlist2.model.Wishlist2Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * This Hook removes customer related objects in wishlist extension.
 */
public class WishlistCustomerCleanupHook implements ItemCleanupHook<CustomerModel>
{
	private static final Logger LOG = LoggerFactory.getLogger(WishlistCustomerCleanupHook.class);

	private ModelService modelService;
	private WriteAuditRecordsDAO writeAuditRecordsDAO;

	@Override
	public void cleanupRelatedObjects(final CustomerModel customerModel)
	{
		validateParameterNotNullStandardMessage("customerModel", customerModel);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Cleaning up customer wishlist related objects for: {}", customerModel);
		}

		// Remove customer wishlist and its audit records
		for (final Wishlist2Model wishlist : customerModel.getWishlist())
		{
			getModelService().remove(wishlist);
			getWriteAuditRecordsDAO().removeAuditRecordsForType(Wishlist2Model._TYPECODE, wishlist.getPk());
		}
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

	protected WriteAuditRecordsDAO getWriteAuditRecordsDAO()
	{
		return writeAuditRecordsDAO;
	}

	@Required
	public void setWriteAuditRecordsDAO(final WriteAuditRecordsDAO writeAuditRecordsDAO)
	{
		this.writeAuditRecordsDAO = writeAuditRecordsDAO;
	}
}
