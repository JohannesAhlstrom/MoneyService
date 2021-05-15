package affix.java.project.moneyservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;


@SuppressWarnings("javadoc")
public class Config {
	private List<String> fileList = new ArrayList<>();
	private List<ExchangeRate> exchangeRateList = new ArrayList<>();
	private Map<String,Double> currencyMap = new HashMap<>();
	private Map<LocalDate, Map<String,Double>> superCurrencyMap = new HashMap<>();
	private Map<LocalDate,List<ExchangeRate>> exchangeRateMap = new HashMap<>();
	private String[] siteNames = {"North","South","East","Center","West","ALL"};
	private String[] periods = {"Day","Week","Month"};
	private Map<String, Integer> siteCounter = new HashMap<>();
	private Float BUYMARGIN = 0.0f;
	private Float SELLMARGIN = 0.0f;
	private Map<String, Map<String,String>> siteTransMap;
	public static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}
	
	//Constructor
	public Config() {
	}
	

	/**
	 * Configures initial setup by calling for:
	 * calls for initial setup of HQ-specific customization.
	 * starts initial setup of daily information from configurationFiles (ProjectConfiguration and CurrencyConfig).
	 * finding the reports and parsing the required information.
	 * @return boolean true if successful.
	 */
	public boolean Configure(HQ hq, Config config, HQIO hqio) {
		logger.finer("Configures initial setup");
		boolean ok = hqio.parseHQConfig(hq, config);
		ok = findParseAndMapConfigFiles(hqio);
		this.siteTransMap = findTransactions();
		return ok;
	}

	/**
	 * Finds and collects all filenames to all reports.
	 * Checks if the paths to the folders containing the reports from the sites is working.
	 * Creates a path to the file for each string, for use when reading the files.
	 * Finds and saves all filenames in a Map siteTransMap 
	 * (used: Map Site, Map.
	 * @return Map String, Map String,String containing all Sites,Filenames and their respective paths.
	 */
	public Map<String, Map <String,String>> findTransactions() {
		Map<String, Map<String,String>> siteTransMap = new HashMap<>();
		File C = new File("./Transactions/CENTER");
		File E = new File("./Transactions/EAST");
		File N = new File("./Transactions/NORTH");
		File S = new File("./Transactions/SOUTH");
		File W = new File("./Transactions/WEST");
		if(C.exists()){
			Map<String, String> transactionPathMap = new HashMap<>();
			Integer counter = 0;
			String[] centerStrings = C.list();
			if(centerStrings != null) {
				for(String str:centerStrings) {
					if(str.contains("Report")) {
						String path = "./Transactions/CENTER/"+str.toString();
						transactionPathMap.putIfAbsent(str.toString(), path);
						counter++;
					}
				}
				siteTransMap.putIfAbsent("CENTER", transactionPathMap);
				this.siteCounter.putIfAbsent("CENTER", counter);
				logger.finer("Transactions found in CENTER: "+ counter);
			}
		}
		if(E.exists()) {
			Map<String, String> transactionPathMap = new HashMap<>();
			Integer counter = 0;
			String[] eastStrings = E.list();
			if(eastStrings != null) {
				for(String str:eastStrings) {
					if(str.contains("Report")) {
						String path = "./Transactions/EAST/"+str.toString();
						transactionPathMap.putIfAbsent(str.toString(), path);
						counter++;
					}
				}
				this.siteCounter.putIfAbsent("EAST", counter);
				siteTransMap.putIfAbsent("EAST", transactionPathMap);
				logger.finer("Transactions found in EAST: "+ counter);
			}
		}
		if(N.exists()) {
			Map<String, String> transactionPathMap = new HashMap<>();
			Integer counter = 0;
			String[] northStrings = N.list();
			if(northStrings != null) {
				for(String str:northStrings) {
					if(str.contains("Report")) {
						String path = "./Transactions/NORTH/"+str.toString();
						transactionPathMap.putIfAbsent(str.toString(), path);
						counter++;
					}
				}
				this.siteCounter.putIfAbsent("NORTH", counter);
				siteTransMap.putIfAbsent("NORTH", transactionPathMap);
				logger.finer("Transactions found in NORTH: "+ counter);
			}
		}

		if(S.exists()) {
			Map<String, String> transactionPathMap = new HashMap<>();
			Integer counter = 0;
			String[] southStrings = S.list();
			if(southStrings != null) {
				for(String str:southStrings) {
					if(str.contains("Report")) {
						String path = "./Transactions/SOUTH/"+str.toString();
						transactionPathMap.putIfAbsent(str.toString(), path);
						counter++;
					}
				}
				this.siteCounter.putIfAbsent("SOUTH", counter);
				siteTransMap.putIfAbsent("SOUTH", transactionPathMap);
				logger.finer("Transactions found in SOUTH: "+ counter);
			}
		}

		if(W.exists()) {
			Map<String, String> transactionPathMap = new HashMap<>();
			Integer counter = 0;
			String[] westStrings = W.list();
			if(westStrings != null) {
				for(String str:westStrings) {
					if(str.contains("Report")) {
						String path = "./Transactions/WEST/"+str.toString();
						transactionPathMap.putIfAbsent(str.toString(), path);
						counter++;
					}
				}
				this.siteCounter.putIfAbsent("WEST", counter);
				siteTransMap.putIfAbsent("WEST", transactionPathMap);
				logger.finer("Transactions found in WEST: "+ counter);
			}
		}
		return siteTransMap;
	}


	/**
	 * This class finds every ProjectConfigFiles and CurrencyConfig files in
	 * a their respective directory, then read, parses and saves the information.
	 * 
	 * Stores the information in exchangeRateMap and SuperCurrencyMap with 
	 * ExchangeRateMap  = Map of LocalDate, List ExchangeRate
	 * SuperCurrencyMap = Map of LocalDate, Map String, Double
	 * 
	 * @return boolean, saves several lists and maps to HQ class.
	 */
	public boolean findParseAndMapConfigFiles(HQIO hqio) {
		boolean ok = false;
		logger.finer("Finds every ProjectConfigFiles and CurrencyConfig files in a their respective directory");
		File file = new File("./Configs");
		File ratesFile = new File("./DailyRates");
		String[] ratesFileNames = ratesFile.list();
		String[] configFileNames = file.list();
		List<String> files = new ArrayList<>();
		for(String st:ratesFileNames) {
			files.add(st);
			logger.finest("Added ratesFileName "+st);
		}
		for(String str:configFileNames) {
			files.add(str);
			logger.finest("Added configFileName "+str);
		}
		
		for(String s: files) {
			if(s.contains("CurrencyConfig")) {
				String path = "./DailyRates/";
				String x = path+s.toString();
				exchangeRateList= HQIO.parseCurrencyConfig(HQIO.readTextFiles(x));
				exchangeRateMap.putIfAbsent(exchangeRateList.get(0).getLocalDate(), exchangeRateList);
				ok = true;
			}
			if(s.contains("ProjectConfig")) {
				String path = "./Configs/";
				String x = path+s.toString();
				currencyMap = hqio.parseProjectConfig(HQIO.readTextFiles(x));
				superCurrencyMap.putIfAbsent(hqio.getRefDate(), currencyMap);
				ok = true;
			}
		}
		return ok;
	}

	/**
	 * Filters keys in a Map containing filenames and their paths on Site name
	 * Checks if the key-string contains the name of the required site.
	 * 
	 * Uses the SearchSite in the filter value to filter a Map containing 
	 * SiteNames, FileNames and Paths on the Key: SearchSite parameter.
	 * returns the filtered file.
	 * 
	 * @param fileMap containing the full, unfiltered map of file-information.
	 * @param  filter Container for searchparameters set in the respective statsChoiceMenus.
	 * @return containing the filtered filenames and their paths
	 */
	public Map<String, Map<String,String>> filterFilesOnSite(Map<String,Map<String,String>> fileMap, Map<String,String> filter) {
		logger.finest("Filters keys in a Map containing filenames and their paths on Site name ");
		Map<String, Map<String, String>> filteredMap = new HashMap<>();
		if(filter.get("SearchSite").contains("ALL")) {
			return fileMap;
		}
		else {
			for(String s:fileMap.keySet()) {
				if(s.toString().toUpperCase().contains(filter.get("SearchSite"))) {
					filteredMap.putIfAbsent(s, fileMap.get(s));
					logger.finest("Added "+s);
				}
			}
		}
		return filteredMap;
	}

	/**
	 * Filters keys in a Map containing filenames and their paths on period:
	 * 
	 * startDate = uses input taken by the user.
	 * endDate = LocalDate to be calculated and applied.
	 * 1: "DAY": startDate = endDate.
	 * 2: "WEEK": startDate is previous or same Monday, endDate = Same or next Friday.
	 * 3: "MONTH": startDate equals the first day of the month, endDate = last day of the same Month.
	 * 
	 * Uses the LocalDate with TemporalAdjusters (Static) library.
	 * Does not accept a weekend-date.
	 * 
	 * @param  fileMap containing the full, unfiltered map of file-information.
	 * @param  filter Container for search parameters set in the respective statsChoiceMenus.
	 * @return containing the filtered Map containting the sitenames, filenames and their paths.
	 */
	public Map<String, Map<String,String>> filterFilesOnPeriod(Map<String,Map<String,String>> fileMap, Map<String,String> filter, HQ hq) {
		LocalDate  startDate = hq.getFilterDate();
		LocalDate endDate; 
		Map<String, Map<String,String>> filteredMap = new HashMap<>();
		switch(filter.get("SearchPeriod")) {
		case "DAY":{
			logger.fine("Search period DAY chosen");
			endDate = startDate;
			for(String s:fileMap.keySet()) {
				Map<String, String> temp = fileMap.get(s.toString());
				Set<String> keySet = temp.keySet();
				for(String sub: keySet) {
					if(sub.toString().contains("Report")) {
						String [] parts = sub.split("_");
						String[] dateParts = parts[2].split("\\.");
						LocalDate fileDate = LocalDate.parse(dateParts[0]);
						if(fileDate.toString().contains(startDate.toString())) {
							Map<String, String> secondTemp = new HashMap<String,String>();
							String t = fileMap.get(s).get(sub); 
							secondTemp.putIfAbsent(sub, t);
							filteredMap.putIfAbsent(s, secondTemp);
						}
					}
				}
			}
			break;
		}
		case "WEEK":{
			logger.fine("Search period WEEK chosen");
			LocalDate inputDate = hq.getFilterDate();
			startDate = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
			Map<String, String> secondTemp = new HashMap<String,String>();
			for(String s:fileMap.keySet()) {
				Map<String, String> temp = fileMap.get(s.toString());
				Set<String> keySet = temp.keySet();
				for(String sub: keySet) {
					if(sub.toString().contains("Report")) {
						String [] parts = sub.split("_");
						String[] dateParts = parts[2].split("\\.");
						LocalDate fileDate = LocalDate.parse(dateParts[0]);
						if(fileDate.isAfter(startDate.minusDays(1)) && fileDate.isBefore(endDate.plusDays(1))) {
							String t = fileMap.get(s).get(sub); 
							secondTemp.putIfAbsent(sub, t);
							filteredMap.putIfAbsent(s, secondTemp);
						}
					}
				}

			}
			break;
		}
		case "MONTH":{
			logger.fine("Search period MONTH chosen");
			LocalDate inputDate = hq.getFilterDate();
			startDate = inputDate.with(TemporalAdjusters.firstDayOfMonth());
			endDate = inputDate.with(TemporalAdjusters.lastDayOfMonth());
			Map<String, String> secondTemp = new HashMap<String,String>();
			for(String s:fileMap.keySet()) {
				Map<String, String> temp = fileMap.get(s.toString());
				Set<String> keySet = temp.keySet();
				for(String sub: keySet) {
					if(sub.toString().contains("Report")) {
						String [] parts = sub.split("_");
						String[] dateParts = parts[2].split("\\.");
						LocalDate fileDate = LocalDate.parse(dateParts[0]);
						if(fileDate.isAfter(startDate.minusDays(1))&&fileDate.isBefore(endDate.plusDays(1))){
							String t = fileMap.get(s).get(sub); 
							secondTemp.putIfAbsent(sub, t);
							filteredMap.putIfAbsent(s, secondTemp);
						}
					}
				}

			}
			break;
		}
		default:{
			System.out.println("Can not search on: "+filter.get("SearchPeriod"));
			break;
		}

		}
		return filteredMap;
	}


	/**
	 * This method filters the incoming list of transactions based on the currency specified in the filtering process
	 * Streams, then saves the stream as a collection(List) of transactions.
	 * 
	 * @param filterMe contains the semi filtered list to be filtered on their currencies.
	 * @param filter containing the "SearchCurrency" part of a full filter.
	 * @return List Transaction a fully filtered list, ready for processing by statistics.
	 */
	public List<Transaction> filterOnCurrency(List<Transaction> filterMe, String filter) {
		logger.finest("Filter incoming list of transactions based on the currency specified in the filtering process");
		if(filter.toString().toUpperCase().trim().contains("ALL")) {return filterMe;}
		else {
			Stream<Transaction> reduction = filterMe.stream().filter(w -> w.toString().contains(filter.toUpperCase().trim()));
			List<Transaction> reducedList = reduction.collect(Collectors.toList());
			return reducedList;
		}
	}


	public List<String> getFileList() {
		return fileList;
	}


	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}


	public List<ExchangeRate> getExchangeRateList() {
		return exchangeRateList;
	}


	public void setExchangeRateList(List<ExchangeRate> exchangeRateList) {
		this.exchangeRateList = exchangeRateList;
	}


	public Map<String, Double> getCurrencyMap() {
		return currencyMap;
	}


	public void setCurrencyMap(Map<String, Double> currencyMap) {
		this.currencyMap = currencyMap;
	}


	public Map<LocalDate, Map<String, Double>> getSuperCurrencyMap() {
		return superCurrencyMap;
	}


	public void setSuperCurrencyMap(Map<LocalDate, Map<String, Double>> superCurrencyMap) {
		this.superCurrencyMap = superCurrencyMap;
	}


	public Map<LocalDate, List<ExchangeRate>> getExchangeRateMap() {
		return exchangeRateMap;
	}


	public void setExchangeRateMap(Map<LocalDate, List<ExchangeRate>> exchangeRateMap) {
		this.exchangeRateMap = exchangeRateMap;
	}


	public String[] getSiteNames() {
		return siteNames;
	}


	public void setSiteNames(String[] siteNames) {
		this.siteNames = siteNames;
	}


	public String[] getPeriods() {
		return periods;
	}


	public void setPeriods(String[] periods) {
		this.periods = periods;
	}


	public Map<String, Integer> getSiteCounter() {
		return siteCounter;
	}


	public void setSiteCounter(Map<String, Integer> siteCounter) {
		this.siteCounter = siteCounter;
	}


	public Float getBUYMARGIN() {
		return BUYMARGIN;
	}


	public void setBUYMARGIN(Float bUYMARGIN) {
		BUYMARGIN = bUYMARGIN;
	}


	public Float getSELLMARGIN() {
		return SELLMARGIN;
	}


	public void setSELLMARGIN(Float sELLMARGIN) {
		SELLMARGIN = sELLMARGIN;
	}


	public Map<String, Map<String, String>> getSiteTransMap() {
		return siteTransMap;
	}


	public void setSiteTransMap(Map<String, Map<String, String>> siteTransMap) {
		this.siteTransMap = siteTransMap;
	}
	
}