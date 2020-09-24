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
package com.hybris.backoffice.variants;

import de.hybris.platform.product.VariantsService;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Locale;
import java.util.Map;


/**
 * Extends {@link VariantsService} with additional useful methods.
 */
public interface BackofficeVariantsService extends VariantsService
{
	/**
	 * Gets values for all Locales, for given localized variant attribute.
	 */
	Map<Locale, Object> getLocalizedVariantAttributeValue(VariantProductModel variant, String qualifier);

	/**
	 * Sets values for all Locales, for given localized variant attribute.
	 */
	void setLocalizedVariantAttributeValue(VariantProductModel variantProductModel, String qualifier,
			Map<Locale, Object> localizedValues);
}
