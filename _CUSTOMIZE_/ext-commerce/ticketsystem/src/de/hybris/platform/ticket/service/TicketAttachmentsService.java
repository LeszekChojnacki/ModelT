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
package de.hybris.platform.ticket.service;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;


/**
 * Service provides functionality for creation {@link MediaModel} attachments with restrictions
 */
public interface TicketAttachmentsService
{
	/**
	 * Create {@link MediaModel} attachment based on provided parameters.
	 * <p>
	 * Created file will be stored in DB inside secure media folder, configured via Spring. Read permission will be
	 * granted only for <i>customer</i> and common agent group also configured via Spring.
	 *
	 * @param name
	 *           filename
	 * @param contentType
	 *           content type
	 * @param data
	 *           binary data
	 * @param customer
	 *           customer
	 * @return created, configured and stored {@link MediaModel}
	 */
	MediaModel createAttachment(final String name, final String contentType, final byte[] data, final UserModel customer);
}