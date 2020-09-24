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
package de.hybris.platform.payment.strategy;




/**
 * This interface should be used to provide the merchant transaction code.
 */
public interface TransactionCodeGenerator
{
	/**
	 * code generation
	 * 
	 * @param base
	 *           the leading part of the generated code
	 * @return the merchant transaction code
	 */
	String generateCode(String base);

}
