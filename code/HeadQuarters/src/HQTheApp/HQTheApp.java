package HQTheApp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import affix.java.project.moneyservice.Config;
import affix.java.project.moneyservice.ExchangeRate;
import affix.java.project.moneyservice.HQ;
import affix.java.project.moneyservice.HQIO;
import affix.java.project.moneyservice.Transaction;
import affix.java.project.moneyservice.TransactionMode;


@SuppressWarnings("javadoc")
public class HQTheApp {

	/**
	 * logger
	 */
	private static Logger logger;

	/**
	 * file handler for logger
	 */
	private static FileHandler fh;

	/**
	 * This method is the Initial start-up sequence holder.
	 * Currently only holds the configuration  method held by Config-class.
	 * Included for easy entry into the main start up sequence.
	 * 
	 * @param args if further customization of properties will be needed.
	 */
	public static void startUp(String[] args, HQ hq, Config config, HQIO hqio) {
		System.out.println("Welcome to HQ");
		config.Configure(hq, config, hqio);
	}

	/**
	 * Allows for user input for choosing the site for which to perform statistics.
	 * 
	 * Presents the names of the sites along with how many reports were found.
	 * Warns the user if no reports are found.
	 * 
	 * Asks for input regarding site names with option to choose all, or return to main
	 * for redoing the sequence if the user so chooses.
	 * The method uses put-if-absent to avoid unintentional return values.
	 * 
	 * Second run, if the filter is re-used, the method checks and replaces with current choice.
	 * 
	 * Checks if there are transactions existing within the chosen site or parameters.
	 * @param filter : which is a container to be filled by the method.
	 * @return Map String, String filter : now filled with "SearchSite" key and the name of the site.
	 */
	@SuppressWarnings("resource")
	public static Map<String, String> statsSiteChoiceMenu(Map<String, String> filter, Config config) {
		boolean ok = true;
		boolean inputCorrect;
		Scanner keyboard = new Scanner(System.in);
		keyboard.useDelimiter(System.lineSeparator());
		do {
			inputCorrect = true;
			ok = true;
			System.out.println("Choose Site");
			int i = 1;
			for(String s:config.getSiteNames()) {
				if(!(s.contains("ALL"))) {
					Integer siteCount = config.getSiteCounter().get(s.toUpperCase().toString());
					System.out.println(String.format("%d  %s \t contains: %d reports",i++, s, siteCount));}
				else if(s.contains("ALL")) {
					Integer allCount =0; 
					for(String calcKeys: config.getSiteCounter().keySet()) {
						allCount += config.getSiteCounter().get(calcKeys.toString());
					}
					System.out.println(String.format("%d  %s \t\t contains: %d reports",i++, s, allCount));
				}
				else { System.out.println("Coult not find any sites");}
			}
			System.out.println("0  Exit");
			try {
				String input=keyboard.next();
				int menuChoice = Integer.parseInt(input);
				switch(menuChoice) {
				case 1:{
					filter.putIfAbsent("SearchSite", "NORTH");
					if(!(filter.get("SearchSite").contains("NORTH"))) {
						filter.replace("SearchSite", "NORTH");
					}
					logger.fine("SearchSite NORTH chosen");
					break;
				}
				case 2:{
					filter.putIfAbsent("SearchSite", "SOUTH");
					if(!(filter.get("SearchSite").contains("SOUTH"))) {
						filter.replace("SearchSite", "SOUTH");
					}
					logger.fine("SearchSite SOUTH chosen");
					break;
				}
				case 3:{
					filter.putIfAbsent("SearchSite", "EAST");
					if(!(filter.get("SearchSite").contains("EAST"))) {
						filter.replace("SearchSite", "EAST");
					}
					logger.fine("SearchSite EAST chosen");
					break;
				}
				case 4:{
					filter.putIfAbsent("SearchSite", "CENTER");
					if(!(filter.get("SearchSite").contains("CENTER"))) {
						filter.replace("SearchSite", "CENTER");
					}
					logger.fine("SearchSite CENTER chosen");
					break;
				}

				case 5: {
					filter.putIfAbsent("SearchSite", "WEST");
					if(!(filter.get("SearchSite").contains("WEST"))) {
						filter.replace("SearchSite", "WEST");
					}
					logger.fine("SearchSite WEST chosen");
					break;
				}
				case 6: {
					filter.putIfAbsent("SearchSite", "ALL");
					if(!(filter.get("SearchSite").contains("ALL"))) {
						filter.replace("SearchSite", "ALL");
					}
					logger.fine("SearchSite ALL chosen");
					break;
				}
				case 0:{
					return filter;
				}
				default:{
					System.out.println("Error in StatsSiteChoiceMenu");
					break;
				}

				}
			}
			catch (NumberFormatException e) {
				System.out.println("Wrong choice! Try again");
				logger.warning("Wrong choice! Try again");
				ok = false;
				inputCorrect = false;
			}
			if(inputCorrect) {
				Map<String, Map<String,String>> temp = config.filterFilesOnSite(config.findTransactions(), filter);
				if(temp.isEmpty()) {
					System.out.println("No transactions found for this site");
					ok = false;
				}
			}
		}while(ok == false);
		return filter;
	}

