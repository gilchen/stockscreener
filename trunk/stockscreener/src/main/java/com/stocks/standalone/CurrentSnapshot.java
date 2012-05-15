package com.stocks.standalone;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stocks.util.Utility;

public class CurrentSnapshot {
	public enum RejectReason {
		NONE, MARKET_CAP_UNAVAILABLE, CORRECTION_52_WK_UNAVAILABLE, NO_FILTER_CHECK_FOUND, OUTSIDE_ALLOWED_CORRECTION_RANGE, NEITHER_CONDITION_MATCHED
	};

	// final static String[] CNBC_ETF_LIST = {".DJIA", ".SPX", "COMP", ".FTSE",
	// ".FCHI", ".GDAXI", "-", "AEM", "AGQ", "BAL", "FTR", "NFLX", "RIMM", "S",
	// "SDOW", "UNG", "TBT", "AMZN", "UPL", "ACI", "DMND", "-", "ERX", "UCO",
	// "CLCV1", "-", "UGL", "INDL", "NLR", "NFX" }; // "BAL", "LIT", "UCO",
	// "NLR", "TMF", "RIG", "CREE", "ECA",
	static String[] CNBC_ETF_LIST = null;

	final static String KEY_FILTER_CHECKS = "FILTER_CHECKS";
	final static List<FilterCheck> listFilterCheck = new ArrayList<FilterCheck>();

	final static String CNBC_URL = "http://data.cnbc.com/quotes/";
	final static String CNBC_URL_EXTN = "http://apps.cnbc.com/company/quote/index.asp?symbol=";
	final static String CNBC_URL_EXTN_COMPANY_PROFILE = "http://apps.cnbc.com/view.asp?country=US&uid=stocks/summary&symbol=";

	final static String ROW_FORMAT = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n";

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

					int indexOfDash = sRange.indexOf("-");
					Range correctionRange52wk = new CurrentSnapshot().new Range(
							new Long(sRange.substring(0, indexOfDash)), 
							new Long(sRange.substring(indexOfDash + 1)));

					indexOfDash = sMinMarketCap.indexOf("-");
					Range minMarketCapRange = new CurrentSnapshot().new Range(
							new Long(Utility.convertFinancials(sMinMarketCap.substring(0, indexOfDash))), 
							new Long(Utility.convertFinancials(sMinMarketCap.substring(indexOfDash + 1))));
					
					Long minSharesOutstanding = new Long(Utility.convertFinancials(sMinSharesOutstanding));

					FilterCheck filterCheck = new CurrentSnapshot().new FilterCheck(name, correctionRange52wk, minMarketCapRange, minSharesOutstanding);
					listFilterCheck.add(filterCheck);
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

		String header = String.format(ROW_FORMAT, 
				"Symbol", 
				"range52wL_pc", 
				"range52wH_pc", 
				"Mkt Cap (Expanded)", 
				"10-Day Avg Vol (Expanded)", 
				"Filter Name",
				"realTime",
				"range", 
				"% Change", 
				"time", 
				"range52wL", 
				"range52wH",
				"Mkt Cap",
				"10-Day Avg Vol",
				"P/E", 
				"Beta", 
				"Industry",
				"Reject Reason"
			);
		sbuf.append(header);
		sbufRejects.append(header);
		processCnbc(sbuf, sbufRejects);
		// System.out.println( "*range52w_pc: (Closely Above +"
		// +RECOMMENDATION_52W_APPRECIATION_PC+ "% & Anything Below "
		// +RECOMMENDATION_52W_CORRECTION_PC+ "% is good)" );
		Utility.saveContent("rpt.csv", sbuf.toString());
		Utility.saveContent("rptRejects.csv", sbufRejects.toString());

