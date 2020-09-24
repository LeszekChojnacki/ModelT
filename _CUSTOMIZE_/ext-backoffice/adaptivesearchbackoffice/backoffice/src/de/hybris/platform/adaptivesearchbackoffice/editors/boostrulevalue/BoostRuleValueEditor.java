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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.editors.boostrulevalue;

import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.EDITOR_WIDGET_INSTANCE_MANAGER_KEY;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearch.util.ObjectConverter;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;

import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.services.PropertyValueService;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.editors.EditorRegistry;
import com.hybris.cockpitng.editors.impl.AbstractCockpitEditorRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class BoostRuleValueEditor extends AbstractCockpitEditorRenderer<Object>
{
	private static final Logger LOG = Logger.getLogger(BoostRuleValueEditor.class);

	protected static final String INDEX_TYPE_PARAM = "indexType";

	protected static final String PROPERTY_VALUE_SERVICE_BEAN_ID = "propertyValueService";

	protected static final Pattern SPEL_REGEXP = Pattern.compile("\\{((.*))\\}");

	private EditorListener<Object> listener;

	@Resource
	private AsSearchProviderFactory asSearchProviderFactory;

	@Resource
	private EditorRegistry editorRegistry;

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		Validate.notNull("All parameters are mandatory", parent, context, listener);
		setListener(listener);

		final Object parentObject = context.getParameter(PARENT_OBJECT_KEY);
		if (!(parentObject instanceof AsBoostRuleModel))
		{
			return;
		}

		final AsBoostRuleModel boostRule = (AsBoostRuleModel) parentObject;
		final String indexProperty = boostRule.getIndexProperty();

		final WidgetInstanceManager widgetInstanceManager = getWidgetInstanceManager(context);
		final String indexType = getIndexType(context, boostRule);

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final Optional<AsIndexPropertyData> indexPropertyData = searchProvider.getIndexPropertyForCode(indexType, indexProperty);

		if (!indexPropertyData.isPresent())
		{
			throw new EditorRuntimeException("Index property not found: " + indexProperty);
		}

		final Class<?> indexPropertyType = indexPropertyData.get().getType();
		Object value;
		try
		{
			value = ObjectConverter.convert(context.getInitialValue(), indexPropertyType);
		}
		catch (final AsException e)
		{
			LOG.error(e);
			value = null;
		}
		final String defaultEditorCode = editorRegistry.getDefaultEditorCode(indexPropertyType.getName());

		final Editor editor = new Editor();
		editor.setParent(parent);
		editor.setWidgetInstanceManager(widgetInstanceManager);
		editor.setInitialValue(value);
		editor.setDefaultEditor(defaultEditorCode);
		editor.setType(indexPropertyType.getName());
		editor.afterCompose();
		editor.addEventListener(Editor.ON_VALUE_CHANGED, event -> updateValue(event.getData(), boostRule));
	}

	protected void updateValue(final Object newValue, final AsBoostRuleModel boostRule)
	{
		String value;
		try
		{
			value = ObjectConverter.convert(newValue, String.class);
		}
		catch (final AsException e)
		{
			LOG.error(e);
			value = null;
		}

		listener.onValueChanged(value);
	}

	protected String getIndexType(final EditorContext<Object> context, final AsBoostRuleModel boostRule)
	{
		final String indexType = (String) context.getParameter(INDEX_TYPE_PARAM);
		final PropertyValueService propertyValueService = BackofficeSpringUtil.getBean(PROPERTY_VALUE_SERVICE_BEAN_ID);

		final Matcher matcher = SPEL_REGEXP.matcher(indexType);
		if (matcher.find())
		{
			final Map<String, Object> evaluationContext = new HashMap<>();
			evaluationContext.put(PARENT_OBJECT_KEY, boostRule);
			final String expression = matcher.group(1);
			return (String) propertyValueService.readValue(evaluationContext, expression);
		}
		else
		{
			return indexType;
		}
	}

	protected WidgetInstanceManager getWidgetInstanceManager(final EditorContext<Object> context)
	{
		return (WidgetInstanceManager) context.getParameter(EDITOR_WIDGET_INSTANCE_MANAGER_KEY);
	}

	public EditorListener<Object> getListener()
	{
		return listener;
	}

	public void setListener(final EditorListener<Object> listener)
	{
		this.listener = listener;
	}
}