	/**
	 * Allows the user to choose the time period for the statistics, along with the start date.
	 * Input-checks are in place for checking if the date is acceptable.
	 * Does not accept weekend start days NOR Days for which there is no data available, Adjusts accordingly.
	 * Has several early exits to caller with return false.
	 * 
	 * Sets the filterDate (for use in automated statistics) to startDate.
	 * 
	 * Stores chosen parameters (period, start date) in searchParameterMap
	 * see "Filter on period" method for more detail.
	 * @param filter container to be filled with searchParameter
	 * @return Map String, String filter: filled with Key- searchPeriod, value- DAY, WEEK, MONTH
	 * 
	 */
	@SuppressWarnings("resource")
	public static Map<String, String> statsPeriodChoiceMenu(Map<String, String> filter, HQ hq, Config config) {
		Scanner keyboard = new Scanner(System.in);
		Scanner in = keyboard;
		keyboard.useDelimiter(System.lineSeparator());
		hq.setStartDate(null);
		boolean ok = false;
		boolean okDate = true;
		System.out.println("Please choose a Period");
		int i = 1;
		for(String s:config.getPeriods()) {System.out.println(String.format("%d  %s",i++, s));};
		System.out.println("0  Exit");

		int menuChoice = 0;
		do {
			try {
				String input=keyboard.next();
				menuChoice=Integer.parseInt(input);
			}catch(InputMismatchException IME) {System.out.println("Error when choosing in menu" +IME.toString());}
			if(menuChoice <0 || menuChoice >3) {System.out.println("Please try again:");}
		}while(menuChoice <0 || menuChoice >3);

		if(menuChoice == 0) {
			return filter;
		}
		do {
			okDate = true;
			System.out.println("Input start date: yyyy-mm-dd, write \"BACK\" to return");
			String choiceOfDate = in.next();

			if(choiceOfDate.toUpperCase().contains("BACK")) {
				return filter;										//EARLY EXIT / ESCAPE
			}
			try {
				hq.setStartDate(LocalDate.parse(choiceOfDate));

			}catch(DateTimeException e) {
				okDate = false;
				System.out.println("Incorrect input, Try Again!");
			}
			if(okDate) {
				if(menuChoice == 1 && ((hq.getStartDate() == null || hq.getStartDate().getDayOfWeek().getValue() == 6 || hq.getStartDate().getDayOfWeek().getValue() == 7))) {
					System.out.println("This date is a weekend, it's " +hq.getStartDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toString());
					System.out.println("Please choose another date:");
					ok = false;
				}
				else if(!(config.getSuperCurrencyMap().containsKey(hq.getStartDate()))){
					System.out.println("No data found for the chosen date.");
					do {
						hq.setStartDate(hq.getStartDate().plusDays(1));
					}while(!(config.getSuperCurrencyMap().containsKey(hq.getStartDate())));
					hq.setFilterDate(hq.getStartDate());
					System.out.println("Adjusted start date to: " +hq.getStartDate().toString());
					ok = true;
				}
				else if( hq.getStartDate() == null) {
					ok = false;
				}
				else{
					hq.setFilterDate(hq.getStartDate());
					ok = true;
				};
			}
		}while(ok == false);
		switch(menuChoice) {
		case 1:{
			filter.putIfAbsent("SearchPeriod", "DAY");
			break;
		}
		case 2:{
			filter.putIfAbsent("SearchPeriod", "WEEK");
			break;
		}
		case 3:{
			filter.putIfAbsent("SearchPeriod", "MONTH");
			break;
		}
		case 0:{
			return filter;
		}
		default:{
			System.out.println("Error in StatsPeriodChoiceMenu");
		}
		}
		return filter;

	}

