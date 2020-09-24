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
package com.hybris.backoffice.config.impl;

import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;

import java.util.function.Consumer;

/**
 * Allows basic operations on Backoffice configuration media
 */
public interface BackofficeConfigurationMediaHelper
{
	/**
	 * Searches for Backoffice configuration media and creates one in case media is not found. <br>
	 *
	 * @param mediaCode code of {@link MediaModel} to be searched for
	 * @param mediaMime mime type used when new {@link MediaModel} is created
	 * @param newMediaInitializer initializer callback to call for new media created by this method
	 * @return {@link MediaModel} corresponding with given mediaCode
	 */
	MediaModel findOrCreateWidgetsConfigMedia(String mediaCode, String mediaMime, Consumer<MediaModel> newMediaInitializer);

	/**
	 * Searches for Backoffice configuration media and creates one in case media is not found. <br>
	 *
	 * @param mediaCode code of {@link MediaModel} to be searched for
	 * @param mediaMime mime type used when new {@link MediaModel} is created
	 * @return {@link MediaModel} corresponding with given mediaCode
	 */
	MediaModel findOrCreateWidgetsConfigMedia(String mediaCode, String mediaMime);

	/**
	 * Searches for Backoffice configuration media and removes all configurations if given mediaCode <br>
	 *     is ambiguous. <br>
	 *
	 * @param mediaCode code of {@link MediaModel} to be searched for
	 * @return {@link MediaModel} corresponding with given mediaCode
	 */
	MediaModel findWidgetsConfigMedia(String mediaCode);

	/**
	 * Creates Backoffice configuration media and in case media cannot be created <br>
	 *     returns existing media with given code
	 *
	 * @param mediaCode code of {@link MediaModel} to be  created
	 * @param mediaMime mime type used when new {@link MediaModel} is created
	 * @return {@link MediaModel} corresponding with given mediaCode
	 */
	MediaModel createWidgetsConfigMedia(String mediaCode, String mediaMime);


	/**
	 * The method tries to load the Media Folder of {@code 'backofficeconfiguration'} qualifier. If the folder is found it
	 * is returned. Otherwise the method tries to create it and return. In case of a race condition, where two subsequent
	 * saves happen and the method fails to save the folder, it re-tries the fetch and return before it finally fails.
	 *
	 * @return the standard secure media folder for backoffice configurations
	 * @see MediaFolderModel
	 */
	MediaFolderModel getSecureFolder();

}
