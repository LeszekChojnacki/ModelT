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
package de.hybris.platform.ordercancel;

import java.io.Serializable;


/**
 * Marker interface used for objects that represents Order Cancel denial reasons. These denial reasons are used to check
 * exact reasons of why the method
 * {@link OrderCancelService#isCancelPossible(de.hybris.platform.core.model.order.OrderModel, de.hybris.platform.core.model.security.PrincipalModel, boolean, boolean)}
 * resulted in the cancel denial. Users can use provided DefaultOrderCancelDenialReason class as an implementation for
 * this interface, or use their own denial reason class.
 *
 * How is OrderCancelDenialReason used:
 *
 * <ul>
 * <li>Create a denial reason representation class. This class must implement OrderCancelDenialReason interface. You can
 * also use class {@link DefaultOrderCancelDenialReason} that is provided by default.</li>
 *
 * <li>Define a unique instance of denial reason representation class for each
 * {@link de.hybris.platform.ordercancel.OrderCancelDenialStrategy} that is defined for the Order Cancel Service. The
 * standard way to do this is via spring xml configuration file. Configured instance should contain an
 * error-code/message/other-data that is specific for the denial strategy. If the denial strategy fires (cancel is
 * denied by the strategy), configured denial reason class instance is returned.</li>
 *
 * <li>use
 * {@link OrderCancelService#isCancelPossible(de.hybris.platform.core.model.order.OrderModel, de.hybris.platform.core.model.security.PrincipalModel, boolean, boolean)}
 * method to determine possibility of the cancel. This method returns a {@link CancelDecision} instance. If cancel is
 * denied, denial reasons can be acquired by calling {@link CancelDecision#getDenialReasons()} method. Every element of
 * the returned list is the denial reason class instance that was configured for cancel-denying
 * OrderCancelDenialStrategy.</li>
 * </ul>
 *
 * Example configuration to put into spring xml configuration file:
 *
 * <pre>
 * {@code
 * <bean id="someDenialStrategy" class="..." scope="...">
 * 
 *     ...
 *
 * 	<property name="reason">
 * 		<bean class="de.hybris.platform.ordercancel.DefaultOrderCancelDenialReason" >
 * 			<property name="code" value="2" />
 * 			<property name="description" value="aMessage" />
 * 		</bean>
 * 	</property>
 * 
 * </bean>
 * }
 * </pre>
 */
public interface OrderCancelDenialReason extends Serializable
{
	//no methods here on purpose
}
