package com.stocks;

import java.io.File;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import com.stocks.util.Utility;

public class Statistics {
	static final String RPT_FILE = "C:/Classroom/JSF/int_ref/workspace/trunk/jasper_reports/D_Analysis_Percent_Move_GroupOnly_Main.jrxml";
	static final String EXPORT_FOLDER = "C:/Temp/stk/Analysis/reports/tmp/";
	static final int MAX_WEEKS_IN_GROUP = 20;
	static Double EXPECTED_GAIN_PERCENT = 0.60;
	static final Integer[] iSuccessPercent = new Integer[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
	static final Double[] dExpectedGainPercent = new Double[]{0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90, 1.00};
	
	static Comparator comparator = new Comparator(){
		public int compare(Object o1, Object o2) {
			KeyValDetails kvd1 = (KeyValDetails) o1;
			KeyValDetails kvd2 = (KeyValDetails) o2;
			
			int ret = 0;
			if( kvd1.value.intValue() < kvd2.value.intValue() ){
				ret = -1;
			}else if( kvd1.value.intValue() == kvd2.value.intValue() ){
				ret = 0;
			}else if( kvd1.value.intValue() > kvd2.value.intValue() ){
				ret = 1;
			}
			return ret;
		}
	};
	
	public static void main(String args[]) throws Exception{
		long start = System.currentTimeMillis();
		Statistics stats = new Statistics();
		Connection con = stats.getConnection();

		try{
			// Another level of filtering to pick the right one for a weekday.
			for( Double percent : dExpectedGainPercent ){
				EXPECTED_GAIN_PERCENT = percent;
				stats.exportCycleResults(con);
			}
		}finally{
			stats.closeResource(null, null, con);
		}
		
		long end = System.currentTimeMillis();
		System.out.println( "Process Completed in " + Utility.round(( (double)(end - start)/1000.0)/60.0)+ " mins. " );
	}

	// Recursively deletes path (including all files and folders under it.)
	private void doDelete(File path) throws IOException{
		if( path.exists() ){
			if(path.isDirectory()) {
				for (File child : path.listFiles()) {
					doDelete(child);
				}
			}
	
			if (!path.delete()) {
				throw new IOException("Could not delete " + path);
			}
		}else{
			System.out.println( "Warning: Path not found." );
		}
	}
	
	// Comment starts here
	private void exportCycleResults(Connection con) throws Exception{
		// Step: Cleanup the folder.
		doDelete( new File(EXPORT_FOLDER) );
		System.out.println( EXPORT_FOLDER + " deleted." );
		
		final StringBuffer exportCycleSimulationReport = new StringBuffer();
		exportCycleSimulationReport.append( "<html><body>" );
		
		for( Integer percent : iSuccessPercent ){
			exportCycleSimulationReport.append( "<span style='background-color:" +getBgColor("_" +percent+ "%")+ "'>" +percent+ "</span> &nbsp;" );
		}
		exportCycleSimulationReport.append( "\n<table border='1' cellspacing='0'>" );
		
		boolean bMoreIterationsRequired = true;
		for(int iteration=0; (bMoreIterationsRequired = execute(con, iteration)); iteration++){
			System.out.println( "\titeration: " +iteration );

			// Simulate here based on next_week_summary<iteration>.txt.
			// Step 1: Read next_week_strategy<iteration>.txt
			final StringBuffer nextWeekStrategy = new StringBuffer();
			String nextWeekStrategyFileName = EXPORT_FOLDER+"/"+"next_week_strategy" +iteration+ ".txt";
			LineNumberReader reader = new LineNumberReader(new FileReader( nextWeekStrategyFileName ) );
			String line = null;
			while( (line = reader.readLine()) != null ){
				nextWeekStrategy.append( line ).append("\n");
			}
			reader.close();
			
			// Step 2: Read the strategy.txt file
			final StringBuffer strategyFileContent = new StringBuffer();
			String strategyFileName = EXPORT_FOLDER+iteration+"/strategy.txt";
			reader = new LineNumberReader(new FileReader( strategyFileName ) );
			line = null;
			while( (line = reader.readLine()) != null ){
				strategyFileContent.append( line ).append("\n");
			}
			reader.close();
			
			// Step 3: Initialize List<NysePick> based on week's strategy
			final List<NysePick> nysePickList = new LinkedList<NysePick>();
			final List<String> keysForStrategy = new LinkedList<String>();
			
			String[] nextWeekStrategyArray = nextWeekStrategy.toString().split("\n");
			for( int i=0; i<nextWeekStrategyArray.length; i++ ){
				String lineStartsWith = nextWeekStrategyArray[i].split(",")[0];
				keysForStrategy.add( lineStartsWith );
				
				int start = strategyFileContent.indexOf(lineStartsWith);
				int end   = strategyFileContent.indexOf("\n", start);
				String matchingStrategyLine = strategyFileContent.substring( start, end );
				String[] thisWeekStrategy = matchingStrategyLine.split("\\$");

				NysePick nysePick = new NysePick(thisWeekStrategy[i+1]);
				//System.out.println( "--> " +nysePick );
				nysePickList.add(nysePick);
			}
			
			// Step 4: See how the strategy worked out.
			simulateForCycle(con, nysePickList, exportCycleSimulationReport, keysForStrategy);

		}
		
		exportCycleSimulationReport.append( "</table></body></html>" );
		
		FileWriter writer = new FileWriter( EXPORT_FOLDER+"/../ExportCycleSimulationReport (" +EXPECTED_GAIN_PERCENT+ ").html" );
		writer.append( exportCycleSimulationReport.toString() );
		writer.close();

	}

	private void simulateForCycle(Connection con, List<NysePick> nysePickList, StringBuffer simulationReport, List<String> keysForStrategy) throws Exception{
		simulationReport.append( "<tr>" );
		NysePick npBuy = null, npSell = null;
		for( int i=0; i<nysePickList.size(); i++ ){
			simulationReport.append( "<td " );
			npBuy = nysePickList.get(i);
			simulationReport.append( "title='" +npBuy.toString()+ " => " +keysForStrategy.get(i)+ "' " );
			
			Nyse nyseBuy = getResult( con, npBuy.buyDate, npBuy.symbol);
			if( nyseBuy == null ){
				// Friday Holiday
				Calendar c = Calendar.getInstance();
				c.setTime( npBuy.buyDate );
				
				while( nyseBuy == null ){
					c.add( Calendar.DATE, -1);
					nyseBuy = getResult( con, c.getTime(), npBuy.symbol);
				}
			}
			
			Double expectedGain = nyseBuy.close + (nyseBuy.close * (EXPECTED_GAIN_PERCENT/100.0));
			
			if( nysePickList.size() > (i+1) ){
				npSell = nysePickList.get(i+1);
			}else{
				npSell = nysePickList.get(i);
				npSell = new NysePick(npSell.symbol, npSell.successPercent, new Date( npSell.buyDate.getTime() ));
				
				Calendar c = Calendar.getInstance();
				c.setTime( npSell.buyDate );
				c.add( Calendar.DATE, 1);
				
				npSell.buyDate = c.getTime();
			}
			
			Nyse nyseSell = getResult( con, npSell.buyDate, npBuy.symbol);

			if( nyseSell == null ){
				simulationReport.append( "><B>Holiday</B>" );
			}else{
				double potential = ((nyseSell.high - nyseBuy.close)/nyseBuy.close)*100.0;
				simulationReport.append( " style='color:" +getBgColor(keysForStrategy.get(i))+ "'" );
				if( expectedGain > nyseSell.low && expectedGain < nyseSell.high ){
					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR>Profit (As expected): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ Utility.round(expectedGain) );
					simulationReport.append( "&nbsp;Potential (" +Utility.round(potential)+ "%)");
				}else if( expectedGain < nyseSell.low ){
					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR>Profit (<B>Beyond Expectations</B>): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.low );
					simulationReport.append( "&nbsp;Potential (" +Utility.round(potential)+ "%)");
				}else{
					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR><span style='background-color:red'>Loss:</span> Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.close );
					double totalLoss = ((nyseBuy.close - nyseSell.close)/nyseSell.close)*100.0;
					simulationReport.append( "&nbsp;Total Loss (" +Utility.round(totalLoss)+ "%)");
				}
			}
			
