package com.stocks.standalone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stocks.util.Utility;

public class StockAlert {

	final static List<ShortPosition> shortPositions = new ArrayList<ShortPosition>();
	
	static{
		loadConfigurationFromXml();
	}

	private static void loadConfigurationFromXml() {
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse( StockAlert.class.getResourceAsStream("/StockAlert.xml") );
			doc.getDocumentElement().normalize();

			NodeList shortPositionList = doc.getElementsByTagName("short_position");
			for (int i=0; i < shortPositionList.getLength(); i++) {
				final Node nNode = shortPositionList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element eElement = (Element) nNode;
					final String symbol = getTagValue("symbol", eElement);
					final Date date = Utility.getDateFor( getTagValue("date", eElement), "MM/dd/yyyy" );
					final Double price = Double.parseDouble(getTagValue("price", eElement));
					final Long volume = Long.parseLong(getTagValue("volume", eElement));
					final String description = getTagValue("description", eElement);
					
					shortPositions.add( new StockAlert().new ShortPosition(symbol, date, price, volume, description) );
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
	
	public static void main(String... args) throws Exception{
		final String ROW_FORMAT = "%-10s %-10s %-25s %-12s %-8s %-20s %-15s %-23s %s";
		
		System.out.println( String.format(ROW_FORMAT, "Symbol", "realTime", "range", "% Change", "time", "Alert Age", "Target Price", "Reqd drop for Trigger", "Description") );
		for(final ShortPosition shortPosition : shortPositions){
			try{
				final Quote quote = CallerQuote.processCnbc(shortPosition.getSymbol());
				
				long days = Utility.getDaysDiffBetween(shortPosition.getDate(), new Date());
				String alertAge = days +" Days";
				if( days > 30 ){
					alertAge += " (EXPIRED)";
				}
				
				Double minus10pc = Utility.round( shortPosition.getPrice() - (shortPosition.getPrice() * (10.0/100.0)) );
				Double realTime = Double.parseDouble(quote.getRealTime());
				Double dayMin = null;
				String sDayMin = quote.getRangeToday().substring(0, quote.getRangeToday().indexOf("-"));
				try{
					dayMin = Double.parseDouble( sDayMin.trim() );
				}
				catch(Exception e){
					dayMin = realTime;
				}
				
				final Double reqdDrop = Utility.round( ((minus10pc - dayMin) / dayMin)*100.0 );
				String sReqdDrop = null;
				if( reqdDrop < 0 ){
					sReqdDrop = reqdDrop+"%";
				}else{
					sReqdDrop = "-- TRIGGERRED--";
				}
				System.out.println( String.format(ROW_FORMAT, quote.getSymbol(), quote.getRealTime(), quote.getRangeToday(), quote.getPcChange(), quote.getTime(), alertAge, minus10pc, sReqdDrop, shortPosition.getDescription()) );
			}
			catch(Exception e){
				System.out.println( "Exception in getting data for " +shortPosition.getSymbol() );
			}
		}
		System.out.println( "Done." );
	}
	
	class ShortPosition{
		String symbol;
		Date date;
		Double price;
		Long volume;
		String description;
		public ShortPosition(String symbol, Date date, Double price,
				Long volume, String description) {
			super();
			this.symbol = symbol;
			this.date = date;
			this.price = price;
			this.volume = volume;
			this.description = description;
		}
		
		public String getSymbol() {
			return symbol;
		}
		public Date getDate() {
			return date;
		}
		public Double getPrice() {
			return price;
		}
		public Long getVolume() {
			return volume;
		}
		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return "ShortPosition [symbol=" + symbol + ", date=" + date
					+ ", price=" + price + ", volume=" + volume
					+ ", description=" + description + "]";
		}
		
		
	}
}
