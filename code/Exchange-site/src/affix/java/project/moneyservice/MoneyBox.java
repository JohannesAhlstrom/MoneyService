package affix.java.project.moneyservice;

import java.util.Map;
import java.util.logging.Logger;

/**
 * This class defines a MoneyBox in MoneyService
 * MoneyBox should be created by a Map<String,Currency> using
 * input from MoneyServiceIO
 */
public class MoneyBox implements java.io.Serializable {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 1L;
	/**
	 * Attribute logger holding the Logger for MoneyBox
	 */
	private static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}

	/**
	 * Attribute currencyMap holding both the String and Currency of the MoneyBox
	 */
	private static Map<String, Currency> currencyMap;


	/**
	 * Constructor
	 * @param currencyMap a Map<String,Currency> holding the Name of the Currencies
	 * and their value
	 */
	public MoneyBox(Map<String, Currency> currencyMap) {
		MoneyBox.currencyMap = currencyMap;
	}


	/**
	 * Getter for attribute currencyMap
	 * @return a Map<String, Currency> holding currencyMap
	 */
	public static Map<String, Currency> getCurrencyMap() {
		return currencyMap;
	}


	/**
	 * Sets the currencyMap for MoneyBox according to input
	 * @param newCurrencyMap holding the Map<String,Currency> for currencyMap
	 */
	public void setCurrencyMap(Map<String, Currency> newCurrencyMap) {
		MoneyBox.currencyMap = newCurrencyMap;
	}


	/**
	 * Getter for attribute serialVersionUID
	 * @return a long holding serialVersionUID
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	/**
	 * This method is used for adding a new Currency.
	 * @param amount a double holding the amount
	 * @param currencyName a String holding the currency name
	 * @param rate a Float holding the currency rate
	 * @return a boolean holding outcome of operation
	 */
	public static boolean addNewCurrency(double amount, String currencyName, Float rate) {
		Currency c = new Currency(0, rate, rate);
		if(currencyMap.containsKey(currencyName)) {
			return false;
		}
		currencyMap.putIfAbsent(currencyName, c);
		currencyMap.get(currencyName).setBuyRate(rate * Config.getBuyRateConfig());
		currencyMap.get(currencyName).setSellRate(rate * Config.getSellRateConfig());

		if(currencyMap.containsKey(currencyName)) {
			logger.fine("New currency added: "+currencyName+ " rate from list:"+rate+" buyRate:"+c.getBuyRate()+" sellRate:"+c.getSellRate());
		}
		return true;
	}

	/**
	 * @param currencyName a String holding the currency name
	 * @param amount a double holding the amount
	 * @return int holding the outcome of the calculation
	 */
	public static int denominationControl(String currencyName, double amount) {

		int result = 0;
		
		if(currencyMap.containsKey(currencyName)) {
		result = (int) (amount % 50);	
		}
		logger.finer("Round off "+(int)amount+ " to "+((int) amount - result));
		return (int) amount - result ;	
	}


}
