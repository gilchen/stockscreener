package com.stocks.standalone;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;

import com.stocks.util.Utility;

public class SharesOutstanding {
	final static String CNBC_URL_EXTN = "http://apps.cnbc.com/company/quote/index.asp?symbol=";

	/**
	 * Main method to trigger the process.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		LineNumberReader lnr = new LineNumberReader( new FileReader(args[0]) );
		List<String> list = new LinkedList<String>();
		String symbol = null;
		while( (symbol = lnr.readLine()) != null ){
			list.add(symbol);
		}
		lnr.close();
		
		processCnbc(list);
		System.out.println("Done.");
	}

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processCnbc(List<String> list) throws Exception {
		System.out.println( "Symbol,Shares Outstanding,Expanded Shares Outstanding" );
		for (String symbol : list) {
			symbol = symbol.trim();
			//System.out.println("Pulling [" + symbol + "]");

			try {
				// Start: Pull Data
				final StringBuffer sbExtn = new StringBuffer(Utility.getContent(CNBC_URL_EXTN + symbol));

				String sharesOutstanding = "";
				int index5 = sbExtn.indexOf("Shares Outstanding");
				if (index5 != -1) {
					sharesOutstanding = sbExtn.substring(index5 + 38, sbExtn.indexOf("<", index5 + 38));
					sharesOutstanding = sharesOutstanding.trim().replaceAll(",", "");
				}
				System.out.println( symbol+","+sharesOutstanding+","+Utility.convertFinancials(sharesOutstanding) );

			} catch (Exception e) {
				System.out.println( "Error in pulling "+symbol );
				e.printStackTrace();
			}
		}
	}
}
