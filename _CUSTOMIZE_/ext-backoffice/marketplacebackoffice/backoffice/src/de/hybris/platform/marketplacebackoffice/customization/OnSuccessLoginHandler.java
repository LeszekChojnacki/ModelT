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
package de.hybris.platform.marketplacebackoffice.customization;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.classification.ClassificationClassesResolverStrategy;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.marketplaceservices.model.VendorUserModel;
import de.hybris.platform.marketplaceservices.strategies.VendorCMSStrategy;
import de.hybris.platform.marketplaceservices.vendor.VendorService;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;


public class OnSuccessLoginHandler implements ApplicationListener<InteractiveAuthenticationSuccessEvent>, ApplicationContextAware,
		InitializingBean, DisposableBean
{
	private SessionService sessionService;
	private AbstractApplicationContext applicationContext;
	private VendorService vendorService;
	private SearchRestrictionService searchRestrictionService;
	private VendorCMSStrategy vendorCmsStrategy;
	private ClassificationClassesResolverStrategy classificationClassesResolverStrategy;

	@Override
	public void onApplicationEvent(final InteractiveAuthenticationSuccessEvent applicationEvent)
	{
		final Authentication userDetails = applicationEvent.getAuthentication();
		final Optional<VendorModel> optional = vendorService.getVendorByUserId(userDetails.getName());
		if (optional.isPresent())
		{
			final VendorModel vendor = optional.get();
			searchRestrictionService.disableSearchRestrictions();

			final Collection<CategoryModel> vendorCategories = getVendorService().getVendorCategories(vendor.getCode());

			final Collection<String> categories = vendorCategories.stream().map(category -> category.getPk().toString())
					.collect(Collectors.toList());

			categories.addAll(
					vendorCategories.stream().flatMap(category -> (classificationClassesResolverStrategy.resolve(category)).stream())
					.map(classifcation -> classifcation.getPk().toString()).collect(Collectors.toList()));

			final List<String> productCarouselComponents = getVendorCmsStrategy()
					.getVendorProductCarouselComponents(vendor).stream()
					.map(component -> component.getPk().toString()).collect(Collectors.toList());
			searchRestrictionService.enableSearchRestrictions();
			sessionService.setAttribute("vendorCategories", categories.isEmpty() ? Collections.singleton(PK.NULL_PK) : categories);
			sessionService.setAttribute("productCarouselComponents",
					productCarouselComponents.isEmpty() ? Collections.singleton(PK.NULL_PK) : productCarouselComponents);
		}
	}

	@Override
	public void destroy() throws Exception
	{
		final ApplicationEventMulticaster applicationEventMulticaster = getApplicationEventMulticaster();
		if (applicationEventMulticaster != null)
		{
			applicationEventMulticaster.removeApplicationListener(this);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		final ApplicationEventMulticaster applicationEventMulticaster = getApplicationEventMulticaster();
		if (applicationEventMulticaster != null)
		{
			applicationEventMulticaster.addApplicationListener(this);
		}
	}


	public ApplicationEventMulticaster getApplicationEventMulticaster()
	{
		return applicationContext.getParent().getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
				ApplicationEventMulticaster.class);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = (AbstractApplicationContext) applicationContext;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected VendorService getVendorService()
	{
		return vendorService;
	}

	@Required
	public void setVendorService(final VendorService vendorService)
	{
		this.vendorService = vendorService;
	}

	protected SearchRestrictionService getSearchRestrictionService()
	{
		return searchRestrictionService;
	}

	@Required
	public void setSearchRestrictionService(final SearchRestrictionService searchRestrictionService)
	{
		this.searchRestrictionService = searchRestrictionService;
	}

	protected VendorCMSStrategy getVendorCmsStrategy()
	{
		return vendorCmsStrategy;
	}

	@Required
	public void setVendorCmsStrategy(final VendorCMSStrategy vendorCmsStrategy)
	{
		this.vendorCmsStrategy = vendorCmsStrategy;
	}

	protected ClassificationClassesResolverStrategy getClassificationClassesResolverStrategy()
	{
		return classificationClassesResolverStrategy;
	}

	@Required
	public void setClassificationClassesResolverStrategy(
			final ClassificationClassesResolverStrategy classificationClassesResolverStrategy)
	{
		this.classificationClassesResolverStrategy = classificationClassesResolverStrategy;
	}

}
