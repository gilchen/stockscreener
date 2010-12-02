package com.stocks;

import java.io.File;
import java.io.FileReader;
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
import java.util.List;

public class Statistics {
	static final String folder = "C:/Temp/stk/Analysis/3_month";
	
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
		String[] files = new File(folder).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		Connection con = stats.getConnection();
		System.out.println( "<html><body><table border='1'>" );
		for( String file : files ){
			//System.out.println( file );
			List<NysePick> list = stats.getStrategy( file );
			System.out.println( "<!--" );
			for( NysePick nysePick : list ){
				System.out.println( nysePick );
			}
			System.out.println( "-->" );
//			if(true) return;
			
			System.out.println( "<tr>" );
			//boolean isHoliday = false;
			NysePick npBuy = null, npSell = null;
			for( int i=0; i<list.size(); i++ ){
				System.out.println( "<td " );
				npBuy = list.get(i);
				
				Nyse nyseBuy = stats.getResult( con, npBuy.buyDate, npBuy.symbol);
				if( nyseBuy == null ){
					// Friday Holiday
					Calendar c = Calendar.getInstance();
					c.setTime( npBuy.buyDate );
					
					while( nyseBuy == null ){
						c.add( Calendar.DATE, -1);
						//npBuy.buyDate = c.getTime();
	
						nyseBuy = stats.getResult( con, c.getTime(), npBuy.symbol);
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
				
				Nyse nyseSell = stats.getResult( con, npSell.buyDate, npBuy.symbol);

				if( nyseSell == null ){
					//isHoliday = true;
					System.out.println( "><B>Holiday</B>" );
				}else{
					if( expectedGain > nyseSell.low && expectedGain < nyseSell.high ){
						System.out.println( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +stats.getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						System.out.println( "<BR>Profit (As expected): Sell " +nyseSell.symbol+ " on " +stats.getStrDate( nyseSell.tradeDate )+ " @"+ expectedGain );
					}else if( expectedGain < nyseSell.low ){
						System.out.println( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +stats.getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						System.out.println( "<BR>Profit (<B>Beyond Expectations</B>): Sell " +nyseSell.symbol+ " on " +stats.getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.low );
					}else{
						System.out.println( "bgcolor=#FFC1C1>Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +stats.getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
						System.out.println( "<BR>Loss: Sell " +nyseSell.symbol+ " on " +stats.getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.close );
					}
				}
				
//				if( isHoliday ){
//					try{
//						tradeDate = stats.getDate( row2[i-0] );
//						symbol = row1[i-2];
//					}
//					catch(Exception e){
//						// When Monday is Holiday
//						tradeDate = oldTradeDate;
//						symbol = row1[i+2];
//						//System.out.println( "Exception " +tradeDate+ ", "+ symbol );
//					}
//					isHoliday = false;
//				}else{
//					tradeDate = stats.getDate( row2[i+1] );
//					symbol = row1[i+2];
//				}
//				nyseBuy = stats.getResult(con, tradeDate, symbol);
				System.out.println( "</td>" );
			}
			System.out.println( "</tr>" );			
			
			
//			String[] rows = stats.readFile(file);
//			String[] row1 = rows[0].split(",");
//			String[] row2 = rows[1].split(",");
//			
//			Date tradeDate = stats.getDate( row2[row2.length-1] );
//			String symbol = row1[1];
//			Nyse nyseBuy = stats.getResult(  con, tradeDate, symbol);
//			System.out.println( "<tr>" );
//			boolean isHoliday = false;
//			for( int i=0; i<5; i++ ){
//				System.out.println( "<td " );
//				Date oldTradeDate = tradeDate;
//				tradeDate = stats.getDate( row2[i+1] );
//				symbol = row1[i+1];
//				Nyse nyseSell = stats.getResult( con, tradeDate, symbol);
//				Double expectedGain = nyseBuy.close + (nyseBuy.close * (0.60/100.0));
//				
//				if( nyseSell == null ){
//					isHoliday = true;
//					System.out.println( "><B>Holiday</B>" );
//				}else{
//					if( expectedGain > nyseSell.low && expectedGain < nyseSell.high ){
//						System.out.println( ">Buy " +symbol+ " on " +stats.getStrDate( oldTradeDate )+ " @"+ nyseBuy.close );
//						System.out.println( "<BR>Profit (As expected): Sell " +symbol+ " on " +stats.getStrDate( tradeDate )+ " @"+ expectedGain );
//					}else if( expectedGain < nyseSell.low ){
//						System.out.println( ">Buy " +symbol+ " on " +stats.getStrDate( oldTradeDate )+ " @"+ nyseBuy.close );
//						System.out.println( "<BR>Profit (<B>Beyond Expectations</B>): Sell " +symbol+ " on " +stats.getStrDate( tradeDate )+ " @"+ nyseSell.low );
//					}else{
//						System.out.println( "bgcolor=#FFC1C1>Buy " +symbol+ " on " +stats.getStrDate( oldTradeDate )+ " @"+ nyseBuy.close );
//						System.out.println( "<BR>Loss: Sell " +symbol+ " on " +stats.getStrDate( tradeDate )+ " @"+ nyseSell.close );
//					}
//				}
//				
//				if( isHoliday ){
//					try{
//						tradeDate = stats.getDate( row2[i-0] );
//						symbol = row1[i-2];
//					}
//					catch(Exception e){
//						// When Monday is Holiday
//						tradeDate = oldTradeDate;
//						symbol = row1[i+2];
//						//System.out.println( "Exception " +tradeDate+ ", "+ symbol );
//					}
//					isHoliday = false;
//				}else{
//					tradeDate = stats.getDate( row2[i+1] );
//					symbol = row1[i+2];
//				}
//				nyseBuy = stats.getResult(con, tradeDate, symbol);
//				System.out.println( "</td>" );
//			}
//			System.out.println( "</tr>" );
		}
		System.out.println( "</table></body></html>" );
		stats.closeResource(null, null, con);
	}
	
	private List<NysePick> getStrategy(String fileName) throws Exception {
		String str[] = new String[2];
		LineNumberReader lnr = new LineNumberReader( new FileReader( folder+"/"+fileName ) );
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
	
	private String[] readFile(String fileName) throws Exception {
		String str[] = new String[2];
		LineNumberReader lnr = new LineNumberReader( new FileReader( folder+"/"+fileName ) );
		str[0] = lnr.readLine();
		str[1] = lnr.readLine();
		lnr.close();
		return str;
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
			return symbol +", "+ successPercent+ ", "+ bd;
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
