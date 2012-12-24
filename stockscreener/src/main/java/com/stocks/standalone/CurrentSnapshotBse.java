package com.stocks.standalone;

import java.text.NumberFormat;

import java.util.ArrayList;
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

public class CurrentSnapshotBse {
	final static Map<String, List<Entry>> mPositions = new HashMap<String, List<Entry>>();

	final static String BSE_URL    = "http://www.bseindia.com/bseplus/StockReach/AdvStockReach.aspx?scripcode=~SCRIPCODE&section=tab1&IsPF=undefined&random=0.22032093349844217";
	final static String ROW_FORMAT_POSITIONS = "%s,%s,%s,%s,%s,%s,%s%n";

	final static NumberFormat NF = NumberFormat.getInstance();

	private static void loadConfigurationFromXml() {
		mPositions.clear();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse( CurrentSnapshotBse.class.getResourceAsStream("/CurrentSnapshotBse.xml") );
			doc.getDocumentElement().normalize();

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
						
						final Entry entry = new CurrentSnapshotBse().new Entry(symbol, new Double(price), new Integer(qty), Utility.getDate(buyDate));
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
		
		final StringBuilder sbufPositions = new StringBuilder();

		final String headerPositions = String.format(ROW_FORMAT_POSITIONS, "Name", "Symbol", "Buy Date", "Price", "Qty", "Real-Time", "Profit/Loss %");
		
		sbufPositions.append( headerPositions );

		processBse(sbufPositions);
		
		Utility.saveContent("rptPositionsBse.csv", sbufPositions.toString());
		System.out.println("Done.");
	}

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processBse(final StringBuilder sbufPositions) throws Exception{
		final String SECTION = "#SECTION#";
		final String HASH = "#@#";
		
		final Set<String> set = new HashSet<String>();
		set.addAll( mPositions.keySet() );
		
		for( String symbol : set ){
			System.out.println( "Pulling " +symbol );
			final StringBuffer sb = new StringBuffer( Utility.getContent( BSE_URL.replace("~SCRIPCODE", symbol) ) );
			
			int nameIndex = sb.indexOf( HASH );
			final String symbolName = sb.substring( nameIndex + HASH.length(), sb.indexOf( HASH, nameIndex+HASH.length() ) );

			int indexOfSection = -1;
			indexOfSection = sb.indexOf(SECTION, indexOfSection+1);
			
			int indexOfHash = indexOfSection;
			for(int i=0; i<6; i++){
				indexOfHash = sb.indexOf(HASH, indexOfHash+1);
			}
			String lastClose = sb.substring(indexOfHash+HASH.length(), sb.indexOf(HASH, indexOfHash+1));

			if( mPositions.containsKey(symbol) ){
				Double realTimePrice = null;
				try{
					realTimePrice = NF.parse(lastClose.trim()).doubleValue();
				}
				catch(Exception e){
				}
				
				final List<Entry> entries = mPositions.get(symbol);
				for(final Entry entry : entries){
					Double profitPc = ((realTimePrice - entry.getPrice())/entry.getPrice())*100.0;
					sbufPositions.append(String.format(ROW_FORMAT_POSITIONS, symbolName, symbol, Utility.getStrDate(entry.getBuyDate()), entry.getPrice().toString(), entry.getQty().toString(), realTimePrice.toString(), Utility.round(profitPc)));
				}
			}
			
		}
	}

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

}
