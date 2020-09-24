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
package de.hybris.platform.warehousing.asn.service.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.warehousing.asn.dao.AsnDao;
import de.hybris.platform.warehousing.asn.service.AsnService;
import de.hybris.platform.warehousing.asn.service.AsnWorkflowService;
import de.hybris.platform.warehousing.asn.strategy.AsnReleaseDateStrategy;
import de.hybris.platform.warehousing.asn.strategy.BinSelectionStrategy;
import de.hybris.platform.warehousing.enums.AsnStatus;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeEntryModel;
import de.hybris.platform.warehousing.model.AdvancedShippingNoticeModel;
import de.hybris.platform.warehousing.stock.services.WarehouseStockService;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.springframework.util.Assert.isTrue;


/**
 * The default implementation of {@link AsnService} will create the stock levels for the given Advanced Shipping Notice, based on given
 * strategies.
 */
public class DefaultAsnService implements AsnService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsnService.class);

	private ModelService modelService;
	private BinSelectionStrategy binSelectionStrategy;
	private AsnReleaseDateStrategy asnReleaseDateStrategy;
	private WarehouseStockService warehouseStockService;
	private AsnDao asnDao;
	private AsnWorkflowService asnWorkflowService;

	@Override
	public void processAsn(final AdvancedShippingNoticeModel asn)
	{
		validateParameterNotNullStandardMessage("asn", asn);
		isTrue(CollectionUtils.isNotEmpty(asn.getAsnEntries()),
				String.format("No entries found in given ASN: [%s]", asn.getInternalId()));

		final WarehouseModel warehouse = asn.getWarehouse();
		asn.getAsnEntries().forEach(
				asnEntry -> createStockLevel(asnEntry, warehouse, getAsnReleaseDateStrategy().getReleaseDateForStockLevel(asnEntry)));
	}

	@Override
	public AdvancedShippingNoticeModel confirmAsnReceipt(final String internalId)
	{
		validateParameterNotNullStandardMessage("internalId", internalId);
		final AdvancedShippingNoticeModel asn = getAsnForInternalId(internalId);

		if (!AsnStatus.CREATED.equals(asn.getStatus()))
		{
			throw new IllegalArgumentException("Only ASN in CREATED status can be updated as RECEIVED");
		}

		asn.setStatus(AsnStatus.RECEIVED);
		getModelService().save(asn);

		LOGGER.info("ASN identified by InternalID: {} has been received", asn.getInternalId());
		return asn;
	}

	@Override
	public AdvancedShippingNoticeModel getAsnForInternalId(final String internalId)
	{
		validateParameterNotNullStandardMessage("internalId", internalId);
		return getAsnDao().getAsnForInternalId(internalId);
	}

	@Override
	public List<StockLevelModel> getStockLevelsForAsn(final AdvancedShippingNoticeModel advancedShippingNotice)
	{
		validateParameterNotNullStandardMessage("advancedShippingNotice", advancedShippingNotice);
		return getAsnDao().getStockLevelsForAsn(advancedShippingNotice);
	}

	@Override
	public AdvancedShippingNoticeModel cancelAsn(final String internalId)
	{
		validateParameterNotNullStandardMessage("internalId", internalId);
		final AdvancedShippingNoticeModel asn = getAsnForInternalId(internalId);
		if (!AsnStatus.CREATED.equals(asn.getStatus()))
		{
			throw new IllegalArgumentException("Only ASN in CREATED status can be cancelled");
		}

		asn.setStatus(AsnStatus.CANCELLED);
		getModelService().save(asn);

		getAsnWorkflowService().startAsnCancellationWorkflow(asn);

		LOGGER.info("ASN identified by InternalID: {} has been cancelled", asn.getInternalId());
		return asn;
	}

	/**
	 * Creates {@link StockLevelModel} based on given {@link AdvancedShippingNoticeEntryModel}, {@link WarehouseModel}
	 * and release date. <br>
	 * Based on {@link BinSelectionStrategy}, product quantity taken from {@link AdvancedShippingNoticeEntryModel} can be divided for different bins. Therefore
	 * for each entry new stock level needs to be created.
	 *
	 * @param asnEntry
	 * 		{@link AdvancedShippingNoticeEntryModel} which keep information about {@link de.hybris.platform.core.model.product.ProductModel#CODE} and quantity
	 * @param warehouse
	 * 		{@link WarehouseModel} which needs to be added to stock level
	 * @param releaseDate
	 * 		the {@link StockLevelModel#RELEASEDATE}
	 */
	protected void createStockLevel(final AdvancedShippingNoticeEntryModel asnEntry, final WarehouseModel warehouse,
			final Date releaseDate)
	{
		final Map<String, Integer> bins = getBinSelectionStrategy().getBinsForAsnEntry(asnEntry);
		if (bins != null)
		{
			bins.entrySet().forEach(entry -> createStockLevel(asnEntry, warehouse, entry.getValue(), releaseDate, entry.getKey()));
		}
	}

	/**
	 * Creates {@link StockLevelModel} based on given {@link AdvancedShippingNoticeEntryModel}, {@link WarehouseModel},
	 * productQuantity, release date and bin.
	 *
	 * @param asnEntry
	 * 		asn entry which keep information about product code
	 * @param warehouse
	 * 		warehouse to be assigned to the new stock level
	 * @param productQuantity
	 * 		product quantity
	 * @param releaseDate
	 * 		release date
	 * @param bin
	 * 		bin to be assigned to the new stock level
	 */
	protected void createStockLevel(final AdvancedShippingNoticeEntryModel asnEntry, final WarehouseModel warehouse,
			final int productQuantity, final Date releaseDate, final String bin)
	{
		if (StringUtils.isNotEmpty(asnEntry.getProductCode()))
		{
			final StockLevelModel stockLevel = getWarehouseStockService()
					.createStockLevel(asnEntry.getProductCode(), warehouse, productQuantity, InStockStatus.NOTSPECIFIED, releaseDate,
							bin);

			stockLevel.setAsnEntry(asnEntry);
			getModelService().save(stockLevel);
		}
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected BinSelectionStrategy getBinSelectionStrategy()
	{
		return binSelectionStrategy;
	}

	@Required
	public void setBinSelectionStrategy(final BinSelectionStrategy binSelectionStrategy)
	{
		this.binSelectionStrategy = binSelectionStrategy;
	}

	protected AsnReleaseDateStrategy getAsnReleaseDateStrategy()
	{
		return asnReleaseDateStrategy;
	}

	@Required
	public void setAsnReleaseDateStrategy(final AsnReleaseDateStrategy asnReleaseDateStrategy)
	{
		this.asnReleaseDateStrategy = asnReleaseDateStrategy;
	}

	protected WarehouseStockService getWarehouseStockService()
	{
		return warehouseStockService;
	}

	@Required
	public void setWarehouseStockService(final WarehouseStockService warehouseStockService)
	{
		this.warehouseStockService = warehouseStockService;
	}

	protected AsnDao getAsnDao()
	{
		return asnDao;
	}

	@Required
	public void setAsnDao(final AsnDao asnDao)
	{
		this.asnDao = asnDao;
	}

	protected AsnWorkflowService getAsnWorkflowService()
	{
		return asnWorkflowService;
	}

	@Required
	public void setAsnWorkflowService(final AsnWorkflowService asnWorkflowService)
	{
		this.asnWorkflowService = asnWorkflowService;
	}
}
