package affix.java.project.moneyservice;

import java.time.LocalDate;

/**
 * This class defines a ExchangeRate in MoneyService
 * ExchangeRate should be created by:
 * -LocalDate holding the day
 * -int holding the scale
 * -String holding the name
 * -Float holding the Rate
 */
public class ExchangeRate {

	/**
	 * Attribute day holding the Day for ExchangeRate in LocalDate
	 */
	private final LocalDate day;
	/**
	 * Attribute currencyName holding the currency name for ExchangeRate in String
	 */
	private String currencyName;
	/**
	 * Attribute exchangeRate holding the exchange rate for ExchangeRate in Float
	 */
	private Float exchangeRate;
	

	/**
	 * Constructor
	 * @param day holding the date 
	 * @param scale holding the scale which the exchangeRate is calculated
	 * @param currency holding the name of the Currency
	 * @param price holding the exchange rate related to the reference currency
	 * @throws IllegalArgumentException
	 */
	public ExchangeRate(LocalDate day, int scale, String currency, Float price) {
		if(scale < 1 || price <0) {
			throw new IllegalArgumentException(String.format("ExchangeRate scale below 1"));
		}
		this.day = day;
		this.currencyName = currency;
		this.exchangeRate = (float)(price/scale);
		
	}
	

	/**
	 * Getter for attribute day
	 * @return a LocalDate holding day
	 */
	public LocalDate getLocalDate() {
		return this.day;
	}
	
	/**
	 * Getter for attribute currencyName
	 * @return a String holding currencyName
	 */
	public String getName() {
		return this.currencyName.trim();
	}
	
	
	/**
	 * Getter for attribute exchangeRate
	 * @return a Float holding exchangeRate
	 */
	public Float getExchangeRate() {
		return this.exchangeRate;
	}
	
	/**
	 * Setter for attribute exchangeRate
	 * @param rate holding Float
	 * @return true
	 */
	public boolean setExchangeRate(Float rate) {
		this.exchangeRate = rate;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("Date: %s, Currency: %s, Exchangerate: %3.4f", this.day.toString(),this.currencyName, this.exchangeRate);
	}
}
