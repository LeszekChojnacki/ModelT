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
package de.hybris.platform.customercouponbackoffice.editors;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customercouponservices.model.CustomerCouponModel;

import java.util.Iterator;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.AdvancedSearch;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Renders the editor area for displaying assigned customers of the current coupon
 */
public class FindAssignedCustomersEditor extends AbstractComponentWidgetAdapterAware implements CockpitEditorRenderer<Object>
{

	private static final String FIND_OUTPUT_SOCKET = "findOutput";
	private static final String LABEL_FINDASSIGNEDCUSTOMERS = "label.button.find.assigned.customers";
	private static final String PRINCIPAL_SEARCH_EDITOR_NAME = "advanced-search";
	private static final String PARENT_OBJECT = "parentObject";
	private static final String ON_CLICK = "onClick";
	private static final String WIM = "wim";


	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		final Div div = new Div();
		final Button button = new Button(Labels.getLabel(LABEL_FINDASSIGNEDCUSTOMERS));
		button.setDisabled(true);
		if (((CustomerCouponModel) context.getParameter(PARENT_OBJECT)).getPk() != null)
		{
			button.setDisabled(false);
			button.addEventListener(ON_CLICK, event -> {
				final AdvancedSearchData searchData = new AdvancedSearchData();
				searchData.setTypeCode(CustomerModel._TYPECODE);
				searchData.setGlobalOperator(ValueComparisonOperator.AND);

				final WidgetInstanceManager wim = (WidgetInstanceManager) context.getParameter(WIM);
				final AdvancedSearchInitContext initContext = createSearchContext(searchData, wim, context);
				sendOutput(FIND_OUTPUT_SOCKET, initContext);
			});
		}

		parent.appendChild(div);
		div.appendChild(button);
	}

	protected AdvancedSearchInitContext createSearchContext(final AdvancedSearchData searchData, final WidgetInstanceManager wim,
			final EditorContext<Object> editorContext)
	{
		final AdvancedSearch config = loadAdvancedConfiguration(wim, PRINCIPAL_SEARCH_EDITOR_NAME);
		config.setDisableSimpleSearch(Boolean.TRUE);

		final Iterator<FieldType> fieldTypes = config.getFieldList().getField().iterator();
		while (fieldTypes.hasNext())
		{
			final FieldType field = fieldTypes.next();
			if (CustomerModel.CUSTOMERCOUPONS.equals(field.getName()))
			{
				field.setDisabled(Boolean.TRUE);
				searchData.addCondition(field, ValueComparisonOperator.CONTAINS, editorContext.getParameter(PARENT_OBJECT));
				searchData.setIncludeSubtypes(Boolean.FALSE);
			}
		}

		return new AdvancedSearchInitContext(searchData, config);
	}

	protected AdvancedSearch loadAdvancedConfiguration(final WidgetInstanceManager wim, final String name)
	{
		final DefaultConfigContext context = new DefaultConfigContext(name, CustomerModel._TYPECODE);

		try
		{
			return wim.loadConfiguration(context, AdvancedSearch.class);
		}
		catch (final CockpitConfigurationException e)//NOSONAR
		{
			return null;
		}
	}

}
