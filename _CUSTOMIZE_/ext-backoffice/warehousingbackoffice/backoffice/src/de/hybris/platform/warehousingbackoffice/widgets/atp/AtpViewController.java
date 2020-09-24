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

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.atp.formula.services.AtpFormulaService;
import de.hybris.platform.warehousing.atp.services.impl.WarehousingCommerceStockService;
import de.hybris.platform.warehousing.model.AtpFormulaModel;
import de.hybris.platform.warehousingbackoffice.dtos.AtpFormDto;
import de.hybris.platform.warehousingbackoffice.dtos.AtpViewDto;

import org.springframework.util.Assert;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Displays atp formulas based on the form input collected by the user
 */
public class AtpViewController extends DefaultWidgetController
{
	protected static final String WAREHOUSINGBACKOFFICE_ATP_VIEWS_TITLE = "warehousingbackoffice.atp.views.title";
	protected static final String IN_SOCKET = "atpFormInput";
	protected Component widgetComponent;

	@Wire
	private Listbox atpListView;

	@WireVariable
	private transient WarehousingCommerceStockService warehousingCommerceStockService;

	@WireVariable
	private transient AtpFormulaService atpFormulaService;

	@Override
	public void initialize(final Component component)
	{
		//Hides the list view before the form is populated
		component.setVisible(false);
		this.widgetComponent = component;
	}

	/**
	 * Initializes the list view.
	 *
	 * @param atpFormDto
	 * 		the form containing atp information
	 */
	@SocketEvent(socketId = IN_SOCKET)
	public void initializeView(final AtpFormDto atpFormDto)
	{
		validateParameterNotNull(atpFormDto, "Empty AtpFormDto object received");
		validateParameterNotNull(atpFormDto.getBaseStore(), "BaseStore cannot be null");
		validateParameterNotNull(atpFormDto.getProduct(), "Product cannot be null");

		widgetComponent.setVisible(true);
		getWidgetInstanceManager().setTitle(getLabel(WAREHOUSINGBACKOFFICE_ATP_VIEWS_TITLE) + atpFormDto.getProduct().getCode());

		final ProductModel product = atpFormDto.getProduct();
		final BaseStoreModel baseStore = atpFormDto.getBaseStore();
		final List<AtpViewDto> atpViewEntries = new ArrayList<>();
		final Collection<AtpFormulaModel> atpFormulas = getAtpFormulaService().getAllAtpFormula();
		atpFormulas.forEach(atpFormula -> populateAtpFormula(atpFormDto, product, baseStore, atpViewEntries, atpFormula));

		getAtpListView().setModel(new ListModelArray<>(atpViewEntries));
	}

	/**
	 * Populates the {@link AtpFormulaModel} with the provided fields.
	 *
	 * @param atpFormDto
	 * @param product
	 * @param baseStore
	 * @param atpViewEntries
	 * @param atpFormula
	 */
	protected void populateAtpFormula(final AtpFormDto atpFormDto, final ProductModel product, final BaseStoreModel baseStore,
			final List<AtpViewDto> atpViewEntries, final AtpFormulaModel atpFormula)
	{
		Boolean isActive;
		Long atp;
		if (atpFormDto.getPointOfService() != null)
		{
			final PointOfServiceModel pointOfService = atpFormDto.getPointOfService();
			Assert.isTrue(baseStore.getPointsOfService().contains(pointOfService),
					String.format("Selected Point of Service: [%s] does not belong to the selected BaseStore: [%s]",
							pointOfService.getName(), baseStore.getUid()));
			pointOfService.getBaseStore().setDefaultAtpFormula(atpFormula);
			atp = getWarehousingCommerceStockService().getStockLevelForProductAndPointOfService(product, pointOfService);
		}
		else
		{
			baseStore.setDefaultAtpFormula(atpFormula);
			atp = getWarehousingCommerceStockService().getStockLevelForProductAndBaseStore(product, baseStore);
		}

		isActive = atpFormula.getBaseStores().contains(baseStore);
		if (isActive)
		{
			atpViewEntries.add(new AtpViewDto(atpFormula, atp, Boolean.TRUE));
		}
	}

	protected WarehousingCommerceStockService getWarehousingCommerceStockService()
	{
		return warehousingCommerceStockService;
	}

	public void setWarehousingCommerceStockService(final WarehousingCommerceStockService warehousingCommerceStockService)
	{
		this.warehousingCommerceStockService = warehousingCommerceStockService;
	}

	protected AtpFormulaService getAtpFormulaService()
	{
		return atpFormulaService;
	}

	public void setAtpFormulaService(final AtpFormulaService atpFormulaService)
	{
		this.atpFormulaService = atpFormulaService;
	}

	protected Listbox getAtpListView()
	{
		return atpListView;
	}

}
