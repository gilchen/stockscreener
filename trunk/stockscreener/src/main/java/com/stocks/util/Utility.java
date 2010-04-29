package com.stocks.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;

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
}
