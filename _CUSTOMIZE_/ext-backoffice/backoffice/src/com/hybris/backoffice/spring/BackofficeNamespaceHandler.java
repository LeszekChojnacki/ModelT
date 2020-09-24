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
package com.hybris.backoffice.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


/**
 * Defines parsers for possible elements in CockpitNG Spring namespace
 */
public class BackofficeNamespaceHandler extends NamespaceHandlerSupport
{

	@Override
	public void init()
	{
		registerBeanDefinitionParser(BeansDefinitionImportParser.ELEMENT_NAME, new BeansDefinitionImportParser());
	}

}
