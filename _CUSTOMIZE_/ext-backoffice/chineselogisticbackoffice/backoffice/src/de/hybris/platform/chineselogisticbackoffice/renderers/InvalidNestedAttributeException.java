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
package de.hybris.platform.chineselogisticbackoffice.renderers;

/**
 * Thrown when a nested property to be rendered has a null parent
 */
public class InvalidNestedAttributeException extends Exception
{
	private static final long serialVersionUID = 9219802332908133366L;

	public InvalidNestedAttributeException(final String message)
   {
      super(message);
   }
}
