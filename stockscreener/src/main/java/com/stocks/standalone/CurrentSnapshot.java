package com.stocks.standalone;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stocks.util.Utility;

public class CurrentSnapshot {
	static String[] CNBC_ETF_LIST = null;

	final static String KEY_FILTER_CHECKS = "FILTER_CHECKS";
	final static List<FilterCheck> listFilterCheck = new ArrayList<FilterCheck>();
	final static Map<String, List<Entry>> mPositions = new HashMap<String, List<Entry>>();

	final static String CNBC_URL = "http://data.cnbc.com/quotes/";
	final static String CNBC_URL_EXTN = "http://apps.cnbc.com/company/quote/index.asp?symbol=";
	final static String CNBC_URL_EXTN_COMPANY_PROFILE = "http://apps.cnbc.com/view.asp?country=US&uid=stocks/summary&symbol=";

	final static String ROW_FORMAT = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n";
	final static String ROW_FORMAT_POSITIONS = "%s,%s,%s,%s,%s,%s%n";

	final static Double RECOMMENDATION_52W_CORRECTION_PC = -40.00;
	final static Double RECOMMENDATION_52W_APPRECIATION_PC = 9.00;

	final static NumberFormat NF = NumberFormat.getInstance();

	private List<String> symbols;

	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	private static void loadConfigurationFromXml() {
		listFilterCheck.clear();
		mPositions.clear();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse( CurrentSnapshot.class.getResourceAsStream("/CurrentSnapshot.xml") );
			doc.getDocumentElement().normalize();

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			CNBC_ETF_LIST = getTagValue("symbols", doc.getDocumentElement()).replaceAll(" ", "").split(",");

			NodeList nList = doc.getElementsByTagName("filter_check");
			//System.out.println("-----------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					String name = getTagValue("name", eElement);
					String sRange = getTagValue("range_correction_52_wk", eElement);
					String sMinMarketCap = getTagValue("range_market_cap", eElement);
					String sMinSharesOutstanding = getTagValue("min_shares_outstanding", eElement);
					String sDailyAvgTradeValue = getTagValue("daily_avg_trade_value", eElement);

					int indexOfDash = sRange.indexOf("-");
					Range correctionRange52wk = new CurrentSnapshot().new Range(
							new Long(sRange.substring(0, indexOfDash)), 
							new Long(sRange.substring(indexOfDash + 1)));

					indexOfDash = sMinMarketCap.indexOf("-");
					Range minMarketCapRange = new CurrentSnapshot().new Range(
							new Long(Utility.convertFinancials(sMinMarketCap.substring(0, indexOfDash))), 
							new Long(Utility.convertFinancials(sMinMarketCap.substring(indexOfDash + 1))));
					
					Long minSharesOutstanding = new Long(Utility.convertFinancials(sMinSharesOutstanding));
					Long dailyAvgTradeValue   = new Long(Utility.convertFinancials(sDailyAvgTradeValue));

					FilterCheck filterCheck = new CurrentSnapshot().new FilterCheck(name, correctionRange52wk, minMarketCapRange, minSharesOutstanding, dailyAvgTradeValue);
					listFilterCheck.add(filterCheck);
				}
			}
			
			// Populate Entries
			NodeList positionList = doc.getElementsByTagName("position");
			for (int i=0; i < positionList.getLength(); i++) {
				final Node nNode = positionList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element eElement = (Element) nNode;
					final String symbol = getTagValue("symbol", eElement);
					mPositions.put(symbol, new ArrayList<Entry>());
					
					NodeList entryList = eElement.getElementsByTagName("entry");
					for(int j=0; j<entryList.getLength(); j++){
						final Element eEntry = (Element) entryList.item(j);
						final String buyDate = getTagValue("buy-date", eEntry);
						final String qty = getTagValue("qty", eEntry);
						final String price = getTagValue("price", eEntry);
						
						final Entry entry = new CurrentSnapshot().new Entry(symbol, new Double(price), new Integer(qty), Utility.getDate(buyDate));
						mPositions.get(symbol).add(entry);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue().trim();
	}

	/**
	 * Main method to trigger the process.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		loadConfigurationFromXml();
		
		System.out.println("Configuration (CORRECTION_52_WK must match, then either of (MIN_MARKET_CAP or MIN_OUTSTANDING_SHARES) )");
		System.out.println("=======================================================================================================");
		for (FilterCheck filterCheck : listFilterCheck) {
			System.out.println(filterCheck);
		}
		System.out.println();

		final StringBuilder sbuf = new StringBuilder();
		final StringBuilder sbufRejects = new StringBuilder();
		final StringBuilder sbufCombined = new StringBuilder();
		final StringBuilder sbufPositions = new StringBuilder();

		final String header = String.format(ROW_FORMAT, 
				"Symbol",
				"realTime",
				"range",
				"% Change",
				"Industry",
				"range52wL_pc",
				"range52wH_pc",
				"Mkt Cap (Expanded)",
				"Mkt Cap",
				"10-Day Avg Vol (Expanded)",
				"10-Day Avg Vol",
				"Daily Avg Trade Value",
				"P/E",
				"Beta",
				"Div/Yield",
				"Analyst Consensus",
				"Shares Outstanding",
				"time",
				"range52wL",
				"range52wH",
				"Best Match"
			);
		final String headerPositions = String.format(ROW_FORMAT_POSITIONS, "Symbol", "Buy Date", "Price", "Qty", "Real-Time", "Profit/Loss %");
		
		sbuf.append(header);
		sbufRejects.append(header);
		sbufPositions.append( headerPositions );

		processCnbc(sbuf, sbufRejects, sbufPositions);
		sbufCombined.append(sbuf).append(sbufRejects.substring( sbufRejects.indexOf("\n")+1 ));
		
		Utility.saveContent("rpt.csv", sbuf.toString());
		Utility.saveContent("rptRejects.csv", sbufRejects.toString());
		Utility.saveContent("rptCombined.csv", sbufCombined.toString());
		Utility.saveContent("rptPositions.csv", sbufPositions.toString());

		System.out.println( "Tips: Pick Stock of lower P/E (Price / EPS) if Market Cap is of both stocks is pretty close. e.g. if P/E of A ($20 / $2 = 10) and P/E of B ($30 / $2 = 15) then A is better than B." );
		System.out.println("Done.");
	}

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processCnbc(final StringBuilder sbuf, final StringBuilder sbufRejects, final StringBuilder sbufPositions) throws Exception {
		Set<String> set = new HashSet(Arrays.asList(CNBC_ETF_LIST));
		set.addAll( mPositions.keySet() );
		
		// Set contains all symbols in <symbols> and <positions>
		for (String symbol : set) {
			symbol = symbol.trim();
			System.out.println("Pulling [" + symbol + "]");

			try {
				// Start: Pull Data
				final StringBuffer sb = new StringBuffer(
						Utility.getContent(CNBC_URL + symbol));
				final StringBuffer sbExtn = new StringBuffer(
						Utility.getContent(CNBC_URL_EXTN + symbol));
				final StringBuffer sbExtnCompPro = new StringBuffer(
						Utility.getContent(CNBC_URL_EXTN_COMPANY_PROFILE
								+ symbol));

				String realTime = "";
				String pcChange = "";
				int index1 = sb.indexOf("cnbc_mrq_pushSymbol(");
				if (index1 != -1) {
					String str = sb.substring(index1 + 20,
							sb.indexOf(");", index1));
					str = str.replace("'", "");
					final String[] arr = str.split(",");
					realTime = arr[1].trim().replaceAll(",", "");
					pcChange = arr[3].trim().replaceAll(",", "");
					pcChange += "%";
				}

				String high = "";
				int index2 = sbExtn.indexOf("High Today");
				if (index2 != -1) {
					int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
					high = sbExtn.substring(index3 + 16,
							sbExtn.indexOf("<", index3 + 1));
					high = high.trim().replaceAll(",", "");
				}

				String low = "";
				index2 = sbExtn.indexOf("Low Today", index2);
				if (index2 != -1) {
					int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
					low = sbExtn.substring(index3 + 16,
							sbExtn.indexOf("<", index3 + 1));
					low = low.trim().replaceAll(",", "");
				}
				String range = low + " - " + high;

				String avgVol_10Days = "";
				index2 = sbExtn.indexOf("10-Day Avg Volume", index2);
				if (index2 != -1) {
					int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
					avgVol_10Days = sbExtn.substring(index3 + 16,
							sbExtn.indexOf("<", index3 + 1));
					avgVol_10Days = avgVol_10Days.trim().replaceAll(",", "");
				}
				
				String dailyAvgTradeValue = "";
				try{
					dailyAvgTradeValue = "\"$" +Utility.getFormattedInteger( Double.parseDouble(Utility.convertFinancials(avgVol_10Days)) * Double.parseDouble(realTime) )+ "\"";
				}
				catch(Exception e){
				}

				String range52w = "", range52wL_pc = "", range52wH_pc = "";
				Double low52w = null, high52w = null, correction52wk = null;
				index2 = sbExtn.indexOf("52-Week High", index2);
				if (index2 != -1) {
					int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
					try {
						high52w = NF.parse(
								sbExtn.substring(index3 + 16,
										sbExtn.indexOf("<", index3 + 1)))
								.doubleValue();
					} catch (Exception e) {
					}
				}

				index2 = sbExtn.indexOf("52-Week Low", index2);
				if (index2 != -1) {
					int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
					try {
						low52w = NF.parse(
								sbExtn.substring(index3 + 16,
										sbExtn.indexOf("<", index3 + 1)))
								.doubleValue();
					} catch (Exception e) {
					}
				}
				range52w = low52w + ", " + high52w;
				try {
					Double low52w_pc = 0.0;
					Double high52w_pc = 0.0;
					Double realTimePrice = NF.parse(realTime.trim())
							.doubleValue();
					low52w_pc = ((realTimePrice - low52w) / low52w) * 100.0;
					high52w_pc = ((realTimePrice - high52w) / high52w) * 100.0;

					range52wL_pc = Utility.round(low52w_pc) + "%";
					correction52wk = Utility.round(high52w_pc);
					range52wH_pc = correction52wk + "%";
				} catch (Exception e) {
				}

				String time = "";
				int index4 = sb.indexOf("var promoTime = ");
				if (index4 != -1) {
					time = sb.substring(index4 + 16, sb.indexOf(";", index4));
					time = time.replace("\"", "");
					time = time.split(" ")[1];
					time = time.trim().replaceAll(",", "");
				}

				String mktCap = "", pe = "", beta = "", divYield = "";
				int index5 = sbExtn.indexOf("Market Cap");
				if (index5 != -1) {
					mktCap = sbExtn.substring(index5 + 30,
							sbExtn.indexOf("<", index5 + 30));
					mktCap = mktCap.trim().replaceAll(",", "");
				}
				
				String sharesOutstanding = "";
				index5 = sbExtn.indexOf("Shares Outstanding");
				if (index5 != -1) {
					sharesOutstanding = sbExtn.substring(index5 + 38,
							sbExtn.indexOf("<", index5 + 38));
					sharesOutstanding = sharesOutstanding.trim().replaceAll(",", "");
				}

				int index6 = sbExtn.indexOf("Price/Earnings");
				if (index6 != -1) {
					pe = sbExtn.substring(index6 + 34,
							sbExtn.indexOf("<", index6 + 34));
					pe = pe.trim().replaceAll(",", "");
				}

				// <p class="label">Dividend + Yield</p><p class="data">-- <span class='fontWeightNormal'>(0.00%)</span></p>
				index6 = sbExtn.indexOf("Dividend + Yield");
				if (index6 != -1) {
					index6 = sbExtn.indexOf("<span", index6);
					int startIndex = sbExtn.indexOf(">(", index6);
					divYield = sbExtn.substring(startIndex+2, sbExtn.indexOf(")<", startIndex));
					divYield = divYield.trim().replaceAll(",", "");
				}
				
				int index7 = sbExtn.indexOf("Beta");
				if (index7 != -1) {
					beta = sbExtn.substring(index7 + 24,
							sbExtn.indexOf("<", index7 + 24));
					beta = beta.trim().replaceAll(",", "");
				}

				String industry = "";
				index5 = sbExtnCompPro.indexOf("Industry:");
				if (index5 != -1) {
					industry = sbExtnCompPro.substring(index5 + 10,
							sbExtnCompPro.indexOf("</div>", index5 + 10))
							.trim();
					industry = industry.trim().replaceAll(",", "");
				}
				
				String analystConsensus = "";
				index5 = sbExtnCompPro.indexOf("Analyst Consensus");
				if (index5 != -1) {
					index5 = sbExtnCompPro.indexOf( "&avg=", index5 );
					analystConsensus = sbExtnCompPro.substring(index5 + 5, sbExtnCompPro.indexOf("&", index5 + 5)).trim();
				}
				// End: Pull Data

				if( mPositions.containsKey(symbol) ){
					Double realTimePrice = null;
					try{
						realTimePrice = NF.parse(realTime.trim()).doubleValue();
					}
					catch(Exception e){
					}
					
					final List<Entry> entries = mPositions.get(symbol);
					for(final Entry entry : entries){
						Double profitPc = ((realTimePrice - entry.getPrice())/entry.getPrice())*100.0;
						sbufPositions.append(String.format(ROW_FORMAT_POSITIONS, symbol, Utility.getStrDate(entry.getBuyDate()), entry.getPrice().toString(), entry.getQty().toString(), realTimePrice.toString(), Utility.round(profitPc)));
					}
					
					if( !Arrays.asList(CNBC_ETF_LIST).contains(symbol) ){
						continue;
					}
				}

/*
				final List<FilterCheck> filterChecks = getFilterChecksByCorrection(Math.abs(correction52wk));
				FilterCheck matchingFilterCheck = null;
				if (mktCap != null) {

					if (!filterChecks.isEmpty()) {
						boolean otherCondition = false;
						for (final FilterCheck filterCheck : filterChecks) {
							// One of the following conditions must be met.
							// Condition 1
							try {
								Long lMktCap = new Long(Utility.convertFinancials(mktCap));
								if (lMktCap > filterCheck.getMinMarketCapRange().getMin()
										&& lMktCap <= filterCheck.getMinMarketCapRange().getMax()) {
									otherCondition = true;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (!otherCondition) {
								// Condition 2
								try {
									Long lMktCap = new Long(Utility.convertFinancials(mktCap));
									Double currentPrice = new Double(realTime.trim().replaceAll(",", ""));

									if ((lMktCap / currentPrice) > filterCheck.getMinSharesOutstanding()) {
										otherCondition = true;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							if (otherCondition) {
								matchingFilterCheck = filterCheck;
								break;
							}
						}

						if (!otherCondition) {
							rejectReason = RejectReason.NEITHER_CONDITION_MATCHED;
						}
					} else {
						rejectReason = RejectReason.NO_FILTER_CHECK_FOUND;
					}
				} else {
					rejectReason = RejectReason.MARKET_CAP_UNAVAILABLE;
				}
*/
				
				final String bestMatchingFilterCheckWithRating = getBestMatchingFilterCheckWithRating(range52wH_pc, Utility.convertFinancials(mktCap), realTime, dailyAvgTradeValue);
				
				if (bestMatchingFilterCheckWithRating != null) {
					sbuf.append(String.format(
							ROW_FORMAT,
							symbol,
							realTime,
							range,
							pcChange,
							industry,
							range52wL_pc,
							range52wH_pc,
							Utility.convertFinancials(mktCap),
							mktCap,
							Utility.convertFinancials(avgVol_10Days),
							avgVol_10Days,
							dailyAvgTradeValue,
							pe,
							beta,
							divYield,
							analystConsensus,
							sharesOutstanding,
							time,
							low52w.toString(),
							high52w.toString(),
							"\""+bestMatchingFilterCheckWithRating+"\""
						));
				} else {
					sbufRejects.append(String.format(
							ROW_FORMAT,
							symbol,
							realTime,
							range,
							pcChange,
							industry,
							range52wL_pc,
							range52wH_pc,
							Utility.convertFinancials(mktCap),
							mktCap,
							Utility.convertFinancials(avgVol_10Days),
							avgVol_10Days,
							dailyAvgTradeValue,
							pe,
							beta,
							divYield,
							analystConsensus,
							sharesOutstanding,
							time,
							low52w.toString(),
							high52w.toString(),
							""));
				}
			} catch (Exception e) {
				//e.printStackTrace();

				sbuf.append("Exception in getting data for " + symbol).append(
						"\n");
				sbufRejects.append("Exception in getting data for " + symbol)
						.append("\n");
			}
		}
	}
	
	private static String getBestMatchingFilterCheckWithRating(String range52wH_pc, String mktCap, String realTime, String dailyAvgTradeValue){
		Double d52wH_pc = Math.abs(new Double( range52wH_pc.replace("%", "") ));
		Double dMktCap = new Double( mktCap );
		Double dRealTime = new Double( realTime.trim().replaceAll(",", "") );
		Double dDailyAvgTradeValue = new Double( dailyAvgTradeValue.trim().replaceAll(",", "").replaceAll("\\$", "").replaceAll("\"", "") );
		Double dMinSharesOutstanding = dMktCap / dRealTime;
		
		int iBestMatch = 0;
		FilterCheck bestFilterCheck = null;
		
		for (final FilterCheck filterCheck : listFilterCheck) {
			int totalMatches = 0;
			// Check 1
			if( d52wH_pc > filterCheck.getCorrectionRange52Wk().getMin() && d52wH_pc <= filterCheck.getCorrectionRange52Wk().getMax() ){
				totalMatches++;
			}
			
			// Check 2
			if( dMktCap >= filterCheck.getMinMarketCapRange().getMin() && dMktCap <= filterCheck.getMinMarketCapRange().getMax() ){
				totalMatches++;
			}
			
			// Check 3
			if( dMinSharesOutstanding >= filterCheck.getMinSharesOutstanding() ){
				totalMatches++;
			}
			
			// Check 4
			if( dDailyAvgTradeValue >= filterCheck.getDailyAvgTradeValue() ){
				totalMatches++;
			}
			
			if( totalMatches > iBestMatch ){
				iBestMatch = totalMatches;
				bestFilterCheck = filterCheck;
			}
		}
		
		if( bestFilterCheck == null ){
			return null;
		}else{
			return iBestMatch +" --> " +bestFilterCheck;
		}
	}

