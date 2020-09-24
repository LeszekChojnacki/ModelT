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
package de.hybris.platform.promotionenginebackoffice.constants;

@SuppressWarnings("PMD")
public class PromotionEngineBackofficeConstants extends GeneratedPromotionEngineBackofficeConstants // NOSONAR
{
	private PromotionEngineBackofficeConstants()
	{
		//empty
	}

	// implement here constants used by this extension
	public interface NotificationSource
	{
		/**
		 * @deprecated Since 6.5 unused
		 */
		@Deprecated
		interface CreateFromTemlate // NOSONAR
		{
			String MESSAGE_SOURCE = EXTENSIONNAME + "-createFromTemplate";
			/**
			 * @deprecated Since 6.5 unused
			 */
			interface EventType // NOSONAR
			{
				String CREATE = "Create";
			}
		}
	}
}
