package com.stocks.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
	public static String getContent(String sUrl) throws Exception {
		StringBuffer content = new StringBuffer();

		URL url = new URL(sUrl);
		final URLConnection urlConnection = url.openConnection();
		urlConnection.setUseCaches(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}
	
	public static void saveContent(String path, String content) throws Exception{
		FileWriter writer = new FileWriter(path);
		writer.append(content);
		writer.close();
	}

	public static Double round(Double d){
		BigDecimal bd = new BigDecimal( d );
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public static boolean areDatesEqual(final Date d1, final Date d2){
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		final String s1 = df.format(d1);
		final String s2 = df.format(d2);
		return s1.equals(s2);
	}

	public static Date getDate(String str) throws Exception {
		return new SimpleDateFormat("MM/dd/yyyy").parse( str );
	}
	
	public static String getStrDate(Date dt) throws Exception{
		return new SimpleDateFormat("MM/dd/yyyy").format( dt );
	}
	
	public static long getDaysDiffBetween(Date date1, Date date2){
		long diff = date1.getTime() - date2.getTime();
		return Math.abs(diff / (1000 * 60 * 60 * 24));
	}
	
//	public static void main(String args[]){
//		String arr[] = new String[]{"MMM", "ABB", "ABT", "ACL", "MO", "AMZN", "AXP", "AMGN", "AAPL", "AZN", "T", "AXAHY", "BBD", "STD", "BCS", "BRK.A", "BHP", "BBL", "BP", "BMY", "BTI", "CAJ", "CAT", "CVX", "LFC", "CHL", "SNP", "CSCO", "C", "CL", "CMCSA", "ABV", "COP", "CS", "CVS", "DB", "DT", "DEO", "EC", "LLY", "E", "XOM", "F", "FTE", "GE", "GSK", "GS", "GOOG", "HPQ", "HMC", "HBC", "INTC", "IBM", "JNJ", "JPM", "KFT", "LYG", "MCD", "MDT", "MRK", "MSFT", "MS", "NTT", "NOK", "NVS", "NVO", "DCM", "OXY", "ORCL", "PEP", "PTR", "PBR", "PFE", "PM", "QCOM", "RTP", "RY", "RDS.A", "SNY", "SAP", "SLB", "SI", "STO", "SU", "TGT", "TEF", "TEVA", "BNS", "BA", "KO", "HD", "PG", "DIS", "TD", "TOT", "USB", "UN", "UL", "UPS", "UTX", "VALE", "VZ", "V", "VOD", "WMT", "WFC", "WBK"};
//		String djia[] = new String[]{"MMM", "AA", "AXP", "T", "BAC", "BA", "CAT", "CVX", "CSCO", "KO", "DD", "XOM", "GE", "HPQ", "HD", "INTC", "IBM", "JNJ", "JPM", "KFT", "MCD", "MRK", "MSFT", "PFE", "PG", "TRV", "UTX", "VZ", "WMT", "DIS"};
//		List<String> arrList = new ArrayList(Arrays.asList(arr));
//		List<String> djiaList = new ArrayList(Arrays.asList(djia));
//		arrList.removeAll(djiaList);
//		System.out.println( arrList );
//	}
}
