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
package de.hybris.platform.warehousingbackoffice.actions.exportpointsofservice;

import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.storelocator.model.OpeningDayModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.model.SpecialOpeningDayModel;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.widgets.collectionbrowser.CollectionBrowserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Filedownload;


/**
 * Action to export a list of {@link PointOfServiceModel} as a CSV file so it can be later imported to Google Business.
 */
public class ExportPointsOfServiceAction implements CockpitAction<Map, Object>
{
	protected static final String CSV_HEADERS =
			"Store code,Business name,Address line 1,Address line 2,Locality,Administrative area,Country,Postal code,"
					+ "Latitude,Longitude,Primary phone,Website,Primary category,"
					+ "Sunday hours,Monday hours,Tuesday hours,Wednesday hours,Thursday hours,Friday hours,Saturday hours,"
					+ "Special hours";
	protected static final String MODEL_PAGEABLE = CollectionBrowserController.MODEL_PAGEABLE;
	private static final Logger LOG = LoggerFactory.getLogger(ExportPointsOfServiceAction.class);
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String TIME_FORMAT = "HH:mm";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

	@Resource
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

	@Override
	public ActionResult<Object> perform(final ActionContext<Map> actionContext)
	{
		final Pageable pageable = (Pageable) (actionContext.getData()).get(MODEL_PAGEABLE);
		final String csvContent = createCsv(pageable.getAllResults());
		writeBinaryResponse(csvContent);
		return new ActionResult<>(ActionResult.SUCCESS);
	}

	/**
	 * Writes a CSV file to the browser for download.
	 *
	 * @param csvContent
	 * 		a string representation of the contents of the file
	 */
	protected void writeBinaryResponse(final String csvContent)
	{
		Filedownload.save(csvContent, "text/comma-separated-values;charset=UTF-8", "list.csv");
		LOG.info("Points of Service CSV generated");
	}

