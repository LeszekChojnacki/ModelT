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
package de.hybris.platform.warehousing.labels.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;

import java.util.Set;


public class PackContext extends CommonPrintLabelContext
{
   private Set<ConsignmentEntryModel> consignmentEntries;

   @Override
   public void init(final ConsignmentProcessModel businessProcessModel, final DocumentPageModel documentPageModel)
   {
      super.init(businessProcessModel,documentPageModel);
      final ConsignmentModel consignment = businessProcessModel.getConsignment();
      consignmentEntries = consignment.getConsignmentEntries();
   }

   public Set<ConsignmentEntryModel> getConsignmentEntries()
   {
      return consignmentEntries;
   }

   /**
    * Extracts the image url for the thumbnail of the product for the selected consignmentEntry
    *
    * @param consignmentEntryModel
    *           the consignment model for which we request the Pick Slip label
    * @return the source url for the product thumbnail (expecting the relative path as a string)
    */
   public String getProductImageURL(final ConsignmentEntryModel consignmentEntryModel)
   {
      String path = null;
      final MediaModel media = consignmentEntryModel.getOrderEntry().getProduct().getThumbnail();
      if (media != null)
      {
         path = media.getDownloadURL();
      }
      return path;
   }

}
