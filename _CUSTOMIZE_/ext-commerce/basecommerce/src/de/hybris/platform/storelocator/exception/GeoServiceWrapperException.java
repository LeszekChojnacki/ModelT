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
package de.hybris.platform.storelocator.exception;

import de.hybris.platform.storelocator.GeoWebServiceWrapper;

import java.util.HashMap;
import java.util.Map;


/**
 * Exception thrown by {@link GeoWebServiceWrapper}
 */
public class GeoServiceWrapperException extends RuntimeException
{

	private final String googleResponseCode;
	/**
	 * A directions request could not be successfully parsed. For example, the request may have been rejected if it
	 * contained more than the maximum number of waypoints allowed.
	 */
	public static final String G_GEO_BAD_REQUEST = "400";

	/**
	 * A geocoding, directions or maximum zoom level request could not be successfully processed, yet the exact reason
	 * for the failure is not known.
	 */
	public static final String G_GEO_SERVER_ERROR = "500";

	/**
	 * The HTTP q parameter was either missing or had no value. For geocoding requests, this means that an empty address
	 * was specified as input. For directions requests, this means that no query was specified in the input.
	 */
	public static final String G_GEO_MISSING_QUERY = "601";

	/**
	 * Synonym for G_GEO_MISSING_QUERY.
	 */
	public static final String G_GEO_MISSING_ADDRESS = "601";

	/**
	 * No corresponding geographic location could be found for the specified address. This may be due to the fact that
	 * the address is relatively new, or it may be incorrect.
	 */
	public static final String G_GEO_UNKNOWN_ADDRESS = "602";

	/**
	 * The geocode for the given address or the route for the given directions query cannot be returned due to legal or
	 * contractual reasons.
	 */
	public static final String G_GEO_UNAVAILABLE_ADDRESS = "603";

	/**
	 * The GDirections object could not compute directions between the points mentioned in the query. This is usually
	 * because there is no route available between the two points, or because we do not have data for routing in that
	 * region.
	 */
	public static final String G_GEO_UNKNOWN_DIRECTIONS = "604";
	public static final String G_GEO_REQUST_DENIED = "REQUEST_DENIED";
	/**
	 * The given key is either invalid or does not match the domain for which it was given.
	 */
	public static final String G_GEO_BAD_KEY = "610";

	/**
	 * The given key has gone over the requests limit in the 24 hour period or has submitted too many requests in too
	 * short a period of time. If you're sending multiple requests in parallel or in a tight loop, use a timer or pause
	 * in your code to make sure you don't send the requests too quickly.
	 */
	public static final String G_GEO_TOO_MANY_QUERIES = "620";

	public static final Map<String, String> errorMessages;

	static
	{
		errorMessages = new HashMap<>(); // NOSONAR
		errorMessages.put(G_GEO_REQUST_DENIED, "A directions request denied");
		errorMessages.put(G_GEO_BAD_REQUEST, "A directions request could not be successfully parsed");
		errorMessages.put(G_GEO_SERVER_ERROR, "Google service unreachable");
		errorMessages.put(G_GEO_MISSING_QUERY, "The HTTP q parameter was either missing or had no value");
		errorMessages.put(G_GEO_MISSING_ADDRESS, "The address in query was either missing or had no value");
		errorMessages.put(G_GEO_UNKNOWN_ADDRESS, "No corresponding geographic location could be found");
		errorMessages.put(G_GEO_UNAVAILABLE_ADDRESS,
				"The geocode for the given address or the route for the given directions query cannot be returned due to legal or contractual reasons");
		errorMessages.put(G_GEO_UNKNOWN_DIRECTIONS, "Could not compute directions between the points mentioned");
		errorMessages.put(G_GEO_BAD_KEY, "The given key is either invalid or does not match the domain for which it was given");
		errorMessages.put(G_GEO_TOO_MANY_QUERIES, "The given key has gone over the requests limit");
	}

	/**
	 * default constructor
	 */
	public GeoServiceWrapperException()
	{
		super();
		this.googleResponseCode = null;
	}

	/**
	 * inherited constructor
	 */
	public GeoServiceWrapperException(final String message, final Throwable nested)
	{
		super(message, nested);
		this.googleResponseCode = null;
	}

	/**
	 * inherited constructor
	 */
	public GeoServiceWrapperException(final String message)
	{
		super(message);
		this.googleResponseCode = null;
	}

	/**
	 * inherited constructor
	 */
	public GeoServiceWrapperException(final Throwable nested)
	{
		super(nested);
		this.googleResponseCode = null;
	}

	/**
	 * specific constructor
	 */
	public GeoServiceWrapperException(final String message, final String googleResponseCode)
	{
		super(message + errorMessages.get(googleResponseCode));
		this.googleResponseCode = googleResponseCode;
	}

	public String getGoogleResponseCode()
	{
		return googleResponseCode;
	}
}
