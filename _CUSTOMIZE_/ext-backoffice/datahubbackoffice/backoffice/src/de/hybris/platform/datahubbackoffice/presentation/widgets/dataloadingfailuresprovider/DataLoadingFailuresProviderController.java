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
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.dataloadingfailuresprovider;

import de.hybris.platform.datahubbackoffice.dataaccess.dataloading.DataLoadingActionConstants;
import de.hybris.platform.datahubbackoffice.dataaccess.dataloading.DataLoadingActionFieldSearchFacadeStrategy;

import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.widgets.advancedsearch.engine.AdvancedSearchQueryData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.DefaultWidgetController;
import com.hybris.datahub.dto.dataloading.DataLoadingActionData;

public class DataLoadingFailuresProviderController extends DefaultWidgetController
{
	@WireVariable("dataLoadingActionFieldSearchFacadeStrategy")
	protected transient DataLoadingActionFieldSearchFacadeStrategy fieldSearchFacade;

	@SocketEvent(socketId = "dataloadingfailuresinput")
	public void getDataLoadingFailures()
	{
		final AdvancedSearchQueryData.Builder builder = new AdvancedSearchQueryData.Builder(
				DataLoadingActionConstants.DATA_LOADING_ACTIONS_IN_ERROR_TYPE_CODE);
		builder.pageSize(30);
		final Pageable<DataLoadingActionData> pageable = fieldSearchFacade.search(builder.build());
		sendOutput("dataloadingfailuresoutput", pageable);
	}

	public void setFieldSearchFacade(final DataLoadingActionFieldSearchFacadeStrategy fieldSearchFacade)
	{
		this.fieldSearchFacade = fieldSearchFacade;
	}
}
