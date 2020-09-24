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
package de.hybris.platform.b2b.jalo;

import de.hybris.platform.b2b.constants.B2BApprovalConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;

import org.apache.log4j.Logger;


public class B2BApprovalprocessManager extends GeneratedB2BApprovalprocessManager
{
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(B2BApprovalprocessManager.class.getName());

	public static final B2BApprovalprocessManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (B2BApprovalprocessManager) em.getExtension(B2BApprovalConstants.EXTENSIONNAME);
	}
}
