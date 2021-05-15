package affix.java.project.moneyservice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("javadoc")
public class Statistics {

	//Attributes
	private Map<LocalDate, Map<String,Double>> superCurrencyMap;
	private Map<LocalDate,List<ExchangeRate>> exchangeRateMap;
	private List<Transaction> sellList;
	private List<Transaction> buyList;
	private DoubleSummaryStatistics recalculatedSell;
	private DoubleSummaryStatistics recalculatedBuy;
	private List<DoubleSummaryStatistics> doubleList = new ArrayList<>();
	private Double result;

	/**
	 * logger 
	 */
	private static Logger logger;

	static {
		logger = Logger.getLogger("affix.java.project.moneyservice");
	}


	//Constructor
	public Statistics(Config config) {
		superCurrencyMap = config.getSuperCurrencyMap();
		exchangeRateMap = config.getExchangeRateMap();
	};
	
	//Methods
	/**
	 * This method takes the filter as a helper and the information provided by the calculations made "Filter finish".
	 * Presents calculated statistics based on whether the result is over 1 SEK and contains at least 1 transaction.
	 * @param filter : for use by the presentations and choosing certain aspects of the presentation.
	 */
	public void presentStatistics(Map<String,String> filter, HQ hq) {
		if(result < 1){
			System.out.println(String.format("Profit too low to be presented for site: %s, currency: %s, date: %s", filter.get("SearchSite").toString(), filter.get("SearchCurrency"), hq.getFilterDate().toString()));
		}
	else if(recalculatedBuy.getCount()<1 && recalculatedSell.getCount()<1){
		System.out.println(String.format("No transactions found for day: %s and currency: %s", hq.getFilterDate().toString(), filter.get("SearchCurrency").toString()));
	}
	else
		{
		System.out.println("------------------------------------------------------------------");
		System.out.println(String.format("Statistics for Site:%s Currency:%s Period:%s. ",filter.get("SearchSite").toString(),filter.get("SearchCurrency").toString(),filter.get("SearchPeriod").toString()));
		if(!(filter.get("SearchPeriod").contains("WEEK") || filter.get("SearchPeriod").contains("MONTH"))) {
			System.out.println("Now showing: " + hq.getFilterDate().toString());
		}
		System.out.println(String.format("[SELL]    Profit: 	 %6.1f SEK 	Transactions: %s",recalculatedSell.getSum(), recalculatedSell.getCount()));
		System.out.println(String.format("[BUY]     Profit: 	 %6.1f SEK 	Transactions: %s", recalculatedBuy.getSum(), recalculatedBuy.getCount()));
		System.out.println(String.format("[SUMMARY] Period profit: %6.1f SEK  	Transactions: %d", result,recalculatedBuy.getCount()+recalculatedSell.getCount()));
		logger.fine(filter.get("SearchSite").toString()+" "+filter.get("SearchCurrency").toString()+" "
				+filter.get("SearchPeriod").toString()+ " Period profit: "+result+" SEK"+" Transactions: "+recalculatedBuy.getCount()+recalculatedSell.getCount());
		}
}

	/**
	 * This method is a helper method which clears the attributes of statistics in preparation for the next run.
	*/
	public void statClear() {
		sellList.clear();
		buyList.clear();
		recalculatedSell = null;
		recalculatedBuy = null; 
		result = (double) 0.0F;
	}

