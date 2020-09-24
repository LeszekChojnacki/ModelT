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
package de.hybris.platform.warehousingbackoffice.widgets.warehousingrefineby;

import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.warehousingbackoffice.config.impl.jaxb.hybris.warehousingrefineby.FieldList;
import de.hybris.platform.warehousingbackoffice.config.impl.jaxb.hybris.warehousingrefineby.RefineBy;
import de.hybris.platform.warehousingbackoffice.config.impl.jaxb.hybris.warehousingrefineby.SearchValue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionDataList;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.DefaultConfigContext;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.dataaccess.facades.type.DataAttribute;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.cockpitng.util.DefaultWidgetController;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;


/**
 * Controller allowing to filter a collection browser based combine with advanced search logic.
 */
public class WarehousingRefineByController extends DefaultWidgetController
{
	private static final Logger LOG = LoggerFactory.getLogger(WarehousingRefineByController.class);

	public static final String SOCKET_IN_NODE_SELECTED = "nodeSelected";
	public static final String SOCKET_IN_ADVANCED_SEARCH_INIT_CONTEXT = "inputContext";
	public static final String SOCKET_OUT_OUTPUT_CONTEXT = "outputContext";

	public static final String GET_NAME = "getName";
	public static final String WAREHOUSINGBACKOFFICE_REFINE_BY = "warehousingbackoffice-refine-by";
	public static final String GROUP_VALUES_SEPARATOR = "/";
	public static final String REFINE_BY_CONTAINER = "refine-by-container";
	public static final String CURRENT_SEARCH = "currentSearch";
	public static final String TYPE_CODE = "typeCode";
	public static final String CONTEXT = "context";
	public static final String CURRENT_SEARCH_SEPARATOR = "/";

	@Wire
	private Div warehousingFilterContainer;
	@Wire
	private Label emptyFilters;

	@WireVariable
	private transient TypeFacade typeFacade;
	@WireVariable
	private transient FlexibleSearchService flexibleSearchService;
	@WireVariable
	private transient EnumerationService enumerationService;

	private transient RefineBy refineByConfig;

	/**
	 * Resets the refine by widget on {@link NavigationNode} selection.
	 *
	 * @param nodeSelected
	 * 		the selected {@link NavigationNode}
	 */
	@SocketEvent(socketId = SOCKET_IN_NODE_SELECTED)
	public void onNodeSelected(final NavigationNode nodeSelected)
	{
		getModel().setValue(CURRENT_SEARCH, null);
		getModel().setValue(TYPE_CODE, null);
		getModel().setValue(CONTEXT, null);
		resetWidget();
	}


	@Override
	public void initialize(final Component comp)
	{
		final String typeCode = getModel().getValue(TYPE_CODE, String.class);
		if (typeCode != null)
		{
			renderRefineByFromConfig(typeCode);
		}
		getModel().setValue(CURRENT_SEARCH, null);
	}

	/**
	 * Adds refine by filters to the initial given {@link AdvancedSearchInitContext}.
	 *
	 * @param context
	 * 		the {@link AdvancedSearchInitContext} input
	 */
	@SocketEvent(socketId = SOCKET_IN_ADVANCED_SEARCH_INIT_CONTEXT)
	public void completeAdvancedSearchInitContext(final AdvancedSearchInitContext context)
	{
		resetWidget();
		getModel().setValue(CONTEXT, context);
		getModel().setValue(TYPE_CODE, context.getAdvancedSearchData().getTypeCode());

		renderRefineByFromConfig(context.getAdvancedSearchData().getTypeCode());

		sendOutput(SOCKET_OUT_OUTPUT_CONTEXT, context);
	}