		System.out.println("Done.");
	}

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processCnbc(final StringBuilder sbuf,
			final StringBuilder sbufRejects) throws Exception {
		Set<String> set = new HashSet(Arrays.asList(CNBC_ETF_LIST));
		for (String symbol : set) {
			symbol = symbol.trim();
			System.out.println("Pulling [" + symbol + "]");

			RejectReason rejectReason = RejectReason.NONE;

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

				String mktCap = "", pe = "", beta = "";
				int index5 = sbExtn.indexOf("Market Cap");
				if (index5 != -1) {
					mktCap = sbExtn.substring(index5 + 30,
							sbExtn.indexOf("<", index5 + 30));
					mktCap = mktCap.trim().replaceAll(",", "");
				}

				int index6 = sbExtn.indexOf("Price/Earnings");
				if (index6 != -1) {
					pe = sbExtn.substring(index6 + 34,
							sbExtn.indexOf("<", index6 + 34));
					pe = pe.trim().replaceAll(",", "");
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
				// End: Pull Data

				final List<FilterCheck> filterChecks = getFilterChecksByCorrection(Math
						.abs(correction52wk));
				FilterCheck matchingFilterCheck = null;
				if (mktCap != null) {

					if (!filterChecks.isEmpty()) {
						boolean otherCondition = false;
						for (final FilterCheck filterCheck : filterChecks) {
							// One of the following conditions must be met.
							// Condition 1
							try {
								Long lMktCap = new Long(
										Utility.convertFinancials(mktCap));
								if (lMktCap > filterCheck
										.getMinMarketCapRange().getMin()
										&& lMktCap <= filterCheck
												.getMinMarketCapRange()
												.getMax()) {
									otherCondition = true;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (!otherCondition) {
								// Condition 2
								try {
									Long lMktCap = new Long(
											Utility.convertFinancials(mktCap));
									Double currentPrice = new Double(realTime
											.trim().replaceAll(",", ""));

									if ((lMktCap / currentPrice) > filterCheck
											.getMinSharesOutstanding()) {
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

				if (rejectReason == RejectReason.NONE) {
					sbuf.append(String.format(
							ROW_FORMAT,
							symbol,
							range52wL_pc,
							range52wH_pc,
							Utility.convertFinancials(mktCap),
							Utility.convertFinancials(avgVol_10Days),
							matchingFilterCheck.getFilterName(),
							realTime,
							range,
							pcChange,
							time,
							low52w.toString(),
							high52w.toString(),
							mktCap,
							avgVol_10Days,
							pe,
							beta,
							industry,
							"\""
									+ (rejectReason + "-->"
											+ matchingFilterCheck.toString() + "\"")));
				} else {
					sbufRejects.append(String.format(
							ROW_FORMAT, 
							symbol,
							range52wL_pc, 
							range52wH_pc,
							Utility.convertFinancials(mktCap),
							Utility.convertFinancials(avgVol_10Days), 
							"",
							realTime, 
							range, 
							pcChange, 
							time, 
							low52w.toString(),
							high52w.toString(), 
							mktCap, 
							avgVol_10Days,
							pe, 
							beta,
							industry, 
							rejectReason));
				}
			} catch (Exception e) {
				// e.printStackTrace();

				sbuf.append("Exception in getting data for " + symbol).append(
						"\n");
				sbufRejects.append("Exception in getting data for " + symbol)
						.append("\n");
			}
		}
	}

	private static List<FilterCheck> getFilterChecksByCorrection(
			Double correction52Wk) {
		final List<FilterCheck> filterChecks = new ArrayList<FilterCheck>();
		for (FilterCheck filterCheck : listFilterCheck) {
			if (correction52Wk > filterCheck.getCorrectionRange52Wk().getMin()
					&& correction52Wk <= filterCheck.getCorrectionRange52Wk()
							.getMax()) {
				filterChecks.add(filterCheck);
			}
		}
		return filterChecks;
	}

	class FilterCheck {
		private String filterName;
		private Range correctionRange52Wk;
		private Range minMarketCapRange;
		private Long minSharesOutstanding;

		public FilterCheck(String filterName, Range correctionRange52Wk,
				Range minMarketCapRange, Long minSharesOutstanding) {
			this.filterName = filterName;
			this.correctionRange52Wk = correctionRange52Wk;
			this.minMarketCapRange = minMarketCapRange;
			this.minSharesOutstanding = minSharesOutstanding;
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

		@Override
		public String toString() {
			return "FilterCheck [name=" + filterName
					+ ", correctionRange52Wk=" + correctionRange52Wk
					+ ", marketCapRange=" + minMarketCapRange
					+ ", minSharesOutstanding=" + Utility.getFormattedNumber(minSharesOutstanding) + "]";
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
