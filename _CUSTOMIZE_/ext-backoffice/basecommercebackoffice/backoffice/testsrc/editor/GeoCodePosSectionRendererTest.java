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
package de.hybris.platform.basecommerce.backoffice.editor;


import com.hybris.cockpitng.engine.WidgetInstanceManager;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.storelocator.GPS;
import de.hybris.platform.storelocator.GeoWebServiceWrapper;
import de.hybris.platform.storelocator.data.AddressData;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@UnitTest
public class GeoCodePosSectionRendererTest
{
	@Mock
	private GPS gps;

	@Mock
	private AddressData addressData;

	@Mock
	private GeoWebServiceWrapper geoServiceWrapper;

	@Mock
	private WidgetInstanceManager wim;

	@InjectMocks
	@Resource
	private GeoCodePosSectionRenderer renderer;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);

		//		Mockito.when(Labels.getLabel(Mockito.anyString(), Mockito.any(Object[].class))).thenReturn("");
	}

	@After
	public void tearDown()
	{
		// implement here code executed after each test
	}

	@Test
	public void desynchronizeViewWithNullsTest()
	{
		//given

		//when
		renderer.desynchronizeView(null, null, null);

		//then
		assertTrue("this is true", true);
	}

	@Test
	public void updatePointOfServiceWithNullsTest()
	{
		//given

		//when
		renderer.updatePointOfService(null, null, null);

		//then
		assertTrue("this is true", true);
	}

	@Test
	public void updatePointOfServiceWithLonLatTest()
	{
		//given
		final double latitude = 10d;
		final double longitude = 20d;
		final Date geoTimestamp = new Date();
		final PointOfServiceModel pos = new PointOfServiceModel();
		Mockito.when(gps.getDecimalLongitude()).thenReturn(longitude);
		Mockito.when(gps.getDecimalLatitude()).thenReturn(latitude);

		//when
		renderer.updatePointOfService(gps, geoTimestamp, pos);

		//then
		assertTrue(latitude == pos.getLatitude());
		assertTrue(longitude == pos.getLongitude());
		assertTrue(geoTimestamp.equals(pos.getGeocodeTimestamp()));
	}

	@Test
	public void geoCodeAddressWithNullsTest()
	{
		//given

		//when
		final boolean geocodingResult = renderer.geoCodeAddress(null, null, null);

		//then
		assertFalse(geocodingResult);

	}

	@Test
	public void geoCodeAddressWithNullsAddressNotNullTest()
	{
		//given
        Mockito.when(geoServiceWrapper.geocodeAddress(addressData)).thenReturn(gps);
        
		//when
		final boolean geocodingResult = renderer.geoCodeAddress(addressData, null, null);

		//then
		assertFalse(geocodingResult);

	}

	@Test
	public void geoCodeAddressTest()
	{
		//given
		final double latitude = 10d;
		final double longitude = 20d;
		Mockito.when(geoServiceWrapper.geocodeAddress(addressData)).thenReturn(gps);
		Mockito.when(gps.getDecimalLatitude()).thenReturn(latitude);
		Mockito.when(gps.getDecimalLongitude()).thenReturn(longitude);
		final PointOfServiceModel pos = new PointOfServiceModel();

		//when
		final boolean geocodingResult = renderer.geoCodeAddress(addressData, pos, wim);

		//then
		assertTrue(geocodingResult);
		assertTrue(latitude == pos.getLatitude());
		assertTrue(longitude == pos.getLongitude());
	}
}
