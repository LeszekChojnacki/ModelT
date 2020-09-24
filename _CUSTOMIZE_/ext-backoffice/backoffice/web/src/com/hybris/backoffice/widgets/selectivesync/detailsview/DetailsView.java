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
package com.hybris.backoffice.widgets.selectivesync.detailsview;

import de.hybris.platform.catalog.model.SyncAttributeDescriptorConfigModel;

import java.util.Objects;

import org.apache.commons.lang.BooleanUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

import com.hybris.backoffice.widgets.selectivesync.renderer.SelectiveSyncRenderer;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.YTestTools;


/** Details view for {@link SelectiveSyncRenderer}. */
public class DetailsView extends Div
{
	protected static final String YTESTID_SYNC_ATTRIBUTE = "details_view_sync_attribute_checkbox";
	protected static final String YTESTID_COPY_BY_VALUE_ATTRIBUTE = "details_view_copy_by_value_attribute_checkbox";
	protected static final String YTESTID_UNTRANSLATABLE_ATTRIBUTE = "details_view_untranslatable_attribute_checkbox";
	protected static final String YTESTID_PARTIALLY_TRANSLATABLE_ATTRIBUTE = "details_view_partially_translatable_attribute_checkbox";
	protected static final String YTESTID_NO_READ_ACCESS_LABEL = "details_view_no_read_access_label";

	private static final String LABEL_DETAILS = "syncAttribute.detailView.details";
	private static final String LABEL_SYNCHRONIZE = "syncAttribute.detailView.synchronize";
	private static final String LABEL_COPY_BY_VALUE = "syncAttribute.detailView.copyByValue";
	private static final String LABEL_UNTRANSLATABLE = "syncAttribute.detailView.untranslatableValue";
	private static final String LABEL_PARTIALLY_UNTRANSLATABLE = "syncAttribute.detailView.partiallyTranslatable";

	private static final String SCLASS_CHECKBOX_SWITCH = "ye-switch-checkbox";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_DETAILS_ITEM = "sync-attribute-config-details-item";
	private static final String SCLASS_SYNC_ATTRIBUTE_CONFIG_DETAILS = "sync-attribute-config-details";

	private static final String BACKOFFICE_DATA_NOT_VISIBLE_LABEL = "backoffice.data.not.visible";

	protected final boolean editable;
	protected final transient WidgetInstanceManager widgetInstanceManager;
	protected final transient PermissionFacade permissionFacade;

	public DetailsView(final CreationContext creationContext)
	{
		this.editable = creationContext.editable;
		this.widgetInstanceManager = creationContext.widgetInstanceManager;
		this.permissionFacade = creationContext.permissionFacade;
	}

	/**
	 * Displays synchronization attributes values from syncAttributeConfig. When one of the value is change
	 * detailsViewAttributeValueChangeListener is alerted.
	 *
	 * @param syncAttributeConfig
	 *           values to display
	 * @param detailsViewAttributeValueChangeListener
	 *           listener for attribute changes
	 */
	public void display(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		clearView();
		setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_DETAILS);

		appendChild(new Label(getLabel(LABEL_DETAILS)));