	/**
	 * Adds refine by filters conditions to the existing ones.
	 *
	 * @param searchData
	 * 		the original {link AdvancedSearchData}
	 * @param searchExpressions
	 * 		the search expressions defined in the configuration
	 */
	protected void addConditionList(final AdvancedSearchData searchData,
			final Map<String, List<SearchConditionData>> searchExpressions)
	{
		final List<SearchConditionData> searchConditionDataList = new ArrayList<>();

		searchExpressions.entrySet().forEach(entry ->
		{
			if (CollectionUtils.isNotEmpty(searchData.getConditions(entry.getKey())))
			{
				searchData.getConditions(entry.getKey()).clear();
			}

			searchConditionDataList.add(SearchConditionDataList.or(entry.getValue()));
		});
		searchData.addConditionList(ValueComparisonOperator.AND, searchConditionDataList);
	}

	/**
	 * Adds the search conditions associated to the selected {@link Checkbox}
	 *
	 * @param checkbox
	 * 		the selected {@link Checkbox}
	 * @param searchFieldConditions
	 * 		the {@link Map<String,List<SearchConditionData>>} containing all the search conditions
	 * @param advancedSearchData
	 * 		the {@link AdvancedSearchData}
	 */
	protected void addSearchCondition(final Checkbox checkbox, final Map<String, List<SearchConditionData>> searchFieldConditions,
			final AdvancedSearchData advancedSearchData)
	{
		if (!searchFieldConditions.containsKey(checkbox.getName()))
		{
			searchFieldConditions.put(checkbox.getName(), new ArrayList<>());
		}

		final DataType dataType = loadDataTypeForCode(advancedSearchData.getTypeCode());
		final DataAttribute dataAttribute = dataType.getAttribute(checkbox.getName());
		final String searchValue = ((Label) checkbox.getNextSibling()).getValue();
		final ArrayList searchValues = new ArrayList();

		if (searchValue.contains(GROUP_VALUES_SEPARATOR))
		{
			Collections.addAll(searchValues, searchValue.split(GROUP_VALUES_SEPARATOR));
		}
		else
		{
			searchValues.add(searchValue);
		}

		final String currentSearch = getModel().getValue(CURRENT_SEARCH, String.class);
		getModel().setValue(CURRENT_SEARCH,
				currentSearch != null ? currentSearch + CURRENT_SEARCH_SEPARATOR + checkbox.getLabel() : checkbox.getLabel());

		final FieldType fieldType = new FieldType();
		fieldType.setDisabled(Boolean.TRUE);
		fieldType.setSelected(Boolean.TRUE);
		fieldType.setName(checkbox.getName());

		searchValues.forEach(value -> searchFieldConditions.get(checkbox.getName()).add(new SearchConditionData(fieldType,
				getFlexibleSearchService().searchUnique(buildSearchQuery(dataAttribute.getValueType().getCode(), value.toString())),
				ValueComparisonOperator.EQUALS)));
	}

	/**
	 * Builds a {@link FlexibleSearchQuery}
	 *
	 * @param typeCode
	 * 		the {@link DataType#code}
	 * @param searchValue
	 * 		the search value
	 * @return the created {@link FlexibleSearchQuery}
	 */
	protected FlexibleSearchQuery buildSearchQuery(final String typeCode, final String searchValue)
	{
		return new FlexibleSearchQuery("SELECT {pk} FROM {" + typeCode + "} WHERE {code} = ?code",
				Collections.singletonMap("code", searchValue));
	}

	/**
	 * Loads a {@link DataType} by its code.
	 *
	 * @param typeCode
	 * 		the {@link DataType#code}
	 * @return the {@link DataType} retrieved
	 */
	protected DataType loadDataTypeForCode(final String typeCode)
	{
		if (StringUtils.isNotBlank(typeCode))
		{
			try
			{
				return getTypeFacade().load(typeCode.trim());
			}
			catch (final TypeNotFoundException e)
			{
				LOG.error("Could not find type " + typeCode.trim(), e);
			}
		}

		return null;
	}

