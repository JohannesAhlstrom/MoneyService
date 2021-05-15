package affix.java.project.moneyservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class defines the input and output of files in MoneyService
 * 
 */
public class MoneyServiceIO {

	/**
	 * Attribute projectConfigFilename holding filepath for ProjectConfig
	 */
	public static String projectConfigFilename = "ProjectConfig_"+LocalDate.now().toString()+".txt";
	
	/**
	 * Attribute currencyConfigFilename holding filepath for CurrencyConfig
	 */
	public static String currencyConfigFilename = "CurrencyConfig_"+LocalDate.now().toString()+".txt";

	/**
	 * Attribute folderPaths holding File paths for storing files
	 */
	public static List<File> folderPaths = new ArrayList<>();
	
	/**
	 * Attribute serializedDailyTransactionFilename holding filename for serialized DailyTransaction
	 */
	static String serializedDailyTransactionFilename = "DailyTransactions.ser";

	/**
	 * Attribute textFormattedDailyTransactions holding filename for textfile DailyTransactions
	 */
	static String textFormattedDailyTransactions = "DailyTransactions.txt";

	/**
	 * Attribute referenceCurrency defining the referenced Currency
	 */
	public static String referenceCurrency;
	
	/**
	 * Attribute refDate holding a LocalDate.
	 */
	public static LocalDate refDate;
	
	/**
	 * Attribute LDT holding the current day and time
	 */
	public static LocalDateTime LDT = LocalDateTime.now(); //TODO

	/**
	 * Changes the filepaths for projectConfigFilename
	 *  and currencyConfigFilename to the inputed LocalDate
	 * @param date holding a LocalDate
	 */
	
	public void changeDate(LocalDate date) {
		projectConfigFilename = getPathName("Configs")+ "ProjectConfig_"+date.toString()+".txt";
		currencyConfigFilename = getPathName("DailyRates")+ "CurrencyConfig_"+date.toString()+".txt";
	}

	/**
	 * Getter for attribute referenceCurrency
	 * @return a String holding referenceCurrency
	 */
	public static String getReferenceCurrency() {
		return referenceCurrency;
	}

	/**
	 * Sets the refDate for MoneyServiceIO.
	 * @param refDate holds a LocalDate for refDate
	 */
	public static void setRefDate(LocalDate refDate) {
		MoneyServiceIO.refDate = refDate;
	}

	/**
	 * Parses the ProjectConfigFile
	 * @param listToBeParsed
	 * @return Map<String,Integer> Containing: CurrencyName and Value
	 */
	public static Map<String,Double> parseProjectConfig(List<String> listToBeParsed){
		Map<String,Double> currencyMap = new HashMap<>();
		Stream<String> fileNameStream = listToBeParsed.stream().limit(1);
		Iterator<String> fileNameStreamIter = fileNameStream.iterator();
		while(fileNameStreamIter.hasNext()) {
			String[] configParts = fileNameStreamIter.next().split("=");
			for(String s:configParts) {
				s=s.trim();
			}
			MoneyServiceIO.currencyConfigFilename = configParts[1].trim();
			String[] dateParts = configParts[1].split("_");
			String[] dateParts2 = dateParts[1].split("\\.");
			MoneyServiceIO.refDate = LocalDate.parse(dateParts2[0].trim());
		}
		Stream<String> currencyStream = listToBeParsed.stream().skip(2);
		Iterator<String> currencyIterator = currencyStream.iterator();
		while(currencyIterator.hasNext()){
			String temp = currencyIterator.next();
			if(!(temp.contains("End") || temp.contains("ReferenceCurrency"))){
				String[] boxParts = temp.split("=");
				if(boxParts[1].isBlank()) {
					boxParts[1] = "0";
				}
				currencyMap.putIfAbsent(boxParts[0].trim(), Double.parseDouble(boxParts[1].trim())); 
			}
		}
		Stream<String> refString = listToBeParsed.stream().skip(2);
		Iterator<String> refIterator = refString.iterator();
		while(refIterator.hasNext()) {
			String tempString = refIterator.next();
			if(tempString.contains("ReferenceCurrency")) {
				String[] parts = tempString.split("=");
				MoneyServiceIO.referenceCurrency = parts[1].trim();
			}

		}
		return currencyMap;
	}

	/**
	 * Parses an entire list of Currencies
	 * @param listToBeParsed List<String> to be parsed
	 * @return the parsed List<ExchangeRate>
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
	 * Reads a file and returns a list of strings.
	 * @param  filename for storage.
	 * @return  The read List<String>.
	 */
	public static List<String> readTextFiles(String filename) {
		List<String> readStringList = new ArrayList<String>();
		try(BufferedReader bf = new BufferedReader(new FileReader(filename.trim()))) {
			while(bf.ready()) {
				String temporaryString = bf.readLine();
				readStringList.add(temporaryString);
			}
		}catch(IOException ioe) {System.out.println("Exception when reading file");}
		return readStringList;
	}

