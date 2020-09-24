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
package de.hybris.platform.basecommerce.strategies;

import de.hybris.platform.basecommerce.exceptions.BaseSiteActivationException;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;


/**
 * Strategy to abstract a session injection from possibly content related logic.
 * 
 * @since 4.5
 * @spring.bean activateBaseSiteInSessionStrategy
 */
public interface ActivateBaseSiteInSessionStrategy<T extends BaseSiteModel>
{
	/**
	 * Adjusts a session state based on the given site information. Depends of the context basecommerce/cms wher used a
	 * specific data is being adjusted. If activation fails for some reason a {@link BaseSiteActivationException} is
	 * thrown.
	 * @throws BaseSiteActivationException
	 */
	void activate(T site);
}
