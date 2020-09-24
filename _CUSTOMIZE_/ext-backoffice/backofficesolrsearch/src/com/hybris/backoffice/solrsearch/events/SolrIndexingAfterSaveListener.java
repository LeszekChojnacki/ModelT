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
package com.hybris.backoffice.solrsearch.events;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.TypeManager;
import de.hybris.platform.servicelayer.exceptions.ModelLoadingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.tx.AfterSaveEvent;
import de.hybris.platform.tx.AfterSaveListener;
import de.hybris.platform.util.ViewResultItem;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.ApplicationUtils;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


/**
 * @deprecated since 1808, functionality provided by an aspect
 *             {@linkplain com.hybris.backoffice.solrsearch.aspects.ObjectFacadeSolrIndexingAspect}
 */
@Deprecated
public class SolrIndexingAfterSaveListener implements AfterSaveListener
{
	private static final Logger LOG = LoggerFactory.getLogger(SolrIndexingAfterSaveListener.class);
	private ModelService modelService;
	private TypeService typeService;
	private SessionService sessionService;
	private UserService userService;
	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;
	private SolrIndexSynchronizationStrategy solrIndexSynchronizationStrategy;
	private Set<Integer> ignoredTypeCodes = new HashSet<>();

	@Override
	public void afterSave(final Collection<AfterSaveEvent> events)
	{
		try
		{
			tryAfterSave(events);
		}
		catch (final RuntimeException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Unexpected exception while executing afterSave method ", e);
			}
			else
			{
				LOG.warn(e.getMessage());
			}
		}
	}

	private void tryAfterSave(final Collection<AfterSaveEvent> events)
	{
		if (ApplicationUtils.isPlatformReady())
		{
			final Map<SolrIndexOperation, List<AfterSaveEvent>> eventsBySolrIndexOperation = events.stream().collect(
					Collectors.groupingBy(this::evaluateIndexOperation));

			if (eventsBySolrIndexOperation != null)
			{
				final Set<PK> removedPks = eventsBySolrIndexOperation
						.getOrDefault(SolrIndexOperation.REMOVE, Collections.emptyList()).stream().map(AfterSaveEvent::getPk)
						.collect(Collectors.toSet());

				prepareAndHandleEvents(SolrIndexOperation.REMOVE, removedPks);

				final Set<PK> changedPks = eventsBySolrIndexOperation
						.getOrDefault(SolrIndexOperation.CHANGE, Collections.emptyList()).stream().map(AfterSaveEvent::getPk)
						.collect(Collectors.toSet());

				changedPks.removeAll(removedPks);

				prepareAndHandleEvents(SolrIndexOperation.CHANGE, changedPks);
			}
		}
	}

	protected SolrIndexOperation evaluateIndexOperation(final AfterSaveEvent e)
	{
		return (e.getType() ^ AfterSaveEvent.REMOVE) == 0 ? SolrIndexOperation.REMOVE : SolrIndexOperation.CHANGE;
	}

	protected void prepareAndHandleEvents(final SolrIndexOperation operation, final Set<PK> events)
	{

		final Map<String, List<PK>> groupedByTypes = groupByTypes(events, operation);

		groupedByTypes.entrySet().stream()
				.filter(entry -> backofficeFacetSearchConfigService.isSolrSearchConfiguredForType(entry.getKey()))
				.forEach(group -> handleChanges(operation, group.getKey(), group.getValue()));
	}

	protected Map<String, List<PK>> groupByTypes(final Set<PK> events, final SolrIndexOperation solrIndexOperation)
	{
		return events.stream().filter(pk -> !getIgnoredTypeCodes().contains(Integer.valueOf(pk.getTypeCode())))
				.collect(Collectors.groupingBy(pk -> findTypeCode(solrIndexOperation, pk)));
	}

	protected String findTypeCode(final SolrIndexOperation solrIndexOperation, final PK pk)
	{
		String typeCode = null;

		if (solrIndexOperation != SolrIndexOperation.REMOVE)
		{
			try
			{
				final Object entity = modelService.get(pk);
				typeCode = getType(entity);
			}
			catch (final ModelLoadingException | JaloObjectNoLongerValidException e)
			{
				LOG.debug("Trying to update nonexistant item", e);
			}
		}

		if (typeCode == null)
		{
			try
			{
				final ComposedType composedType = TypeManager.getInstance().getRootComposedType(pk.getTypeCode());
				typeCode = composedType.getCode();
			}
			catch (final JaloItemNotFoundException e)
			{
				LOG.debug("TypeCode lookup failed", e);
			}
		}

		return typeCode;
	}

	public String getType(final Object object)
	{
		if (object == null)
		{
			return null;
		}
		if (object instanceof ItemModel)
		{
			return ((ItemModel) object).getItemtype();
		}
		else if (object instanceof HybrisEnumValue)
		{
			return ((HybrisEnumValue) object).getType();
		}
		else if (object instanceof ViewResultItem)
		{
			return ((ViewResultItem) object).getComposedType().getCode();
		}
		return object.getClass().getName();
	}

	protected void handleChange(final SolrIndexOperation solrIndexOperation, final String typeCode, final PK pk)
	{
		switch (solrIndexOperation)
		{
			case CHANGE:
				handleVariantsChange(solrIndexOperation, typeCode, pk);
				solrIndexSynchronizationStrategy.updateItem(typeCode, pk.getLongValue());
				break;

			case REMOVE:
				solrIndexSynchronizationStrategy.removeItem(typeCode, pk.getLongValue());
				break;

			default:
				break;
		}
	}

	protected void handleChanges(final SolrIndexOperation solrIndexOperation, final String typeCode, final List<PK> pks)
	{
		switch (solrIndexOperation)
		{
			case CHANGE:
				pks.forEach(pk -> handleVariantsChange(solrIndexOperation, typeCode, pk));
				solrIndexSynchronizationStrategy.updateItems(typeCode, pks);
				break;

			case REMOVE:
				solrIndexSynchronizationStrategy.removeItems(typeCode, pks);
				break;

			default:
				break;
		}
	}



	private void handleVariantsChange(final SolrIndexOperation solrIndexOperation, final String typeCode, final PK pk)
	{
		if (typeService.isAssignableFrom(ProductModel._TYPECODE, typeCode))
		{
			getSessionService().executeInLocalView(new SessionExecutionBody()
			{
				@Override
				public void executeWithoutResult()
				{
					try
					{
						final ProductModel productModel = modelService.get(pk);
						final Collection<VariantProductModel> variants = productModel.getVariants();
						if (CollectionUtils.isNotEmpty(variants))
						{
							variants.forEach(variant -> handleChange(solrIndexOperation, VariantProductModel._TYPECODE, variant.getPk()));
						}
					}
					catch (final ModelLoadingException e)
					{
						LOG.debug("Couldn't get variants after removing item, because item is missing", e);
					}
				}
			}, getUserService().getAdminUser());

		}
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	public SolrIndexSynchronizationStrategy getSolrIndexSynchronizationStrategy()
	{
		return solrIndexSynchronizationStrategy;
	}

	@Required
	public void setSolrIndexSynchronizationStrategy(final SolrIndexSynchronizationStrategy solrIndexSynchronizationStrategy)
	{
		this.solrIndexSynchronizationStrategy = solrIndexSynchronizationStrategy;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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

	public Set<Integer> getIgnoredTypeCodes()
	{
		return ignoredTypeCodes;
	}

	public void setIgnoredTypeCodes(final Set<Integer> ignoredTypeCodes)
	{
		this.ignoredTypeCodes = ignoredTypeCodes;
	}

	protected enum SolrIndexOperation
	{
		CHANGE, REMOVE
	}
}