	/**
	 * Builds a CSV based upon a list of {@link PointOfServiceModel}s
	 *
	 * @param pointsOfService
	 * 		list of {@link PointOfServiceModel}s
	 * @return a CSV string representation of the list of {@link PointOfServiceModel}s including column headers
	 */
	protected String createCsv(final List<PointOfServiceModel> pointsOfService)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(CSV_HEADERS + "\n");
		createCsvContent(stringBuilder, pointsOfService);
		return stringBuilder.toString();
	}

	/**
	 * Creates a CSV body representing a list of {@link PointOfServiceModel}s
	 *
	 * @param stringBuilder
	 * 		{@link StringBuilder} used to build CSV
	 * @param pointsOfService
	 * 		the {@link PointOfServiceModel}s to be represented as CSVs
	 */
	protected void createCsvContent(final StringBuilder stringBuilder, final List<PointOfServiceModel> pointsOfService)
	{
		pointsOfService.stream().filter(this::validatePointOfServiceMeetsRequirements).forEach(pointOfService -> {
			stringBuilder.append(prepareCsvValue(pointOfService.getName())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getDisplayName())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getLine1())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getLine2())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getTown())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getRegion().getName())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getCountry().getName())).append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getAddress().getPostalcode())).append(',');
			stringBuilder
					.append(pointOfService.getAddress().getLatitude() != null ? pointOfService.getAddress().getLatitude() : "" + ',');
			stringBuilder.append(
					pointOfService.getAddress().getLongitude() != null ? pointOfService.getAddress().getLongitude() : "" + ',');
			stringBuilder.append(pointOfService.getAddress().getPhone1()).append(',');
			final BaseSiteModel baseSiteModel;
			final String url =
					(baseSiteModel = pointOfService.getBaseStore().getCmsSites().stream().findFirst().orElse(null)) != null ?
							getSiteBaseUrlResolutionService().getMediaUrlForSite(baseSiteModel, true) :
							null;
			stringBuilder.append(url != null ? url : "").append(',');
			stringBuilder.append(prepareCsvValue(pointOfService.getBusinessCategory())).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.SUNDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.MONDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.TUESDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.WEDNESDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.THURSDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.FRIDAY)).append(',');
			stringBuilder.append(getDailySchedule(pointOfService, Calendar.SATURDAY)).append(',');

			if (pointOfService.getOpeningSchedule() != null && !pointOfService.getOpeningSchedule().getOpeningDays().isEmpty())
			{
				stringBuilder.append(prepareCsvValue(pointOfService.getOpeningSchedule().getOpeningDays().stream()
						.filter(day -> day instanceof SpecialOpeningDayModel)
						.map(day -> getSpecialDaySchedule((SpecialOpeningDayModel) day)).reduce((a, b) -> a + "," + b).orElse("")));
			}

			stringBuilder.append('\n');
		});
	}

	/**
	 * Returns true if the {@link PointOfServiceModel} contains or references all of the data required to pass minimum requirements
	 * for an import to Google My Business
	 *
	 * @param pointOfService
	 * 		the {@link PointOfServiceModel} to be validated
	 * @return whether or not the {@link PointOfServiceModel} meets the requirements of Google My Business
	 */
	protected boolean validatePointOfServiceMeetsRequirements(final PointOfServiceModel pointOfService)
	{
		boolean pointOfServiceMeetsRequirements = true;
		final BaseSiteModel baseSiteModel;
		final String url;

		if (pointOfService.getBaseStore() == null)
		{
			url = null;
		}
		else
		{
			url = (baseSiteModel = pointOfService.getBaseStore().getCmsSites().stream().findFirst().orElse(null)) != null ?
					getSiteBaseUrlResolutionService().getMediaUrlForSite(baseSiteModel, true) :
					null;
		}

		if (pointOfService.getAddress() == null)
		{
			pointOfServiceMeetsRequirements = false;
		}
		else if (pointOfService.getAddress().getRegion() == null || pointOfService.getAddress().getCountry() == null)
		{
			pointOfServiceMeetsRequirements = false;
		}
		else if (pointOfService.getAddress().getPhone1() == null || pointOfService.getAddress().getPhone1().isEmpty() && (
				url == null || url.isEmpty()))
		{
			pointOfServiceMeetsRequirements = false;
		}
		else if (pointOfService.getBusinessCategory() == null || pointOfService.getBusinessCategory()
				.isEmpty())
		{
			pointOfServiceMeetsRequirements = false;
		}

		return pointOfServiceMeetsRequirements;
	}

	/**
	 * Generates a formatted 24 hour representation of the opening hours of a {@link PointOfServiceModel} for a given day of the week
	 *
	 * @param pointOfService
	 * 		the {@link PointOfServiceModel} for which the opening hours must be represented
	 * @param dayOfWeek
	 * 		the day of the week specified as a {@link Calendar} constant
	 * @return a formatted 24 hour representation of the opening hours
	 */
	protected String getDailySchedule(final PointOfServiceModel pointOfService, final int dayOfWeek)
	{
		final Calendar calendar = Calendar.getInstance();
		final Calendar openingTime = Calendar.getInstance();
		final Calendar closingTime = Calendar.getInstance();
		if (pointOfService.getOpeningSchedule() != null)
		{
			final OpeningDayModel openingDay = pointOfService.getOpeningSchedule().getOpeningDays().stream().filter(day -> {
				if (!(day instanceof SpecialOpeningDayModel))
				{
					calendar.setTime(day.getOpeningTime());
					return calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
				}
				return false;
			}).findFirst().orElse(null);

			if (openingDay != null)
			{
				openingTime.setTime(openingDay.getOpeningTime());
				closingTime.setTime(openingDay.getClosingTime());

				return timeFormat.format(openingTime.getTime()) + "-" + timeFormat.format(closingTime.getTime());
			}
		}
		return "";
	}

	/**
	 * Provides the opening and closing times for a {@link SpecialOpeningDayModel} showing the date as well sa the hours, or an 'x'
	 * in the case of being closed.
	 *
	 * @param specialOpeningDayModel
	 * 		{@link SpecialOpeningDayModel} for which to show the date and opening times
	 * @return String of the format "yyyy-MM-dd: HH:mm-HH:mm" if open or "yyyy-MM-dd: x" if closed
	 */
	protected String getSpecialDaySchedule(final SpecialOpeningDayModel specialOpeningDayModel)
	{
		final Calendar calendar = Calendar.getInstance();
		final StringBuilder stringBuilder = new StringBuilder();
		calendar.setTime(specialOpeningDayModel.getDate());
		stringBuilder.append(dateFormat.format(calendar.getTime())).append(": ");

		if (specialOpeningDayModel.isClosed())
		{
			stringBuilder.append('x');
		}
		else
		{
			final Calendar openingTime = Calendar.getInstance();
			final Calendar closingTime = Calendar.getInstance();
			openingTime.setTime(specialOpeningDayModel.getOpeningTime());
			closingTime.setTime(specialOpeningDayModel.getClosingTime());

			stringBuilder.append(timeFormat.format(openingTime.getTime())).append("-")
					.append(timeFormat.format(closingTime.getTime()));
		}
		return stringBuilder.toString();
	}

	/**
	 * Prepares a value to be inserted into a CSV
	 *
	 * @param value
	 * 		the value to be prepared
	 * @return a CSV-ready value
	 */
	protected String prepareCsvValue(final String value)
	{
		return "\"" + value.trim() + "\"";
	}

	@Override
	public boolean canPerform(final ActionContext<Map> ctx)
	{
		return true;
	}

	@Override
	public boolean needsConfirmation(final ActionContext<Map> ctx)
	{
		return false;
	}

	@Override
	public String getConfirmationMessage(final ActionContext<Map> ctx)
	{
		return null;
	}

	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}
}
