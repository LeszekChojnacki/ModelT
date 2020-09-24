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
package de.hybris.platform.stock;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.stock.exception.InsufficientStockLevelException;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Service for 'Stock Level Management'. It offers ...
 * <ul>
 * <li>updating Stock Levels (based on the specified {@link StockLevelModel}.
 * <li>searching for Stock Levels (based on the specified {@link StockLevelModel}.
 * <li>reserving and releasing Stock Levels (based on the specified {@link StockLevelModel}.
 * <li>checking for available amount (based on the specified {@link StockLevelModel}.
 * </ul>
 *
 * @spring.bean stockService
 */
public interface StockService
{
	String BEAN_NAME = "stockService";

	/**
	 * Checks the stock level status of the product in the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		warehouse of the product
	 * @return stock level status
	 */
	StockLevelStatus getProductStatus(ProductModel product, WarehouseModel warehouse);

	/**
	 * Checks the stock level status of the product in all specified warehouses.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		warehouses of the product
	 * @return stock level status
	 */
	StockLevelStatus getProductStatus(ProductModel product, Collection<WarehouseModel> warehouses);

	/**
	 * Finds the stock levels of the specified product from all warehouses, and calculates the total actual amount.
	 *
	 * @param product
	 * 		the product
	 * @return calculated actual amount of the specified product
	 * @throws de.hybris.platform.stock.exception.StockLevelNotFoundException
	 * 		if no such {@link StockLevelModel} can be found
	 */
	int getTotalStockLevelAmount(ProductModel product);

	/**
	 * Finds all stock levels of the specified product in the specified warehouses, and calculates the total actual
	 * amount.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		the warehouses
	 * @return calculated actual amount of the specified product in the warehouses
	 * @throws de.hybris.platform.stock.exception.StockLevelNotFoundException
	 * 		if no such {@link StockLevelModel} can be found
	 */
	int getTotalStockLevelAmount(ProductModel product, Collection<WarehouseModel> warehouses);

	/**
	 * Finds the stock level of the specified product in the specified warehouse, and calculates the actual available
	 * amount.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		warehouse of the product
	 * @return actual available amount for the specific product
	 * @throws de.hybris.platform.stock.exception.StockLevelNotFoundException
	 * 		if no such {@link StockLevelModel} can be found
	 */
	int getStockLevelAmount(ProductModel product, WarehouseModel warehouse);

	/**
	 * Updates the actual stock level of the specified product in the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse where product is 'stored'
	 * @param actualAmount
	 * 		the actual amount of the product
	 * @param comment
	 * 		the comment for the update operation
	 */
	void updateActualStockLevel(ProductModel product, WarehouseModel warehouse, int actualAmount, String comment);

	/**
	 * Reserves the product in the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse where product is 'stored'
	 * @param amount
	 * 		the amount of the reservation
	 * @param comment
	 * 		the comment for the reservation
	 * @throws InsufficientStockLevelException
	 * 		if not enough products are available in the stock
	 */
	void reserve(ProductModel product, WarehouseModel warehouse, int amount, String comment)
			throws InsufficientStockLevelException;

	/**
	 * Release the product in the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse where product is 'stored'
	 * @param amount
	 * 		the amount of the release
	 * @param comment
	 * 		the comment for the release
	 */
	void release(ProductModel product, WarehouseModel warehouse, int amount, String comment);

	/**
	 * Sets the in stock status of all stock levels of the product in the specified warehouses.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		the warehouses
	 * @param status
	 * 		the in stock status
	 */
	void setInStockStatus(ProductModel product, Collection<WarehouseModel> warehouses, InStockStatus status);

	/**
	 * Checks the stock level status of the product in the specified warehouses.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse
	 * @return the in stock status
	 */
	InStockStatus getInStockStatus(ProductModel product, WarehouseModel warehouse);

	/**
	 * Gets the available quantity of the product for the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse
	 * @param date
	 * 		the date the specified quantity has to be available at least (could be null).
	 * @param language
	 * 		language for which the text should be localized
	 * @return the configured availability text message
	 */
	String getAvailability(ProductModel product, WarehouseModel warehouse, Date date, LanguageModel language);

	/**
	 * Gets the available quantity of the product for the specified warehouses.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		the warehouses
	 * @param date
	 * 		the date the specified quantity has to be available at least (could be null).
	 * @param language
	 * 		language for which the text should be localized
	 * @return the configured availability text message
	 */
	String getAvailability(ProductModel product, List<WarehouseModel> warehouses, Date date, LanguageModel language);

	/**
	 * Gets availability date by invoking strategy for calculating product availability, passing product, quantity and
	 * warehouse as parameters.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		the warehouse
	 * @param quantity
	 * 		the asked quantity
	 * @param language
	 * 		language for which the text should be localized
	 * @return String the configured availability text message
	 */
	String getAvailability(ProductModel product, WarehouseModel warehouse, int quantity, LanguageModel language);

	/**
	 * Gets availability date by invoking strategy for calculating product availability, passing product, quantity and
	 * warehouses as parameters.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		the warehouses
	 * @param quantity
	 * 		the asked quantity
	 * @param language
	 * 		language for which the text should be localized
	 * @return the configured availability text message
	 */
	String getAvailability(ProductModel product, List<WarehouseModel> warehouses, int quantity, LanguageModel language);

	/**
	 * Returns the warehouse which offers the "best" product "quantity".
	 *
	 * @param map
	 * 		the mapped quantities of a certain product
	 * @return WarehouseModel best match
	 */
	WarehouseModel getBestMatchOfQuantity(Map<WarehouseModel, Integer> map);

	/**
	 * Returns the warehouse which offers the "best" product "availability" .
	 *
	 * @param map
	 * 		the mapped available dates of a certain product
	 * @return WarehouseModel best match
	 */
	WarehouseModel getBestMatchOfAvailability(Map<WarehouseModel, Date> map);

	/**
	 * Finds the stock level of the specified product at the specified warehouse.
	 *
	 * @param product
	 * 		the product
	 * @param warehouse
	 * 		warehouse of the product
	 * @return found stock level, and null if no such stock level can be found.
	 */
	StockLevelModel getStockLevel(ProductModel product, WarehouseModel warehouse);

	/**
	 * Finds the stock levels of the specified product from all warehouses.
	 *
	 * @param product
	 * 		the product
	 * @return all found stock levels of product
	 */
	Collection<StockLevelModel> getAllStockLevels(ProductModel product);

	/**
	 * Finds all stock levels of the specified product in the specified warehouses.
	 *
	 * @param product
	 * 		the product
	 * @param warehouses
	 * 		the warehouses
	 * @return found stock levels of the product
	 */
	Collection<StockLevelModel> getStockLevels(ProductModel product, Collection<WarehouseModel> warehouses);

	/**
	 * Gets the product quantity for the specified product, warehouses and date.
	 *
	 * @param warehouses
	 * 		the warehouses
	 * @param product
	 * 		the product
	 * @param date
	 * 		the date the specified quantity has to be available at least.
	 * @return the mapped quantity
	 */
	Map<WarehouseModel, Integer> getAvailability(List<WarehouseModel> warehouses, ProductModel product, Date date);

	/**
	 * Returns product availability, passing product, and quantity as parameters.
	 *
	 * @param warehouses
	 * 		the warehouses
	 * @param product
	 * 		the product
	 * @param preOrderQuantity
	 * 		the asked min. preOrderQuantity
	 * @return the date, when the questioned quantity will be available
	 */
	Map<WarehouseModel, Date> getAvailability(List<WarehouseModel> warehouses, ProductModel product, int preOrderQuantity);

}