	/**
	 * 
	 * 	The method divides the remaining filtered transactions based on transaction:Mode
	 * (buy or sell). From there it extracts the sum and multiplies by daily exchange rate
	 * and the margin is calculated, profits and frequencies are gathered and stored for presentation.
	 * 
	 * @param reducedList This list has previously been subjected to previous filtering.
	 */
	public void filterFinish(List<Transaction> reducedList, Config config) {
		Stream<Transaction> sellStream = reducedList.stream().filter(w -> w.getMode()==TransactionMode.SELL);
		Stream<Transaction> buyStream = reducedList.stream().filter(w -> w.getMode()==TransactionMode.BUY);
		List<Transaction> sellList = sellStream.collect(Collectors.toList());
		List<Transaction> buyList = buyStream.collect(Collectors.toList());
		List<Float> sellResultsCalculations = new ArrayList<>();
		for(Transaction t:sellList) {
			Float rate = null;
			LocalDate searchDate = t.getTimeStamp().toLocalDate();
			if(superCurrencyMap.containsKey(searchDate)) {
				List<ExchangeRate> tempList = exchangeRateMap.get(searchDate);
				for(ExchangeRate er:tempList) {
					if(er.getName().contains(t.getCurrencyCode())) {
						rate = er.getExchangeRate();
					}
				}
				Float result = (t.getAmount()*(rate*config.getSELLMARGIN()) - (t.getAmount()*rate));
				sellResultsCalculations.add(result);
			}
			else {
				System.out.println(String.format("No rates found for date: %s for transaction ID: %d ",t.getTimeStamp().toLocalDate(),t.getId()));
			}
		}
		List<Float> buyResultsCalculations = new ArrayList<>();
		for(Transaction t:buyList) {
			Float rate = null;	
			LocalDate  searchDate = t.getTimeStamp().toLocalDate();
			if(superCurrencyMap.containsKey(searchDate)) {
				List<ExchangeRate> tempList = exchangeRateMap.get(searchDate);
				for(ExchangeRate er:tempList) {
					if(er.getName().contains(t.getCurrencyCode())) {
						rate = er.getExchangeRate();
					}
				}

				Float result = (t.getAmount()*rate) - (t.getAmount()*(rate*config.getBUYMARGIN()));
				buyResultsCalculations.add(result);
			}
			else {
				System.out.println(String.format("No rates found for date: %s for transaction ID: %d ",t.getTimeStamp().toLocalDate(),t.getId()));
			}
		}
		DoubleSummaryStatistics sellResultCalcStats = sellResultsCalculations.stream().collect(Collectors.summarizingDouble(w-> w.longValue()));
		DoubleSummaryStatistics buyResultCalcStats = buyResultsCalculations.stream().collect(Collectors.summarizingDouble(w-> w.longValue()));
		this.sellList = sellList;
		this.buyList = buyList;
		this.recalculatedBuy = buyResultCalcStats;
		this.recalculatedSell = sellResultCalcStats;
		this.doubleList.add(recalculatedBuy);
		this.doubleList.add(recalculatedSell);
		this.result = (sellResultCalcStats.getSum()+buyResultCalcStats.getSum());
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



	public List<Transaction> getSellList() {
		return sellList;
	}



	public void setSellList(List<Transaction> sellList) {
		this.sellList = sellList;
	}



	public List<Transaction> getBuyList() {
		return buyList;
	}



	public void setBuyList(List<Transaction> buyList) {
		this.buyList = buyList;
	}



	public DoubleSummaryStatistics getRecalculatedSell() {
		return recalculatedSell;
	}



	public void setRecalculatedSell(DoubleSummaryStatistics recalculatedSell) {
		this.recalculatedSell = recalculatedSell;
	}



	public DoubleSummaryStatistics getRecalculatedBuy() {
		return recalculatedBuy;
	}



	public void setRecalculatedBuy(DoubleSummaryStatistics recalculatedBuy) {
		this.recalculatedBuy = recalculatedBuy;
	}



	public List<DoubleSummaryStatistics> getDoubleList() {
		return doubleList;
	}



	public void setDoubleList(List<DoubleSummaryStatistics> doubleList) {
		this.doubleList = doubleList;
	}



	public Double getResult() {
		return result;
	}



	public void setResult(Double result) {
		this.result = result;
	}



	public static Logger getLogger() {
		return logger;
	}



	public static void setLogger(Logger logger) {
		Statistics.logger = logger;
	}



	@Override
	public String toString() {
		return String.format(
				"Statistics [superCurrencyMap=%s, exchangeRateMap=%s, sellList=%s, buyList=%s, recalculatedSell=%s, recalculatedBuy=%s, doubleList=%s, result=%s]",
				superCurrencyMap, exchangeRateMap, sellList, buyList, recalculatedSell, recalculatedBuy, doubleList,
				result);
	}

	

}
