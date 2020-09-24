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
package com.hybris.pcmbackoffice.widgets.charts.facetchart;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;

import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;


public class CatalogVersionFacetChartFiltersRenderer implements FacetChartFiltersRenderer
{
	private static final String CATALOG_VERSION_SELECTED_VALUE = "catalogVersionSelectedValue";

	private static final String LABEL_CATALOG_VERSION = "solrchart.catalogversionfilterrenderer.catalog_version";

	private static final String FACET_NAME = "catalogVersion";

	private static final String SCLASS_BUTTON_REMOVE_FILTER = "ye-text-button ye-delete-btn";

	private int order = 100;

	private CatalogVersionService catalogVersionService;
	private UserService userService;
	private LabelService labelService;
	private PermissionFacade permissionFacade;

	private WidgetInstanceManager widgetInstanceManager;
	private BiConsumer<String, Set<String>> facetSelectionChange;

	@Override
	public void renderFilters(final WidgetInstanceManager widgetInstanceManager, final Div filterContainer,
			final BiConsumer<String, Set<String>> facetSelectionChange)
	{
		this.widgetInstanceManager = widgetInstanceManager;
		this.facetSelectionChange = facetSelectionChange;

		appendCatalogVersionLabel(filterContainer);
		appendRemoveFilterButton(filterContainer);
		appendChosenBox(filterContainer);
	}

	protected void appendRemoveFilterButton(final Div filterContainer)
	{
		final Button removeFilterButton = new Button();
		removeFilterButton.setClass(SCLASS_BUTTON_REMOVE_FILTER);
		removeFilterButton.addEventListener(Events.ON_CLICK, event -> removeFilters());
		removeFilterButton.setParent(filterContainer);
	}

	protected void appendCatalogVersionLabel(final Div filterContainer)
	{
		final Label catalogVersionLabel = new Label(Labels.getLabel(LABEL_CATALOG_VERSION));
		catalogVersionLabel.setParent(filterContainer);
	}

	protected void appendChosenBox(final Div filterContainer)
	{
		final Chosenbox chosenbox = new Chosenbox();
		chosenbox.setModel(getCatalogVersions());
		chosenbox.addEventListener(Events.ON_SELECT,
				event -> onSelectCatalogVersion(((SelectEvent<Chosenbox, String>) event).getSelectedObjects()));

		final Collection<String> selectedValues = readSelectedValues();
		final Set<String> selectedCatalogVersions = getSelectedCatalogVersions((ListModelList) chosenbox.getModel(),
				selectedValues);
		if (selectedValues.size() != selectedCatalogVersions.size())
		{
			storeSelectedValues(selectedCatalogVersions);
			if (selectedCatalogVersions.isEmpty())
			{
				facetSelectionChange.accept(FACET_NAME, selectedCatalogVersions);
				return;
			}
		}
		chosenbox.setSelectedObjects(selectedCatalogVersions);
		chosenbox.setParent(filterContainer);
	}

	protected ListModelList<String> getCatalogVersions()
	{
		final ListModelList<String> listModel = new ListModelList<>();
		listModel.setMultiple(true);
		listModel.addAll(catalogVersionService //
				.getAllReadableCatalogVersions(userService.getCurrentUser()).stream() //
				.filter(f -> permissionFacade.canReadInstance(f)) //
				.map(labelService::getObjectLabel) //
				.collect(Collectors.toSet()));
		return listModel;
	}

	protected Set<String> getSelectedCatalogVersions(final ListModelList<String> listModel,
			final Collection<String> selectedValues)
	{
		return selectedValues.stream() //
				.filter(listModel::contains) //
				.collect(Collectors.toSet());
	}

	protected void onSelectCatalogVersion(final Set<String> selectedCatalogVersions)
	{
		storeSelectedValues(selectedCatalogVersions);
		facetSelectionChange.accept(FACET_NAME, selectedCatalogVersions);
	}

	protected Collection<String> readSelectedValues()
	{
		final Collection<String> selectedValues = widgetInstanceManager.getModel().getValue(CATALOG_VERSION_SELECTED_VALUE,
				Collection.class);
		return selectedValues == null ? Collections.emptySet() : selectedValues;
	}

	protected void removeFilters()
	{
		storeSelectedValues(Collections.emptySet());
		facetSelectionChange.accept(FACET_NAME, Collections.emptySet());
	}

	protected void storeSelectedValues(final Collection<String> selectedItems)
	{
		widgetInstanceManager.getModel().setValue(CATALOG_VERSION_SELECTED_VALUE, selectedItems);
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public PermissionFacade getPermissionFacade()
	{
		return permissionFacade;
	}

	@Required
	public void setPermissionFacade(final PermissionFacade permissionFacade)
	{
		this.permissionFacade = permissionFacade;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(final int order)
	{
		this.order = order;
	}
}
