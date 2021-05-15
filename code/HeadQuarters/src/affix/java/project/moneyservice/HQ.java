package affix.java.project.moneyservice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


@SuppressWarnings("javadoc")
public class HQ {

	//Attributes
	private List<List<Transaction>> transactionList;
	private List<Transaction> reductionList;
	private Map<String, Map<String,String>> fileNames;
	private LocalDate startDate;
	private int thisYear;
	private LocalDate filterDate;
	@SuppressWarnings("unused")
	private static Logger logger;
  
	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}
	
	/**
	 * Constructor
	 */
	public HQ() {
		this.transactionList = new ArrayList<>();
		this.reductionList = new ArrayList<>();
		this.fileNames = new HashMap<>();
	}
	
	/**
	 * This method reduces a map of filenames and performs the necessary
	 * calculations for presenting the user with statistics.
	 * Filenames contained in the format of Map String, Map String, String
	 * are filtered based on information criteria in the filter parameter.
	 * 
	 * The filtered filenames, which are paths to Daily Reports of transactions are read from the 
	 * locations and subsequently transformed from reports to transactions via flattened sets.
	 * The set is used in order to not accept doubles.
	 * 
	 * This method also calls "filter finish" in Statistics for finishing statistics
	 * presentStatistics in statistics for presenting the statistics to the user.
	 * @param filter Contains parameters for stream reductions.
	 */
	//Methods
	public void runStatistics(Map<String,String> filter, Config config, HQIO hqio) {
		fileNames = config.getSiteTransMap();
		fileNames = config.filterFilesOnSite(fileNames, filter);
		if(fileNames.isEmpty()) {
			System.out.println("No transactions found");}
		fileNames = config.filterFilesOnPeriod(fileNames, filter, this);
		
		Set<String> flatList = new HashSet<>();
		for(String s: fileNames.keySet()) {
			flatList.addAll(fileNames.get(s).values());
		}
		for(String s: flatList) {
			transactionList.add(hqio.readReport(s));
		}
		Statistics statRun = new Statistics(config);
		List<Transaction> flattenedList = new ArrayList<>();
		transactionList.forEach(flattenedList::addAll);
		reductionList = config.filterOnCurrency(flattenedList, filter.get("SearchCurrency"));
		statRun.filterFinish(reductionList, config);
		statRun.presentStatistics(filter, this);
	}
	
	/**
	 * This method is a helper method which clears the attributes of HQ in preparation for the 
	 * next run of Statistics. 
	 * @return boolean cleared. 
	 */
	public boolean clearHQ() {
		boolean cleared = false;
		getTransactionList().clear();
		getFileNames().clear();
		getReductionList().clear();
		cleared = true;
		return cleared;
	}

	public List<List<Transaction>> getTransactionList() {
		return transactionList;
	}

	public void setTransactionList(List<List<Transaction>> transactionList) {
		this.transactionList = transactionList;
	}

	public List<Transaction> getReductionList() {
		return reductionList;
	}

	public void setReductionList(List<Transaction> reductionList) {
		this.reductionList = reductionList;
	}

	public Map<String, Map<String, String>> getFileNames() {
		return fileNames;
	}

	public void setFileNames(Map<String, Map<String, String>> fileNames) {
		this.fileNames = fileNames;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public int getThisYear() {
		return thisYear;
	}

	public void setThisYear(int thisYear) {
		this.thisYear = thisYear;
	}

	public LocalDate getFilterDate() {
		return filterDate;
	}

	public void setFilterDate(LocalDate filterDate) {
		this.filterDate = filterDate;
	}

}
