package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Nyse;
import com.stocks.model.Report;

public class NyseTrailingSetCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
	private static final double CORRECTION_PERCENT = -40.0;
//	private static final double CORRECTION_MONTHS = 3d;
	private static int TOTAL_BUY = 0;
	private static int TOTAL_SELL = 0;

	public static void main(String args[]) throws Exception{
//		FileInputStream fis = new FileInputStream("c:/Temp/chart.png");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		int bytesRead = 0;
//		byte[] b = new byte[1024];
//		while( (bytesRead = fis.read(b)) != -1 ){
//			baos.write(b, 0, bytesRead);
//		}
//		fis.close();
//		
//		String str = new String( new Base64().encodeBase64(baos.toByteArray()) );
//		System.out.println( str );
//		
//		if(true) return;
		
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		NyseTrailingSetCommand nyseTrailingSetCommand = (NyseTrailingSetCommand) context.getBean("nyseTrailingSetCommand");
		
		Calendar cStartDate = Calendar.getInstance();
		cStartDate.set(Calendar.YEAR, 2009);
		cStartDate.set(Calendar.MONTH, Calendar.JANUARY);
		cStartDate.set(Calendar.DATE, 1);
		
		nyseTrailingSetCommand.setStartDate(cStartDate.getTime());
		nyseTrailingSetCommand.setEndDate(new Date());
		nyseTrailingSetCommand.processNyse();
		
		System.out.println( "Done." );
		System.exit(0);
	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));

		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseTrailingSetCommand... (Ensure Start Date is 01/01/2009.)" );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Trailing Set Report (%s - %s percent Up from Bottom) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		//List<String> symbols = Arrays.asList(new String[]{"PCX"}); //getStockService().getAllSymbols();
		List<String> symbols = getStockService().getAllSymbols();
		
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, getStartDate(), getEndDate());
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				checkQualification(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "Total Buy: " +TOTAL_BUY ).append("\n");
		sb.append( "Total Sell: " +TOTAL_SELL ).append("\n");
		sb.append( "Total Pending: " +(TOTAL_BUY-TOTAL_SELL) ).append("\n");
		
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseTrailingSetCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private void checkQualification(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		final int TRAILING_LIST_SIZE = 52 * 5; // 52wk
		
		List<Double> list = Arrays.asList(cClose);
		List<BuySellInfo> buySellInfoList = new ArrayList<BuySellInfo>();
		Double low52wk_AtLastBuy = Double.MAX_VALUE;
		
		for( int subjectIndex=TRAILING_LIST_SIZE; subjectIndex<list.size(); subjectIndex++ ){
			List<Double> trailingList = list.subList(subjectIndex - TRAILING_LIST_SIZE, subjectIndex);
			final Double low52wk  = Collections.min(trailingList); // 52wkL
			final Double high52wk = Collections.max(trailingList); // 52wkH
			
			Double priceAtSubjectIndex = list.get( subjectIndex );
			final Double low52w_pc  = ((priceAtSubjectIndex - low52wk)/low52wk)*100.0; // 4.6
			final Double high52w_pc = ((priceAtSubjectIndex - high52wk)/high52wk)*100.0; // -85
			
			if( high52w_pc <= CORRECTION_PERCENT && (low52w_pc >= FROM_PERCENT && low52w_pc <= TO_PERCENT ) && (low52wk < low52wk_AtLastBuy) ){
				buySellInfoList.add( new BuySellInfo(subjectIndex, null) );
				low52wk_AtLastBuy = low52wk; // This is used as a check not to re-enter, until a low below the previous 52WkLow is hit.
				
				if( subjectIndex == (list.size()-1) ){
					System.out.println( String.format("\tConsider [%s] for buying today.", scCode) );
				}
			}
			if( !buySellInfoList.isEmpty() ){
				for( BuySellInfo bsi : buySellInfoList ){
					if( bsi.getSellIndex() == null ){ // If not sold yet
						Double priceAtBuyIndex = list.get( bsi.getBuyIndex() );
						Double targetPrice = priceAtBuyIndex + (priceAtBuyIndex * 0.10); // 10%
						
						if( priceAtSubjectIndex >= targetPrice ){
							bsi.setSellIndex( subjectIndex );
						}
					}
				}
			}
		}
		
		if( !buySellInfoList.isEmpty() ){
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());

			StringBuffer buySellInfoBuffer = new StringBuffer();
			
			int BUY = 0, SELL=0;
			for( BuySellInfo bsi : buySellInfoList ){
				BUY++;
				buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("~RECOMMENDED_BUY_PRICE", "Buy@" +cCloseList.get(bsi.getBuyIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getBuyIndex() ) ).append("|") ;
				if( bsi.getSellIndex() != null ){
					SELL++;
					buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("000000", "348017" ).replace("~RECOMMENDED_BUY_PRICE", "Sell@" +cCloseList.get(bsi.getSellIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getSellIndex() ) ).append("|") ;
				}
			}
			
			TOTAL_BUY+=BUY;
			TOTAL_SELL+=SELL;
			sb.append( String.format("Symbol: %s, Bought: %d, Sold: %d, Success pc: %f%n", scCode, BUY, SELL, ((((double)SELL-(double)BUY)/(double)BUY)*100.0)+100.0 ) );
			System.out.println( scCode+ "\t" +BUY+ "\t" +SELL );
			
			if( buySellInfoBuffer.length() > 0 ){
				url += buySellInfoBuffer.substring(0, buySellInfoBuffer.length()-1);
			}
			
			//sb.append( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );

			String postString = toHttpPostString(url, scCode.toString());
			sb.append( postString ).append("\n");
			//System.out.println( postString );
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}

class BuySellInfo{
	private Integer buyIndex, sellIndex;
	
	public BuySellInfo(Integer buyIndex, Integer sellIndex) {
		super();
		this.buyIndex = buyIndex;
		this.sellIndex = sellIndex;
	}

	public Integer getBuyIndex() {
		return buyIndex;
	}

	public void setBuyIndex(Integer buyIndex) {
		this.buyIndex = buyIndex;
	}

	public Integer getSellIndex() {
		return sellIndex;
	}

	public void setSellIndex(Integer sellIndex) {
		this.sellIndex = sellIndex;
	}
	
	@Override
	public String toString() {
		return "BuyIndex: " +this.getBuyIndex()+ ", SellIndex: " +this.getSellIndex();
	}
}
