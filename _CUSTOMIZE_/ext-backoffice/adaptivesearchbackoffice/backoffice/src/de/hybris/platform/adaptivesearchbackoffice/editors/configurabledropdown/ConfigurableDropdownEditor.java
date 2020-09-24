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
package de.hybris.platform.adaptivesearchbackoffice.editors.configurabledropdown;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class ConfigurableDropdownEditor implements CockpitEditorRenderer<Object>
{
	protected static final String DATA_PROVIDER = "dataProvider";
	protected static final String DATA_PROVIDER_PARAMETERS = "dataProviderParameters";

	protected static final String PLACEHOLDER = "placeholderKey";

	protected static final String PROPERTY_VALUE_SERVICE_BEAN_ID = "propertyValueService";

	private static final Pattern SPEL_REGEXP = Pattern.compile("\\{((.*))\\}");

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		final String placeholderKey = ObjectUtils.toString(context.getParameter(PLACEHOLDER));

		final DataProvider dataProvider = createDataProvider(context);
		final Map<String, Object> dataProviderParameters = createDataProviderParameters(context);

		final Combobox combobox = new Combobox();
		combobox.setReadonly(true);
		combobox.setDisabled(!context.isEditable());
		combobox.setModel(createModel(dataProvider, dataProviderParameters, context.getInitialValue()));
		combobox.setItemRenderer(createItemRenderer(dataProvider, dataProviderParameters));
		combobox.addEventListener(Events.ON_CHANGE, createOnChangeHandler(context, listener));

		if (StringUtils.isNotBlank(placeholderKey))
		{
			combobox.setPlaceholder(Labels.getLabel(placeholderKey));
		}

		parent.appendChild(combobox);
	}

	protected DataProvider createDataProvider(final EditorContext<Object> context)
	{
		final String dataProvider = ObjectUtils.toString(context.getParameter(DATA_PROVIDER));
		return BackofficeSpringUtil.getBean(dataProvider);
	}

	protected Map<String, Object> createDataProviderParameters(final EditorContext<Object> context)
	{
		final String dataProviderParametersString = ObjectUtils.toString(context.getParameter(DATA_PROVIDER_PARAMETERS));
		if (StringUtils.isBlank(dataProviderParametersString))
		{
			return Collections.emptyMap();
		}

		final PropertyValueService propertyValueService = BackofficeSpringUtil.getBean(PROPERTY_VALUE_SERVICE_BEAN_ID);
		final Map<String, String> parameters = Splitter.on(";").withKeyValueSeparator("=").split(dataProviderParametersString);

		final Map<String, Object> dataProviderParameters = parameters.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> evaluate(context, entry.getValue(), propertyValueService)));

		dataProviderParameters.put(PARENT_OBJECT_KEY, context.getParameter(PARENT_OBJECT_KEY));

		return dataProviderParameters;
	}

	protected Object evaluate(final EditorContext<Object> context, final String value,
			final PropertyValueService propertyValueService)
	{
		final Matcher matcher = SPEL_REGEXP.matcher(value);
		if (matcher.find())
		{
			final Map<String, Object> evaluationContext = createEvaluationContext(context);
			final String expression = matcher.group(1);
			return propertyValueService.readValue(evaluationContext, expression);
		}
		else
		{
			return value;
		}
	}

	protected Map<String, Object> createEvaluationContext(final EditorContext<Object> context)
	{
		final Map<String, Object> evaluationContext = new HashMap<>();
		evaluationContext.put(PARENT_OBJECT_KEY, context.getParameter(PARENT_OBJECT_KEY));

		return evaluationContext;
	}

	protected ListModel createModel(final DataProvider dataProvider, final Map<String, Object> dataProviderParameters,
			final Object initialValue)
	{
		final List<Object> data = new ArrayList<>();
		data.add(null);
		data.addAll(dataProvider.getData(dataProviderParameters));

		final ListModelList<Object> model = new ListModelList<>(data);
		if (initialValue != null)
		{
			final Optional<Object> selectedItem = data.stream()
					.filter(item -> Objects.equals(initialValue, dataProvider.getValue(item, dataProviderParameters))).findFirst();

			if (selectedItem.isPresent())
			{
				model.setSelection(Collections.singletonList(selectedItem.get()));
			}
		}

		return model;
	}

	protected ComboitemRenderer createItemRenderer(final DataProvider dataProvider,
			final Map<String, Object> dataProviderParameters)
	{
		return (item, data, index) -> {
			item.setValue(dataProvider.getValue(data, dataProviderParameters));
			item.setLabel(dataProvider.getLabel(data, dataProviderParameters));
		};
	}

	protected EventListener createOnChangeHandler(final EditorContext<Object> context, final EditorListener editorListener)
	{
		return event -> {
			final Combobox combobox = (Combobox) event.getTarget();
			final Comboitem selectedItem = combobox.getSelectedItem();

			if (selectedItem == null && CollectionUtils.isNotEmpty(combobox.getItems()))
			{
				combobox.setSelectedIndex(0);
			}
			else if (selectedItem != null)
			{
				editorListener.onValueChanged(selectedItem.getValue());
			}
		};
	}
}
