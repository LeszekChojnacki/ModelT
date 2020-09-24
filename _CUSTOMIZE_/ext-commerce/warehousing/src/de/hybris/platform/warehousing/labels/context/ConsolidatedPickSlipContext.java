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
package de.hybris.platform.warehousing.labels.context;

import de.hybris.platform.acceleratorservices.document.context.AbstractDocumentContext;
import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.warehousing.data.pickslip.ConsolidatedPickSlipFormEntry;
import de.hybris.platform.warehousing.inventoryevent.service.InventoryEventService;
import de.hybris.platform.warehousing.model.AllocationEventModel;
import de.hybris.platform.warehousing.model.InventoryEventModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.warehousing.constants.WarehousingConstants.CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME;
import static java.util.stream.Collectors.groupingBy;


/**
 * Context for ConsolidatedPickLabel generating and sorting the form entries.
 */
public class ConsolidatedPickSlipContext extends AbstractDocumentContext<BusinessProcessModel>
{
	private List<ConsolidatedPickSlipFormEntry> formEntries;
	private InventoryEventService inventoryEventService;
	private List<ConsignmentModel> consignmentList;
	private Comparator<ConsolidatedPickSlipFormEntry> consolidatedPickSlipComparator;
	private DateTool date;
	private EscapeTool escapeTool;