	/**
	 * Loads the configuration of the refine by according to the given type.
	 *
	 * @param typeCode
	 * 		the {@link DataType#code}
	 * @return the completed {@link RefineBy}
	 */
	protected RefineBy loadRefineByConfiguration(final String typeCode)
	{
		final DefaultConfigContext context = new DefaultConfigContext(WAREHOUSINGBACKOFFICE_REFINE_BY, typeCode.trim());

		try
		{
			return getWidgetInstanceManager().loadConfiguration(context, RefineBy.class);
		}
		catch (final CockpitConfigurationException cce) //NOSONAR
		{
			LOG.warn(String.format("Could not load refine by configuration for type [%s] ", typeCode));
		}

		return null;
	}

	/**
	 * Render a checkbox with her associated hidden {@link Label} which contains the search value.
	 *
	 * @param searchFieldName
	 * 		the name of the field to search on
	 * @param searchFieldPossibleValues
	 * 		the list of search expressions to search
	 * @param typeCode
	 * 		the {@link DataType#code}
	 * @param searchFieldValue
	 * 		the value of the field to search
	 * @param searchFieldDiv
	 * 		the {@link Div} to which the checkbox will be added
	 */
	protected void renderCheckbox(final String searchFieldName, final ArrayList searchFieldPossibleValues, final String typeCode,
			final SearchValue searchFieldValue, final Div searchFieldDiv)
	{
		String fieldLabel = StringUtils.EMPTY;
		String searchValue;

		if (searchFieldValue.getUniqueValue() != null && StringUtils.isNotEmpty(searchFieldValue.getUniqueValue()))
		{
			searchValue = searchFieldValue.getUniqueValue();
			final FlexibleSearchQuery query = buildSearchQuery(typeCode, searchFieldValue.getUniqueValue());
			Object possibleValue = getFlexibleSearchService().searchUnique(query);
			searchFieldPossibleValues.add(possibleValue);

			try
			{
				fieldLabel = StringUtils.isNotEmpty(searchFieldValue.getLabel()) ?
						Labels.getLabel(searchFieldValue.getLabel()) :
						possibleValue.getClass().getMethod(GET_NAME).invoke(possibleValue).toString();
			}
			catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NullPointerException e) //NOSONAR
			{
				LOG.info(String.format("No name defined into the database for the given type for code %s .", searchFieldValue));
			}
		}
		else
		{
			fieldLabel = Labels.getLabel(searchFieldValue.getLabel());
			final StringJoiner joiner = new StringJoiner(GROUP_VALUES_SEPARATOR);

			searchFieldValue.getGroupMemberValue().forEach(value ->
			{
				joiner.add(value.getValue());
				final FlexibleSearchQuery query = buildSearchQuery(typeCode, value.getValue());
				searchFieldPossibleValues.add(getFlexibleSearchService().searchUnique(query));
			});
			searchValue = joiner.toString();
		}

		final Checkbox checkbox = new Checkbox();
		checkbox.setName(searchFieldName);
		checkbox.setLabel(fieldLabel);

		List<String> checkedBoxes = new ArrayList<>();
		final String currentSearch = getModel().getValue(CURRENT_SEARCH, String.class);
		if (currentSearch != null)
		{
			checkedBoxes = Arrays.asList(currentSearch.split(CURRENT_SEARCH_SEPARATOR));
		}
		checkbox.setChecked(checkedBoxes.contains(fieldLabel));

		checkbox.addEventListener(Events.ON_CHECK, this::submitFilter);
		getEmptyFilters().setVisible(false);

		final Label searchFieldLabel = new Label();
		searchFieldLabel.setVisible(false);
		searchFieldLabel.setValue(searchValue);

		searchFieldDiv.appendChild(checkbox);
		searchFieldDiv.appendChild(searchFieldLabel);

