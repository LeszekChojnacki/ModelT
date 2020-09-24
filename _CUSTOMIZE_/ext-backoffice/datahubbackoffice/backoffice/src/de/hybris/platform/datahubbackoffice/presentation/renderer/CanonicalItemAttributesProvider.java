/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

package de.hybris.platform.datahubbackoffice.presentation.renderer;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.Attribute;
import com.hybris.datahub.dto.item.ItemData;

import java.util.List;

/**
 * An abstraction of the process of retrieving attributes for a canonical item.
 */
public interface CanonicalItemAttributesProvider
{
	/**
	 * Retrieves attributes of the given item.
	 * @param item a canonical item, whose attribute values need to be retrieved.
	 * @return a list of attributes or an empty list, if the item does not have attributes.
	 */
	List<Attribute> getAttributes(ItemData item);
}
