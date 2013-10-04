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

	final static List<Alert> alerts = new ArrayList<Alert>();
	
	static{
		loadConfigurationFromXml();
	}

	private static void loadConfigurationFromXml() {
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse( StockAlert.class.getResourceAsStream("/alert.xml") );
			doc.getDocumentElement().normalize();

			NodeList alertList = doc.getElementsByTagName("Alert");
			for (int i=0; i < alertList.getLength(); i++) {
				final Node nNode = alertList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					final Element eElement = (Element) nNode;
					final String symbol = getTagValue("Symbol", eElement);
					final Date positionDate = Utility.getDateFor( getTagValue("PositionDate", eElement), "MM/dd/yyyy" );
					final Double positionPrice = Double.parseDouble(getTagValue("PositionPrice", eElement));
					final Double triggerChangePercent = Double.parseDouble(getTagValue("TriggerChangePercent", eElement));
					final String description = getTagValue("Description", eElement);
					
					alerts.add( new StockAlert().new Alert(symbol, positionDate, positionPrice, triggerChangePercent, description) );
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
		final String ROW_FORMAT_CSV = "%s,%s,%s,%s,%s,%s,%s,%s,%s";
		
		final StringBuilder sbCsv = new StringBuilder();
		
		System.out.println( String.format(ROW_FORMAT, "Symbol", "realTime", "range", "% Change", "time", "Alert Age", "Target Price", "Reqd change for Trigger", "Description") );
		
		sbCsv.append( String.format(ROW_FORMAT_CSV, "Symbol", "realTime", "range", "% Change", "time", "Alert Age", "Target Price", "Reqd change for Trigger", "Description") ).append( "\n" );
		
		for(final Alert alert : alerts){
			try{
				final Quote quote = CallerQuote.processCnbc(alert.getSymbol());
				
				long days = Utility.getDaysDiffBetween(alert.getPositionDate(), new Date());
				String alertAge = days +" Days";
				
				Double triggerPrice = Utility.round( alert.getPositionPrice() + (alert.getPositionPrice() * (alert.getTriggerChangePercent()/100.0)) );
				Double realTime = Double.parseDouble(quote.getRealTime());
				Double dayMin = realTime;
				Double dayMax = realTime;
				String sDayMin = quote.getRangeToday().substring(0, quote.getRangeToday().indexOf("-"));
				String sDayMax = quote.getRangeToday().substring(quote.getRangeToday().indexOf("-")+1 );
				try{
					dayMin = Double.parseDouble( sDayMin.trim() );
				}
				catch(Exception e){
				}
				try{
					dayMax = Double.parseDouble( sDayMax.trim() );
				}
				catch(Exception e){
				}
				
				
				Double reqdChange = null;
				if( alert.getTriggerChangePercent() > 0 ){
					reqdChange = Utility.round( ((triggerPrice - dayMax) / dayMax)*100.0 );
				}else{
					reqdChange = Utility.round( ((triggerPrice - dayMin) / dayMin)*100.0 );
				}
				
				String sReqdChange = null;
				if( alert.getTriggerChangePercent() > 0 && reqdChange > 0 ){
					sReqdChange = reqdChange+"%";
				}else if( alert.getTriggerChangePercent() < 0 && reqdChange < 0){
					sReqdChange = reqdChange+"%";
				}else{
					sReqdChange = "** TRIGGERRED **";
				}
				System.out.println( String.format(ROW_FORMAT, quote.getSymbol(), quote.getRealTime(), quote.getRangeToday(), quote.getPcChange(), quote.getTime(), alertAge, triggerPrice, sReqdChange, alert.getDescription()) );
				sbCsv.append( String.format(ROW_FORMAT_CSV, quote.getSymbol(), quote.getRealTime(), quote.getRangeToday(), quote.getPcChange(), quote.getTime(), alertAge, triggerPrice, sReqdChange, "\"" +alert.getDescription()+ "\"") ).append("\n");
			}
			catch(Exception e){
				System.out.println( "Exception in getting data for " +alert.getSymbol() );
				sbCsv.append( "Exception in getting data for " +alert.getSymbol() ).append( "\n" );
			}
		}
		Utility.saveContent("alert.csv", sbCsv.toString());
		System.out.println( "Done." );
	}
	
	class Alert{
		String symbol;
		Date positionDate;
		Double positionPrice;
		Double triggerChangePercent;
		String description;
		public Alert(String symbol, Date positionDate, Double positionPrice,
				Double triggerChangePercent, String description) {
			super();
			this.symbol = symbol;
			this.positionDate = positionDate;
			this.positionPrice = positionPrice;
			this.triggerChangePercent = triggerChangePercent;
			this.description = description;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public Date getPositionDate() {
			return positionDate;
		}
		public void setPositionDate(Date positionDate) {
			this.positionDate = positionDate;
		}
		public Double getPositionPrice() {
			return positionPrice;
		}
		public void setPositionPrice(Double positionPrice) {
			this.positionPrice = positionPrice;
		}
		public Double getTriggerChangePercent() {
			return triggerChangePercent;
		}
		public void setTriggerChangePercent(Double triggerChangePercent) {
			this.triggerChangePercent = triggerChangePercent;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		@Override
		public String toString() {
			return "Alert [symbol=" + symbol + ", positionDate=" + positionDate
					+ ", positionPrice=" + positionPrice
					+ ", triggerChangePercent=" + triggerChangePercent
					+ ", description=" + description + "]";
		}
		
		
	}
}
