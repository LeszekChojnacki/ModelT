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
package de.hybris.platform.security;

/**
 * The interface providing functionality to eliminate Cross-site scripting (XSS) vulnerabilities.
 */
public interface XssEncodeService
{
	/**
	 * Encodes input HTML to be XSS-safe code
	 *
	 * @param input
	 *           untrusted HTML-string to clear it
	 * @return a safe (w/o potential XSS vulnerabilities) representation of the HTML
	 */
	String encodeHtml(String input);
}