	@Override
	public void init(final BusinessProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
	{
		super.init(businessProcessModel, documentPageModel);
		consignmentList = extractConsignments(businessProcessModel);
		if (CollectionUtils.isNotEmpty(consignmentList))
		{
			final List<ConsignmentEntryModel> consignmentEntries = consignmentList.stream()
					.flatMap(c -> c.getConsignmentEntries().stream()).collect(Collectors.toList());

			setFormEntries(createPickSlipFormEntries(consignmentEntries));
		}
		this.date = new DateTool();
		this.escapeTool = new EscapeTool();
	}

	/**
	 * Escapes String to generate safe HTML
	 *
	 * @param stringToEscape
	 * 		String to escape
	 * @return escaped String
	 */
	public String escapeHtml(final String stringToEscape)
	{
		return escapeTool.html(stringToEscape);
	}

	/***
	 * Creates a list of {@link ConsignmentEntryModel}. Groups by product and bin and sorts using a comparator.
	 * @param consignmentEntries Consignment Entries
	 */
	protected List<ConsolidatedPickSlipFormEntry> createPickSlipFormEntries(final List<ConsignmentEntryModel> consignmentEntries)
	{
		final List<ConsolidatedPickSlipFormEntry> formEntryList = new ArrayList<>();

		final List<AllocationEventModel> allocationEvents = consignmentEntries.stream().map(this::extractAllocationEvents)
				.flatMap(Collection::stream).collect(Collectors.toList());

		final Map<ProductModel, Map<String, List<AllocationEventModel>>> eventMapMap = allocationEvents.stream()
				.collect(groupingBy(entry -> entry.getConsignmentEntry().getOrderEntry().getProduct(), groupingBy(this::extractBin)));

		// Formatting
		for (Map.Entry<ProductModel, Map<String, List<AllocationEventModel>>> entry : eventMapMap.entrySet())
		{
			final ProductModel product = entry.getKey();
			final Map<String, List<AllocationEventModel>> binsEntries = entry.getValue();

			for (Map.Entry<String, List<AllocationEventModel>> binEntries : binsEntries.entrySet())
			{
				final ConsolidatedPickSlipFormEntry formEntry = new ConsolidatedPickSlipFormEntry();
				final List<AllocationEventModel> entries = binEntries.getValue();

				formEntry.setProduct(product);
				formEntry.setBin(binEntries.getKey());
				formEntry.setQuantity(entries.stream().mapToLong(InventoryEventModel::getQuantity).sum());
				formEntry.setAllocationEvents(entries);
				formEntryList.add(formEntry);
			}
		}

		formEntryList.sort(getConsolidatedPickSlipComparator());

		return formEntryList;

	}

	/**
	 * Extracts the bin out of the {@link StockLevelModel} in the {@link AllocationEventModel}
	 *
	 * @param allocationEventModel
	 * 		the {@link AllocationEventModel}
	 * @return a string containing the bin
	 */
	protected String extractBin(final AllocationEventModel allocationEventModel)
	{
		return allocationEventModel.getStockLevel().getBin() != null ? allocationEventModel.getStockLevel().getBin() : "";
	}

	/**
	 * Extracts {@link ConsignmentModel} stored in the {@link BusinessProcessParameterModel} of the {@link BusinessProcessModel}
	 *
	 * @param businessProcessModel
	 * 		the businessProcess containing the consignments
	 * @return list of {@link ConsignmentModel}
	 */
	protected List<ConsignmentModel> extractConsignments(final BusinessProcessModel businessProcessModel)
	{
		final List<ConsignmentModel> consignments = new ArrayList<>();
		if (businessProcessModel.getContextParameters().iterator().hasNext())
		{
			final BusinessProcessParameterModel param = businessProcessModel.getContextParameters().iterator().next();
			if (CONSOLIDATED_CONSIGNMENTS_BP_PARAM_NAME.equals(param.getName()))
			{
				consignments.addAll((List<ConsignmentModel>) param.getValue());
			}
		}
		return consignments;
	}

	@Override
	protected BaseSiteModel getSite(final BusinessProcessModel businessProcessModel)
	{
		final List<ConsignmentModel> consignments = extractConsignments(businessProcessModel);
		return CollectionUtils.isNotEmpty(consignments) ? consignments.iterator().next().getOrder().getSite() : null;
	}

	@Override
	protected LanguageModel getDocumentLanguage(final BusinessProcessModel businessProcessModel)
	{
		final BaseSiteModel baseSite = getSite(businessProcessModel);
		return baseSite != null ? baseSite.getDefaultLanguage() : null;
	}

	/**
	 * Extracts the bin location of the product for the selected {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntryModel
	 * 		the {@link ConsignmentEntryModel} for which we request the Consolidated Pick Slip label
	 * @return The binCode location of the product
	 */
	protected Collection<AllocationEventModel> extractAllocationEvents(final ConsignmentEntryModel consignmentEntryModel)
	{
		final Collection<AllocationEventModel> allocationEvents = getInventoryEventService()
				.getAllocationEventsForConsignmentEntry(consignmentEntryModel);

		return allocationEvents;
	}

	/**
	 * Extracts the image url for the thumbnail of a {@link ProductModel}
	 *
	 * @param productModel
	 * 		the {@link ProductModel} for which we request Product Image
	 * @return the source url for the product thumbnail (expecting the relative path as a string)
	 */
	public String getProductImageURL(final ProductModel productModel)
	{
		return productModel.getThumbnail() != null ? productModel.getThumbnail().getDownloadURL() : "";
	}

	/**
	 * Returns the quantity of row for the amount of column based on the list of {@link ConsignmentModel}
	 *
	 * @param columns
	 * 		amount of columns needed
	 * @return amount of rows required minus one.
	 */
	public int getRowQuantity(final int columns)
	{
		return columns != 0 ? ((int) Math.ceil((double) consignmentList.size() / (double) columns)) - 1 : 0;
	}

	public DateTool getDate()
	{
		return date;
	}

	public List<ConsolidatedPickSlipFormEntry> getFormEntries()
	{
		return formEntries;
	}

	public void setFormEntries(final List<ConsolidatedPickSlipFormEntry> formEntries)
	{
		this.formEntries = formEntries;
	}

	public List<ConsignmentModel> getConsignmentList()
	{
		return consignmentList;
	}

	public void setConsignmentList(final List<ConsignmentModel> consignmentList)
	{
		this.consignmentList = consignmentList;
	}

	protected Comparator<ConsolidatedPickSlipFormEntry> getConsolidatedPickSlipComparator()
	{
		return consolidatedPickSlipComparator;
	}

	@Required
	public void setConsolidatedPickSlipComparator(final Comparator<ConsolidatedPickSlipFormEntry> consolidatedPickSlipComparator)
	{
		this.consolidatedPickSlipComparator = consolidatedPickSlipComparator;
	}

	protected InventoryEventService getInventoryEventService()
	{
		return inventoryEventService;
	}

	@Required
	public void setInventoryEventService(final InventoryEventService inventoryEventService)
	{
		this.inventoryEventService = inventoryEventService;
	}
}
