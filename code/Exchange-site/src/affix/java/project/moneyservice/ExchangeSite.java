package affix.java.project.moneyservice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;


/**
 * This class defines the ExchangeSite in MoneyService
 * ExchangeSite should be created with a String holding a name
 *
 */
public class ExchangeSite implements MoneyService {
	/**
	 * Attribute name holding the name of the ExchangeSite
	 */
	public static String name;
	/**
	 * Attribute transactionList holding the List of Transactions
	 */
	public static List <Transaction> transactionList = new ArrayList<>();
	/**
	 * Attribute backupReport holding the Report for ExchangeSite
	 */
	private Report backupReport;
	/**
	 * Attribute theBox holding the MoneyBox for the ExchangeSite
	 */
	private static MoneyBox theBox;
	/**
	 * Attribute currencyMap holding the map of currency in ExchangeSite
	 */
	private static Map<String, Currency> currencyMap;
	/**
	 * Attribute rates holding the exchange rates for ExchangeSite
	 */
	private static List<ExchangeRate> rates;

	/**
	 * Attribute logger holding the Logger for ExchangeSite
	 */
	private static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}

	/**
	 * Constructor
	 * @param Name holding the name of the Site in String
	 */
	public ExchangeSite(String Name) {
		this(Name,LocalDateTime.now());
	}

	/**
	 * Overloaded Constructor
	 * @param Name holding the name of the site in String
	 * @param TimeStamp holding the time for the Report in LocalDateTime
	 */
	public ExchangeSite(String Name, LocalDateTime TimeStamp) {
		ExchangeSite.name = Name;
		backupReport = new Report(TimeStamp, transactionList);
		ExchangeSite.currencyMap = new TreeMap<String, Currency>();
		ExchangeSite.theBox= new MoneyBox(currencyMap);
		ExchangeSite.rates = new ArrayList<ExchangeRate>();
	}

	/**
	 * Fills the MoneyBox and set the rates.
	 * Calls function fillTheMoneyBox with theBox and currencyMap
	 * Calls function setTheRates and sets rates in ExchangeSite
	 * Calls function setRatesInCurrency and sends currencyMap and rates
	 */
	public void startTheDay() {
		logger.fine("Starting the day!");

		Config.fillTheMoneyBox(ExchangeSite.theBox, ExchangeSite.currencyMap);

		ExchangeSite.rates = Config.setTheRates();

		Config.setRatesInCurrency(ExchangeSite.rates, currencyMap);

	}

	/**
	 * Calculates the price 
	 * @param currencyCode is the currency
	 * @param amount is the amount of currency
	 * @param transactionType is the type of Transaction
	 * @return int holding the cost
	 */
	public int calculatePrice(String currencyCode, int amount,TransactionMode transactionType) {
		Map<String, Currency> currencyMap= MoneyBox.getCurrencyMap();
		float calcPrice =0;
		double price =0;

		if(transactionType == TransactionMode.SELL) {
			calcPrice = currencyMap.get(currencyCode).sellRate.floatValue();

		}
		else if(transactionType == TransactionMode.BUY) {
			calcPrice = currencyMap.get(currencyCode).buyRate.floatValue();
		}
		price = amount*calcPrice;
		logger.finer("Calculate price for: "+currencyCode+ " "+amount+" price: "+(int) Math.round(price));
		return (int) Math.round(price);
	}

	/**
	 *  Checks if we can buy the currency the customer wants to sell.
	 * @param orderData holding value, currencyCode and transaction mode
	 * @throws IllegalArgumentException
	 * @return Boolean, if we can afford to buy the currency 
	 */
	@Override
	public boolean buyMoney(Order orderData) throws IllegalArgumentException {
		int value = orderData.getValue();
		String currency = orderData.getCurrencyCode();

		String refCurrency = MoneyServiceIO.getReferenceCurrency();
		Optional<Double> totalRefCurrency = getAvailableAmount(refCurrency);
		Optional<Double> customerCur = getAvailableAmount(currency);


		if(customerCur.isEmpty() || totalRefCurrency.isEmpty()) {
			logger.finest("buyMoney returns false");
			return false;
		}
		double exRate = calculatePrice(currency, value,TransactionMode.BUY);


		return exRate<totalRefCurrency.get();


	}

	/**
	 *  Checks if we can sell the currency the customer wants to buy.
	 * @param orderData holding value, currencyCode and Transaction Mode
	 * @throws IllegalArgumentException
	 * @return Boolean, if we have the foreign currency and right amount 
	 */
	@Override
	public boolean sellMoney(Order orderData) throws IllegalArgumentException {
		int value = orderData.getValue(); 
		String currency = orderData.getCurrencyCode(); 

		Optional<Double> totalRefCurrency = getAvailableAmount(currency);

		if(totalRefCurrency.isEmpty()) {
			logger.finest("sellMoney returns false");
			return false;	
		}

		return value<=totalRefCurrency.get();
	}

	/**
	 * Prints the SiteReport to a text file
	 * @param fileFormat holding the name of the to be created file
	 */
	@Override
	public void printSiteReport(String fileFormat) {
		logger.finest("Printing site report to file: "+MoneyServiceIO.getPathName("SiteReports")+"SiteReport_"+Config.getSiteName()+"_"+LocalDate.now()+".txt");
		
		MoneyServiceIO.saveTxtMoneyBox(MoneyBox.getCurrencyMap(), MoneyServiceIO.getPathName("SiteReports")+"SiteReport_"+Config.getSiteName()+"_"+LocalDate.now()+".txt");
	}

	/**
	 * Called when ExchangeSite shuts down and prints the daily Transactions to a file
	 * @param fileFormat holding the name of the to be created file either txt or serialized
	 */
	@Override
	public void shutDownService(String fileFormat) {

		if(fileFormat.contains(".txt")) {
			logger.fine("Saving daily transactions as text");
			MoneyServiceIO.saveDailyTransactionListAsText(transactionList,  backupReport.getUniqueFileName());
		}else if(fileFormat.contains(".db")) {
			logger.fine("Saving daily transactions as serialized");
			MoneyServiceIO.saveSerializedDailyTransactions(transactionList, MoneyServiceIO.getPathName("Transactions")+ backupReport.getUniqueFileName());
		}
		
		printSiteReport(fileFormat);

	}

	@Override
	public Map<String, Currency> getCurrencyMap() {
		Map<String,Currency> tempMap = MoneyBox.getCurrencyMap();
		return tempMap; 
	}
	/**
	 * Checks how much of the input String we have available and removes any type of null
	 * @param currencyCode holding the type of Currency in a String
	 */
	@Override
	public Optional<Double> getAvailableAmount(String currencyCode) 
	{

		if(MoneyBox.getCurrencyMap().get(currencyCode) == null) {

			return Optional.empty();
		}
		Optional <Double> temp = Optional.of(MoneyBox.getCurrencyMap().get(currencyCode).getTotalValue());
		return temp;
	}
	
	/**
	 * Calls after buyMoney or sellMoney
	 * Complete The Transaction
	 * @param orderData holding value, currencyCode and Transaction Mode
	 * @return Order holding the order
	 */
	public Order completeOrder(Order orderData) {		
		int value = orderData.getValue();
		String currency = orderData.getCurrencyCode();

		Optional<Double> customerCur = getAvailableAmount(currency);
		String refCurrency = MoneyServiceIO.getReferenceCurrency();
		Optional<Double> companyCur = getAvailableAmount(refCurrency);


		if(orderData.getTransactionType() == TransactionMode.BUY) {
			int cost = calculatePrice(currency, value,TransactionMode.BUY);
			int buy = customerCur.get().intValue();
			MoneyBox.getCurrencyMap().get(currency).setTotalValue(value + buy);
			int sell = companyCur.get().intValue();
			MoneyBox.getCurrencyMap().get(refCurrency).setTotalValue(sell - cost);
			logger.finest("Removing "+cost+refCurrency+" from "+(currencyMap.get(refCurrency).getTotalValue()+cost)+ " theBox. Total in box after: "+MoneyBox.getCurrencyMap().get(refCurrency).getTotalValue().intValue());
			logger.finest("Adding "+value+currency+" to total: "+buy+ ". Total in box after: "+MoneyBox.getCurrencyMap().get(currency).getTotalValue().intValue());
			transactionList.add(new Transaction(orderData));
		}

		else if(orderData.getTransactionType() == TransactionMode.SELL) {
			int amountBeforeTransaction = customerCur.get().intValue();
			MoneyBox.getCurrencyMap().get(currency).setTotalValue(amountBeforeTransaction - value);
			logger.finest("Removing "+value+currency+" from "+amountBeforeTransaction+" box. Total after sell: "+MoneyBox.getCurrencyMap().get(currency).getTotalValue().intValue());
			int ourCurrency = companyCur.get().intValue();
			int priceOfOrder = calculatePrice(currency, value,TransactionMode.SELL);
			MoneyBox.getCurrencyMap().get(refCurrency).setTotalValue(ourCurrency + priceOfOrder);
			logger.finest("Income "+priceOfOrder+" "+MoneyServiceIO.getReferenceCurrency()+" to box. "+companyCur+ " total in box after: "+MoneyBox.getCurrencyMap().get(refCurrency).getTotalValue().intValue()); 
			transactionList.add(new Transaction(orderData));
		}
		logger.fine("Completed order: "+orderData.toString());
		return orderData;
	}

	/**
	 * Adds the input Order in Order queue list
	 * @param d holding the Order
	 * @return A list of the added Order
	 */
	public List<Order> addOrderToQueue(Order d){
		List<Order> orderList = new LinkedList<Order>();
		orderList.add(d);
		logger.finer("Order added to queue");

		return orderList;
	}

	/**
	 * Processes a list of orders and returns the leftovers
	 * @param orderList A list of Orders
	 * @return orderList Updated a list of orders that didn't go trough.
	 */
	public List<Order> processOrderQueue(List<Order> orderList){
		for(Order o : orderList)
		{
			if(o.getTransactionType() == (TransactionMode.BUY)) {
				if(buyMoney(o)) {
					completeOrder(o);
					logger.finer("Order completed");
					orderList.remove(o);
					logger.finer("Order removed from orderList");
				}
				else {
					System.out.println("Order :"+ o +" did not go trough");
					logger.fine("Order :"+ o +" did not go trough");
				}
			}
			else if(o.getTransactionType() == (TransactionMode.SELL)) {
				if(sellMoney(o)) {
					completeOrder(o);
					logger.finer("Order completed");
					orderList.remove(o);
					logger.finer("Order removed from orderList");
				}
				else {
					System.out.println("Order :"+ o +" did not go trough");
					logger.fine("Order :"+ o +" did not go trough");
				}
			}
		}
		return orderList;
	}

	/**
	 * Getter for attribute transactionList
	 * @return List<Transaction> holding transactionList
	 */
	public List<Transaction> getTransactionList(){
		return transactionList;
	}


	/**
	 * Getter for attribute rates
	 * @return List<ExchangeRate> holding rates
	 */
	public List<ExchangeRate> getRates() {
		return rates;
	}
	/**
	 * Setter for attribute name
	 * @param name holding the name of the Site
	 */
	public void setName(String name) {
		ExchangeSite.name = name;
	}

}