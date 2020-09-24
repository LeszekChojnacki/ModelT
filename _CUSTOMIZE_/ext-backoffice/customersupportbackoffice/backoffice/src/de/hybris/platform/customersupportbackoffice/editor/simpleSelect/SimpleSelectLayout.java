/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.editor.simpleSelect;

import com.hybris.cockpitng.core.config.impl.jaxb.hybris.Base;
import com.hybris.cockpitng.editor.commonreferenceeditor.AbstractReferenceEditor;
import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorLayout;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.util.YTestTools;
import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.ListitemRenderer;

public class SimpleSelectLayout<T, K> extends ReferenceEditorLayout<T>
{
    protected static final String CSS_REFERENCE_EDITOR_REMOVE_BTN = "ye-default-reference-editor-remove-button";
    protected static final String CSS_REFERENCE_EDITOR_SELECTED_ITEM_LABEL = "ye-default-reference-editor-selected-item-label";
    protected static final String CSS_REFERENCE_EDITOR_SELECTED_ITEM_CONTAINER = "ye-default-reference-editor-selected-item-container";

    protected static final String YTESTID_REMOVE_BUTTON = "reference-editor-remove-button";
    protected static final String YTESTID_REFERENCE_ENTRY = "reference-editor-reference";

    private final AbstractReferenceEditor<T, K> referenceEditor;

    public SimpleSelectLayout(AbstractReferenceEditor<T, K> referenceEditorInterface, Base configuration)
    {
        super(referenceEditorInterface, configuration);
        this.referenceEditor = referenceEditorInterface;
    }
    @Override
    protected ListitemRenderer<T> createSelectedItemsListItemRenderer()
    {
        return (item, data, index) -> //NOSONAR
        {
            final Label label = new Label(referenceEditor.getStringRepresentationOfObject(data));
            label.setSclass(CSS_REFERENCE_EDITOR_SELECTED_ITEM_LABEL);
            YTestTools.modifyYTestId(label, YTESTID_REFERENCE_ENTRY);
            label.setMultiline(true);

            final Div removeImage = new Div();
            removeImage.setSclass(CSS_REFERENCE_EDITOR_REMOVE_BTN);
            YTestTools.modifyYTestId(removeImage, YTESTID_REMOVE_BUTTON);
            removeImage.setVisible(referenceEditor.isEditable());
            removeImage.addEventListener(Events.ON_CLICK, event -> referenceEditor.removeSelectedObject(data));

            final Div layout = new Div();
            layout.setSclass(CSS_REFERENCE_EDITOR_SELECTED_ITEM_CONTAINER);

            UITools.modifySClass(label, "ye-editor-disabled", true);
            layout.appendChild(label);

            if (!referenceEditor.isDisableRemoveReference())
            {
                layout.appendChild(removeImage);
                UITools.modifySClass(layout, "ye-remove-enabled", true);
            }

            final Listcell cell = new Listcell();
            if (!referenceEditor.isDisableDisplayingDetails())
            {
                cell.addEventListener(Events.ON_DOUBLE_CLICK,
                        event -> referenceEditor.sendOutput(CustomersupportbackofficeConstants.SIMPLE_SELECT_OUT_SOCKET_ID, data)
                );
            }
            cell.appendChild(layout);
            cell.setParent(item);
        };
    }
}
