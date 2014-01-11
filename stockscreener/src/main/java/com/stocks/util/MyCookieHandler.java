package com.stocks.util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCookieHandler extends CookieHandler {
	// This program utilizes cookies as exported by "Export Cookies" Addon from Mozilla Firefox.
	  private static final Map<String, List<String>> cookies = new HashMap<String, List<String>>();
	  static{
		  List<String> cookieList = new ArrayList<String>();

		  String cookieFile = System.getProperty("cookie.file");
		  System.out.println( "cookieFile: " +cookieFile );
		  if( cookieFile != null ){
			  try{
				  String content = Utility.getContent(cookieFile);
				  if( content != null ){
					  for( String line : content.split("\n") ){
						  if( line.startsWith(".google.com") ){
							  String cols[] = line.split("\t");
							  String cookie = cols[5] +"="+ cols[6];
							  cookieList.add(cookie);
						  }
					  }
				  }
			  }
			  catch(IOException e){
				  System.out.println( "IOException occured while reading cookieFile: " +e.getMessage() );
			  }
			  catch(Exception e){
				  e.printStackTrace();
			  }
		  }
		  
//		  for( String l : Arrays.asList(new String[]{ 
//				  "SC=RV=8197-657319-698419-672823-7216210-9617-973037374876684-658799-706102:NAV=CP", 
//				  "NID=67=hOt7DnZt3M8eGknSSvc2Ukq8OSFuqvb2w9DBW7V2EfKQNY2GtEJKx8RapCAtO6POp09GVkhqbNUQGhmep8AmahSNovWbHcHgPpGfJ7kbA8lo9K9NfknhcrLw1gki53BbszirLNRAjhbUj_iOBpE6O8ljZ6aAVS_Q4e_MsyTms040HLvr6MGDQWI0_95JZio1GU6tvQPXpzDAgGcIXoV0qA", 
//				  "PREF=ID=b3448cf9e5f5a7c3:U=9b725f6ff6c66fb5:FF=0:LD=en:TM=1374532037:LM=1375534971:GM=1:S=KAoxWKpO9yWMhCgO", 
//				  "S=quotestreamer=pmvZQwQy7EdxjCx4pLTmaA", 
//				  "SAPISID=eX9apS-n5gho-i43/AjXmQwzW4XXQ9_ioe", 
//				  "HSID=A0nZI_oKB4ACT1U3K", 
//				  "APISID=L9kCAR5vIDV25_Xr/An69sEpIh1thD9F4e", 
//				  "SID=DQAAAMMAAAARuGiBIo7J2eng94_sWk7pcYDc-8-pbMACBika4iYo25wNgtz6Qdz9kIb90cTo5yGddbZ_SZY9yKdWOkW7ySNerSf1xbMOXTkRf00qh02v27MQtQtn5thrs0Zr0WMXA3-jI3EBU4YVPWYunzzhQCC-kMr9Gti3yED_LW72eQWy3xSACPrBwJaD1e1YWdOljB61EQStETZkPAbYBymkSBNC1LBDkiuoHwbcx36_3uEvV-dCXgX-k1gqvJYj9Ru_KcXfIKFlGdwKm7_yWQsskvQq", 
//				  "SSID=AMBQ3sJUJL-4KxU8X"}) ){
//			  cookieList.add(l);
//		  }
		System.out.println( "cookieList: " +cookieList );
		cookies.put( "www.google.com", cookieList );
	  }
	  
	  @Override 
	  public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
	    Map<String, List<String>> ret = new HashMap<String, List<String>>();
	    synchronized (cookies) {
	      String host = uri.getHost();
	      List<String> store = cookies.get(host);
	      if (store != null) {
	        store = Collections.unmodifiableList(store);
	        ret.put("Cookie", store);
	      }
	    }
	    return Collections.unmodifiableMap(ret);
	  }

	  @Override 
	  public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
	    List<String> newCookies = responseHeaders.get("Set-Cookie");
	    if (newCookies != null) {
	      synchronized (cookies) {
	        List<String> store = cookies.get(uri.getHost());
	        if (store == null) {
	          store = new ArrayList<String>();
	          cookies.put(uri.getHost(), store);
	        }
	        store.addAll(newCookies);
	      }
	    }
	  }
	}