		renderSynchronizeAttribute(syncAttributeConfig, detailsViewAttributeValueChangeListener);
		renderCopyByValueAttribute(syncAttributeConfig, detailsViewAttributeValueChangeListener);
		renderUntranslatableAttribute(syncAttributeConfig, detailsViewAttributeValueChangeListener);
		renderPartiallyTranslatableAttribute(syncAttributeConfig, detailsViewAttributeValueChangeListener);
	}

	protected void renderSynchronizeAttribute(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		if (canRead(syncAttributeConfig, SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC))
		{
			final Component sync = createSynchronizeAttribute(syncAttributeConfig, detailsViewAttributeValueChangeListener);
			appendChild(createDetailsListItem(sync));
		}
		else
		{
			appendChild(createNoReadAccess());
		}
	}

	protected void renderCopyByValueAttribute(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		if (canRead(syncAttributeConfig, SyncAttributeDescriptorConfigModel.COPYBYVALUE))
		{
			final Component copyByValue = createCopyByValue(syncAttributeConfig, detailsViewAttributeValueChangeListener);
			appendChild(createDetailsListItem(copyByValue));
		}
		else
		{
			appendChild(createNoReadAccess());
		}
	}

	protected void renderUntranslatableAttribute(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		if (canRead(syncAttributeConfig, SyncAttributeDescriptorConfigModel.UNTRANSLATABLE))
		{
			final Component untranslatable = createUntranslatable(syncAttributeConfig, detailsViewAttributeValueChangeListener);
			appendChild(createDetailsListItem(untranslatable));
		}
		else
		{
			appendChild(createNoReadAccess());
		}
	}

	protected void renderPartiallyTranslatableAttribute(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		if (canRead(syncAttributeConfig, SyncAttributeDescriptorConfigModel.PARTIALLYTRANSLATABLE))
		{
			final Component partiallyTranslatable = createPartiallyTranslatable(syncAttributeConfig,
					detailsViewAttributeValueChangeListener);
			appendChild(createDetailsListItem(partiallyTranslatable));
		}
		else
		{
			appendChild(createNoReadAccess());
		}
	}

	protected Div createDetailsListItem(final Component component)
	{
		final Div listItem = new Div();
		listItem.appendChild(component);
		listItem.setSclass(SCLASS_SYNC_ATTRIBUTE_CONFIG_DETAILS_ITEM);
		return listItem;
	}

	protected Checkbox createSynchronizeAttribute(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		final Checkbox attribute = createAttributeCheckbox(BooleanUtils.isTrue(syncAttributeConfig.getIncludedInSync()));
		attribute.setDisabled(!canChange(syncAttributeConfig, SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC));
		attribute.setLabel(getLabel(LABEL_SYNCHRONIZE));
		YTestTools.modifyYTestId(attribute, YTESTID_SYNC_ATTRIBUTE);
		attribute.addEventListener(Events.ON_CHECK, event -> {

			syncAttributeConfig.setIncludedInSync(attribute.isChecked());

			detailsViewAttributeValueChangeListener.attributeChanged(syncAttributeConfig,
					SyncAttributeDescriptorConfigModel.INCLUDEDINSYNC, BooleanUtils.isTrue(syncAttributeConfig.getIncludedInSync()));
		});
		return attribute;
	}

	protected Checkbox createCopyByValue(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		final Checkbox attribute = createAttributeCheckbox(BooleanUtils.isTrue(syncAttributeConfig.getCopyByValue()));
		attribute.setDisabled(!canChange(syncAttributeConfig, SyncAttributeDescriptorConfigModel.COPYBYVALUE));
		attribute.setLabel(getLabel(LABEL_COPY_BY_VALUE));
		YTestTools.modifyYTestId(attribute, YTESTID_COPY_BY_VALUE_ATTRIBUTE);
		attribute.addEventListener(Events.ON_CHECK, event -> {

			syncAttributeConfig.setCopyByValue(attribute.isChecked());

			detailsViewAttributeValueChangeListener.attributeChanged(syncAttributeConfig,
					SyncAttributeDescriptorConfigModel.COPYBYVALUE, BooleanUtils.isTrue(syncAttributeConfig.getCopyByValue()));
		});
		return attribute;
	}

	protected Checkbox createUntranslatable(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		final Checkbox attribute = createAttributeCheckbox(BooleanUtils.isTrue(syncAttributeConfig.getUntranslatable()));
		attribute.setDisabled(!canChange(syncAttributeConfig, SyncAttributeDescriptorConfigModel.UNTRANSLATABLE));
		attribute.setLabel(getLabel(LABEL_UNTRANSLATABLE));
		YTestTools.modifyYTestId(attribute, YTESTID_UNTRANSLATABLE_ATTRIBUTE);
		attribute.addEventListener(Events.ON_CHECK, event -> {

			syncAttributeConfig.setUntranslatable(attribute.isChecked());

			detailsViewAttributeValueChangeListener.attributeChanged(syncAttributeConfig,
					SyncAttributeDescriptorConfigModel.UNTRANSLATABLE, BooleanUtils.isTrue(syncAttributeConfig.getUntranslatable()));
		});
		return attribute;
	}

	protected Checkbox createPartiallyTranslatable(final SyncAttributeDescriptorConfigModel syncAttributeConfig,
			final DetailsViewAttributeValueChangeListener detailsViewAttributeValueChangeListener)
	{
		final Checkbox attribute = createAttributeCheckbox(BooleanUtils.isTrue(syncAttributeConfig.getPartiallyTranslatable()));
		attribute.setDisabled(!canChange(syncAttributeConfig, SyncAttributeDescriptorConfigModel.PARTIALLYTRANSLATABLE));
		attribute.setLabel(getLabel(LABEL_PARTIALLY_UNTRANSLATABLE));
		YTestTools.modifyYTestId(attribute, YTESTID_PARTIALLY_TRANSLATABLE_ATTRIBUTE);
		attribute.addEventListener(Events.ON_CHECK, event -> {

			syncAttributeConfig.setPartiallyTranslatable(attribute.isChecked());

			detailsViewAttributeValueChangeListener.attributeChanged(syncAttributeConfig,
					SyncAttributeDescriptorConfigModel.PARTIALLYTRANSLATABLE,
					BooleanUtils.isTrue(syncAttributeConfig.getPartiallyTranslatable()));
		});
		return attribute;
	}

	protected boolean canRead(final Object object, final String property)
	{
		return permissionFacade.canReadInstanceProperty(object, property);
	}

	protected boolean canChange(final Object object, final String property)
	{
		return editable && permissionFacade.canChangeInstanceProperty(object, property);
	}

	protected Checkbox createAttributeCheckbox(final boolean checked)
	{
		final Checkbox attribute = new Checkbox();
		attribute.setChecked(checked);
		attribute.setSclass(SCLASS_CHECKBOX_SWITCH);
		attribute.setDisabled(!editable);
		return attribute;
	}

	protected String getLabel(final String label)
	{
		return widgetInstanceManager.getLabel(label);
	}

	protected Div createNoReadAccess()
	{
		final Label noReadAccessLabel = new Label(Labels.getLabel(BACKOFFICE_DATA_NOT_VISIBLE_LABEL));
		YTestTools.modifyYTestId(noReadAccessLabel, YTESTID_NO_READ_ACCESS_LABEL);
		final Div labelContainer = new Div();
		labelContainer.appendChild(noReadAccessLabel);
		return labelContainer;
	}

	/**
	 * Clear view
	 */
	public void clearView()
	{
		this.getChildren().clear();
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		final DetailsView that = (DetailsView) o;
		return editable == that.editable;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(editable);
	}

	/** Context for creating {@link DetailsView}. */
	public static class CreationContext
	{
		private WidgetInstanceManager widgetInstanceManager;
		private boolean editable;
		private PermissionFacade permissionFacade;

		public void setWidgetInstanceManager(final WidgetInstanceManager widgetInstanceManager)
		{
			this.widgetInstanceManager = widgetInstanceManager;
		}

		public void setEditable(final boolean editable)
		{
			this.editable = editable;
		}

		public void setPermissionFacade(final PermissionFacade permissionFacade)
		{
			this.permissionFacade = permissionFacade;
		}
	}
}
