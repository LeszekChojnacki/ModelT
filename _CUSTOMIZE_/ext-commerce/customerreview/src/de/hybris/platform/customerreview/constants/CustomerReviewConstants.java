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
package de.hybris.platform.customerreview.constants;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.SingletonCreator;
import de.hybris.platform.util.config.ConfigIntf;

import org.apache.log4j.Logger;


/**
 * This class contain the constants used in the customerreview package
 */
public class CustomerReviewConstants extends GeneratedCustomerReviewConstants // NOSONAR
{
	private static final Logger LOG = Logger.getLogger(CustomerReviewConstants.class.getName());

	/**
	 * Defines the property keys of the properties.
	 */
	public static class KEYS
	{
		public static final String MINIMAL_RATING = "customerreview.minimalrating";
		public static final String MAXIMAL_RATING = "customerreview.maximalrating";

		private KEYS()
		{
			// prevent instantiation
		}
	}

	/**
	 * Defines the default values of all properties.
	 */
	public static class DEFAULTS
	{
		public static final double MINIMAL_RATING = 0.0;
		public static final double MAXIMAL_RATING = 5.0;

		private DEFAULTS()
		{
			// prevent instantiation
		}
	}

	/**
	 * Get the configuration from current tenant
	 */
	private final ConfigIntf config;
	/**
	 * Define minimal rating
	 */
	public volatile double MINRATING = DEFAULTS.MINIMAL_RATING;//NOPMD //NOSONAR
	/**
	 * Define maximal rating
	 */
	public volatile double MAXRATING = DEFAULTS.MAXIMAL_RATING;//NOPMD //NOSONAR

	@SuppressWarnings("deprecation")
	private CustomerReviewConstants()
	{
		final Tenant tenant = Registry.getCurrentTenantNoFallback();
		this.config = tenant.getConfig();
		this.MINRATING = getMinRating();
		this.MAXRATING = getMaxRating();
		// Register a listener for configuration changes during production.
		config.registerConfigChangeListener((final String key, final String value) ->
		{
			// Maybe min and max rating should not be changed in a running system!
			if (key.equals(KEYS.MINIMAL_RATING))
			{
				MINRATING = getMinRating();
			}
			else if (key.equals(KEYS.MAXIMAL_RATING))
			{
				MAXRATING = getMaxRating();
			}
		});
	}

	/**
	 * Gets a singleton instance of CustomerReviewConstants
	 *
	 * @return The customer review constants object
	 */
	public static CustomerReviewConstants getInstance()
	{
		return Registry.getSingleton(new SingletonCreator.Creator<CustomerReviewConstants>()
		{
			private final String SINGLETON_CREATOR_ID = CustomerReviewConstants.class.getName().intern(); //NOSONAR

			@Override
			protected String getID()
			{
				return SINGLETON_CREATOR_ID;
			}

			@Override
			protected CustomerReviewConstants create()
			{
				return new CustomerReviewConstants();
			}
		});
	}

	/**
	 * Gets the initial value of maximal rating
	 */
	private double getMaxRating()
	{
		double maxRating = 0;
		try
		{
			maxRating = config.getDouble(KEYS.MAXIMAL_RATING, DEFAULTS.MAXIMAL_RATING);
		}
		catch (final NumberFormatException e)
		{
			LOG.error("The parameter \"" + KEYS.MAXIMAL_RATING + "\" has illegal format ("
					+ Config.getParameter("customerreview.maximalrating") + "), using default value: " + DEFAULTS.MAXIMAL_RATING);
		}
		return maxRating;
	}

	/**
	 * Gets the initial value of minimal rating
	 */
	private double getMinRating()
	{
		double minRating = 0;
		try
		{
			minRating = config.getDouble(KEYS.MINIMAL_RATING, DEFAULTS.MINIMAL_RATING);
		}
		catch (final NumberFormatException e)
		{
			LOG.error("The parameter \"" + KEYS.MINIMAL_RATING + "\" has illegal format ("
					+ Config.getParameter("customerreview.minimalrating") + "), using default value: " + DEFAULTS.MINIMAL_RATING);
		}
		return minRating;
	}
}
