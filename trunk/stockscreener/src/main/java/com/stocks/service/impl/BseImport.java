package com.stocks.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Required;

import com.stocks.model.Bse;
import com.stocks.model.BsePK;
import com.stocks.model.KeyValue;
import com.stocks.service.StockImportService;
import com.stocks.service.StockService;

public class BseImport{
	private static final String BSE_IMPORT_KEY = "LAST_BSE_IMPORT";
	private String downloadFolder;
	private StockImportService stockImportService;
	private StockService stockService;
	
	public String getDownloadFolder() {
		return downloadFolder;
	}

	@Required
	public void setDownloadFolder(String downloadFolder) {
		this.downloadFolder = downloadFolder;
	}
	
	public StockImportService getStockImportService() {
		return stockImportService;
	}

	@Required
	public void setStockImportService(StockImportService stockImportService) {
		this.stockImportService = stockImportService;
	}

	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

//	public static void main(String args[]) throws Exception{
//		if( args.length != 1 ){
//			System.out.println("Usage: java BseImport <DDMMYY>");
//			return;
//		}
//
//		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime( sdf.parse(args[0]) );
//
//		BseImport bseImport = new BseImport();
//		bseImport.downloadFile(calendar);
//		System.out.println( "Done." );
//	}

	public void importData() throws Exception{
		KeyValue keyValue = getStockService().getKeyValue( BSE_IMPORT_KEY );
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( sdf.parse( keyValue.getV() ) );

		downloadFile(calendar);
	}
	
	private void downloadFile(final Calendar calendar) throws Exception{
		if( calendar.after( Calendar.getInstance() ) ){
			return;
		}

		// http://bseindia.com/bhavcopy/eq140109_csv.zip
		String sUrl = "http://bseindia.com/bhavcopy/";
		String fileName = "eq" +new SimpleDateFormat("ddMMyy").format(calendar.getTime()) +"_csv.zip";
		sUrl += fileName;

		final File file = new File(getDownloadFolder()+"/"+fileName);
		if( !(file.exists()) ){
			URL url = new URL(sUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			if( httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND ){
				System.out.println( ":( Error 404 for [" +sUrl+ "]" );
			}else if( httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK ){
				String sPath = getDownloadFolder() +"/"+ fileName;
				InputStream is = httpURLConnection.getInputStream();
				OutputStream os = new FileOutputStream( sPath  );
				byte[] b = new byte[256];
				int bytesRead = -1;
				while( (bytesRead = is.read(b, 0, b.length)) != -1 ){
					os.write( b, 0, bytesRead );
				}

				if( is != null ){
					is.close();
				}
				if( os != null ){
					os.close();
				}
				System.out.println( "::: Downloaded to " +sPath );
			}else{
				System.out.println( ":( Error " +httpURLConnection.getResponseCode()+". Retrying..." );
				downloadFile(calendar);
			}
		}
		
		if( file.exists() ){
			importFile(calendar);
			
			KeyValue keyValue = new KeyValue();
			keyValue.setK( BSE_IMPORT_KEY );
			keyValue.setV(new SimpleDateFormat("ddMMyy").format(calendar.getTime()));
			getStockService().saveKeyValue(keyValue);
		}

		calendar.add(Calendar.DAY_OF_MONTH, 1);
		downloadFile(calendar);
	}
	
	private void importFile(final Calendar calendar) throws Exception{
		final String fileName = "eq" +new SimpleDateFormat("ddMMyy").format(calendar.getTime()) +"_csv.zip";
		final String sPath = getDownloadFolder() +"/"+ fileName;
		String csvFileName = unzip(sPath);
		
		if( csvFileName != null ){
			final LineNumberReader reader = new LineNumberReader(new FileReader(getDownloadFolder()+"/"+csvFileName));
			String line = reader.readLine(); // ignore first line.
			String[] columns;
			while( (line = reader.readLine()) != null ){
				columns = line.split("[,]", -1);
				
				BsePK bsePK = new BsePK(calendar.getTime(), new Integer(columns[0]));
				Bse bse = new Bse();
				bse.setBsePK(bsePK);
				bse.setScName(columns[1]);
				bse.setScGroup(columns[2]);
				bse.setScType(columns[3]);
				bse.setOpen(new Double(columns[4]));
				bse.setHigh(new Double(columns[5]));
				bse.setLow(new Double(columns[6]));
				bse.setClose(new Double(columns[7]));
				bse.setLast(new Double(columns[8]));
				bse.setPrevClose(new Double(columns[9]));
				bse.setNoTrades(new Long(columns[10]));
				bse.setNoOfShares(new Long(columns[11]));
				bse.setNetTurnover(new Double(columns[12]));
				bse.setTdcloindi(columns[13]);
				
				getStockImportService().saveBse(bse);
			}
			System.out.println( "Data imported for " +calendar.getTime() );
		}
	}
	
	private String unzip(final String sPath) throws Exception{
		ZipInputStream is = null;
		OutputStream os = null;
		String fileName = null;
		try {
			is = new ZipInputStream( new FileInputStream(sPath) );
			ZipEntry zipEntry = is.getNextEntry();
			if( zipEntry != null ){
				fileName = zipEntry.getName();
				os = new FileOutputStream( getDownloadFolder() +"/"+ fileName );
				byte[] b = new byte[1024];
				int bytesRead = -1;
				while( (bytesRead = is.read(b)) != -1 ){
					os.write(b, 0, bytesRead);
				}
			}
		}finally{
			if( is != null ){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if( os != null ){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileName;
	}
}