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
package de.hybris.platform.solrfacetsearchbackoffice.editor;

import de.hybris.platform.core.Registry;
import de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.DropdownNamesProvider;
import de.hybris.platform.solrfacetsearchbackoffice.dropdownproviders.DropdownValuesProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class ConfigurableDropdownEditor implements CockpitEditorRenderer<Object>
{

	public static final Object EMPTY_OPTION = new Object();
	public static final String ATTR_BEAN_TYPE = "dropDownValueClassTypes";
	public static final String ATTR_VALUES_PROVIDER = "dropDownValuesProvider";
	public static final String ATTR_NAMES_PROVIDER = "dropDownValuesNameProvider";
	public static final String ATTR_PLACEHOLDER = "placeholderKey";
	private static final String ATTR_OPTIONS = "dropDownOptions";
	public static final String TO_LOWER_CASE_OPTION = "toLowerCase";
	public static final String EMPTY_LABEL = " ";

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		final Combobox combobox = new Combobox();
		combobox.setDisabled(!context.isEditable());

		final String dropDownValueClassTypes = ObjectUtils.toString(context.getParameter(ATTR_BEAN_TYPE));
		final String dropDownValuesProvider = ObjectUtils.toString(context.getParameter(ATTR_VALUES_PROVIDER));
		final String placeholderKey = ObjectUtils.toString(context.getParameter(ATTR_PLACEHOLDER));
		final String dropDownOptions = ObjectUtils.toString(context.getParameter(ATTR_OPTIONS));

		final List<Object> data = Lists.newArrayList();
		final Map<String, String> options = getDropdownOptions(dropDownOptions);

		data.addAll(getValues(dropDownValuesProvider, dropDownValueClassTypes, options));
		data.add(0, EMPTY_OPTION);

		final ListModel comboModel = createComboModelWithSelection(data, context.getInitialValue());
		final String nameProvider = ObjectUtils.toString(context.getParameter(ATTR_NAMES_PROVIDER));
		final DropdownNamesProvider dropdownNamesProvider = BackofficeSpringUtil.getBean(nameProvider);

		combobox.setModel(comboModel);
		combobox.setItemRenderer(createComboRenderer(dropdownNamesProvider, placeholderKey, options));
		combobox.addEventListener(Events.ON_CHANGE, createOnChangeHandler(listener));

		if (StringUtils.isNotBlank(placeholderKey))
		{
			combobox.setPlaceholder(Labels.getLabel(placeholderKey));
		}

		parent.appendChild(combobox);
	}

	protected List<Object> getValues(final String dropDownValuesProvider, final String dropDownValueClassTypes,
			final Map<String, String> options)
	{
		try
		{
			final DropdownValuesProvider dropdownValuesProvider = Registry.getApplicationContext().getBean(dropDownValuesProvider,
					DropdownValuesProvider.class);
			return dropdownValuesProvider.getValues(dropDownValueClassTypes, options);
		}
		catch (final BeansException e)
		{
			throw new EditorRuntimeException(e);
		}
	}

	protected EventListener createOnChangeHandler(final EditorListener editorListener)
	{
		return event -> {
			final Combobox combobox = (Combobox) event.getTarget();
			Comboitem selectedItem = combobox.getSelectedItem();

			if (selectedItem == null && CollectionUtils.isNotEmpty(combobox.getItems()))
			{
				selectedItem = combobox.getItemAtIndex(0);
				combobox.setSelectedItem(selectedItem);
			}

			if (selectedItem != null)
			{
				editorListener.onValueChanged(selectedItem.getValue());
			}
		};
	}

	protected ComboitemRenderer createComboRenderer(final DropdownNamesProvider dropdownProvider, final String placeholderKey,
			final Map<String, String> options)
	{
		return new ItemRenderer(dropdownProvider, placeholderKey, options);
	}

	protected String getDataName(final Object data)
	{
		if (data == null)
		{
			return "";
		}

		return data.toString();
	}

	protected ListModel createComboModelWithSelection(final List<Object> data, final Object initValue)
	{

		final ListModelList<Object> comboModel = new ListModelList<>(data);
		if (initValue != null)
		{
			final List<Object> selectedObjects = Lists.newArrayList();
			selectedObjects.add(initValue);
			comboModel.setSelection(selectedObjects);
		}
		return comboModel;
	}

	protected Map<String, String> getDropdownOptions(final String drodownOptions)
	{
		if (StringUtils.isNotBlank(drodownOptions))
		{
			return Splitter.on(",").withKeyValueSeparator("=").split(drodownOptions);
		}
		return null;
	}

	protected class ItemRenderer implements ComboitemRenderer<Object>
	{
		private final DropdownNamesProvider dropdownProvider;
		private final String placeholderKey;
		private final Map<String, String> options;

		public ItemRenderer(final DropdownNamesProvider dropdownProvider, final String placeholderKey,
				final Map<String, String> options)
		{
			this.dropdownProvider = dropdownProvider;
			this.placeholderKey = placeholderKey;
			this.options = options;
		}

		protected DropdownNamesProvider getDropdownProvider()
		{
			return dropdownProvider;
		}

		protected String getPlaceholderKey()
		{
			return placeholderKey;
		}

		protected Map<String, String> getOptions()
		{
			return options;
		}

		@Override
		public void render(final Comboitem item, final Object data, final int index) throws Exception
		{
			if (Objects.equals(EMPTY_OPTION, data))
			{

				item.setValue(null);
				String label = EMPTY_LABEL;
				if (StringUtils.isNotBlank(placeholderKey))
				{
					label = Labels.getLabel(placeholderKey);
				}
				item.setLabel(label);

			}
			else
			{
				if (dropdownProvider != null)
				{
					item.setValue(data);
					item.setLabel(dropdownProvider.getName(data, options));

				}
				else
				{
					item.setValue(data);
					item.setLabel(getDataName(data));
				}
			}
		}
	}
}