	/**
	 * Allows the user to choose which currency to use.
	 * Checks for all available distinct (Streamed) currencies in the database using the period.
	 * Saves the chosen parameters (currency or all) in the filter. 
	 * Takes the startDate chosen in statsPeriodChoiceMenu and makes adjustments if necessary.
	 * see statsPeriodChoiceMenu 
	 * @param filter : which is a container to be filled by the method.
	 * @return Map String, String filter : now filled with "SearchCurrency" key and the choice of currency.
	 * @throws NullPointerException
	 */
	@SuppressWarnings("resource")
	public static Map<String, String> statsCurrencyChoiceMenu(Map<String, String> filter, HQ hq, Config config) throws NullPointerException{

		boolean ok = false;
		List<String> searchList = new ArrayList<>();
		Map<LocalDate,Map<String, Double>> currencyMap = new HashMap<>(config.getSuperCurrencyMap());
		Set<LocalDate> bigKeySet = currencyMap.keySet();
		LocalDate startDate = hq.getStartDate();
		LocalDate endDate = null;
		switch(filter.get("SearchPeriod")) {
		case "DAY":{
			endDate = startDate;
			break;
		}
		case "WEEK":{
			LocalDate inputDate = hq.getStartDate();
			startDate = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			endDate = startDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
			System.out.println(String.format("Chosen date: %s %s. Adjusted search-period to include:\nStart date: %s %s and End date: %s %s", inputDate.getDayOfWeek().toString(), inputDate.toString(),startDate.getDayOfWeek().toString(), startDate.toString(),endDate.getDayOfWeek().toString(), endDate.toString()));
			break;
		}
		case "MONTH":{
			LocalDate inputDate = hq.getStartDate();
			startDate = inputDate.with(TemporalAdjusters.firstDayOfMonth());
			endDate = inputDate.with(TemporalAdjusters.lastDayOfMonth());
			System.out.println(String.format("Chosen date: %s %s. Adjusted search-period to include:\nStart date: %s %s and End date: %s %s", inputDate.getDayOfWeek().toString(), inputDate.toString(),startDate.getDayOfWeek().toString(), startDate.toString(),endDate.getDayOfWeek().toString(), endDate.toString()));
			break;
		}
		default: {System.out.println("Period not found (Error in statsCurrencyChoiceMenu");
		break;
		}
		}
		for(LocalDate ld:bigKeySet) {
			if(ld.isBefore(endDate.plusDays(1)) && ld.isAfter(startDate.minusDays(1))){
				Map<String,Double> temp = currencyMap.get(ld);
				searchList.addAll(temp.keySet());
			}
		}
		Stream<String> searchListStream = searchList.stream().distinct();
		searchList = searchListStream.collect(Collectors.toList());

		if(searchList.isEmpty()) {
			System.out.println("No currencies available during the chosen period");
			return filter;													//RETURNS EARLY / ESCAPES
		}
		else {
			System.out.println("These currencies are available");
			System.out.println("\nWrite\"ALL\" to include every currency\nWrite \"BACK\" to return");
		}

		for(String search : searchList) {
			System.out.println(search);
		}

		Scanner in = new Scanner(System.in);
		do {
			String choice = in.next().toUpperCase();
			if(choice.trim().contains("BACK")){
				return filter; 												//RETURNS EARLY / ESCAPES
			}

			if(choice.contains("ALL")) {
				filter.putIfAbsent("SearchCurrency", choice.toString());
				ok = true;
			}
			else if(searchList.contains(choice.trim())) {
				filter.putIfAbsent("SearchCurrency",choice.trim());
				ok = true;
			}

			else {
				System.out.println("Try again");
			}
		}while(ok == false);

		return filter;
	}

