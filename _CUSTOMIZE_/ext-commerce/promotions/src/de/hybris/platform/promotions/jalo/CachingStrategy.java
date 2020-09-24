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
package de.hybris.platform.promotions.jalo;

import java.util.List;


/**
 * Created with IntelliJ IDEA. User: gary Date: 24/06/2013 Time: 12:52 To change this template use File | Settings |
 * File Templates.
 */
public interface CachingStrategy
{

	void put(String code, List<PromotionResult> results);

	List<PromotionResult> get(String code);

	void remove(String code);
}