	/**
	 * Saves the daily transactions in serialized format
	 * @param listToBeSaved List<Transaction> of Transaction to be saved
	 * @param filename String of a filename 
	 * @return boolean true if done
	 */
	public static boolean saveSerializedDailyTransactions(List<Transaction> listToBeSaved, String filename) {
		boolean saved = false;
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename.trim()))){
			oos.writeObject(listToBeSaved);
		}
		catch(IOException ioe) {System.out.println(String.format("Error when saving serialized daily transaction"+ioe.toString()));
		return false;
		}
		saved = true;
		return saved;
	}


	/**
	 * Saves the MoneyBox in a txt file
	 * @param listToBeSaved Map<String,Currency> of the MoneyBox that is to be saved
	 * @param filename holding the name of the file
	 * @return boolean holding outcome of the operation
	 */
	public static boolean saveTxtMoneyBox(Map<String, Currency> listToBeSaved, String filename) {
		boolean saved = false;
		try(PrintWriter pw = new PrintWriter(new FileWriter(filename.trim()))){
			
			pw.println("CurrencyConfig = CurrencyConfig_" + LocalDate.now().toString()+ ".txt");
			for(String k:listToBeSaved.keySet()) {
				pw.println(k+" = "+listToBeSaved.get(k).getTotalValue().intValue());
			}
			pw.println("End");
			pw.println("ReferenceCurrency = " + MoneyServiceIO.getReferenceCurrency());
				

		}
		catch(IOException ioe) {System.out.println(String.format("Error when Saving Daily Transactions as text" + ioe.toString()));
		}
		saved = true;
		return saved;
	}	


	/**
	 * Saves a daily report in serialized form.
	 * @param r Report to be saved
	 * @return Boolean true if successful.
	 */
	public static boolean saveSerializedReport(Report r) {
		boolean saved = false;
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(r.getUniqueFileName().trim()))){
			oos.writeObject(r.getDailyTransaction());
		}
		catch(IOException ioe) { 
			System.out.println(String.format("Error when saving serialized daily transaction "+ioe.toString()));
			return false;
		}
		saved = true;
		return saved;
	}



	/**
	 * Reads Serialized daily transactions
	 * @param  filename holding the name of the file to be read
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static List<Transaction> readSerializedDailyTransactionList(String filename){
		List<Transaction> transactionList = null;
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename.trim()))){
			transactionList = (List<Transaction>) ois.readObject();
		}
		catch(IOException |ClassNotFoundException ioe) {System.out.println(String.format("Error when reading serialized daily transactions"+ioe.toString()));
		}
		return transactionList;
	}

	/**
	 * Saves the dailyTransactions as a list on a text file
	 * @param dailyList List<Transaction> of Transactions to be saved
	 * @param filename String holding the name of the file to be saved
	 * @return boolean true if successful.
	 */
	public static boolean saveDailyTransactionListAsText(List<Transaction> dailyList, String filename){
		boolean saved = false;
		try(PrintWriter pw = new PrintWriter(new FileWriter(filename.trim()))){
			for(Transaction t:dailyList) {
				pw.println(t.toString());
			}
		}
		catch(IOException ioe) {System.out.println(String.format("Error when Saving Daily Transactions as text" + ioe.toString()));
		}
		saved = true;
		return saved;

	}
	
	/**
	 * Adds the String path as a File in folderPaths
	 * @param path String holding the filepath
	 */
	public static void setFolderPath(String path) {
		folderPaths.add(new File(path));
	}
	
	/**
	 * Prints out the Files in folderPaths
	 */
	public static void printPathList() {
		for(File s: folderPaths){
			System.out.println("Path:"+s.getName());
		}
	}

  /*
	public static List <File> findFolders() {
		List<File> folderList = new ArrayList<>();
		folderList.add(new File("Configs"));
		folderList.add(new File("DailyRates"));
		folderList.add(new File("Documents"));
		folderList.add(new File("Orders"));
		folderList.add(new File("SiteReports"));
		folderList.add(new File("Transactions"));

		for(File temp: folderList) {
			if(temp.exists()){
				//				System.out.println("Folder" + temp.getName() + " exsists");//DEBUG

			}
			else {
				temp.mkdir();
				System.out.println("Folder" + temp.getName() + " created.");
			}
		}
		return folderList;
	}
  */
	
	/**
	 * Gets the folderpath from folderPaths
	 * @param folder String holding the folderpath
	 * @return String of the modified path to the folder
	 */
	public static String getPathName(String folder) {
		
		String fileName = ""; 
		for(File temp: MoneyServiceIO.folderPaths) {
			if(temp.getName().equals(folder)){
				fileName = temp.getPath() + "/";

			}
		}
		if(folder == "Transactions") {
			fileName = fileName + Config.getSiteName() +"/"; 
		}
		
		return fileName;
	}

}