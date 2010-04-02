package com.stocks.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.dao.NyseDao;
import com.stocks.model.KeyValue;
import com.stocks.model.Nyse;
import com.stocks.model.NysePK;
import com.stocks.service.ImportService;
import com.stocks.service.StockService;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class NyseImportServiceImpl implements ImportService{
	private String downloadFolder;
	private StockService stockService;

	@Autowired(required = true)
    private PlatformTransactionManager transactionManager;

    private NyseDao nyseDao;
	
	public String getDownloadFolder() {
		return downloadFolder;
	}

	@Required
	public void setDownloadFolder(String downloadFolder) {
		this.downloadFolder = downloadFolder;
	}
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public NyseDao getNyseDao() {
		return nyseDao;
	}

	@Required
	public void setNyseDao(NyseDao nyseDao) {
		this.nyseDao = nyseDao;
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void importData() throws Exception{
		importForStockExchange( NYSE_IMPORT_KEY, "NYSE" );
		importForStockExchange( NASDAQ_IMPORT_KEY, "NASDAQ" );
		importForStockExchange( AMEX_IMPORT_KEY, "AMEX" );
	}
	
	private void importForStockExchange(final String key, final String stockExchange) throws Exception{
		KeyValue keyValue = getStockService().getKeyValue( key );

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( sdf.parse( keyValue.getV() ) );
		
		while( calendar.before( Calendar.getInstance() ) ){
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			if( importFile(calendar, stockExchange) ){
				keyValue.setV(new SimpleDateFormat("ddMMyy").format(calendar.getTime()));
				getStockService().saveKeyValue(keyValue);
			}
		}
	}
	
	public boolean importFile(final Calendar calendar, final String stockExchange) throws Exception {
		boolean dataImported = false;
		// NYSE_20091202.csv
		final String fileName = stockExchange +"_" +new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()) +".csv";
		final String sPath = getDownloadFolder() +"/"+ fileName;

		if( new File(sPath).exists() ){
			dataImported = true;
			final LineNumberReader reader = new LineNumberReader(new FileReader(sPath));
			String line = reader.readLine(); // ignore first line.
			String[] columns;
			System.out.println( "Importing ..." );
			while( (line = reader.readLine()) != null ){
				columns = line.split("[,]", -1);
				
				NysePK nysePK = new NysePK(calendar.getTime(), columns[0]);
				Nyse nyse = new Nyse();
				nyse.setNysePK(nysePK);
				
				nyse.setOpen(new Double(columns[2]));
				nyse.setHigh(new Double(columns[3]));
				nyse.setLow(new Double(columns[4]));
				nyse.setClose(new Double(columns[5]));
				nyse.setVolume(new Long(columns[6]));
				
				getNyseDao().save(nyse);
			}
			System.out.println( stockExchange + " Data imported for " +calendar.getTime() );
		}else{
			System.out.println( "No data for " +calendar.getTime() );
		}
		
		return dataImported;
	}
}