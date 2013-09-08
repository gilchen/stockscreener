package com.stocks.standalone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NewAdditionsToShort {
	final static String PATH = "C:/Classroom/JSF/int_ref/workspace/trunk/stk/nyse/";
	
	public static void main(String... args) throws Exception {
		final List<String> previousList = getSymbolsFromFile("shortlist_08-26-2013.txt");
		final List<String> currentList = getSymbolsFromFile("shortlist_08-27-2013.txt");
		System.out.println( "Total " +currentList.size()+ " Symbols:\n" +currentList );
		currentList.removeAll( previousList );
		System.out.println( "Total " +currentList.size()+ " New Symbols Added Today:\n" +currentList );
	}
	
	private static List<String> getSymbolsFromFile(String file) throws Exception{
		BufferedReader reader = null;
		final List<String> symbols = new ArrayList<String>();
		try{
			reader = new BufferedReader( new FileReader(  PATH + file ) );
			String line = null;
			while( (line = reader.readLine() ) != null ){
				if( !line.trim().equals("") ){
					symbols.add( line.split("\t")[0] );
				}
			}
		}
		finally{
			if( reader != null ){
				reader.close();
			}
		}
		return symbols;
	}
}
