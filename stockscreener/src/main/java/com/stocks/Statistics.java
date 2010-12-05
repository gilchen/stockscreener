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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	static Comparator comparator = new Comparator(){
		public int compare(Object o1, Object o2) {
			Object[] arr1 = (Object[]) o1;
			Object[] arr2 = (Object[]) o2;
			
			String s1 = (String)arr1[1];
			String s2 = (String)arr2[1];
			
			s1 = s1.equals("null") ? "0" : s1;
			s2 = s2.equals("null") ? "0" : s2;
			
			int ret = 0;
			if( Integer.parseInt(s1) < Integer.parseInt(s2) ){
				ret = -1;
			}else if( Integer.parseInt(s1) == Integer.parseInt(s2) ){
				ret = 0;
			}else if( Integer.parseInt(s1) > Integer.parseInt(s2) ){
				ret = 1;
			}
			return ret;
		}
	};
	
	public static void main(String args[]) throws Exception{
		Statistics stats = new Statistics();
		Connection con = stats.getConnection();

		stats.analyzeNWeekData(con, 2);
		stats.analyzeNWeekData(con, 4); // 1 Month
		
		stats.analyzeNWeekData(con, 6);
		stats.analyzeNWeekData(con, 8); // 2 Month
		
		stats.analyzeNWeekData(con, 10);
		stats.analyzeNWeekData(con, 12); // 3 Month
		
		stats.analyzeNWeekData(con, 14);
		stats.analyzeNWeekData(con, 16); // 4 Month
		
		stats.analyzeNWeekData(con, 18);
		stats.analyzeNWeekData(con, 20); // 5 Month

		stats.closeResource(null, null, con);
	}
	
	private void analyzeNWeekData(Connection con, int numberOfWeeks) throws Exception{
		Calendar cStartDate = Calendar.getInstance();
		Calendar cLastFriday = Calendar.getInstance();
		if( cLastFriday.get( Calendar.DAY_OF_WEEK ) >= Calendar.FRIDAY ){
			cLastFriday.set( Calendar.DAY_OF_WEEK, Calendar.FRIDAY );
		}else{
			cLastFriday.set( Calendar.DAY_OF_WEEK, Calendar.FRIDAY );
			cLastFriday.add( Calendar.DATE, -7 );
		}
		
//		cLastFriday.set(Calendar.DATE, 3);
//		cLastFriday.set(Calendar.MONTH, Calendar.DECEMBER);
//		cLastFriday.set(Calendar.YEAR, 2010);
		
		// Initialize Dates
		cStartDate.set(Calendar.DATE, 1);
		cStartDate.set(Calendar.MONDAY, Calendar.MARCH);
		cStartDate.set(Calendar.YEAR, 2010);

		Calendar cEndDate = (Calendar) cStartDate.clone();
		cEndDate.add(Calendar.DATE, (numberOfWeeks*7)-3);

		String exportFolder = "C:/Temp/stk/Analysis/reports/" +numberOfWeeks+"Week/";
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
		writer.write( "<B>" +numberOfWeeks+ " Weeks Data Starting 03/01/2010.</B>" );
		writer.write( "<table border='1'>\n" );
		
		Map<Integer, Integer> mTotalTradingDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalTradingDays
		Map<Integer, Integer> mTotalSuccessDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalSuccessDays
		
		for( String file : files ){
			//System.out.println( "-> " + file );
			List<NysePick> list = getStrategy( exportFolder+file );
			writer.write( "<!--\n" );
			for( NysePick nysePick : list ){
				writer.write( nysePick.toString()+"\n" );
			}
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
		
		writer.write( "<tr>\n" );
		for(int i=0; i<5; i++){
			writer.write( "<td>" );
			int totalTradingDays = mTotalTradingDays.get(i);
			int totalSuccessDays = mTotalSuccessDays.get(i);
			writer.write( totalSuccessDays +"/"+totalTradingDays+ " (" +(int)(((double)totalSuccessDays/(double)totalTradingDays)*100.0)+ "%)" );
			writer.write( "</td>" );
		}
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
		List<Object[]> l1 = new ArrayList<Object[]>();
		List<Object[]> l2 = new ArrayList<Object[]>();
		List<Object[]> l3 = new ArrayList<Object[]>();
		List<Object[]> l4 = new ArrayList<Object[]>();
		List<Object[]> l5 = new ArrayList<Object[]>();
		
		while( (line = lnr.readLine()) != null ){
			String[] arrLine = line.split("[,]", -1);
			l1.add( new Object[]{arrLine[1], arrLine[2]} );
			l2.add( new Object[]{arrLine[1], arrLine[3]} );
			l3.add( new Object[]{arrLine[1], arrLine[4]} );
			l4.add( new Object[]{arrLine[1], arrLine[5]} );
			l5.add( new Object[]{arrLine[1], arrLine[6]} );
		}
		lnr.close();
		
		Object[] o1 = Collections.max(l1, comparator);
		Object[] o2 = Collections.max(l2, comparator);
		Object[] o3 = Collections.max(l3, comparator);
		Object[] o4 = Collections.max(l4, comparator);
		Object[] o5 = Collections.max(l5, comparator);
		
		List<NysePick> list = new ArrayList<NysePick>();
		String[] s = str[1].split("[,]", -1);
		
		Calendar c = Calendar.getInstance();
		c.setTime( getDate(s[s.length-1]) );
		
		list.add( new NysePick( (String)o1[0], new Integer((String)o1[1]), c.getTime() ) );
		c.add(Calendar.DATE, 3);
		
		list.add( new NysePick( (String)o2[0], new Integer((String)o2[1]), c.getTime() ) );
		c.add(Calendar.DATE, 1);

		list.add( new NysePick( (String)o3[0], new Integer((String)o3[1]), c.getTime() ) );
		c.add(Calendar.DATE, 1);

		list.add( new NysePick( (String)o4[0], new Integer((String)o4[1]), c.getTime() ) );
		c.add(Calendar.DATE, 1);

		list.add( new NysePick( (String)o5[0], new Integer((String)o5[1]), c.getTime() ) );
		c.add(Calendar.DATE, 1);

		return list;
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
