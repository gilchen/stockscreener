package com.stocks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class Statistics {
	static final String RPT_FILE = "C:/Classroom/JSF/int_ref/workspace/trunk/jasper_reports/D_Analysis_Percent_Move_GroupOnly_Main.jrxml";
	static final String EXPORT_FOLDER = "C:/Temp/stk/Analysis/reports/";
	static final StringBuffer sbConsolidatedReport = new StringBuffer();
	static Integer PERCENT = 50;
	static Map<Integer, ArrayList<KeyValDetails>> wkDayIteration = new HashMap<Integer, ArrayList<KeyValDetails>>();
	
	static Comparator comparator = new Comparator(){
		public int compare(Object o1, Object o2) {
			KeyValDetails kvd1 = (KeyValDetails) o1;
			KeyValDetails kvd2 = (KeyValDetails) o2;
			
			int ret = 0;
			if( kvd1.value < kvd2.value ){
				ret = -1;
			}else if( kvd1.value == kvd2.value ){
				ret = 0;
			}else if( kvd1.value > kvd2.value ){
				ret = 1;
			}
			return ret;
		}
	};
	
	public static void main(String args[]) throws Exception{
		Statistics stats = new Statistics();
		Connection con = stats.getConnection();

		//
		Calendar cLastFriday = Calendar.getInstance();
		cLastFriday.set( Calendar.DATE, 23 );
		cLastFriday.set( Calendar.MONTH, Calendar.JULY );
		cLastFriday.set( Calendar.YEAR, 2010 );
		//
		
		Integer[] arrPercent = new Integer[]{50, 60, 70, 80, 90, 100};
		for( int iteration=19; iteration<=21; iteration++ ){
			wkDayIteration.clear();
			wkDayIteration.put(Calendar.MONDAY, new ArrayList<KeyValDetails>());
			wkDayIteration.put(Calendar.TUESDAY, new ArrayList<KeyValDetails>());
			wkDayIteration.put(Calendar.WEDNESDAY, new ArrayList<KeyValDetails>());
			wkDayIteration.put(Calendar.THURSDAY, new ArrayList<KeyValDetails>());
			wkDayIteration.put(Calendar.FRIDAY, new ArrayList<KeyValDetails>());
			
			for( Integer pc : arrPercent ){
				PERCENT = pc;
				int MAX_WEEKS_IN_GROUP = 20;
//				for( int i=2; i<=MAX_WEEKS_IN_GROUP; i+=2 ){
//					stats.analyzeNWeekData(con, i, MAX_WEEKS_IN_GROUP, cLastFriday);
//				}
				
		//		stats.analyzeNWeekData(con, 2, 20);
		//		stats.analyzeNWeekData(con, 4, 20); // 1 Month
		//		
		//		stats.analyzeNWeekData(con, 6, 20);
		//		stats.analyzeNWeekData(con, 8, 20); // 2 Month
		//		
		//		stats.analyzeNWeekData(con, 10, 20);
		//		stats.analyzeNWeekData(con, 12, 20); // 3 Month
		//		
		//		stats.analyzeNWeekData(con, 14, 20);
		//		stats.analyzeNWeekData(con, 16, 20); // 4 Month
		//		
		//		stats.analyzeNWeekData(con, 18, 20);
				stats.analyzeNWeekData(con, 20, 20, cLastFriday); // 5 Month
		
				FileWriter writer = new FileWriter( EXPORT_FOLDER+"Wk"+iteration+"_"+PERCENT+".html" );
				writer.write("<html><body><table border='1'>\n");
				writer.write( sbConsolidatedReport.toString() );
				writer.write("</table></body></html>\n");
				writer.close();
				
				sbConsolidatedReport.delete(0, sbConsolidatedReport.length());
			}
			
			ArrayList<KeyValDetails> monKvd = wkDayIteration.get(Calendar.MONDAY);
			ArrayList<KeyValDetails> tueKvd = wkDayIteration.get(Calendar.TUESDAY);
			ArrayList<KeyValDetails> wedKvd = wkDayIteration.get(Calendar.WEDNESDAY);
			ArrayList<KeyValDetails> thuKvd = wkDayIteration.get(Calendar.THURSDAY);
			ArrayList<KeyValDetails> friKvd = wkDayIteration.get(Calendar.FRIDAY);
			
			Collections.sort( monKvd, comparator );
			Collections.sort( tueKvd, comparator );
			Collections.sort( wedKvd, comparator );
			Collections.sort( thuKvd, comparator );
			Collections.sort( friKvd, comparator );
			
			System.out.println( "\tMon: " +monKvd.get( monKvd.size()-1 ) );
			System.out.println( "\tTue: " +tueKvd.get( tueKvd.size()-1 ) );
			System.out.println( "\tWed: " +wedKvd.get( wedKvd.size()-1 ) );
			System.out.println( "\tThu: " +thuKvd.get( thuKvd.size()-1 ) );
			System.out.println( "\tFri: " +friKvd.get( friKvd.size()-1 ) );
			
			
			cLastFriday.add(Calendar.DATE, 7);
		}
		
		stats.closeResource(null, null, con);
		System.out.println( "Process Completed." );
	}
	
	private void analyzeNWeekData(Connection con, int numberOfWeeks, int maxWeeksInGroup, Calendar cLastFriday) throws Exception{
//		Calendar cLastFriday = Calendar.getInstance();
//		if( cLastFriday.get( Calendar.DAY_OF_WEEK ) >= Calendar.FRIDAY ){
//			cLastFriday.set( Calendar.DAY_OF_WEEK, Calendar.FRIDAY );
//		}else{
//			cLastFriday.set( Calendar.DAY_OF_WEEK, Calendar.FRIDAY );
//			cLastFriday.add( Calendar.DATE, -7 );
//		}
		
//		cLastFriday.set(Calendar.DATE, 3);
//		cLastFriday.set(Calendar.MONTH, Calendar.DECEMBER);
//		cLastFriday.set(Calendar.YEAR, 2010);
		
		// Initialize Dates
		Calendar cStartDate = Calendar.getInstance();
		cStartDate.set(Calendar.DATE, 1);
		cStartDate.set(Calendar.MONDAY, Calendar.MARCH);
		cStartDate.set(Calendar.YEAR, 2010);

		Calendar cEndDate = (Calendar) cStartDate.clone();
		cEndDate.add(Calendar.DATE, (maxWeeksInGroup*7)-3);
		cStartDate.setTime( cEndDate.getTime() );
		cStartDate.add(Calendar.DATE, -(numberOfWeeks*7)+3);

		String exportFolder = EXPORT_FOLDER+numberOfWeeks+"Week/";
		File file = new File(exportFolder);
		if( !file.exists() ){
			file.mkdir();
		}
		
		while( cEndDate.before( cLastFriday ) ){
			cStartDate.add(Calendar.DATE, 7);
			cEndDate.add(Calendar.DATE, 7);
			
			generateReports( con, cStartDate.getTime(), cEndDate.getTime(), exportFolder );
		}
		process(exportFolder, con, numberOfWeeks);
	}
	
	private void generateReports(Connection con, Date startDate, Date endDate, String exportFolder) throws Exception {
		String exportFileName = exportFolder+new java.text.SimpleDateFormat("MM_dd_yyyy").format( endDate )+ ".csv";
		Map params = new HashMap();
		
		params.put("pStartDate", startDate);
		params.put("pEndDate", endDate);
		params.put("pPercentMove", new Double("0.60"));
		params.put("SUBREPORT_DIR", "C:\\Program Files\\JasperSoft\\iReport-3.0.0\\");
		
		JasperDesign design = JRXmlLoader.load( RPT_FILE );
		design.setIgnorePagination(true);
		JasperReport report = JasperCompileManager.compileReport(design);
		JasperPrint print = JasperFillManager.fillReport(report, params, con);
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, new File(exportFileName));
		exporter.exportReport();
		//System.out.println( "Date Range: " +getStrDate(startDate)+"-"+getStrDate(endDate) );
		System.out.println( "CSV Generated: " +exportFileName );
	}
	
	private void process(String exportFolder, Connection con, int numberOfWeeks) throws Exception{
		FileWriter writer = new FileWriter( exportFolder+"rpt.html" );
		
		String[] files = new File(exportFolder).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});

		writer.write( "<html><body>" );
		writer.write( "<B>" +numberOfWeeks+ " Weeks Data</B>" );
		writer.write( "<table border='1'>\n" );
		
		Map<Integer, Integer> mTotalTradingDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalTradingDays
		Map<Integer, Integer> mTotalSuccessDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalSuccessDays
		
		StringBuffer sbStrategy = null;
		List<NysePick> list = null;
		for( String file : files ){
			//System.out.println( "-> " + file );
			list = getStrategy( exportFolder+file );
	
			writer.write( "<!--\n" );
			sbStrategy = new StringBuffer();
			
			for( NysePick nysePick : list ){
				sbStrategy.append( nysePick.toString() ).append("\n");
			}
			writer.write( sbStrategy.toString() );
			writer.write( "-->\n" );

			writer.write( "<tr>\n" );
			NysePick npBuy = null, npSell = null;
			for( int i=0; i<list.size(); i++ ){
				writer.write( "<td " );
				npBuy = list.get(i);
				
				Nyse nyseBuy = getResult( con, npBuy.buyDate, npBuy.symbol);
				if( nyseBuy == null ){
					// Friday Holiday
					Calendar c = Calendar.getInstance();
					c.setTime( npBuy.buyDate );
					
					while( nyseBuy == null ){
						c.add( Calendar.DATE, -1);
						nyseBuy = getResult( con, c.getTime(), npBuy.symbol);
					}
				}
				
				Double expectedGain = nyseBuy.close + (nyseBuy.close * (0.60/100.0));
				
				if( list.size() > (i+1) ){
					npSell = list.get(i+1);
				}else{
					npSell = list.get(i);
					npSell = new NysePick(npSell.symbol, npSell.successPercent, new Date( npSell.buyDate.getTime() ));
					
					Calendar c = Calendar.getInstance();
					c.setTime( npSell.buyDate );
					c.add( Calendar.DATE, 1);
					
					npSell.buyDate = c.getTime();
				}
				
				Nyse nyseSell = getResult( con, npSell.buyDate, npBuy.symbol);

				if( nyseSell == null ){
					writer.write( "><B>Holiday</B>" );
				}else{
					Integer totalTradingDays = mTotalTradingDays.get(i);
					if( totalTradingDays == null ){
						totalTradingDays = new Integer(0);
					}
					mTotalTradingDays.put(i, totalTradingDays.intValue()+1);

					Integer totalSuccessDays = mTotalSuccessDays.get(i);
					if( totalSuccessDays == null ){
						totalSuccessDays = new Integer(0);
					}

					if( expectedGain > nyseSell.low && expectedGain < nyseSell.high ){
						mTotalSuccessDays.put(i, totalSuccessDays.intValue()+1);
						
						writer.write( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						writer.write( "<BR>Profit (As expected): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ expectedGain );
					}else if( expectedGain < nyseSell.low ){
						mTotalSuccessDays.put(i, totalSuccessDays.intValue()+1);

						writer.write( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						writer.write( "<BR>Profit (<B>Beyond Expectations</B>): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.low );
					}else{
						writer.write( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						writer.write( "<BR><span style='background-color:red'>Loss:</span> Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.close );
					}
				}
				
				writer.write( "</td>\n" );
			}
			writer.write( "</tr>\n" );
		}
		
		Calendar cal = Calendar.getInstance();
		for( NysePick nysePick : list ){
			cal.setTime(nysePick.buyDate);
			switch( cal.get( Calendar.DAY_OF_WEEK ) ){
				case Calendar.FRIDAY:
					wkDayIteration.get(Calendar.MONDAY).add( new KeyValDetails( nysePick.symbol, nysePick.successPercent, "Buy on "+getStrDate(nysePick.buyDate) ) );
					break;
				case Calendar.MONDAY:
					wkDayIteration.get(Calendar.TUESDAY).add( new KeyValDetails( nysePick.symbol, nysePick.successPercent, "Buy on "+getStrDate(nysePick.buyDate) ) );
					break;
				case Calendar.TUESDAY:
					wkDayIteration.get(Calendar.WEDNESDAY).add( new KeyValDetails( nysePick.symbol, nysePick.successPercent, "Buy on "+getStrDate(nysePick.buyDate) ) );
					break;
				case Calendar.WEDNESDAY:
					wkDayIteration.get(Calendar.THURSDAY).add( new KeyValDetails( nysePick.symbol, nysePick.successPercent, "Buy on "+getStrDate(nysePick.buyDate) ) );
					break;
				case Calendar.THURSDAY:
					wkDayIteration.get(Calendar.FRIDAY).add( new KeyValDetails( nysePick.symbol, nysePick.successPercent, "Buy on "+getStrDate(nysePick.buyDate) ) );
					break;
			}
		}
		
		
		writer.write( "<tr>\n" );
		sbConsolidatedReport.append( "<tr title='" +sbStrategy+ "'>" );
		sbConsolidatedReport.append( "<td>" +numberOfWeeks+ "Weeks</td>" );
		for(int i=0; i<5; i++){
			writer.write( "<td>" );
			int totalTradingDays = 0;
			if(mTotalTradingDays.get(i) != null){
				totalTradingDays = mTotalTradingDays.get(i);
			}
			
			int totalSuccessDays = 0;
			if( mTotalSuccessDays.get(i) != null){
				totalSuccessDays = mTotalSuccessDays.get(i);
			}
			String summary = totalSuccessDays +"/"+totalTradingDays+ " (" +(int)(((double)totalSuccessDays/(double)totalTradingDays)*100.0)+ "%)";
			writer.write( summary );
			
			sbConsolidatedReport.append("<td>").append( summary ).append("</td>");
			
			writer.write( "</td>" );
		}
		sbConsolidatedReport.append( "</tr>\n" );
		writer.write( "</tr>\n" );
		
		writer.write( "</table></body></html>\n" );
		
		writer.close();
		System.out.println( "Report Generated." );
	}

	private List<NysePick> getStrategy(String fileName) throws Exception {
		String str[] = new String[2];
		LineNumberReader lnr = new LineNumberReader( new FileReader( fileName ) );
		str[0] = lnr.readLine();
		str[1] = lnr.readLine();
		
		String line = null;
		List<KeyValDetails> l1 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l2 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l3 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l4 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l5 = new ArrayList<KeyValDetails>();
		
		while( (line = lnr.readLine()) != null ){
			String[] arrLine = line.split("[,]", -1);
			l1.add( new KeyValDetails(arrLine[1], (arrLine[2] != null && !arrLine[2].equals("null") ? new Integer(arrLine[2]) : 0), "") );
			l2.add( new KeyValDetails(arrLine[1], (arrLine[3] != null && !arrLine[3].equals("null") ? new Integer(arrLine[3]) : 0), "") );
			l3.add( new KeyValDetails(arrLine[1], (arrLine[4] != null && !arrLine[4].equals("null") ? new Integer(arrLine[4]) : 0), "") );
			l4.add( new KeyValDetails(arrLine[1], (arrLine[5] != null && !arrLine[5].equals("null") ? new Integer(arrLine[5]) : 0), "") );
			l5.add( new KeyValDetails(arrLine[1], (arrLine[6] != null && !arrLine[6].equals("null") ? new Integer(arrLine[6]) : 0), "") );
		}
		lnr.close();
		
		TreeSet ts1 = new TreeSet(comparator);
		ts1.addAll(l1);
		KeyValDetails o1 = getNPlus(ts1);

		TreeSet ts2 = new TreeSet(comparator);
		ts2.addAll(l2);
		KeyValDetails o2 = getNPlus(ts2);

		TreeSet ts3 = new TreeSet(comparator);
		ts3.addAll(l3);
		KeyValDetails o3 = getNPlus(ts3);

		TreeSet ts4 = new TreeSet(comparator);
		ts4.addAll(l4);
		KeyValDetails o4 = getNPlus(ts4);

		TreeSet ts5 = new TreeSet(comparator);
		ts5.addAll(l5);
		KeyValDetails o5 = getNPlus(ts5);

//		Object[] o1 = Collections.max(l1, comparator);
//		Object[] o2 = Collections.max(l2, comparator);
//		Object[] o3 = Collections.max(l3, comparator);
//		Object[] o4 = Collections.max(l4, comparator);
//		Object[] o5 = Collections.max(l5, comparator);
		
		List<NysePick> list = new ArrayList<NysePick>();
		String[] s = str[1].split("[,]", -1);
		
		Calendar c = Calendar.getInstance();
		c.setTime( getDate(s[s.length-1]) );
		list.add( new NysePick( o1.key, o1.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 3);
		list.add( new NysePick( o2.key, o2.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o3.key, o3.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o4.key, o4.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o5.key, o5.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);

		return list;
	}
	
	private KeyValDetails getNPlus(TreeSet<KeyValDetails> ts1){
		Iterator<KeyValDetails> iterator = ts1.iterator();
		while(iterator.hasNext()){
			KeyValDetails o = iterator.next();
			if( o.value > PERCENT ){
				return o;
			}
		}
		return ts1.floor( new KeyValDetails("", PERCENT, "") );
	}
	
	private Date getDate(String str) throws Exception {
		return new java.text.SimpleDateFormat("MM/dd/yyyy").parse( str );
	}
	
	private String getStrDate(Date dt) throws Exception{
		return new java.text.SimpleDateFormat("MM/dd/yyyy").format( dt );
	}
	
	private Connection getConnection() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection("jdbc:mysql://10.8.48.78/ind_stocks", "root", "password");
		return connection;
	}
	
	private void closeResource(ResultSet rs, Statement stmt, Connection con) throws Exception{
		if( rs != null ){
			rs.close();
		}
		if(stmt != null){
			stmt.close();
		}
		if(con != null){
			con.close();
		}
	}
	
	private Nyse getResult(Connection con, Date tradeDate, String symbol) throws Exception {
		Nyse nyse = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try{
			stmt = con.prepareStatement( "select * from nyse where symbol=? and trade_date=? and volume > 0" );
			stmt.setString(1, symbol);
			stmt.setDate(2, new java.sql.Date(tradeDate.getTime()));
			rs = stmt.executeQuery();
			
			if( rs != null && rs.next() ){
				Date tradeDt = rs.getDate("trade_date");
				String sym = rs.getString("symbol");
			    Double open = rs.getDouble("open");
			    Double high = rs.getDouble("high");
			    Double low = rs.getDouble("low");
			    Double close = rs.getDouble("close");
				
				nyse = new Nyse( tradeDt, sym, open, high, low, close );
			}
		}
		finally{
			closeResource(rs, stmt, null);
		}
		return nyse;
	}
	
	class KeyValDetails{
		String key;
		Integer value;
		String details;
		public KeyValDetails(String key, Integer value, String details) {
			super();
			this.key = key;
			this.value = value;
			this.details = details;
		}

		@Override
		public String toString() {
			return "Key: " +this.key+", Value: "+this.value+", Details: "+ this.details;
		}
	}
	
	class NysePick{
		String symbol;
		Integer successPercent;
		Date buyDate;
		public NysePick(String symbol, Integer successPercent, Date buyDate) {
			super();
			this.symbol = symbol;
			this.successPercent = successPercent;
			this.buyDate = buyDate;
		}
		
		@Override
		public String toString(){
			String bd = null;
			try{
				bd = getStrDate(buyDate);
			}
			catch(Exception e){
				
			}
			return "Buy " +symbol +" on " +bd+ " ("+ successPercent+ "% chances of success)";
		}
	}
	
	class Nyse{
		Date tradeDate;
		String symbol;
	    Double open;
	    Double high;
	    Double low;
	    Double close;
		
	    public Nyse(Date tradeDate, String symbol, Double open, Double high,
				Double low, Double close) {
			super();
			this.tradeDate = tradeDate;
			this.symbol = symbol;
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
		}
	}
}