	/**
	 * Contents in general:
	 * Startup sequence. 
	 * Saves choices to the filter.
	 * Holds algorithm for automated statistics, triggering different behavior depending on choices and filters.
	 * Checks for erroneous inputs in dates.
	 * Runs statistics.
	 * see statSiteChoiceMenu, statPeriodChoiceMenu, statCurrencyChoiceMenu. Takes user input for chosen Site, Period and Currency.
	 * @param args (Not used by default)
	 */
	@SuppressWarnings({ "resource" })
	public static void main(String[] args) {
		
		logger = Logger.getLogger("affix.java.project.moneyservice");
		HQ hq = new HQ();
		Config config = new Config();
		HQIO hqio = new HQIO();
		startUp(args, hq, config,hqio);

		String logFormat = hqio.getLogFormat();

		try {
			if(logFormat.equals("text")) {
				fh = new FileHandler(hqio.getLogName());
				fh.setFormatter(new SimpleFormatter());
			}
			else {
				fh = new FileHandler("HQlog.xml");
				fh.setFormatter(new XMLFormatter());
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.addHandler(fh);

		String currentLevel = hqio.getLogLevel();

		logger.setLevel(Level.parse(currentLevel));

		boolean continueWithProgram = true;
		logger.finer("Creating HQ");

		Scanner keyboard = new Scanner(System.in);
		keyboard.useDelimiter(System.lineSeparator());

		do {
			Map<String, String> filter = new HashMap<>();
			continueWithProgram = true;
			System.out.println("------------------------------------------------------------------");
			System.out.println("Main Menu:\n1 Statistics\n2 Configure\n3 Control SiteReports\n0 Exit");
			try {
				String input = keyboard.next();
				int menuChoice = Integer.parseInt(input);
				switch(menuChoice) {
				case 1:{
					logger.fine("Pressed 1. Statistics");
					filter = statsSiteChoiceMenu(filter, config);
					if(filter.containsKey("SearchSite") && continueWithProgram == true) {
						filter = statsPeriodChoiceMenu(filter, hq, config);
					}
					else {
						continueWithProgram = false;
					}
					if(filter.containsKey("SearchPeriod") && continueWithProgram == true) {
						filter = statsCurrencyChoiceMenu(filter, hq, config);
					}
					else {
						continueWithProgram = false;
					}
					if(filter.containsKey("SearchCurrency") && filter.containsKey("SearchSite") && filter.containsKey("SearchPeriod") && (hq.getStartDate() != null) && continueWithProgram == true) {
						continueWithProgram = true;
					}
					else {
						continueWithProgram = false;
					}
					if(continueWithProgram == true) {

						// Case 1
						if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("DAY") && ((!(filter.get("SearchCurrency").contains("ALL")))))){

							hq.runStatistics(filter, config, hqio);
						}

						//Case 2
						else if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("DAY") && ((filter.get("SearchCurrency").contains("ALL"))))){

							for(String s: config.getSuperCurrencyMap().get(hq.getFilterDate()).keySet()) {
								filter.replace("SearchCurrency",s);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
							filter.replace("SearchCurrency", "ALL");
							hq.runStatistics(filter, config, hqio);
							hq.clearHQ();
						}

						//Case 3
						else if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("WEEK") && (!(filter.get("SearchCurrency").contains("ALL"))))){

							LocalDate startDate = hq.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
							LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
							Stream<LocalDate> streamedperiod = startDate.datesUntil(endDate.plusDays(1), Period.ofDays(1));
							List<LocalDate> period = streamedperiod.collect(Collectors.toList());

							for(LocalDate ld:period) {
								filter.replace("SearchPeriod", "DAY");
								hq.setFilterDate(ld);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
							filter.replace("SearchPeriod","WEEK");
							hq.runStatistics(filter, config, hqio);
							hq.clearHQ();
						}

						//Case 4
						else if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("WEEK") && ((filter.get("SearchCurrency").contains("ALL"))))){	

							LocalDate startDate = hq.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
							LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
							Stream<LocalDate> streamedperiod = startDate.datesUntil(endDate.plusDays(1), Period.ofDays(1));
							List<LocalDate> period = streamedperiod.collect(Collectors.toList());

							for(LocalDate ld:period) {
								if(ld.isBefore(hq.getStartDate())) {
									continue;
								}
								else {
									hq.setFilterDate(ld);
									for(String currency:config.getSuperCurrencyMap().get(ld).keySet()) {
										filter.replace("SearchPeriod", "DAY");
										filter.replace("SearchCurrency",currency);
										hq.runStatistics(filter, config, hqio);
										hq.clearHQ();
									}
								}
							}
							filter.replace("SearchCurrency", "ALL");
							filter.replace("SearchPeriod","WEEK");
							hq.runStatistics(filter, config, hqio);
							hq.clearHQ();
						}


						//Case 5
						else if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("MONTH") && (!(filter.get("SearchCurrency").contains("ALL"))))){

							hq.runStatistics(filter, config, hqio);
							hq.clearHQ();
						}

