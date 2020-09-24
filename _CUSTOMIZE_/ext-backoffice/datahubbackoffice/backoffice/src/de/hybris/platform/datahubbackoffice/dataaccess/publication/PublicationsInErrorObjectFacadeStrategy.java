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
 */
package de.hybris.platform.datahubbackoffice.dataaccess.publication;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacadeStrategy;
import com.hybris.datahub.dto.publication.TargetSystemPublicationData;

public class PublicationsInErrorObjectFacadeStrategy implements ObjectFacadeStrategy
{
	@Override
	public <T> boolean canHandle(final T objectOrCode)
	{
		return StringUtils.equals(ObjectUtils.toString(objectOrCode), PublicationConstants.PUBLICATIONS_IN_ERROR_TYPE_CODE)
				|| objectOrCode instanceof TargetSystemPublicationData;
	}

	@Override
	public <T> T load(final String uniqueId, final Context context)
	{
		return null;
	}

	@Override
	public <T> void delete(final T instnace, final Context context)
	{
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	@Override
	public <T> T create(final String uniqueId, final Context context)
	{
		return null;
	}

	@Override
	public <T> T save(final T instance, final Context context)
	{
		return instance;
	}

	@Override
	public <T> T reload(final T instance, final Context context)
	{
		return instance;
	}
}
