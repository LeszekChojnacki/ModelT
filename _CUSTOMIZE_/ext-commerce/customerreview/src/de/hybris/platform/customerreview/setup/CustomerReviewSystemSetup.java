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
package de.hybris.platform.customerreview.setup;

import de.hybris.platform.core.Constants;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.SearchRestrictionModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.customerreview.constants.CustomerReviewConstants;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


@SuppressWarnings("deprecation")
@SystemSetup(extension = CustomerReviewConstants.EXTENSIONNAME)
public class CustomerReviewSystemSetup
{
	private static final Logger LOG = Logger.getLogger(CustomerReviewSystemSetup.class);

	private static final String SEARCH_RESTRICTION_FRONT_END_REVIEWS = "FrontEnd_Reviews";

	private static final String SEARCH_RESTRICTION_CREATE_DEFAULT_KEY = "customerreview.searchrestrictions.create";

	private ModelService modelService;
	private TypeService typeService;
	private UserService userService;
	private SearchRestrictionService searchRestrictionService;
	private ConfigurationService configurationService;

	private String searchRestrictionCreateKey = SEARCH_RESTRICTION_CREATE_DEFAULT_KEY;

	public void setSearchRestrictionCreateKey(final String searchRestrictionCreateKey)
	{
		this.searchRestrictionCreateKey = searchRestrictionCreateKey;
	}

	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createSearchRestrictions(@SuppressWarnings("unused") final SystemSetupContext context)
	{
		final ComposedTypeModel restrictedType = getTypeService().getComposedTypeForClass(CustomerReviewModel.class);
		final UserGroupModel userGroupModel = getUserService().getUserGroupForUID(Constants.USER.CUSTOMER_USERGROUP);
		if (getConfigurationService().getConfiguration().getBoolean(searchRestrictionCreateKey))
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating essential data for " + CustomerReviewConstants.EXTENSIONNAME);
			}

			removeRestrictions(restrictedType, userGroupModel);

			final EnumerationValueModel enumerationValueModel = getTypeService().getEnumerationValue(
					CustomerReviewConstants.TC.CUSTOMERREVIEWAPPROVALTYPE,
					CustomerReviewConstants.Enumerations.CustomerReviewApprovalType.APPROVED);

			final SearchRestrictionModel searchRestriction = getModelService().create(SearchRestrictionModel.class);
			searchRestriction.setCode(SEARCH_RESTRICTION_FRONT_END_REVIEWS);
			searchRestriction.setActive(Boolean.TRUE);
			searchRestriction.setQuery("{approvalStatus}=" + enumerationValueModel.getPk());
			searchRestriction.setRestrictedType(restrictedType);
			searchRestriction.setPrincipal(userGroupModel);
			searchRestriction.setGenerate(Boolean.TRUE);
			getModelService().save(searchRestriction);
		}
		else
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Creating essential data for " + CustomerReviewConstants.EXTENSIONNAME + " skipped, due to '"
						+ searchRestrictionCreateKey + "' is not setup");
			}
			removeRestrictions(restrictedType, userGroupModel);
		}
	}

	/**
	 *
	 */
	private void removeRestrictions(final ComposedTypeModel restrictedType, final UserGroupModel userGroupModel)
	{
		final Collection<SearchRestrictionModel> restrictions = getSearchRestrictionService().getSearchRestrictions(userGroupModel,
				true, Collections.singleton(restrictedType));

		for (final SearchRestrictionModel r : restrictions)
		{
			if (r.getCode().equals(SEARCH_RESTRICTION_FRONT_END_REVIEWS))
			{
				modelService.remove(r);
			}
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

	protected TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
