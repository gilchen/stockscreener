package com.stocks.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.AggregateInformationDetailsDao;
import com.stocks.model.AggregateInformationDetails;
import com.stocks.model.AggregateInformationDetailsPK;
import com.stocks.util.Utility;

public class AggregateInformationDetailsDaoImpl extends AbstractDao implements AggregateInformationDetailsDao {
	private static final String JAVA_OBJECT_REPO_PATH;
	static Properties properties = new Properties();	
	static{
		FileInputStream fis = null;
		try{
			fis = new FileInputStream( System.getProperty("propsFilePath") );
			properties.load( fis );
			JAVA_OBJECT_REPO_PATH = properties.getProperty("java.object.repo.path");
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Exception in loading properties: " +e);
		}
		finally{
			if( fis != null ){
				try{
					fis.close();
				}
				catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException("Exception in closing properties file: " +e);
				}
			}
		}
	}
	
	
	public void delete(AggregateInformationDetails aggregateInformationDetails) {
		throw new RuntimeException("Not Implemented.");
	}

	public List<AggregateInformationDetails> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public List<AggregateInformationDetails> findAggregateInformationDetailsBySymbol(final String symbol){
		List<AggregateInformationDetails> results = new ArrayList<AggregateInformationDetails>();
		final File repoFolder = new File(JAVA_OBJECT_REPO_PATH + symbol + "/");
		String[] files = repoFolder.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(symbol+"_");
			}
		});
		
		for( final String fileName : files ){
			try{
				results.add( (AggregateInformationDetails) Utility.readObjectFromDisk(JAVA_OBJECT_REPO_PATH + symbol + "/" +fileName) );
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return results;
	}
	
	public AggregateInformationDetails read(AggregateInformationDetailsPK aggregateInformationDetailsPK) {
		try{
			final AggregateInformationDetails aggregateInformationDetails = (AggregateInformationDetails) Utility.readObjectFromDisk(JAVA_OBJECT_REPO_PATH + aggregateInformationDetailsPK.getSymbol() + "/" +getFileName(aggregateInformationDetailsPK));
			return aggregateInformationDetails;
		}
		catch(FileNotFoundException e){
			return null;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public void save(AggregateInformationDetails aggregateInformationDetails) {
		try{
			Utility.saveObjectToDisk(JAVA_OBJECT_REPO_PATH + aggregateInformationDetails.getAggregateInformationDetailsPK().getSymbol() + "/" + getFileName(aggregateInformationDetails.getAggregateInformationDetailsPK()), aggregateInformationDetails);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	private String getFileName(final AggregateInformationDetailsPK aggregateInformationDetailsPK){
		String fileName = aggregateInformationDetailsPK.getSymbol() +"_"+ Utility.getStrDate(aggregateInformationDetailsPK.getTradeDate(), "MM_dd_yyyy") +".ser";
		return fileName;
	}
}
