package affix.java.project.moneyservice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a configuration in MoneyService
 * Config should set up the Attributes from default setup or
 * by reading the config file on startup.
 *
 */
public class Config {
	/**
	 * Attribute siteName holding the name of the Site
	 */
	private static String siteName= "default";
	/**
	 * Attribute logName holding the name of the Log
	 */
	private static String logName= "MoneyServiceLog";
	/**
	 * Attribute logFormat holding the format of the log
	 */
	private static String logFormat = "text";
	/**
	 * Attribute logLevel holding the level of the log
	 */
	private static String logLevel = "OFF";
	/**
	 * Attribute MIN_AMMOUNT holding the minimum amount that can be purchased/sold
	 */
	private static int MIN_AMMOUNT = 50;
	/**
	 * Attribute MAX_AMMOUNT holding the maximum amount that can be purchased/sold
	 */
	private static int MAX_AMMOUNT = 50000;
	/**
	 * Attribute buyRateConfig holding the buy rate
	 */
	private static float buyRateConfig = 0.995f;
	/**
	 * Attribute sellRateConfig holding the sell rate
	 */
	private static float sellRateConfig = 1.005f;
	/**
	 * Attribute password holding a char array of letters
	 */
	private static char[] password = {'Q','w','e','r','t','y','u','i'}; 
	/**
	 * Attribute exchangeRateList holding a List of Exchange Rates
	 */
	public static List<ExchangeRate> exchangeRateList = new ArrayList<ExchangeRate>();