		final FieldType searchField = new FieldType();
		searchField.setName(searchFieldName);
	}

	/**
	 * Renders the filters based on warehousingbackoffice-refine-by configuration.
	 *
	 * @param typeCode
	 * 		the {@link DataType#code}
	 */
	protected void renderRefineByFromConfig(final String typeCode)
	{
		setRefineByConfig(loadRefineByConfiguration(typeCode));

		if (getRefineByConfig() != null)
		{
			final FieldList fieldList = getRefineByConfig().getFieldList();
			final DataType dataType = loadDataTypeForCode(typeCode);

			if (dataType != null && fieldList != null)
			{
				fieldList.getSearchField().forEach(searchField ->
				{
					final Div searchFieldDiv = new Div();
					final Label searchFieldLabel = new Label(Labels.getLabel(searchField.getLabel()));
					searchFieldDiv.appendChild(searchFieldLabel);

					final String searchFieldName = searchField.getName();
					final List<SearchValue> searchValues = searchField.getSearchValue();

					final ArrayList searchExpressionsList = new ArrayList();

					final DataAttribute dataAttribute = dataType.getAttribute(searchFieldName);
					if (dataAttribute != null && dataAttribute.isSearchable())
					{
						Validate
								.notNull(String.format("Cannot find attribute = %s for type = %s ", searchFieldName, dataType.getCode()),
										dataAttribute);

						searchValues.forEach(searchFieldValue -> renderCheckbox(searchFieldName, searchExpressionsList,
								dataAttribute.getValueType().getCode(), searchFieldValue, searchFieldDiv));
					}
					getWarehousingFilterContainer().appendChild(searchFieldDiv);
				});
			}
		}
	}

	/**
	 * Resets the content of the filter container.
	 */
	protected void resetWidget()
	{
		getWarehousingFilterContainer().setId(REFINE_BY_CONTAINER);
		while (getWarehousingFilterContainer().getChildren().size() > 0)
		{
			final Component comp = getWarehousingFilterContainer().getChildren().get(0);
			getWarehousingFilterContainer().removeChild(comp);
		}
		getEmptyFilters().setVisible(true);
	}

	/**
	 * Submits a new search on checkbox selection.
	 *
	 * @param event
	 * 		the click event on one of the checkboxes
	 */
	protected void submitFilter(final Event event)
	{
		final AdvancedSearchInitContext context = getModel().getValue(CONTEXT, AdvancedSearchInitContext.class);
		final AdvancedSearchData advancedSearchData = context.getAdvancedSearchData();
		final Map<String, List<SearchConditionData>> searchFieldConditions = new HashMap<>();

		final List<SearchConditionData> conditions = advancedSearchData
				.getConditions(AdvancedSearchData.ORPHANED_SEARCH_CONDITIONS_KEY);
		if (CollectionUtils.isNotEmpty(conditions))
		{
			conditions.clear();
		}
		final List<Component> refineBySections = getWarehousingFilterContainer().getChildren();

		final List checkboxes = new ArrayList();
		refineBySections.forEach(component -> checkboxes
				.addAll(component.getChildren().stream().filter(child -> child instanceof Checkbox).collect(Collectors.toList())));

		getModel().setValue(CURRENT_SEARCH, null);
		checkboxes.stream().filter(component -> ((Checkbox) component).isChecked())
				.forEach(component -> addSearchCondition((Checkbox) component, searchFieldConditions, advancedSearchData));
		addConditionList(advancedSearchData, searchFieldConditions);

		sendOutput(SOCKET_OUT_OUTPUT_CONTEXT, context);
	}

	protected RefineBy getRefineByConfig()
	{
		return refineByConfig;
	}

	public void setRefineByConfig(final RefineBy refineByConfig)
	{
		this.refineByConfig = refineByConfig;
	}

	protected Div getWarehousingFilterContainer()
	{
		return warehousingFilterContainer;
	}

	public void setWarehousingFilterContainer(final Div warehousingFilterContainer)
	{
		this.warehousingFilterContainer = warehousingFilterContainer;
	}

	protected Label getEmptyFilters()
	{
		return emptyFilters;
	}

	public void setEmptyFilters(final Label emptyFilters)
	{
		this.emptyFilters = emptyFilters;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}
}