			simulationReport.append( "</td>\n" );
		}
		simulationReport.append( "<tr>\n" );
	}
	
	private String getBgColor(String keysForStrategy){
		// 20wk_100%
		String bgColor = "";
		if( keysForStrategy.indexOf( "_10%" ) != -1 ){
			bgColor = "#000000";
		}else if( keysForStrategy.indexOf( "_20%" ) != -1 ){
			bgColor = "#000099";
		}else if( keysForStrategy.indexOf( "_30%" ) != -1 ){
			bgColor = "#009900";
		}else if( keysForStrategy.indexOf( "_40%" ) != -1 ){
			bgColor = "#009999";
		}else if( keysForStrategy.indexOf( "_50%" ) != -1 ){
			bgColor = "#990000";
		}else if( keysForStrategy.indexOf( "_60%" ) != -1 ){
			bgColor = "#990099";
		}else if( keysForStrategy.indexOf( "_70%" ) != -1 ){
			bgColor = "#999900";
		}else if( keysForStrategy.indexOf( "_80%" ) != -1 ){
			bgColor = "#999999";
		}else if( keysForStrategy.indexOf( "_90%" ) != -1 ){
			bgColor = "#3399FF";
		}else if( keysForStrategy.indexOf( "_100%" ) != -1 ){
			bgColor = "#FF00FF";
		}
		return bgColor;
	}

	
	private boolean execute(Connection con, int simulationIteration) throws Exception{
		boolean bMoreIterationsRequired = true;
		// Step1: Generate required data till today.
		if( !generateWeeklyReport(simulationIteration, con) ){
			return false;
		}

		// Get list of folders that contain .csv files 
		String[] folders = new File(EXPORT_FOLDER).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return new File( dir.getAbsolutePath()+"/"+name ).isDirectory();
			}
		});
		
		List<String> folderList = Arrays.asList(folders);
		Collections.sort(folderList, new Comparator(){
			public int compare(Object o1, Object o2) {
				return new Integer(o1.toString()).compareTo( new Integer(o2.toString() ) );
			}
		});


		// Step 2: Generate strategies for each .csv file
		for(String folderName : folderList){
			//System.out.println( folderName );
			if( !new File( EXPORT_FOLDER+"/"+folderName+"/strategy.txt" ).exists() ){
				generateStrategy(EXPORT_FOLDER+"/"+folderName, con);
			}
		}

		// Step 3: Run/Simulate Strategies.
		runStrategy(con);
		
		// Step 4: Conclude best strategy
		LineNumberReader reader = new LineNumberReader(new FileReader( EXPORT_FOLDER+"summary.txt" ) );
		String line = null;
		
		final List<Integer> mondayCollection = new ArrayList<Integer>();
		final List<Integer> tuesdayCollection = new ArrayList<Integer>();
		final List<Integer> wednesdayCollection = new ArrayList<Integer>();
		final List<Integer> thursdayCollection = new ArrayList<Integer>();
		final List<Integer> fridayCollection = new ArrayList<Integer>();
		
		final List<String> summaryCollection = new LinkedList<String>();
		
		while( (line = reader.readLine()) != null ){
			// 2wk_50%,12/20 (60%),13/21 (61%),16/21 (76%),15/20 (75%),16/21 (76%)
			summaryCollection.add( line );
			
			String[] weekResults = line.split(",");
			mondayCollection.add( new Integer(    weekResults[1].substring( weekResults[1].indexOf("(")+1, weekResults[1].indexOf("%") ) ) );
			tuesdayCollection.add( new Integer(   weekResults[2].substring( weekResults[2].indexOf("(")+1, weekResults[2].indexOf("%") ) ) );
			wednesdayCollection.add( new Integer( weekResults[3].substring( weekResults[3].indexOf("(")+1, weekResults[3].indexOf("%") ) ) );
			thursdayCollection.add( new Integer(  weekResults[4].substring( weekResults[4].indexOf("(")+1, weekResults[4].indexOf("%") ) ) );
			fridayCollection.add( new Integer(    weekResults[5].substring( weekResults[5].indexOf("(")+1, weekResults[5].indexOf("%") ) ) );
		}
		reader.close();
		
		final Integer maxMonday = Collections.max( mondayCollection );
		final Integer maxTuesday = Collections.max( tuesdayCollection );
		final Integer maxWednesday = Collections.max( wednesdayCollection );
		final Integer maxThursday = Collections.max( thursdayCollection );
		final Integer maxFriday = Collections.max( fridayCollection );

		final StringBuffer nextWeekStrategy = new StringBuffer();
		for( int i=0; i<5; i++ ){
			for( int j=summaryCollection.size(); j>0; j-- ){
				final String summary = summaryCollection.get(j-1);
				String[] weekResults = summary.split(",");
				if( i == 0 ){ // Monday
					if( weekResults[1].indexOf( maxMonday+"%" ) != -1 ){
						//System.out.println( "Monday: " +summary );
						nextWeekStrategy.append( summary ).append("\n");
						break;
					}
				}else if( i == 1 ){ // Tuesday
					if( weekResults[2].indexOf( maxTuesday+"%" ) != -1 ){
						//System.out.println( "Tuesday: " +summary );
						nextWeekStrategy.append( summary ).append("\n");
						break;
					}
				}else if( i == 2 ){ // Wednesday
					if( weekResults[3].indexOf( maxWednesday+"%" ) != -1 ){
						//System.out.println( "Wednesday: " +summary );
						nextWeekStrategy.append( summary ).append("\n");
						break;
					}
				}else if( i == 3 ){ // Thursday
					if( weekResults[4].indexOf( maxThursday+"%" ) != -1 ){
						//System.out.println( "Thursday: " +summary );
						nextWeekStrategy.append( summary ).append("\n");
						break;
					}
				}else if( i == 4 ){ // Friday
					if( weekResults[5].indexOf( maxFriday+"%" ) != -1 ){
						//System.out.println( "Friday: " +summary );
						nextWeekStrategy.append( summary ).append("\n");
						break;
					}
				}
			}
		}
		
		FileWriter writer = new FileWriter( EXPORT_FOLDER+"/next_week_strategy" +simulationIteration+ ".txt" );
		writer.append( nextWeekStrategy.toString() );
		writer.close();
		
		return bMoreIterationsRequired;
	}
	
	/* Step 1
		1. Execute "D_Analysis_Percent_Move_GroupOnly_Main.jrxml" for following date ranges (2 weeks to 20 weeks = 10 reports)
		
		File name should be iteration/StartDate-EndDate.csv
		
		20wk-------------
		18wk  -----------
		...
		8wk      --------
		6wk        ------
		4wk          ----
		2wk            --
	 */
	private boolean generateWeeklyReport(int iteration, Connection con) throws Exception{
		boolean bMoreIterationsRequired = true;
		Calendar cStartDate = Calendar.getInstance();
		cStartDate.set(Calendar.DATE, 1);
		cStartDate.set(Calendar.MONDAY, Calendar.MARCH);
		cStartDate.set(Calendar.YEAR, 2010);

		Calendar cEndDate = (Calendar) cStartDate.clone();
		cEndDate.add(Calendar.DATE, ((MAX_WEEKS_IN_GROUP*7)-3) +(iteration * 7)); 
		// TODO: As of now cEndDate is relative to 03/01/2010. 
		// You can make it relative to today. e.g. MAX_WEEKS_IN_GROUP (20 weeks) before today.
		// This way, analysis will not be done based on piled-up data. It will be done based on 
		// trailing data instead.
		// BUT DO THIS AFTER 01/01/2011 only when there is 20 weeks of data available to consider.
		// Following code will be useful. Please retest.
		//Calendar cToday = Calendar.getInstance();
		//cToday.add(Calendar.DATE, -(MAX_WEEKS_IN_GROUP*7));
		//cToday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		//cEndDate.add(Calendar.DATE, (iteration * 7));
		//
		
//		if( Utility.areDatesEqual(cEndDate.getTime(), Calendar.getInstance().getTime() ) || 
//				cEndDate.before( Calendar.getInstance() ) ){
		
		Calendar cFridayOfThisWeek = Calendar.getInstance();
		cFridayOfThisWeek.set( Calendar.DAY_OF_WEEK, Calendar.FRIDAY );
		
		if( Utility.areDatesEqual(cEndDate.getTime(), cFridayOfThisWeek.getTime() ) || 
				cEndDate.before( cFridayOfThisWeek ) ){ // If endDate is before or equal to Friday of this week.
			cStartDate.setTime( cEndDate.getTime() );
			cStartDate.add(Calendar.DATE, 3);
	
			int step = 2;
			for( int numberOfWeeks=2; numberOfWeeks<=MAX_WEEKS_IN_GROUP; numberOfWeeks+=step ){
				cStartDate.add(Calendar.DATE, -(7*step));
				
				String exportFolder = EXPORT_FOLDER+iteration+"/";
				File file = new File(exportFolder);
				if( !file.exists() ){
					file.mkdirs();
				}
				generateReports( con, cStartDate.getTime(), cEndDate.getTime(), exportFolder, numberOfWeeks );
			}
		}else{
			bMoreIterationsRequired = false;
		}
		
		return bMoreIterationsRequired;
	}
	
	private void generateReports(Connection con, Date startDate, Date endDate, String exportFolder, int numberOfWeeks) throws Exception {
		String exportFileName = exportFolder+new java.text.SimpleDateFormat("MM_dd_yyyy").format( startDate )+"-"+new java.text.SimpleDateFormat("MM_dd_yyyy").format( endDate )+ "$" +numberOfWeeks+"wks.csv";
		Map params = new HashMap();
		
		params.put("pStartDate", startDate);
		params.put("pEndDate", endDate);
		params.put("pPercentMove", new Double(EXPECTED_GAIN_PERCENT.toString()));
		params.put("SUBREPORT_DIR", "C:\\Program Files\\JasperSoft\\iReport-3.0.0\\");
		
		JasperDesign design = JRXmlLoader.load( RPT_FILE );
		design.setIgnorePagination(true);
		JasperReport report = JasperCompileManager.compileReport(design);
		JasperPrint print = JasperFillManager.fillReport(report, params, con);
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, new File(exportFileName));
		exporter.exportReport();
		//System.out.println( "Date Range: " +getStrDate(startDate)+"-"+getStrDate(endDate) );
		//System.out.println( "CSV Generated: " +exportFileName );
	}
	
	private void generateStrategy(String csvFolder, Connection con) throws Exception{
		String[] files = new File(csvFolder).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		
		List<String> fileList = Arrays.asList( files );
		
		// Below sorting is required so that files with least data is processed first followed by others.
		Collections.sort(fileList, new Comparator(){
			public int compare(Object o1, Object o2) {
				String s1 = (String) o1;
				String s2 = (String) o2;
				Integer index1 = Integer.parseInt( s1.substring( s1.indexOf("$")+1, s1.indexOf("wks.csv")) );
				Integer index2 = Integer.parseInt( s2.substring( s2.indexOf("$")+1, s2.indexOf("wks.csv")) );

				return index1.compareTo(index2);
			}
		});
		//System.out.println( fileList );
		
		List<NysePick> strategyList = null;

		StringBuffer strategyBuffer = new StringBuffer("DataLength_Percent$Symbol,BuyOn,Success%$..");
		for( String file : fileList ){
			//System.out.println( "-> " + file );
			for( Integer percent : iSuccessPercent ){
				strategyList = getStrategy( csvFolder+"/"+file, percent );

				String dataLength = file.substring( file.indexOf("$")+1 , file.indexOf("wks.csv"));
				strategyBuffer.append("\n").append(dataLength+"wk_"+percent+"%");
				for(final NysePick nysePick : strategyList){
					strategyBuffer.append("$").append( nysePick.toString());
				}
			}
		}
		
		FileWriter writer = new FileWriter( csvFolder+"/strategy.txt" );
		writer.append( strategyBuffer.toString() );
		writer.close();
	}

	private void runStrategy(Connection con) throws Exception{
		// Step 1: Get List of all folders that contain strategy.txt
		String[] folders = new File(EXPORT_FOLDER).list(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return new File( dir.getAbsolutePath()+"/"+name ).isDirectory();
			}
		});
		
		List<String> folderList = Arrays.asList(folders);
		Collections.sort(folderList, new Comparator(){
			public int compare(Object o1, Object o2) {
				return new Integer(o1.toString()).compareTo( new Integer(o2.toString() ) );
			}
		});
		
		// Step 2: Cache strategy.txt in a Map.
		// 0={strategy.txt}, 1={strategy.txt}
		LinkedHashMap<Integer, StringBuffer> strategyMap = new LinkedHashMap<Integer, StringBuffer>();
		for( String folder : folderList ){
			Integer key = new Integer(folder);
			strategyMap.put(key, new StringBuffer());
			
			String strategyFileName = EXPORT_FOLDER+folder+"/"+"strategy.txt";
			LineNumberReader reader = new LineNumberReader(new FileReader( strategyFileName ) );
			String line = null;
			while( (line = reader.readLine()) != null ){
				strategyMap.get(key).append( line ).append("\n");
			}
			reader.close();
		}
		
		// Step 3: Run Strategy
		StringBuffer sbSummaryOnly = new StringBuffer();

		int step = 2;
		for( int numberOfWeeks=2; numberOfWeeks<=MAX_WEEKS_IN_GROUP; numberOfWeeks+=step ){
			for( Integer percent : iSuccessPercent ){
				final Map<Integer, Integer> mTotalTradingDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalTradingDays
				final Map<Integer, Integer> mTotalSuccessDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalSuccessDays
				final StringBuffer simulationReport = new StringBuffer();
				simulationReport.append( "<html><body><table border='1'>\n" );
				
				String lineStartsWith = numberOfWeeks +"wk_"+ percent +"%"; //2wk_50%
				// Iterate thru each folder for the given Key.
				for( String folder : folderList ){
					Integer key = new Integer(folder);
					StringBuffer strategyFileContent = strategyMap.get(key);
					int start = strategyFileContent.indexOf(lineStartsWith);
					int end   = strategyFileContent.indexOf("\n", start);
					String matchingStrategyLine = strategyFileContent.substring( start, end );
					String[] thisWeekStrategy = matchingStrategyLine.split("\\$");

					final List<NysePick> nysePickList = new LinkedList<NysePick>();
					for( int i=0; i<thisWeekStrategy.length; i++ ){
						if( i == 0 ){
							continue;
						}else{
							NysePick nysePick = new NysePick(thisWeekStrategy[i]);
							//System.out.println( "--> " +nysePick );
							nysePickList.add(nysePick);
						}
					}

					simulate(con, nysePickList, mTotalTradingDays, mTotalSuccessDays, simulationReport);
				}
				
				// Generate Summary here.
				sbSummaryOnly.append( lineStartsWith );

				simulationReport.append("<tr>\n");
				for(int i=0; i<5; i++){
					simulationReport.append( "<td>" );
					int totalTradingDays = 0;
					if(mTotalTradingDays.get(i) != null){
						totalTradingDays = mTotalTradingDays.get(i);
					}
					
					int totalSuccessDays = 0;
					if( mTotalSuccessDays.get(i) != null){
						totalSuccessDays = mTotalSuccessDays.get(i);
					}
					String summary = totalSuccessDays +"/"+totalTradingDays+ " (" +(int)(((double)totalSuccessDays/(double)totalTradingDays)*100.0)+ "%)";
					simulationReport.append( summary );
					simulationReport.append( "</td>" );
					
					sbSummaryOnly.append(",").append( summary );
				}
				sbSummaryOnly.append("\n");
				simulationReport.append( "</tr>\n" );
				simulationReport.append( "</table></body></html>\n" );
				
				FileWriter writer = new FileWriter( EXPORT_FOLDER+lineStartsWith+".html" );
				writer.append( simulationReport.toString() );
				writer.close();
			}
		}
		
		FileWriter writer = new FileWriter( EXPORT_FOLDER+"summary.txt" );
		writer.append( sbSummaryOnly.toString() );
		writer.close();
	}
	
	private void simulate(Connection con, List<NysePick> nysePickList, Map<Integer, Integer> mTotalTradingDays, Map<Integer, Integer> mTotalSuccessDays, StringBuffer simulationReport) throws Exception{
		simulationReport.append( "<tr>" );
		NysePick npBuy = null, npSell = null;
		for( int i=0; i<nysePickList.size(); i++ ){
			simulationReport.append( "<td " );
			npBuy = nysePickList.get(i);
			simulationReport.append( "title='" +npBuy.toString()+ "' " );
			
			Nyse nyseBuy = getResult( con, npBuy.buyDate, npBuy.symbol);
			if( nyseBuy == null ){
				// Friday Holiday
				Calendar c = Calendar.getInstance();
				c.setTime( npBuy.buyDate );
				
				while( nyseBuy == null ){
					c.add( Calendar.DATE, -1);
					nyseBuy = getResult( con, c.getTime(), npBuy.symbol);
				}
			}
			
			Double expectedGain = nyseBuy.close + (nyseBuy.close * (EXPECTED_GAIN_PERCENT/100.0));
			
			if( nysePickList.size() > (i+1) ){
				npSell = nysePickList.get(i+1);
			}else{
				npSell = nysePickList.get(i);
				npSell = new NysePick(npSell.symbol, npSell.successPercent, new Date( npSell.buyDate.getTime() ));
				
				Calendar c = Calendar.getInstance();
				c.setTime( npSell.buyDate );
				c.add( Calendar.DATE, 1);
				
				npSell.buyDate = c.getTime();
			}
			
			Nyse nyseSell = getResult( con, npSell.buyDate, npBuy.symbol);

			if( nyseSell == null ){
				simulationReport.append( "><B>Holiday</B>" );
			}else{
				Integer totalTradingDays = mTotalTradingDays.get(i);
				if( totalTradingDays == null ){
					totalTradingDays = new Integer(0);
				}
				mTotalTradingDays.put(i, totalTradingDays.intValue()+1);

				Integer totalSuccessDays = mTotalSuccessDays.get(i);
				if( totalSuccessDays == null ){
					totalSuccessDays = new Integer(0);
				}

				if( expectedGain > nyseSell.low && expectedGain < nyseSell.high ){
					mTotalSuccessDays.put(i, totalSuccessDays.intValue()+1);
					
					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR>Profit (As expected): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ expectedGain );
				}else if( expectedGain < nyseSell.low ){
					mTotalSuccessDays.put(i, totalSuccessDays.intValue()+1);

					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR>Profit (<B>Beyond Expectations</B>): Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.low );
				}else{
					simulationReport.append( ">Buy " +nyseBuy.symbol+ " (" +npBuy.successPercent+ "%) " +" on " +getStrDate( nyseBuy.tradeDate )+ " @"+ nyseBuy.close );
					simulationReport.append( "<BR><span style='background-color:red'>Loss:</span> Sell " +nyseSell.symbol+ " on " +getStrDate( nyseSell.tradeDate )+ " @"+ nyseSell.close );
				}
			}
			
			simulationReport.append( "</td>\n" );
		}
		simulationReport.append( "<tr>\n" );
	}

	private List<NysePick> getStrategy(String fileName, int percent) throws Exception {
		String str[] = new String[2];
		LineNumberReader lnr = new LineNumberReader( new FileReader( fileName ) );
		str[0] = lnr.readLine();
		str[1] = lnr.readLine();
		
		String line = null;
		List<KeyValDetails> l1 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l2 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l3 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l4 = new ArrayList<KeyValDetails>();
		List<KeyValDetails> l5 = new ArrayList<KeyValDetails>();
		
		while( (line = lnr.readLine()) != null ){
			String[] arrLine = line.split("[,]", -1);
			l1.add( new KeyValDetails(arrLine[1], (arrLine[2] != null && !arrLine[2].equals("null") ? new Integer(arrLine[2]) : 0), "") );
			l2.add( new KeyValDetails(arrLine[1], (arrLine[3] != null && !arrLine[3].equals("null") ? new Integer(arrLine[3]) : 0), "") );
			l3.add( new KeyValDetails(arrLine[1], (arrLine[4] != null && !arrLine[4].equals("null") ? new Integer(arrLine[4]) : 0), "") );
			l4.add( new KeyValDetails(arrLine[1], (arrLine[5] != null && !arrLine[5].equals("null") ? new Integer(arrLine[5]) : 0), "") );
			l5.add( new KeyValDetails(arrLine[1], (arrLine[6] != null && !arrLine[6].equals("null") ? new Integer(arrLine[6]) : 0), "") );
		}
		lnr.close();
		
		TreeSet ts1 = new TreeSet(comparator);
		ts1.addAll(l1);
		KeyValDetails o1 = getNPlus(ts1, percent);

		TreeSet ts2 = new TreeSet(comparator);
		ts2.addAll(l2);
		KeyValDetails o2 = getNPlus(ts2, percent);

		TreeSet ts3 = new TreeSet(comparator);
		ts3.addAll(l3);
		KeyValDetails o3 = getNPlus(ts3, percent);

		TreeSet ts4 = new TreeSet(comparator);
		ts4.addAll(l4);
		KeyValDetails o4 = getNPlus(ts4, percent);

		TreeSet ts5 = new TreeSet(comparator);
		ts5.addAll(l5);
		KeyValDetails o5 = getNPlus(ts5, percent);

		List<NysePick> list = new LinkedList<NysePick>();
		String[] s = str[1].split("[,]", -1);
		
		Calendar c = Calendar.getInstance();
		c.setTime( getDate(s[s.length-1]) );
		list.add( new NysePick( o1.key, o1.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 3);
		list.add( new NysePick( o2.key, o2.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o3.key, o3.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o4.key, o4.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);
		list.add( new NysePick( o5.key, o5.value, c.getTime() ) );
		
		c.add(Calendar.DATE, 1);

		return list;
	}
	
	private KeyValDetails getNPlus(TreeSet<KeyValDetails> ts1, int percent){
		Iterator<KeyValDetails> iterator = ts1.iterator();
		while(iterator.hasNext()){
			KeyValDetails o = iterator.next();
			if( o.value >= percent ){
				return o;
			}
		}
		return ts1.floor( new KeyValDetails("", percent, "") );
	}

	private Date getDate(String str) throws Exception {
		return new SimpleDateFormat("MM/dd/yyyy").parse( str );
	}
	
	private String getStrDate(Date dt) throws Exception{
		return new SimpleDateFormat("MM/dd/yyyy").format( dt );
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
	
	class KeyValDetails{
		String key;
		Integer value;
		String details;
		public KeyValDetails(String key, Integer value, String details) {
			super();
			this.key = key;
			this.value = value;
			this.details = details;
		}

		@Override
		public String toString() {
			return "Key: " +this.key+", Value: "+this.value+", Details: "+ this.details;
		}
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
		public NysePick(String toString){
			String[] arr = toString.split(",");
			this.symbol = arr[0];
			try{
				this.buyDate = getDate(arr[1]);
			}
			catch(Exception e){
				
			}
			this.successPercent = new Integer( arr[2] );
		}
		
		@Override
		public String toString(){
			String bd = null;
			try{
				bd = getStrDate(buyDate);
			}
			catch(Exception e){
			}
			//return "Buy " +symbol +" on " +bd+ " ("+ successPercent+ "% chances of success)";
			return symbol +","+ bd +","+ successPercent;
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
