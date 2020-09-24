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
package com.hybris.backoffice.cockpitng.dnd;

import java.util.List;

import org.zkoss.zk.ui.HtmlBasedComponent;

import com.hybris.cockpitng.core.context.CockpitContext;


/**
 * Allows to consume drop items by a widget. It can be used as business object for
 * {@link com.hybris.cockpitng.dnd.DragAndDropStrategy#makeDroppable(HtmlBasedComponent, Object, CockpitContext)}
 */
public interface DropConsumer<T>
{
	void itemsDropped(List<T> droppedItems);
}