	/**
	 * Attribute logger holding the Logger
	 */
	private static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}

	/**
	 * Enum LogLev for all different levels of Logger
	 */
	public enum LogLev {
		/**
		 * LogLevel All
		 */
		ALL,
		/**
		 * LogLev Config
		 */
		CONFIG,
		/**
		 * LogLev Fine
		 */
		FINE, 
		/**
		 * LogLev Finer
		 */
		FINER, 
		/**
		 * LogLev Finest
		 */
		FINEST,
		/**
		 * LogLev Info
		 */
		INFO,
		/**
		 * LogLev Severe
		 */
		SEVERE,
		/**
		 * LogLev Warning
		 */
		WARNING, 
		/**
		 * LogLev Off
		 */
		OFF
	}

	/**
	 * Reads through the config file and adds the values to the site
	 * @param filename holding the name of the config file defines as String
	 * @return boolean holding the outcome of operation
	 */
	public static boolean readConfigFile(String filename) {
		boolean okRead = false;
		int ok=0;
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			while(br.ready()){
				String input = br.readLine();
				String[] parts = input.split("="); 

				if(parts.length == 2) {
					String key = parts[0].strip();
					String value = parts[1].strip();


					switch (key) {
					case "siteName":
						if(value.isEmpty()) {
							System.out.println("Site name missing in config file!");
							System.out.println("Default used: "+Config.getSiteName());
							break;
						}
						Config.setSiteName(value);
						System.out.println("Site name set to: "+value);
						ok++;		
						break;
					case "logName":
						if(value.isEmpty()) {
							System.out.println("Log name missing in config file!");
							System.out.println("Default used: "+Config.getLogName());
							break;
						}
						
						Config.setLogName(value+LocalDate.now());
						System.out.println("Log name set to: "+value);
						ok++;			
						break;
					case "logFormat":	
						if((value.isEmpty()) || !(value.toLowerCase().equals("text") || value.toLowerCase().equals("xml"))){
							System.out.println("************Bad log format in config file!***********");
							System.out.println("Default used: "+Config.getLogFormat());
							break;
						}
						Config.setLogFormat(value.toLowerCase());
						System.out.println("Log format set to: "+value);
						ok++;		
						break;

					case "logLevel":					
						for(LogLev l: LogLev.values()) {
							String temp = l.toString();
							if(temp.equals(value)){
								logLevel = value.toUpperCase().trim();
								System.out.println("Log level set to: "+value);
								ok++;
								break;
							}
						}						 
						break;

					case "min_ammount":
						Config.setMIN_AMMOUNT(Integer.parseInt(value));
						System.out.println("min_ammount set to: "+value);
						ok++;
						break;
					case "max_ammount":
						Config.setMAX_AMMOUNT(Integer.parseInt(value));
						System.out.println("max_ammount set to: "+value);
						ok++;
						break;
					case "buyRate":
						setBuyRateConfig(Float.parseFloat(value));
						System.out.println("buyRate marginal set to: "+value);
						ok++;
						break;
					case "sellRate":
						setSellRateConfig(Float.parseFloat(value));
						System.out.println("sellRate marginal set to: "+value);
						ok++;
						break;
					case "password":
						setPassword(value);
						ok++;
						break;
					case "configFolder":
						MoneyServiceIO.setFolderPath(value);
						System.out.println("File Path for configFolder set");
						ok++;
						break;
					case "dailyRatesFolder":
						MoneyServiceIO.setFolderPath(value);
						System.out.println("File Path for DailyRateFolder set");
						ok++;
						break;
					case "orderFolder":
						MoneyServiceIO.setFolderPath(value);
						System.out.println("File Path for OrderFolder set");
						ok++;
						break;
					case "siteReportsFolder":
						MoneyServiceIO.setFolderPath(value);
						System.out.println("File Path for SiteReportFolder set");
						ok++;
						break;
					case "transactionsFolder":
						MoneyServiceIO.setFolderPath(value);
						System.out.println("File Path for TransactionFolder set");
						ok++;
						break;
					default:
						break;
					}
				}

			}
		}
		catch (IOException ioe) {
			System.out.println("Exception occurred: " + ioe);
		}
		catch (NumberFormatException e) {
		}

		if(ok == 14) {
			System.out.println("Configuration of the system OK!");
			return okRead=true;
		}

		return okRead;
	}

	/**
	 * The method sets buy and sell rate in each currency.
	 * @param currencyList holding the list of exchange rates
	 * @param currencyMap holding a map of the currencies and their name
	 */

	public static void setRatesInCurrency(List<ExchangeRate> currencyList, Map<String, Currency> currencyMap) {			
		for(ExchangeRate s : currencyList) {
			String key = s.getName();
			if(currencyMap.containsKey(s.getName())) {
				Float buyRate = s.getExchangeRate() * getBuyRateConfig();
				currencyMap.get(key).setBuyRate(buyRate);
				Float sellRate = s.getExchangeRate() * getSellRateConfig();
				currencyMap.get(key).setSellRate(sellRate);
				logger.finer(""+key+" buyRate: "+buyRate+ ", sellRate: "+sellRate);				
			}
		}
	}


	/**
	 * Reads the daily rates and adds them to a list to be returned
	 * @return a List<ExchangeRate> holding test
	 */
	public static List<ExchangeRate> setTheRates() {
		List<ExchangeRate> test = new ArrayList<ExchangeRate>(MoneyServiceIO.parseCurrencyConfig(MoneyServiceIO.readTextFiles(MoneyServiceIO.getPathName("DailyRates")+MoneyServiceIO.currencyConfigFilename)));
		logger.finer("*********** Getting rates from "+MoneyServiceIO.currencyConfigFilename+ " ************");

		return test;
	}


	/**
	 * Fills the MoneyBox with currencies and their amount called using MoneyServiceIO functions
	 * @param theBox holding an empty MoneyBox to be filled
	 * @param currencyMap holding the Currencies
	 * @return a MoneyBox filled with currencies
	 */
	public static MoneyBox fillTheMoneyBox(MoneyBox theBox, Map<String, Currency> currencyMap ) {
		Map<String, Double> testMap = new HashMap<String,Double>(MoneyServiceIO.parseProjectConfig(MoneyServiceIO.readTextFiles(MoneyServiceIO.getPathName("Configs")+MoneyServiceIO.projectConfigFilename)));

		Set<String> keySet = testMap.keySet();
		for(String k:keySet) {
			Currency tempCurrency = new Currency(testMap.get(k).intValue(), 0.0f, 0.0f);
			currencyMap.putIfAbsent(k, tempCurrency);
			logger.finer("Filled MoneyBox with: "+k+" amount: "+tempCurrency);
		}

		return theBox;
	}

	/**
	 * Controls the password
	 * @param temp holding a String of the password
	 * @return a boolean holding the outcome of the operation
	 */
	public static boolean controlPwd(String temp) {

		char[] tempPass = new char[temp.length()];

		for(int i=0; i<temp.length(); i++) {
			tempPass[i] = temp.charAt(i);
		}
		boolean passwordMatch = Arrays.equals(tempPass, password);

		return passwordMatch;
	}


	/**
	 * Setter for attribute password
	 * @param password holding a String of Letters
	 */
	static void setPassword(String password) {

		for(int i=0; i<password.length();i++) {
			Config.password[i] = password.charAt(i);			
		}
	}

	/**
	 * Getter for attribute logName
	 * @return a String holding logName
	 */
	public static String getLogName() {
		return logName;
	}


	/**
	 * Getter for attribute exchangeRateList
	 * @return a List<ExchangeRate> holding exchangeRateList
	 */
	public static List<ExchangeRate> getExchangeRateList(){
		return exchangeRateList;
	}

	/**
	 * Setter for attribute MIN_AMMOUNT
	 * @param MIN_AMMOUNT holding an int
	 * @return true
	 */
	public static boolean setMIN_AMMOUNT(int MIN_AMMOUNT) {
		Config.MIN_AMMOUNT = MIN_AMMOUNT;
		return true;
	}

	/**
	 * Setter for attribute MAX_AMMOUNT
	 * @param MAX_AMMOUNT holding an int
	 * @return true
	 */
	public static boolean setMAX_AMMOUNT(int MAX_AMMOUNT) {
		Config.MAX_AMMOUNT = MAX_AMMOUNT;
		return true;
	}


	/**
	 * Getter for attribute MIN_AMMOUNT
	 * @return a int holding MIN_AMMOUNT
	 */
	public static int getMIN_AMMOUNT() {
		return MIN_AMMOUNT;
	}

	/**
	 * Getter for attribute MAX_AMMOUNT
	 * @return a int holding MAX_AMMOUNT
	 */
	public static int getMAX_AMMOUNT() {
		return MAX_AMMOUNT;
	}

	/**
	 * Getter for attribute buyRateConfig
	 * @return a float holding buyRateConfig
	 */
	public static float getBuyRateConfig() {
		return buyRateConfig;
	}


	/**
	 * Setter for attribute buyRateConfig
	 * @param buyRateConfig holding a float
	 */
	public static void setBuyRateConfig(float buyRateConfig) {
		Config.buyRateConfig = buyRateConfig;
	}


	/**
	 * Getter for attribute sellRateConfig
	 * @return a float holding sellRateConfig
	 */
	public static float getSellRateConfig() {
		return sellRateConfig;
	}


	/**
	 * Setter for attribute sellRateConfig
	 * @param sellRateConfig holding a float
	 */
	public static void setSellRateConfig(float sellRateConfig) {
		Config.sellRateConfig = sellRateConfig;
	}

	/**
	 * Getter for attribute logFormat
	 * @return a String holding logFormat
	 */
	public static String getLogFormat() {
		return logFormat;
	}

	/**
	 * Getter for attribute logLevel
	 * @return a String holding logLevel
	 */
	public static String getLogLevel() {
		return logLevel;
	}


	/**
	 * Setter for attribute logName
	 * @param logName holding a String
	 */
	public static void setLogName(String logName) {
		Config.logName = logName;
	}

	/**
	 * Setter for attribute logFormat
	 * @param logFormat holding a String
	 */
	public static void setLogFormat(String logFormat) {
		Config.logFormat = logFormat;
	}

	/**
	 * Setter for attribute logger
	 * @param logLevel holding Level
	 */
	public static void setLogLevel(Level logLevel) {
		logger.setLevel(logLevel);
	}

	/**
	 * Setter for attribute logger
	 * @param logger holding a Logger
	 */
	public static void setLogger(Logger logger) {
		Config.logger = logger;
	}


	/**
	 * Getter for attribute siteName
	 * @return a String holding siteName
	 */
	public static String getSiteName() {
		return siteName;
	}


	/**
	 * Setter for attribute siteName
	 * @param siteName holding a String
	 */
	public static void setSiteName(String siteName) {
		Config.siteName = siteName;
	}

}
