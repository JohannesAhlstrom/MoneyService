package affix.java.project.moneyservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

@SuppressWarnings("javadoc")
public class HQIO {
	private String parsedSiteName;
	private LocalDate parsedDate;
	private LocalDate refDate;
	private String refCurrency;
	private String logName= "HQLog";
	private String logFormat = "text";
	private String logLevel = "OFF";
	private static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}


	//Constructor
	public HQIO() {

	}


	/**
	 * 
	 * Enumerations containing logging levels.
	 *
	 */
	public enum LogLev {ALL, CONFIG, FINE, FINER, FINEST, INFO, SEVERE, WARNING, OFF}

	/**
	 * This method finds and parses the initial HQ setup and customization configuration file.
	 * Taking info such as: SITENAMES, Margins for sell and buy transactions and the current year.
	 * @return boolean true if successful.
	 */
	public boolean parseHQConfig(HQ hq, Config config) {
		logger.finer("Finds and parses the initial HQ setup");
		boolean ok = false;
		File file = new File("./Configs");
		String[] configFileNames = file.list();
		for(String s: configFileNames) {
			if(s.contains("HQConfig")) {
				String path = "./Configs/";
				String x = path+s.toString();
				List<String> readInfo = HQIO.readTextFiles(x);
				Iterator <String> iter = readInfo.iterator();
				while(iter.hasNext()) {
					String temp = iter.next();
					String[] parts = temp.split("=");
					switch(parts[0].trim()) {
					case "SITENAMES":{
						String[] names = parts[1].trim().split(",");
						config.setSiteNames(names);
						if(config.getSiteNames() == null) {
							logger.finer("No sitenames found");
							ok = false;
						}
						else {
							ok = true;
							logger.finer("SITENAMES: "+parts[1]);
						}
						break;
					}
					case "SELLMARGIN":{
						config.setSELLMARGIN(Float.parseFloat(parts[1].strip()));
						if(config.getSELLMARGIN() == null) {
							logger.finer("No sellmargin found");
							ok = false;
						}
						else {
							ok = true;
							logger.finer("Sellmargin set to: "+config.getSELLMARGIN());
						}
						break;
					}
					case "BUYMARGIN":{
						config.setBUYMARGIN(Float.parseFloat(parts[1].strip()));
						if(config.getBUYMARGIN() == null) {
							logger.finer("No buymargin found");
							ok = false;
						}
						else {
							ok = true;
							logger.finer("Buymargin set to "+config.getBUYMARGIN());
						}
						break;
					}
					case "YEAR":{
						hq.setThisYear(Integer.parseInt(parts[1].strip()));
						if(hq.getThisYear() <= 0) {
							logger.finer("Year set to "+hq.getThisYear());
							ok = false;
						}
						else {
							ok = true;
						}
						break;
					}
					case "logLevel":					
						for(LogLev l: LogLev.values()) {
							String tempLevel = l.toString();
							if(tempLevel.equals(parts[1].strip())){
								logLevel = parts[1].strip().toUpperCase().trim();
								System.out.println("Log level set to: "+logLevel);
								break;
							}
						}						 
						break;
					case "logFormat":
						if((parts[1].strip().isEmpty()) || !(parts[1].strip().toLowerCase().equals("text") || parts[1].strip().toLowerCase().equals("xml"))){
							System.out.println("************Bad log format in config file!***********");
							System.out.println("Default used: "+logFormat);
							break;
						}
						logFormat = parts[1].strip().toLowerCase();
						System.out.println("Log level set to: "+logFormat);
						break;

					case "logName":
						if(parts[1].strip().isEmpty()) {
							System.out.println("Log name missing in config file!");
							System.out.println("Default used: "+logName);
							break;
						}
						logName = parts[1].strip()+LocalDate.now();
						System.out.println("Log name set to: "+logName);
						break;

					default:{
						System.out.println("In initial HQconfiguration Default");
						ok = false;
						break;
					}
					}
				}
			}
		}
		return ok;
	}


	/**
	 * Reads text files into a list of strings for parsing.
	 * 
	 * @param filename
	 * @return containing all the rows of the filenames, used for parsing.
	 */
	public static List<String> readTextFiles(String filename) {
		List<String> readStringList = new ArrayList<String>();
		try(BufferedReader bf = new BufferedReader(new FileReader(filename.trim()))) {
			while(bf.ready()) {
				String temporaryString = bf.readLine();
				readStringList.add(temporaryString);
			}
		}catch(IOException ioe) {System.out.println("Exception when reading file");}
		logger.finest("Reads text files into a list of strings for parsing");
		return readStringList;
	}

	/**
	 * This method takes the list of strings belonging to a ProjectConfigfile and parses them.
	 * 
	 * This method uses the stream API to handle and assign the individual currencyNames and their assigned
	 * total amounts to be entered into the "moneyBox". The Strings are widely used in the application.
	 * Used to keep track of which currencies were available or used at specific periods.
	 * 
	 * @param listToBeParsed the contents of a ProjectConfigFile.
	 * @return Map String, Double for use to keep track of which currencies are available.
	 */
	public Map<String,Double> parseProjectConfig(List<String> listToBeParsed){
		Map<String,Double> currencyMap = new HashMap<>();
		Stream<String> fileNameStream = listToBeParsed.stream().limit(1);
		Iterator<String> fileNameStreamIter = fileNameStream.iterator();
		while(fileNameStreamIter.hasNext()) {
			String[] configParts = fileNameStreamIter.next().split("=");
			for(String s:configParts) {
				s=s.trim();
			}
			String[] dateParts = configParts[1].split("_");
			String[] dateParts2 = dateParts[1].split("\\.");
			refDate = LocalDate.parse(dateParts2[0].trim());
		}
		Stream<String> currencyStream = listToBeParsed.stream().skip(2);
		Iterator<String> currencyIterator = currencyStream.iterator();
		while(currencyIterator.hasNext()){
			String temp = currencyIterator.next();
			if(!(temp.contains("End") || temp.contains("ReferenceCurrency"))){
				String[] boxParts = temp.split("=");
				currencyMap.putIfAbsent(boxParts[0].trim(), Double.parseDouble(boxParts[1].trim()));
			}
		}
		Stream<String> refString = listToBeParsed.stream().skip(2);
		Iterator<String> refIterator = refString.iterator();
		while(refIterator.hasNext()) {
			String tempString = refIterator.next();
			if(tempString.contains("ReferenceCurrency")) {
				String[] parts = tempString.split("=");
				refCurrency = parts[1].trim();
			}

		}
		return currencyMap;
	}

	/**
	 *This method takes the list of strings belonging to a CurrencyConfigfile and parses them.
	 * 
	 * Parses currency Configuration files into the daily ExchangeRates which are later mapped 
	 * with a localDate as Key.
	 * 
	 * @param listToBeParsed the contents of a CurrencyConfigFile.
	 * @return List ExchangeRate for use by the system when running stats-calculations.
	 */
	public static List<ExchangeRate> parseCurrencyConfig(List<String> listToBeParsed) {
		List<ExchangeRate> exchangeRateList = new ArrayList<>();
		List<String> CurrencyList = new ArrayList<String>();
		Stream<String> streamToBeIterated = listToBeParsed.stream().skip(1);
		Iterator<String> iter = streamToBeIterated.iterator();
		while(iter.hasNext()) {
			String tempString = iter.next();
			CurrencyList.add(tempString);
		}
		Iterator<String> listIter = CurrencyList.iterator();
		while(listIter.hasNext()) {
			String[] parts = listIter.next().split("\t");
			for(String s:parts) {
				s = s.trim();
			}

			LocalDate day = LocalDate.parse(parts[0]);
			String[] valueParts = parts[2].split(" ");
			int scalar = Integer.parseInt(valueParts[0].trim());
			Float price = Float.parseFloat(parts[3].trim());
			try {
				ExchangeRate er = new ExchangeRate(day,scalar,valueParts[1].trim(),price);
				exchangeRateList.add(er);
			}
			catch(IllegalArgumentException ioe) {System.out.println(String.format("NO"));
			};

		}
		return exchangeRateList;

	}

	/**
	 * Reads a file filled with transactions. Parses the date and handled site.
	 * @param filename from which to read the objects (Handled as Path).
	 * @return List Transaction from the report, these are handled in HQ.runStatistics().
	 */
	@SuppressWarnings("unchecked")
	public List<Transaction> readReport(String filename){
		String[] fileNamePartsSeparation = filename.split("_");
		String[] datePartsSeparated = fileNamePartsSeparation[2].split("\\.");
		parsedDate = LocalDate.parse(datePartsSeparated[0].trim());
		parsedSiteName = fileNamePartsSeparation[1].trim();

		List<Transaction> readList = new ArrayList<Transaction>();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))){
			readList = (List<Transaction>) ois.readObject();
		}
		catch(IOException |ClassNotFoundException ioe){System.out.println("Error in readingReport" +ioe.toString());}
		return readList;
	}


	public String getParsedSiteName() {
		return parsedSiteName;
	}


	public void setParsedSiteName(String parsedSiteName) {
		this.parsedSiteName = parsedSiteName;
	}


	public LocalDate getParsedDate() {
		return parsedDate;
	}


	public void setParsedDate(LocalDate parsedDate) {
		this.parsedDate = parsedDate;
	}


	public LocalDate getRefDate() {
		return refDate;
	}


	public void setRefDate(LocalDate refDate) {
		this.refDate = refDate;
	}


	public String getRefCurrency() {
		return refCurrency;
	}


	public void setRefCurrency(String refCurrency) {
		this.refCurrency = refCurrency;
	}


	public String getLogName() {
		return logName;
	}


	public void setLogName(String logName) {
		this.logName = logName;
	}


	public String getLogFormat() {
		return logFormat;
	}


	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}


	public String getLogLevel() {
		return logLevel;
	}


	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}


	@Override
	public String toString() {
		return String.format(
				"HQIO [parsedSiteName=%s, parsedDate=%s, refDate=%s, refCurrency=%s, logName=%s, logFormat=%s, logLevel=%s]",
				parsedSiteName, parsedDate, refDate, refCurrency, logName, logFormat, logLevel);
	}

}