package com.stocks.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utility {
	public static String getContent(String sUrl) throws Exception {
		StringBuffer content = new StringBuffer();

		URL url = new URL(sUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	public static Double round(Double d){
		BigDecimal bd = new BigDecimal( d );
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
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