//	private static List<FilterCheck> getFilterChecksByCorrection(
//			Double correction52Wk) {
//		final List<FilterCheck> filterChecks = new ArrayList<FilterCheck>();
//		for (FilterCheck filterCheck : listFilterCheck) {
//			if (correction52Wk > filterCheck.getCorrectionRange52Wk().getMin()
//					&& correction52Wk <= filterCheck.getCorrectionRange52Wk()
//							.getMax()) {
//				filterChecks.add(filterCheck);
//			}
//		}
//		return filterChecks;
//	}

	class Entry{
		private String symbol;
		private Double price;
		private Integer qty;
		private Date buyDate;
		
		public Entry(String symbol, Double price, Integer qty, Date buyDate) {
			super();
			this.symbol = symbol;
			this.price = price;
			this.qty = qty;
			this.buyDate = buyDate;
		}

		public String getSymbol() {
			return symbol;
		}

		public Double getPrice() {
			return price;
		}

		public Integer getQty() {
			return qty;
		}

		public Date getBuyDate() {
			return buyDate;
		}

		@Override
		public String toString() {
			return "Entry [symbol=" + symbol + ", price=" + price + ", qty="
					+ qty + ", buyDate=" + buyDate + "]";
		}

	}
	
	class FilterCheck {
		private String filterName;
		private Range correctionRange52Wk;
		private Range minMarketCapRange;
		private Long minSharesOutstanding;
		private Long dailyAvgTradeValue;

		public FilterCheck(String filterName, Range correctionRange52Wk,
				Range minMarketCapRange, Long minSharesOutstanding, Long dailyAvgTradeValue) {
			this.filterName = filterName;
			this.correctionRange52Wk = correctionRange52Wk;
			this.minMarketCapRange = minMarketCapRange;
			this.minSharesOutstanding = minSharesOutstanding;
			this.dailyAvgTradeValue = dailyAvgTradeValue;
		}

		public String getFilterName() {
			return filterName;
		}

		public Range getCorrectionRange52Wk() {
			return correctionRange52Wk;
		}

		public Range getMinMarketCapRange() {
			return minMarketCapRange;
		}

		public Long getMinSharesOutstanding() {
			return minSharesOutstanding;
		}

		public Long getDailyAvgTradeValue() {
			return dailyAvgTradeValue;
		}

		@Override
		public String toString() {
			return "FilterCheck [name=" + filterName
					+ ", correctionRange52Wk=" + correctionRange52Wk
					+ ", marketCapRange=" + minMarketCapRange
					+ ", minSharesOutstanding=" + Utility.getFormattedNumber(minSharesOutstanding)
					+ ", dailyAvgTradeValue=" +Utility.getFormattedInteger(dailyAvgTradeValue)+ "]";
		}
	}

	class Range {
		private Long min;
		private Long max;

		public Range(Long min, Long max) {
			this.min = min;
			this.max = max;
		}

		public Long getMin() {
			return min;
		}

		public Long getMax() {
			return max;
		}

		@Override
		public String toString() {
			return "Range(" + Utility.getFormattedNumber(min) +" - "+ Utility.getFormattedNumber(max) + ")";
		}
	}
}
