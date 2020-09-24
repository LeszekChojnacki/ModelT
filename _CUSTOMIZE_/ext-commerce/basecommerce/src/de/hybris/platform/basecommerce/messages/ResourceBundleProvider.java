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
package de.hybris.platform.basecommerce.messages;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Localized bundle provider mechanism for loading messages from [resourceBundle}[de].properties for each extension
 * separately.
 * 
 * Sample:
 * 
 * <pre>
 * <bean id="basecommerce.resourceBundleProvider"
 * 	class="de.hybris.platform.basecommerce.messages.impl.DefaultResourceBundleProvider" scope="tenant">
 * 	<!-- hybris specific resource bundles: [extension]/resources/localization/[extension]/[resourceBundle]. -->
 * 	<property name="resourceBundle" value="BasecommerceMessages"/>
 * </bean>
 * 
 * </pre>
 * 
 * @spring.bean basecommerce.resourceBundleProvider
 */
public interface ResourceBundleProvider
{
	ResourceBundle getResourceBundle(final Locale locale);
}
