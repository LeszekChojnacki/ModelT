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
package de.hybris.platform.promotionengineservices.promotionengine.report.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.union;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderEntryLevelPromotionEngineResults;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.OrderLevelPromotionEngineResults;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResult;
import de.hybris.platform.promotionengineservices.promotionengine.report.data.PromotionEngineResults;
import de.hybris.platform.promotions.model.PromotionOrderEntryConsumedModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;


/**
 * Populator assigns promotions results associated with {@link AbstractOrderModel} that aren't related to discounts to
 * either order or order entry level promotions lists and stores results into {@link PromotionEngineResults}
 */
public class NonDiscountPromotionEngineResultsClassifyingPopulator
		implements Populator<AbstractOrderModel, PromotionEngineResults>
{
	private Converter<PromotionResultModel, PromotionEngineResult> promotionResultConverter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populate(final AbstractOrderModel source, final PromotionEngineResults target)
	{
		checkArgument(nonNull(source), "Source cannot be null");
		checkArgument(nonNull(target), "Target cannot be null");
		final Set<PromotionResultModel> nonDiscountPromotions = collectNonDiscountPromotions(source.getAllPromotionResults(),
				target.getOrderLevelPromotionEngineResults(), target.getOrderEntryLevelPromotionEngineResults());
		final List<PromotionEngineResult> nonDiscountPromotionsResult = getPromotionResultConverter()
				.convertAll(nonDiscountPromotions);
		final List<PromotionEngineResult> allowedPromotionResults = nonDiscountPromotionsResult.stream()
				.filter(promotionEngineResult -> promotionEngineResult.isFired()).collect(toList());
		classify(allowedPromotionResults, target);
		cleanup(target);
	}

	protected void cleanup(final PromotionEngineResults target)
	{
		if (isNotEmpty(target.getOrderEntryLevelPromotionEngineResults()))
		{
			target.getOrderEntryLevelPromotionEngineResults().removeIf(result -> isEmpty(result.getPromotionEngineResults()));
		}
	}

	/**
	 * Tries to classify and assign promotion either as an order or an order entry level promotion using heuristic
	 * approach. These promotion results that has assigned some consumed entries and all refers to the same order entry
	 * will be appended to the an order entry level promotions otherwise will be classsfied as order level promotions
	 *
	 * @param promotionsToClassify
	 * @param target
	 */
	protected void classify(final List<PromotionEngineResult> promotionsToClassify, final PromotionEngineResults target)
	{

		if (isEmpty(promotionsToClassify))
		{
			return;
		}
		final Map<Boolean, List<PromotionEngineResult>> groupedPromotionsResult = promotionsToClassify.stream()
				.collect(groupingBy(this::isOrderEntryRelated));

		final List<PromotionEngineResult> orderEntryLevelPromotionsResult = groupedPromotionsResult.get(Boolean.TRUE);
		if (isNotEmpty(orderEntryLevelPromotionsResult))
		{
			updateOrderEntryLevelPromotions(orderEntryLevelPromotionsResult, target);
		}

		final List<PromotionEngineResult> orderLevelPromotionsResultToAppend = groupedPromotionsResult.get(Boolean.FALSE);
		if (isNotEmpty(orderLevelPromotionsResultToAppend))
		{
			updateOrderLevelPromotions(orderLevelPromotionsResultToAppend, target);
		}
	}

	/**
	 * Updates order entry level promotions list based on the list of additional promotions that need to be added
	 *
	 * @param toAppend
	 *           - promotions that need to assigned as order entry level promotions
	 * @param target
	 *           - promotions result that needs to be updated
	 */
	protected void updateOrderEntryLevelPromotions(final List<PromotionEngineResult> toAppend, final PromotionEngineResults target)
	{
		final Map<AbstractOrderEntryModel, List<PromotionEngineResult>> orderEntryRelatedPromotions = toAppend.stream()
				.collect(groupingBy(orderEntry()));
		for (final OrderEntryLevelPromotionEngineResults result : target.getOrderEntryLevelPromotionEngineResults())
		{
			final List<PromotionEngineResult> promotionsResultToAppend = orderEntryRelatedPromotions.get(result.getOrderEntry());
			if (isNotEmpty(promotionsResultToAppend))
			{
				final List<PromotionEngineResult> union = isNotEmpty(result.getPromotionEngineResults())
						? union(result.getPromotionEngineResults(), promotionsResultToAppend) : promotionsResultToAppend;
				result.setPromotionEngineResults(union);
			}
		}
	}

	/**
	 * Updates order entry level promotions list based on the list of additional promotions that need to be added
	 *
	 * @param toAppend
	 *           - promotions that need to assigned as order level promotions
	 * @param target
	 *           - promotions result that needs to be updated
	 */
	protected void updateOrderLevelPromotions(final List<PromotionEngineResult> toAppend, final PromotionEngineResults target)
	{
		if (isNotEmpty(toAppend))
		{
			final OrderLevelPromotionEngineResults orderLevelPromotion = target.getOrderLevelPromotionEngineResults();
			final List<PromotionEngineResult> promotionEngineResults = orderLevelPromotion.getPromotionEngineResults();
			final List<PromotionEngineResult> union = isNotEmpty(promotionEngineResults) ? union(promotionEngineResults, toAppend)
					: toAppend;
			target.getOrderLevelPromotionEngineResults().setPromotionEngineResults(union);
		}
	}

	/**
	 * Provides function that provide an order entry associated with {@link PromotionEngineResult}
	 *
	 */
	protected Function<PromotionEngineResult, AbstractOrderEntryModel> orderEntry()
	{
		return promotionResult -> promotionResult.getPromotionResult().getConsumedEntries().iterator().next().getOrderEntry();
	}

	/**
	 * Checks if promotion results get be identified as order entry related
	 *
	 * @param promotionEngineResult
	 * @return true - if promotion engine result is order entry related, otherwise false
	 */
	protected boolean isOrderEntryRelated(final PromotionEngineResult promotionEngineResult)
	{
		final Collection<PromotionOrderEntryConsumedModel> consumedEntries = promotionEngineResult.getPromotionResult()
				.getConsumedEntries();
		return hasConsumedEntries(consumedEntries) && hasSameOrderEntry(consumedEntries);
	}

	/**
	 * Checks if all consumed entries are related to a single order entry
	 *
	 * @param consumedEntries
	 * @return true if all consumedEntries are related to the same order entry, otherwise false
	 */
	protected boolean hasSameOrderEntry(final Collection<PromotionOrderEntryConsumedModel> consumedEntries)
	{
		return consumedEntries.stream().map(PromotionOrderEntryConsumedModel::getOrderEntry).filter(Objects::nonNull).distinct()
				.count() == 1;
	}

	protected boolean hasConsumedEntries(final Collection<PromotionOrderEntryConsumedModel> consumedEntries)
	{
		return isNotEmpty(consumedEntries);
	}

	protected Set<PromotionResultModel> collectNonDiscountPromotions(final Set<PromotionResultModel> allPromotions,
			final OrderLevelPromotionEngineResults orderDiscountPromotions,
			final List<OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotions)
	{
		final Set<PromotionResultModel> convertedPromotions = collectConvertedPromotionsResult(orderDiscountPromotions,
				orderEntryDiscountPromotions);

		final Set<PromotionResultModel> result = isNotEmpty(allPromotions) ? newHashSet(allPromotions) : newHashSet();
		result.removeAll(convertedPromotions);
		return result;
	}

	protected Set<PromotionResultModel> collectConvertedPromotionsResult(
			final OrderLevelPromotionEngineResults orderDiscountPromotions,
			final List<OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotions)
	{
		final Stream<PromotionEngineResult> orderPromotions = toPromotionEngineResultsStream(orderDiscountPromotions);
		final Stream<PromotionEngineResult> orderEntryPromotions = toPromotionEngineResultsStream(orderEntryDiscountPromotions);
		return concat(orderPromotions, orderEntryPromotions).map(PromotionEngineResult::getPromotionResult).collect(toSet());
	}

	protected Stream<PromotionEngineResult> toPromotionEngineResultsStream(
			final List<OrderEntryLevelPromotionEngineResults> orderEntryDiscountPromotions)
	{
		return isNotEmpty(orderEntryDiscountPromotions)
				? orderEntryDiscountPromotions.stream().filter(oedp -> Objects.nonNull(oedp.getPromotionEngineResults()))
						.flatMap(oedp -> oedp.getPromotionEngineResults().stream())
				: Stream.empty();
	}

	protected Stream<PromotionEngineResult> toPromotionEngineResultsStream(
			final OrderLevelPromotionEngineResults orderDiscountPromotions)
	{
		return null != orderDiscountPromotions && isNotEmpty(orderDiscountPromotions.getPromotionEngineResults())
				? Stream.of(orderDiscountPromotions.getPromotionEngineResults()).flatMap(l -> l.stream()) : Stream.empty();
	}

	protected Converter<PromotionResultModel, PromotionEngineResult> getPromotionResultConverter()
	{
		return promotionResultConverter;
	}

	@Required
	public void setPromotionResultConverter(final Converter<PromotionResultModel, PromotionEngineResult> promotionResultConverter)
	{
		this.promotionResultConverter = promotionResultConverter;
	}
}
