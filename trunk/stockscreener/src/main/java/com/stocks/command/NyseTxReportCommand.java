package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;
import com.stocks.model.NysePK;
import com.stocks.model.NyseTx;
import com.stocks.model.Report;

public class NyseTxReportCommand extends AbstractCommand {

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseTxReportCommand..." );
			processNyseTransactions();
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyseTransactions() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Nyse Tx Report - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );
		List<NyseTx> nyseTxList = getStockService().getAllNyseTransactions();
		List<Double> cClose = new ArrayList<Double>();

		// Group Transactions by Symbol and add to Map
		Map<String, List<NyseTx>> mSymbolNyseTx = new HashMap<String, List<NyseTx>>();
		for( final NyseTx nyseTx : nyseTxList ){
			List<NyseTx> nyseTransactions = mSymbolNyseTx.get( nyseTx.getSymbol() );
			if( nyseTransactions == null ){
				nyseTransactions = new ArrayList<NyseTx>();
			}
			nyseTransactions.add(nyseTx);
			mSymbolNyseTx.put(nyseTx.getSymbol(), nyseTransactions);
		}
		// End

		double ctr = 0.0;
		for( final List<NyseTx> nyseTransactions : mSymbolNyseTx.values() ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(nyseTransactions.get(0).getSymbol(), getStartDate(), getEndDate());
			for(final Nyse nyse : nyseList){
				cClose.add( nyse.getClose() );
			}
			
			// In case there is no data for this symbol e.g. symbol is a NASDAQ symbol or misentered.
			if( cClose.size() == 0 ){
				continue;
			}
			
			StringBuffer sbChm = new StringBuffer();
			StringBuffer sbChmTgt = new StringBuffer();
			Double min = Collections.min(cClose);
			Double max = Collections.max(cClose);
			Double diff = max - min;
			for( final NyseTx nyseTx : nyseTransactions ){
				Nyse nyse = new Nyse();
				NysePK nysePK = new NysePK();
				nysePK.setSymbol( nyseTx.getSymbol() );
				nysePK.setTradeDate( nyseTx.getTxDate() );
				nyse.setNysePK(nysePK);
				
				// Attempt to match dates 5 times as there can be long weekends.
				for(int i=0; i<5; i++){
					int index = nyseList.indexOf( nyse );
					if( index != -1 ){
						String str = GOOGLE_CHART_CHM_PARAM_VALUE.replaceAll("000000", (nyseTx.getTrxType().equals("B") ? "0000FF" : "FFA500" ) );
						str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(index));
						str = str.replace("~RECOMMENDED_BUY_PRICE",  nyseTx.getPrice().toString());
						sbChm.append( str ).append("|");
						break;
					}else{
						Date tradeDate = nyse.getNysePK().getTradeDate();
						tradeDate.setDate( tradeDate.getDate()-1 );
						nyse.getNysePK().setTradeDate( tradeDate );
					}
				}
				
				/*
				Steps for getting tgt value between zero and 1.
				1	Find Min		57.9
				2	Find Max		79.27
				3	Pull Max - Min		21.37
				4	Take a Tgt		75.32
				5	Pull Tgt - Min		17.42
				6	Divide 5) by 3)		0.815161441
				7	Convert to 1 if 6) is greater than one (when Tgt is above max).
				*/
				if( nyseTx.getTrxType().equals("B") ){
					Double tgt = nyseTx.getPrice() + (nyseTx.getPrice() * 0.10);
					Double tgtLine = (tgt - min) / diff;
					if( tgtLine > 1.0 ){
						tgtLine = 1.0;
					}
					String chmHLine = GOOGLE_CHART_CHM_SLTP;
					chmHLine = chmHLine.replace("~VAL_BETWEEN_ZERO_AND_ONE", tgtLine.toString().replaceAll("FF0000", "0000FF"));
					sbChmTgt.append( chmHLine );
				}
			}

			sb.append( String.format("%s%n", nyseTransactions.toString() ) );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose.toArray(new Double[]{})).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose.toArray(new Double[]{}));
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			
			// sbChm can be blank if insufficient data is available for the stock.
			if( sbChm.length() > 0 ){
				url += sbChm.substring(0, sbChm.length()-1);
			}
			if( sbChmTgt.length() > 0 ){
				url += sbChmTgt.toString();
			}

			sb.append( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +nyseTransactions.get(0).getSymbol()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );

			ctr += nyseTransactions.size();
			getPercentCompleteReporter().setPercentComplete( (ctr / nyseTxList.size()) * 100.00 );
		}

		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseTxReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
