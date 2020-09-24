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
package de.hybris.platform.datahubbackoffice.presentation.editor;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

import com.google.common.collect.Lists;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.util.BackofficeSpringUtil;

public class ConfigurableDropdownEditor implements CockpitEditorRenderer<Object>
{
	private static final String PROVIDER_ERROR_MESSAGE = "datahub.configurable.dropdown.failure";
	private static final Object EMPTY_OPTION = new Object();
	private static final String I18N_PREFIX = "i18n.";
	private static final String ATTR_PROVIDER = "availableValuesProvider";
	private static final String ATTR_AVAILABLE_VALUES = "availableValues";
	private static final String ATTR_PLACEHOLDER = "placeholderKey";
	private static final String EMPTY_LABEL = " ";

	@WireVariable
	private NotificationService notificationService;

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		final Combobox combobox = new Combobox();
		combobox.setDisabled(!context.isEditable());
		final List<Object> data = Lists.newArrayList();

		final String optionProviderId = ObjectUtils.toString(context.getParameter(ATTR_PROVIDER));
		final String placeholderKey = ObjectUtils.toString(context.getParameter(ATTR_PLACEHOLDER));
		final DropdownProvider dropdownProvider = getProviderIfPossible(optionProviderId);
		if (dropdownProvider != null)
		{
			try
			{
				data.addAll(dropdownProvider.getAllValues());
			}
			catch (final Exception exception)
			{
				notificationService.notifyUser(context.getSuccessNotificationId(),
						PROVIDER_ERROR_MESSAGE, NotificationEvent.Level.FAILURE, optionProviderId, exception);
			}
		}
		else
		{
			final String rawOptions = ObjectUtils.toString(context.getParameter(ATTR_AVAILABLE_VALUES));
			final String[] optionArray = rawOptions.split(",");
			data.addAll(Lists.newArrayList(optionArray));
		}

		data.add(0, EMPTY_OPTION);

		final ListModel comboModel = createComboModelWithSelection(data, context.getInitialValue(), dropdownProvider);
		combobox.setModel(comboModel);
		combobox.setItemRenderer(createComboRenderer(dropdownProvider, placeholderKey));
		combobox.addEventListener(Events.ON_CHANGE, createOnChangeHandler(listener));
		if (StringUtils.isNotBlank(placeholderKey))
		{
			combobox.setPlaceholder(Labels.getLabel(placeholderKey));
		}

		parent.appendChild(combobox);
	}

	protected EventListener createOnChangeHandler(final EditorListener editorListener)
	{
		return event -> {
			final Comboitem selectedItem = ((Combobox) event.getTarget()).getSelectedItem();
			if (selectedItem != null)
			{
				editorListener.onValueChanged(selectedItem.getValue());
			}
		};
	}

	protected ComboitemRenderer createComboRenderer(final DropdownProvider dropdownProvider, final String placeholderKey)
	{
		return (item, data, index) -> {
			if (EMPTY_OPTION.equals(data))
			{
				item.setValue(null);
				final String label = StringUtils.isNotBlank(placeholderKey) ? Labels.getLabel(placeholderKey) : EMPTY_LABEL;
				item.setLabel(label);
			}
			else if (dropdownProvider != null)
			{
				item.setValue(data);
				item.setLabel(dropdownProvider.getName(data));
			}
			else
			{
				final String stringData = ObjectUtils.toString(data);
				item.setValue(StringUtils.removeStart(stringData, I18N_PREFIX));
				item.setLabel(Labels.getLabel(stringData));
			}
		};
	}

	protected ListModel createComboModelWithSelection(final List<Object> data, final Object initValue,
			final DropdownProvider dropdownProvider)
	{
		final ListModelList<Object> comboModel = new ListModelList<Object>(data);
		if (initValue != null)
		{
			final List<Object> selectedObjects = Lists.newArrayList();
			if (dropdownProvider == null)
			{
				final String toSelect = String.format("%s%s", I18N_PREFIX, initValue);
				selectedObjects.add(toSelect);
			}
			else
			{
				selectedObjects.add(initValue);
			}
			comboModel.setSelection(selectedObjects);
		}
		return comboModel;
	}

	protected DropdownProvider getProviderIfPossible(final String optionProviderId)
	{
		return BackofficeSpringUtil.getBean(optionProviderId);
	}
}
