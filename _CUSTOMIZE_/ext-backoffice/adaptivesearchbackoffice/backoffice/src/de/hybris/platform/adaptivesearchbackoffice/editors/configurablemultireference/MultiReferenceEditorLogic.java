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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference;

import de.hybris.platform.adaptivesearchbackoffice.data.AbstractEditorData;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorLogic;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRenderer;

import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModel;

import com.hybris.cockpitng.components.Editor;


/**
 * Logic for multi reference editor.
 */
public interface MultiReferenceEditorLogic<D extends AbstractEditorData, V> extends EditorLogic<Collection<V>>
{
	/**
	 * Returns the editor context.
	 *
	 * @return the editor context
	 */
	String getContext();

	/**
	 * Returns whether the editor items can be sorted.
	 *
	 * @return <code>true</code> if the editor items can be sorted, <code>false</code> otherwise
	 */
	boolean isSortable();

	/**
	 * Returns the columns.
	 *
	 * @return the columns
	 */
	Collection<String> getColumns();

	/**
	 * Returns the editable columns.
	 *
	 * @return the editable columns
	 */
	Collection<String> getEditableColumns();

	/**
	 * Returns the data handler.
	 *
	 * @return the data handler
	 */
	DataHandler<D, V> getDataHandler();

	/**
	 * Returns the list model.
	 *
	 * @return the list model
	 */
	ListModel<D> getListModel();

	/**
	 * Returns the renderer for the item master section.
	 *
	 * @return the renderer for the item master section
	 */
	EditorRenderer getItemMasterRenderer();

	/**
	 * Returns the renderer for the item detail section.
	 *
	 * @return the renderer for the item detail section
	 */
	EditorRenderer getItemDetailRenderer();

	/**
	 * Returns whether the given component is open. This can be used, for example, to check if an editor item is open.
	 *
	 * @param component
	 *           - the component
	 *
	 * @return <code>true</code> if the component is open, <code>false</code> otherwise
	 */
	boolean isOpen(Component component);

	/**
	 * Changes the open status of a component.
	 *
	 * @param component
	 *           - the component
	 * @param open
	 *           - the new open status
	 */
	void setOpen(Component component, boolean open);

	/**
	 * Finds the nearest editor component.
	 *
	 * @return the nearest editor component or <code>null</code> if it could not be found
	 */
	Editor findEditor(Component component);

	/**
	 * Finds the nearest editor item component.
	 *
	 * @return the nearest editor item component or <code>null</code> if it could not be found
	 */
	Component findEditorItem(Component component);

	/**
	 * Updates the value of the editor.
	 *
	 * @param value
	 *           - the value
	 */
	void updateValue(Collection<V> value);

	/**
	 * Updates the value of an item attribute.
	 *
	 * @param data
	 *           - the item data
	 * @param attributeName
	 *           - the attribute name
	 * @param attributeValue
	 *           - the attribute value
	 */
	void updateAttributeValue(D data, String attributeName, Object attributeValue);

	/**
	 * Triggers the creation of a new item.
	 */
	void triggerCreateReference();

	/**
	 * Triggers the update of an existing item.
	 *
	 * @param data
	 *           - the item data
	 */
	void triggerUpdateReference(final D data);
}