						//Case 6
						else if((!(filter.get("SearchSite").contains("ALL")) && filter.get("SearchPeriod").contains("MONTH") && ((filter.get("SearchCurrency").contains("ALL"))))){	


							for(String currency: config.getSuperCurrencyMap().get(hq.getFilterDate()).keySet()) {
								filter.replace("SearchCurrency", currency);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
							filter.replace("SearchCurrency","ALL");
							hq.runStatistics(filter, config, hqio);
							hq.clearHQ();
						}

						//Case 7
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("DAY") && (!(filter.get("SearchCurrency").contains("ALL"))))){

							for(String name: config.getSiteNames()){
								filter.replace("SearchSite", name);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
						}

						//Case 8
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("DAY") && ((filter.get("SearchCurrency").contains("ALL"))))){	

							for(String name: config.getSiteNames()){
								filter.replace("SearchSite", name);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
						}

						//Case 9
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("WEEK") && (!(filter.get("SearchCurrency").contains("ALL"))))){

							LocalDate startDate = hq.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
							LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
							Stream<LocalDate> streamedperiod = startDate.datesUntil(endDate.plusDays(1), Period.ofDays(1));
							List<LocalDate> period = streamedperiod.collect(Collectors.toList());

							for(String name: config.getSiteNames()) {
								filter.replace("SearchSite", name);

								for(LocalDate ld: period) {
									hq.setFilterDate(ld);
									filter.replace("SearchPeriod","DAY");
									hq.runStatistics(filter, config, hqio);
								}
								filter.replace("SearchPeriod", "WEEK");
								hq.runStatistics(filter, config, hqio);
							}

						}

						//Case 10
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("WEEK") && ((filter.get("SearchCurrency").contains("ALL"))))){


							LocalDate startDate = hq.getStartDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
							LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
							Stream<LocalDate> streamedperiod = startDate.datesUntil(endDate.plusDays(1), Period.ofDays(1));
							List<LocalDate> period = streamedperiod.collect(Collectors.toList());



							for(String name: config.getSiteNames()) {
								filter.replace("SearchSite", name);

								for(LocalDate ld: period) {
									hq.setFilterDate(ld);
									filter.replace("SearchPeriod","DAY");
									filter.replace("SearchCurrency", "ALL");
									hq.runStatistics(filter, config, hqio);
								}
								filter.replace("SearchPeriod", "WEEK");
								hq.runStatistics(filter, config, hqio);
							}
						}

						//Case 11
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("MONTH") && (!(filter.get("SearchCurrency").contains("ALL"))))){

							for(String name: config.getSiteNames()) {
								filter.replace("SearchSite", name);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
						}


						//Case 12
						else if((filter.get("SearchSite").contains("ALL") && filter.get("SearchPeriod").contains("MONTH") && ((filter.get("SearchCurrency").contains("ALL"))))){

							for(String name: config.getSiteNames()) {
								filter.replace("SearchSite", name);
								hq.runStatistics(filter, config, hqio);
								hq.clearHQ();
							}
						}	
					}
					break;
				}
				case 2:{
					createConfiguration(config);
					break;
				}

				case 3:{
					controlMoneyBox(config, hqio);
					break;
				}

				case 0:{
					System.exit(0);
					break;
				}
				default:{
					System.out.println("Error, try again");
					break;
				}
				}
			}catch(NumberFormatException e){
				System.out.println("Wrong choice! Try again");
			};
			continueWithProgram = true;
		}while(continueWithProgram == true);

	}
	/**
	 * Controls the result reported from the site, by doing a calculation if there is a difference between the moneybox sent to site
	 * and the reported moneyBox sent back from exchangeSite.
	 * User enters a date to control and the method reads the files and returns statistics from that period and gives the user info of 
	 * what currencies is reported to contain differences.
	 */
	private static void controlMoneyBox(Config config, HQIO hqio) {
		HQ hq = new HQ();
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		LocalDate searchDate= LocalDate.now();
		boolean dateOk = false;

		do {
			try { 
				System.out.println("Enter date to control:");
				searchDate= LocalDate.parse(in.next());
				dateOk = true;
			}
			catch (DateTimeParseException e) {
				logger.info("Wrong date");
				dateOk = false;
			}
		}while(!dateOk);

		Map<String, Double> differenceMap = new HashMap<>();
		Map <String, Double> moneyBoxMap =config.getSuperCurrencyMap().get(searchDate);


		LocalDate fileDate = LocalDate.now();
		File temp = new File("./SiteReports");
		String[] fileNames = temp.list();
		String s = "./SiteReports/";
		for(String tempString : fileNames) {
			String [] parts = tempString.split("_");
			String test = parts[2];
			String[] dateParts = test.split("\\.");
			fileDate = LocalDate.parse(dateParts[0].trim());
			if(fileDate.equals(searchDate)) {
				differenceMap = hqio.parseProjectConfig(hqio.readTextFiles(s+tempString.trim()));

			}
		}
		boolean ok = true;
		Double diffRefCur = 0.0D;
		Map <String, String> filter = new HashMap<>();
		filter = HQTheApp.statsSiteChoiceMenu(filter, config);
		String site = filter.get("SearchSite");
		for(String stringCheck: differenceMap.keySet()) {
			ok = true;
			Map <String, Map <String, String>> tempMap = new HashMap<>();
			List <Transaction> transactionList = new ArrayList<>();
			List<List<Transaction>> listOfList = new ArrayList<>();
			Set<String> flatList = new HashSet<>();
			List<Transaction> flattenedList = new ArrayList<>();
			if(stringCheck.contains("SEK")) {

				filter.putIfAbsent("SearchSite", site);
				filter.putIfAbsent("SearchPeriod", "DAY");
				filter.replace("SearchCurrency", "ALL");
				tempMap = config.filterFilesOnSite(config.getSiteTransMap(), filter);
				tempMap = config.filterFilesOnPeriod(tempMap, filter, hq);
				for(String str: tempMap.keySet()) {
					flatList.addAll(tempMap.get(str).values());
				}
				for(String str1: flatList) {
					listOfList.add(hqio.readReport(str1));
				}
				listOfList.forEach(flattenedList::addAll);

				for(Transaction t: flattenedList ) {
					LocalDate ld = t.getTimeStamp().toLocalDate();
					Float tempExchangeRate = 0.0F;
					List<ExchangeRate> exTemp = new ArrayList<>();
					exTemp = config.getExchangeRateMap().get(ld);

					Double tempDiff = 0.0D;
					if(t.getMode() == TransactionMode.BUY) {
						for(ExchangeRate er:exTemp) {
							tempExchangeRate = er.getExchangeRate();
							if(er.getName().equals(t.getCurrencyCode())){
								tempDiff =  (double) (t.getAmount()*tempExchangeRate);
								tempDiff *= config.getBUYMARGIN();	
								diffRefCur -= tempDiff;
							}
						}						 
					}
					else {
						for(ExchangeRate er:exTemp) {
							tempExchangeRate = er.getExchangeRate();
							if(er.getName().equals(t.getCurrencyCode())){
								tempDiff =  (double) (t.getAmount()*tempExchangeRate);
								tempDiff *= config.getSELLMARGIN();
								diffRefCur += tempDiff;
							}
						}				
					}

				}

				if(!(moneyBoxMap.get(stringCheck)+ diffRefCur.intValue() == differenceMap.get(stringCheck)) ) {
					System.out.println("Moneybox for Ref currency " + stringCheck+" not correct");
					ok = false;	
				}
			}
			else if(!(differenceMap.get(stringCheck).equals(moneyBoxMap.get(stringCheck)))) {

				filter.putIfAbsent("SearchSite", site);
				hq.setFilterDate(searchDate);
				filter.putIfAbsent("SearchPeriod", "DAY");
				filter.putIfAbsent("SearchCurrency", stringCheck);
				if(filter.containsKey("SearchCurrency")) {
					filter.replace("SearchCurrency", stringCheck);
				}
				tempMap = config.filterFilesOnSite(config.getSiteTransMap(), filter);
				tempMap = config.filterFilesOnPeriod(tempMap, filter, hq);

				for(String str: tempMap.keySet()) {
					flatList.addAll(tempMap.get(str).values());
				}
				for(String str1: flatList) {
					listOfList.add(hqio.readReport(str1));
				}
				listOfList.forEach(flattenedList::addAll);
				transactionList = config.filterOnCurrency(flattenedList, filter.get("SearchCurrency").toString());
				int result = 0;
				for(Transaction t : transactionList) {
					if(t.getMode() == TransactionMode.BUY) {

						result += t.getAmount();
					}
					else {
						result -= t.getAmount();
					}
				}
				if(!(moneyBoxMap.get(stringCheck)+ result == differenceMap.get(stringCheck)) ) {
					ok = false;
				}
				if(!ok) {
					System.out.println("Moneybox for currency " + stringCheck+" not correct");
				}
			}
		}
		if(ok) {
			System.out.println("Money Box check OK!");
		}
	}

	/**
	 * this method creates a ProjectConfig.txt file to be used in MoneyService Exchange-site to set up the moneyBox.
	 * The user enters the reference currency (Reference currency), and then adds each currency to the file with values of how
	 * much money should be added to moneyBox along with additional information, such as filename path to 
	 * the required daily currencyConfig-file.
	 */
	@SuppressWarnings("resource")
	public static void createConfiguration(Config config) {
		System.out.println("Create ProjectConfig files");
		List <String> currencies = new ArrayList<>();
		List <Integer> value = new ArrayList<>();
		Scanner in = new Scanner(System.in);
		boolean again = true;
		boolean ok = false;
		boolean valid = false;
		String refCurrency = "";
		String tempCurrency = "";
		do {
			System.out.println("Enter date for currency search YYYY-MM-DD");
			try{
				LocalDate searchDate = LocalDate.parse(in.nextLine().trim());
				System.out.println("What reference currency do you want?");
				String tempRef = in.next().toUpperCase().trim();
				if(config.getSuperCurrencyMap().keySet().contains(searchDate)) {
					Set<String> keySet = config.getSuperCurrencyMap().get(searchDate).keySet();
					for(String temp: keySet) {
						if(temp.strip().toUpperCase().equals(tempRef)) {
							refCurrency = tempRef;
							ok=true;	
						}
					}
				}
				else {
					System.out.println("Reference currency not found in database for the chosen date.");
					System.out.println("Warning, outside of controlled currencies");
					System.out.println("Creating new currency.");
					refCurrency = tempRef;
					ok = true;
				}
				if(!ok) {
					System.out.println("Reference currency dont exist");
					in.nextLine().trim();
				}

			}
			catch(NullPointerException e) {
				System.out.println("Date does not exist" );
				logger.info(e.toString());

			}
			catch (DateTimeParseException e) {
				logger.info(e.toString());
			}
		}while(!ok);
		List<String> okCurrency = new ArrayList<>();
		List<String> addedCurrencies = new ArrayList<>();
		for(String currency : config.getCurrencyMap().keySet()) {

			okCurrency.add(currency);

		}
		do{
			do {
				valid = false;
				System.out.println("Enter currency to add to the file.");
				if( okCurrency.contains( tempCurrency = in.next().toUpperCase()) && !(addedCurrencies.contains(tempCurrency))) {
					addedCurrencies.add(tempCurrency);
					valid= true;


				}else {
					System.out.println("Currency not found, creating new currency");
					System.out.println("Warning, outside of controlled currencies");
					addedCurrencies.add(tempCurrency);
					valid = true;


				}
			}while(!valid);
			try {
				System.out.println("Enter amount of currency.");

				int tempValue = in.nextInt();

				System.out.println("Do you want to add more currencies?\ny=Yes, n=No");
				String userChoice = in.next();
				if(tempCurrency != "" && tempValue != 0) {

					currencies.add(tempCurrency);
					value.add(tempValue);
				}else {
					tempCurrency = "";
					tempValue = 0;
				}
				if( !userChoice.contains("y".toLowerCase())) {
					again = false;
				}
			}
			catch (InputMismatchException e) {
				logger.info(e.toString());
				System.out.println("Input is not a number try again ");
				in.nextLine();
				addedCurrencies.remove(addedCurrencies.size()-1);

			}
			catch(NoSuchElementException e) {
				logger.info(e.toString());
				System.out.println("input string empty try again; ");

			}
		}while(again != false);
		createProjectConfigTxt(currencies, value, refCurrency);

	}

	/**
	 * helper method to create the .txt (text) file.
	 * @param currency Holding currency as strings.
	 * @param value holding value of each currency as Integer.
	 * @param refCurrency : holds the currency to be set as reference currency.
	 */
	public static void createProjectConfigTxt(List <String> currency, List <Integer> value, String refCurrency) {
		LocalDate currentTime = LocalDate.now();
		String filename = "./Configs/ProjectConfig_"+ currentTime.toString()+".txt";
		try(PrintWriter pw = new PrintWriter(new FileWriter(filename.trim()))){
			pw.println("CurrencyConfig = CurrencyConfig_" + currentTime.toString()+ ".txt");
			for(int i = 0; i < currency.size(); i++) {
				pw.println(currency.get(i).toString()+ " = " + value.get(i).toString());
			}
			pw.println("End");
			pw.println("ReferenceCurrency = " + refCurrency);
		}
		catch(IOException ioe) {
			logger.info(ioe.toString());
			System.out.println(String.format("Error when Saving ProjectConfig as text" ));
		}
	}



}
