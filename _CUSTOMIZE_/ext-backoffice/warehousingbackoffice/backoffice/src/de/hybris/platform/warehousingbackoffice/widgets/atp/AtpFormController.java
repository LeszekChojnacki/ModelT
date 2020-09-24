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
 *
 */
package de.hybris.platform.warehousingbackoffice.widgets.atp;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousingbackoffice.dtos.AtpFormDto;

import java.util.ArrayList;
import java.util.List;

import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.util.DefaultWidgetController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;


/**
 * A form for the user to enter required information to search for a particular ATP formula.
 */
public class AtpFormController extends DefaultWidgetController
{
	protected static final String WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_MISSING_BASE_STORE = "warehousingbackoffice.atpform.validation.missing.base.store";
	protected static final String WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_MISSING_PRODUCT_CODE = "warehousingbackoffice.atpform.validation.missing.product.code";
	protected static final String WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_VALID_POS = "warehousingbackoffice.atpform.validation.valid.pos";
	protected static final String ON_STORE_CHANGE = "onStoreChange";
	protected static final String ON_LATER_STORE_CHANGE = "onLaterStoreChange";
	protected static final String ON_POS_CHANGE = "onPoSChange";
	protected static final String ON_LATER_POS_CHANGE = "onLaterPoSChange";
	protected static final String SEARCH_ATP = "searchAtp";
	protected static final String WAREHOUSINGBACKOFFICE_ATPFORM_TITLE = "warehousingbackoffice.atpform.title";
	protected static final String OUT_CONFIRM = "populatedAtpForm";
	protected static final String SELECT_A_POINT_OF_SERVICE = "warehousingbackoffice.atpform.pos.placeholder";
	protected static final String SELECT_A_BASE_STORE = "warehousingbackoffice.atpform.store.placeholder";

	@Wire
	private Combobox baseStores;
	@Wire
	private Combobox pointOfServices;
	@Wire
	private Editor product;

	@WireVariable
	private transient BaseStoreService baseStoreService;

	@Override
	public void initialize(final Component component)
	{
		getWidgetInstanceManager().setTitle(getLabel(WAREHOUSINGBACKOFFICE_ATPFORM_TITLE));
		refreshForm();
	}

	/**
	 * Refreshes the form
	 */
	protected void refreshForm()
	{
		final List baseStoreList = new ArrayList();
		baseStoreList.add(getLabel(SELECT_A_BASE_STORE));
		baseStoreList.addAll(getBaseStoreService().getAllBaseStores());
		getBaseStores().setModel(new ListModelArray<>(baseStoreList));
		getBaseStores().setValue(getLabel(SELECT_A_BASE_STORE));
		addListeners();
	}

	/**
	 * Populates the {@link AtpFormDto} with the user input and sends
	 * it as an event to be displayed in a list.
	 */
	@ViewEvent(componentID = SEARCH_ATP, eventName = Events.ON_CLICK)
	public void performSearchOperation()
	{
		validateForm();
		final BaseStoreModel selectedBaseStore = getBaseStores().getSelectedItem().getValue();
		final PointOfServiceModel selectedPos = (getPointOfServices().getSelectedItem() != null && getPointOfServices() //NOSONAR
				.getSelectedItem().getValue() instanceof PointOfServiceModel) ?
				((PointOfServiceModel) getPointOfServices().getSelectedItem().getValue()) : null;
		final AtpFormDto atpForm = new AtpFormDto(((ProductModel) getProduct().getValue()), selectedBaseStore, selectedPos);
		sendOutput(OUT_CONFIRM, atpForm);
	}

	/**
	 * Adds listeners on all components which will receive user input.
	 */
	protected void addListeners()
	{
		getBaseStores().addEventListener(ON_STORE_CHANGE,
				event -> Events.echoEvent(ON_LATER_STORE_CHANGE, getBaseStores(), event.getData()));
		getBaseStores().addEventListener(ON_LATER_STORE_CHANGE, event -> {
			if (getBaseStores().getSelectedItem() != null && getBaseStores().getSelectedItem() //NOSONAR
					.getValue() instanceof BaseStoreModel)
			{
				final List pointOfServiceList = new ArrayList();
				pointOfServiceList.add(getLabel(SELECT_A_POINT_OF_SERVICE));
				pointOfServiceList.addAll(((BaseStoreModel) getBaseStores().getSelectedItem().getValue()).getPointsOfService());
				getPointOfServices().setModel(new ListModelArray<>(pointOfServiceList));
				getPointOfServices().setValue(getLabel(SELECT_A_POINT_OF_SERVICE));
			}
			Clients.clearWrongValue(getBaseStores());
			getBaseStores().invalidate();
		});

		getPointOfServices().addEventListener(ON_POS_CHANGE,
				event -> Events.echoEvent(ON_LATER_POS_CHANGE, getPointOfServices(), event.getData()));
		getPointOfServices().addEventListener(ON_LATER_POS_CHANGE, event -> {
			Clients.clearWrongValue(getPointOfServices());
			getPointOfServices().invalidate();
		});
	}

	/**
	 * `
	 * Validates the form and verifies whether the user input is correct.
	 */
	protected void validateForm()
	{
		if (getProduct().getValue() == null || ((ProductModel) getProduct().getValue()).getCode().isEmpty())
		{
			throw new WrongValueException(getProduct(), getLabel(WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_MISSING_PRODUCT_CODE));
		}

		if (getBaseStores().getSelectedItem() == null || !(getBaseStores().getSelectedItem() //NOSONAR
				.getValue() instanceof BaseStoreModel))
		{
			throw new WrongValueException(getBaseStores(), getLabel(WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_MISSING_BASE_STORE));
		}

		if (getPointOfServices().getSelectedItem() != null && getPointOfServices().getSelectedItem().getValue() != getLabel(
				SELECT_A_POINT_OF_SERVICE) && !((BaseStoreModel) getBaseStores().getSelectedItem().getValue()).getPointsOfService()
				.contains(getPointOfServices().getSelectedItem().getValue()))
		{
			throw new WrongValueException(getPointOfServices(), getLabel(WAREHOUSINGBACKOFFICE_ATPFORM_VALIDATION_VALID_POS));
		}
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	protected Combobox getBaseStores()
	{
		return baseStores;
	}

	protected Combobox getPointOfServices()
	{
		return pointOfServices;
	}

	protected Editor getProduct()
	{
		return product;
	}
}
