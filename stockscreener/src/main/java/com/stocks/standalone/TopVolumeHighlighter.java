package com.stocks.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.josql.Query;
import org.josql.QueryResults;

import com.stocks.util.Utility;

public class TopVolumeHighlighter {
	static Properties properties = new Properties();
	static{
		try{
			properties.load( TopVolumeHighlighter.class.getResourceAsStream("/TopVolumeHighlighter.properties") );
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Exception in loading properties: " +e);
		}
	}
	
	final static List<Double> Y_LABEL_POS = Arrays.asList(new Double[]{0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50, 0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90, 0.95});

	final static int INDX_D = 0;
	final static int INDX_C = 1;
	final static int INDX_H = 2;
	final static int INDX_L = 3;
	final static int INDX_O = 4;
	final static int INDX_V = 5;
	final static Long DATE_OFFSET = 1000L;
	final static String CHART_HTML = "\n<DIV id='~DATE' style='visibility:hidden;position:absolute;top:10;left:10'>" +
			"\n<applet name='applet~DATE' code='com.objectplanet.chart.ChartApplet' archive='chart.jar' width='1000' height='350'>" +
			"\n<param name='zoomon' value='true'>" +
			//"\n<param name='doubleBufferingOff' value='true'>" +
			"\n<param name='chart' value='line'>" +
			"\n<param name='chartTitle' value='~SYMBOL'>" +
			"\n<param name='sampleValues_0' value='~CLOSELIST'>" +
			"\n<param name='sampleValues_1' value='~VOLUMELIST'>" +
			"\n<param name='sampleLabels' value='~INDICES'>" +
			"\n<param name='seriesRange_0' value='2'>" +
			"\n<param name='sampleLabelsOn' value='true'>" +
			"\n<param name='sampleLabelStyle' value='floating'>" +
			"\n<param name='floatingLabelFont' value='Verdana, plain, 10'>" +
			"\n<param name='sampleColors' value='blue, red'>" +
			"\n<param name='sampleHighlightOn' value='true'>" +
			"\n<param name='sampleHighlightStyle' value='circle_opaque'>" +
			"\n<param name='sampleHighlightSize' value='4'>" +
			"\n<param name='rangeColor' value='red'>" +
			"\n<param name='rangeColor_2' value='blue'>" +
			"\n<param name='seriesCount' value='2'>" +
			"\n<param name='valueLabelsOn' value='true'>" +
			"\n<param name='valueLabelStyle' value='floating'>" +
			"\n<param name='valueLinesOn' value='true'>" +
			"\n<param name='defaultGridLinesOn' value='true'>" +
			
			"<!-- LABELS -->" +

			"\n<param name='legendOn' value='true'>" +
			"\n<param name='legendPosition' value='top'>" +
			"\n<param name='legendLabels' value='Close,Volume'>" +
			"\n<param name='rangeOn_2' value='true'>" +
			"\n<param name='range_2' value='~HIGHEST_CLOSE'>" +
			"\n<param name='lowerRange_2' value='~LOWEST_CLOSE'>" +
			
			"\n<param name='range_1' value='~HIGHEST_VOL'>" +
			"\n<param name='lowerRange_1' value='~LOWEST_VOL'>" +
			"\n<param name='lineWidth' value='1,1'>" +

			
			"\n<param name='rangePosition' value='right'>" +
			"\n<param name='rangePosition_2' value='left'>" +
			"\n<param name='rangeAxisLabel' value='Volume'>" +
			"\n<param name='rangeAxisLabelFont' value='Verdana, bold, 16'>" +
			"\n<param name='rangeAxisLabelAngle' value='90'>" +
			"\n<param name='rangeAxisLabel_2' value='Close'>" +
			"\n<param name='rangeAxisLabelAngle_2' value='270'>" +
			"\n<param name='rangeLabelPrefix_2' value='$'>" +
			"\n<param name='multiSeriesOn' value='true'>" +
			"\n<param name='rangeDecimalCount' value='0'>" +
			"\n<param name='rangeDecimalCount_2' value='2'>" +
			"\n<param name='sampleDecimalCount' value='2'>" +
			"\n<param name='sampleDecimalCount_2' value='0'>" +
			"\n<param name='chartBackground' value='#DADAFF'>" +
			"\n<param name='sampleScrollerOn' value='true'>" +
			"\n<param name='visibleSamples' value='0,100'>" +
			"\n</applet><BR>" +
			"\n<input type=button value='Get Value for Alert' onclick='getValueForAlert(\"applet~DATE\")'>" +
			"\n</DIV>";

	public static void main(String... args) throws Exception{
		String[] symbolPipeDates = properties.getProperty("symbol.pipe.date").split(",");
		final StringBuilder sbSummaryOnly = new StringBuilder();
		//sbSummaryOnly.append( "<table border=1>" ).append("\n");
		sbSummaryOnly.append("Symbol,D Pressure(against Whole),P Pressure(against Whole),Total D Still to cover (CxV),Total P Still to realize (CxV),Pump is x Times Dump (CxV)").append("\n");
		for(String s : symbolPipeDates){
			final String[] symbolPipeDate = s.split("[|]");
			final String symbol = symbolPipeDate[0];
			final Date ignoreBeforeDate = Utility.getDateFor(symbolPipeDate[1], "MM/dd/yyyy");
			try{
				generateReport( symbol, ignoreBeforeDate, sbSummaryOnly );
			}
			catch(Exception e){
				System.out.println( "Exception in generating report for "+ symbol );
				e.printStackTrace();
			}
		}
		//sbSummaryOnly.append("\n").append( "</table>" );

		final String rptPath = properties.getProperty("rpt.folder") + "/rpt_Summary.csv";
		Utility.saveContent( rptPath, sbSummaryOnly.toString());
		
		
		System.out.println( "Done" );
	}
	
	private static void generateReport(String symbol, Date ignoreBeforeDate, StringBuilder sbSummaryOnly) throws Exception{
		int ignoreBeforeIndex = 0;
		
		//final String content = Utility.getContent( "file:///C:/Temp/ForSrid/tmp/" +symbol+ "-1M.txt" );
		final String content = Utility.getContent( properties.getProperty("google.url.intra.day").replaceAll("~SYMBOL", symbol) );
		try{
			Utility.saveContent( properties.getProperty("cache.folder")+"/"+symbol+".txt", content);
		}
		catch(Exception e){
			System.out.println( "Exception in saving "+symbol+ " to cache folder." );
		}
		final List<IntraDayStructure> idsList = getIntraDayStructureList(content);

		// Start
		final QueryResults qrDates = Query.parseAndExec("SELECT min(index), max(index) FROM com.stocks.standalone.IntraDayStructure group by formatDate( new java.sql.Date(time) )", new ArrayList<IntraDayStructure>(idsList) );
		final Map<ArrayList, List<List<Integer>>> resultsDates = qrDates.getGroupByResults();
		final Set<ArrayList> treeSet = new TreeSet<ArrayList>( new Comparator<ArrayList>(){
			@Override
			public int compare(ArrayList arg0, ArrayList arg1) {
				int ret = 0;
				try{
					ret = Utility.getDateFor(arg0.get(0).toString(), "dd/MMM/yyyy").compareTo( Utility.getDateFor(arg1.get(0).toString(), "dd/MMM/yyyy") );
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return ret;
			}
		});
		treeSet.addAll( resultsDates.keySet() );

		final StringBuilder sb = new StringBuilder();
		sb.append( "<html>" );
		sb.append( "<head>" );
		sb.append( "<script>" ).append("\n");
		sb.append( "var prevElement;" ).append("\n");
		sb.append( "function show(id){" ).append("\n");
		sb.append( "  if(prevElement && prevElement.style && prevElement.style.visibility){" ).append("\n");
		sb.append( "    prevElement.style.visibility = 'hidden';" ).append("\n");
		sb.append( "  }" ).append("\n");
		sb.append( "  document.getElementById( id ).style.visibility = 'visible';" ).append("\n");
		sb.append( "  prevElement = document.getElementById( id );" ).append("\n");
		sb.append( "}" ).append("\n");
		
		sb.append( "function setBackground(chkBoxObj, bgColor){" ).append("\n");
		sb.append( "	 chkBoxObj.parentNode.parentNode.style.backgroundColor = chkBoxObj.checked ? bgColor : '#FFFFFF';" ).append("\n");
		sb.append( "}" ).append("\n");

		sb.append( "function repaint(highlightOnly){" ).append("\n");
		sb.append( "	var radios = document.getElementsByTagName('input');" ).append("\n");
		sb.append( "	arr = new Array();" ).append("\n");
		sb.append( "	index = 0;" ).append("\n");
		sb.append( "	for (i = 0; i < radios.length; i++) {" ).append("\n");
		sb.append( "		if (radios[i].type == 'radio' && radios[i].checked) {" ).append("\n");
		sb.append( "			if(highlightOnly == 'All' || radios[i].value.charAt(0) == highlightOnly){").append("\n");
		sb.append( "				if( highlightOnly == 'All' || document.getElementById('c' +radios[i].name.substring(1)).checked ){").append("\n");
		sb.append( "					arr[index++] = radios[i].value;" ).append("\n");
		sb.append( "				}" ).append("\n");
		sb.append( "			}" ).append("\n");
		sb.append( "		}" ).append("\n");
		sb.append( "	}" ).append("\n");
		
		sb.append( "	if(arr.length < " +properties.getProperty("top.n")+ "){").append("\n");
		sb.append( "		for( i = arr.length; i<" +properties.getProperty("top.n")+ "; i++ ){").append("\n");
		sb.append( "			arr[i] = '';").append("\n");
		sb.append( "		}").append("\n");
		sb.append( "	}").append("\n");
		
		sb.append( "	for( i = 0; i<arr.length; i++ ){" ).append("\n");
		sb.append( "		document.appletAll.setParameter('label_' +(i+1), arr[i]);" ).append("\n");
		sb.append( "	}" ).append("\n");
		sb.append( "}" ).append("\n");
		
		sb.append( "function getValueForAlert(appletName){" ).append("\n");
		sb.append( "	chrt = document[appletName].chart;" ).append("\n");
		sb.append( "	selectedIndex = chrt.getLastSelectedSample();" ).append("\n");
		sb.append( "	if( selectedIndex != -1 ){" ).append("\n");
		sb.append( "		symbol = chrt.getTitle();" ).append("\n");
		sb.append( "		symbol = symbol.substring(0, symbol.indexOf(' '));" ).append("\n");
		sb.append( "		sample0 = chrt.getChartData().getSample(0, selectedIndex);" ).append("\n");
		sb.append( "		sample1 = chrt.getChartData().getSample(1, selectedIndex);" ).append("\n");
		sb.append( "		template = '<short_position>\\n\\t<symbol>~SYMBOL</symbol>\\n\\t<date>~DATE</date>\\n\\t<price>~PRICE</price>\\n\\t<volume>~VOL</volume>\\n\\t<description>Uncovered Short Position in ~SYMBOL @~PRICE on ~DATE Vol ~VOL Val $~VAL</description>\\n</short_position>';" ).append("\n");
		sb.append( "		template = template.replace( /~SYMBOL/g, symbol );" ).append("\n");
		sb.append( "		template = template.replace( /~DATE/g, getDateForIndex(sample0.getLabel()) );" ).append("\n");
		sb.append( "		template = template.replace( /~PRICE/g, sample0.getFloatValue() );" ).append("\n");
		sb.append( "		template = template.replace( /~VOL/g, sample1.getValue() );" ).append("\n");
		sb.append( "		template = template.replace( /~VAL/g, sample0.getFloatValue() * sample1.getValue() );" ).append("\n");
		sb.append( "		prompt( '', template );" ).append("\n");
		sb.append( "	}else{" ).append("\n");
		sb.append( "		alert('Please make a selection in Chart.');" ).append("\n");
		sb.append( "	}" ).append("\n");
		sb.append( "}" ).append("\n");
		
		sb.append( "function getDateForIndex(x){" ).append("\n");
		sb.append( "	for( i=0; i<arrExprDate.length; i++ ){" ).append("\n");
		sb.append( "		if( eval(arrExprDate[i][0]) ){" ).append("\n");
		sb.append( "			return arrExprDate[i][1];" ).append("\n");
		sb.append( "		}" ).append("\n");
		sb.append( "	}" ).append("\n");
		sb.append( "	return null;" ).append("\n");
		sb.append( "}" ).append("\n");
		
		sb.append( "</script>" ).append("\n");
		sb.append( "</head>" ).append("\n");
		sb.append( "<body>" ).append("\n");
		sb.append( "Report generated on <B>" ).append( new Date() ).append("</B>").append("\n");
		sb.append( "<P>This report reads IntraDay data from Google Finance. It generates Intra Day Charts for each day in the Past Month." ).append("\n");
		sb.append( "It lays out 1 min close and volume for each day. Clicking the date link will display 1 min IntraDay Chart for that day." ).append("\n");
		sb.append( "<B>Note</B>: There is a consolidated chart displayed in the end of this report. This chart displays close and volume for the entire month." ).append("\n");
		sb.append( "This chart is the combined version of all individual date charts together. It highlights top volumes only that are dumps or pumps." ).append("\n");

		final StringBuilder sbTabPanel = new StringBuilder();
		
		final List<String> javascriptToDetermineDateFromIndex = new ArrayList<String>();
		
		final int TD_PER_TR = 5;
		int tdCounter = 0;
		int considerAfterIndex = 0;
		for( final ArrayList key : treeSet ){
			final List<List<Integer>> minMax = resultsDates.get(key);
			final String sDate = key.get(0).toString().replaceAll("/", "_");
			
			//System.out.println( "\t" +sDate+ " -> " +minMax.get(0).get(0) +","+ minMax.get(0).get(1) );
			
			// Process SubLists
			int fromIndex = minMax.get(0).get(0)-1;
			int toIndex   = minMax.get(0).get(1);

			if( sDate.equalsIgnoreCase( Utility.getStrDate(ignoreBeforeDate, "dd_MMM_yyyy") ) ){
				ignoreBeforeIndex = fromIndex;
				considerAfterIndex = toIndex;
			}
			
			final List<IntraDayStructure> idsSubList = idsList.subList(fromIndex, toIndex);
			final QueryResults qrMinMax = Query.parseAndExec("SELECT min(volume), max(volume) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", new ArrayList<IntraDayStructure>(idsSubList) );
			final List<ArrayList<Long>> resultsMinMax = qrMinMax.getResults();
			final Long minVolume = resultsMinMax.get(0).get(0);
			final Long maxVolume = resultsMinMax.get(0).get(1);
			final String sDateReFormatted = Utility.getStrDate( Utility.getDateFor(sDate, "dd_MMM_yyyy") );
			final String chartHTML = processSubList(idsSubList, symbol, sDateReFormatted, minVolume, maxVolume, -1, -1);
			
			sb.append( chartHTML ).append( "<BR>" );
			
			if( ( tdCounter++ % TD_PER_TR ) == 0 ){
				sbTabPanel.append( "<BR>" );
				tdCounter = 1;
			}
			sbTabPanel.append( "<span title='" +fromIndex+ " - " +toIndex+ "'><a href='javascript:show(\"" +sDateReFormatted+ "\");'>" ).append( sDateReFormatted ).append( "</a></span> &nbsp; | &nbsp; " );
			javascriptToDetermineDateFromIndex.add( "x > " +fromIndex+ " && x <= " +toIndex+ ";"+sDateReFormatted );
		}
		sb.append( sbTabPanel );
		// End

		// ************************************ Consolidated Report *************************************** //
		
		// http://josql.sourceforge.net/manual/limit-clause.html
		QueryResults qrTopN = Query.parseAndExec("SELECT * FROM com.stocks.standalone.IntraDayStructure where index > " +considerAfterIndex+ " order by volume desc", new ArrayList<IntraDayStructure>(idsList) );
		final Integer iTopN = Integer.parseInt(properties.getProperty("top.n"));
		List<IntraDayStructure> results = qrTopN.getResults();
		if( results.isEmpty() ){ // In case the provided date is today.
			qrTopN = Query.parseAndExec("SELECT * FROM com.stocks.standalone.IntraDayStructure where index > " +ignoreBeforeIndex+ " order by volume desc", new ArrayList<IntraDayStructure>(idsList) );
			results = qrTopN.getResults();
		}
		
		
		final List<Integer> indices = new ArrayList<Integer>();
		int iTopDumps = 0, iTopPumps = 0;
		Double highestPumpCxV = 0.0, highestDumpCxV = 0.0;

		for( IntraDayStructure ids : results ){
			final IntraDayStructure idsPrev = idsList.get(ids.getIndex() >= 2 ? ids.getIndex()-2 : ids.getIndex()-1);
			
			if(ids.getIndex() == idsPrev.getIndex()){
				continue; // Do not consider.
			}
			
			if( iTopPumps < iTopN && ids.getClose() >= idsPrev.getClose() ){
				iTopPumps++;
				
				indices.add( ids.getIndex() );
				
				final Double CxV = ids.getClose() * ids.getVolume();
				if( highestPumpCxV < CxV ){
					highestPumpCxV = CxV;
				}

			}else if( iTopDumps < iTopN && ids.getClose() < idsPrev.getClose() ){
				iTopDumps++;
				
				indices.add( ids.getIndex() );

				final Double CxV = ids.getClose() * ids.getVolume();
				if( highestDumpCxV < CxV ){
					highestDumpCxV = CxV;
				}

			}
			
			if( iTopPumps == iTopN && iTopDumps == iTopN ){
				break;
			}
			
			//int relativeIndex = ids.getIndex() - idsList.get(0).getIndex(); // 1-based
			
		}
		
		final Long highestVol = results.get(0).getVolume();
		//final Long lowestVol  = results.get( results.size()-1 ).getVolume();
		final Long lowestVol  = idsList.get( indices.get( indices.size()-1 )-1 ).getVolume();
		//System.out.println( lowestVol +", "+ highestVol );

		final StringBuilder sbAll     = new StringBuilder("<table border=1>");
		final StringBuilder sbDumpPressureMonitor = new StringBuilder("<table border=1>");
		sbDumpPressureMonitor.append("<tr><td>Dump@</td><td>Tgt D Pressure<BR>(Upto -10%)</td><td>D Pressure(Min)<BR>Remaining</td><td>D Pressure<BR>(Achieved)</td></tr>");
		final StringBuilder sbPumpPressureMonitor = new StringBuilder("<table border=1>");
		sbPumpPressureMonitor.append("<tr><td>Pump@</td><td>Tgt P Pressure<BR>(Upto +10%)</td><td>P Pressure(Max)<BR>Remaining</td><td>P Pressure<BR>(Achieved)</td></tr>");
		
		int yIndex = 0;
		//final Double realTime = idsList.get(idsList.size()-1).getClose();
		Double realTime = idsList.get(idsList.size()-1).getClose();
		Double dSumDumpPressure = 0.0;
		Double dSumPumpPressure = 0.0;

		//Double dSumDumpPressureRealtime = 0.0;
		//Double dSumPumpPressureRealtime = 0.0;
		
		Double dSumDumpPressureCxV = 0.0;
		Double dSumPumpPressureCxV = 0.0;
		
		Double dTotalDumpPressureCxV = 0.0, dTotalPumpPressureCxV = 0.0; 
		
		for( final IntraDayStructure ids : idsList ){
			if( yIndex == Y_LABEL_POS.size() ){
				yIndex = 0;
			}
			
			if( indices.contains( ids.getIndex() ) ){
				// highlight
				final IntraDayStructure idsPrev = idsList.get(ids.getIndex() >= 2 ? ids.getIndex()-2 : ids.getIndex()-1);
				final double change = Utility.round( ((ids.getClose() - idsPrev.getClose()) / idsPrev.getClose()) * 100.00 );

				int relativeIndex = ids.getIndex() - idsList.get(0).getIndex(); // 1-based

				double x = 100.00 + ((((double)relativeIndex - (double)idsList.size()) / (double)idsList.size()) * 100.00);
				x = (x > 20.0 ? x-20.0 : x+40.0);
				String strX = "0." +(x+"").replace(".", "");
				final String label = " Price: " +ids.getClose()+ " (" +idsPrev.getClose()+ " " +change+"%) Index: " +ids.getIndex()+ " Val $" +Utility.getFormattedInteger(ids.getClose()*ids.getVolume()).replaceAll(",", " ")+ "," +strX+ "," +Y_LABEL_POS.get(yIndex++)+ "," +(relativeIndex)+",0";

				// Start:
				final QueryResults qrMinMax = Query.parseAndExec("SELECT min(low), max(high) FROM com.stocks.standalone.IntraDayStructure where index >= " +ids.getIndex()+ " LIMIT 1,1", new ArrayList<IntraDayStructure>(idsList) );
				final List<ArrayList<Double>> resultsMinMax = qrMinMax.getResults();
				final Double min = resultsMinMax.get(0).get(0);
				final Double max = resultsMinMax.get(0).get(1);

				final Double CxV = ids.getClose() * ids.getVolume();
				
				String sCheckBoxChecked = "";
				if( ids.getClose() < idsPrev.getClose() ){
					// Dump
					Double nPC = (((CxV-highestDumpCxV)/highestDumpCxV)*10.0)+10.0; // Assuming 10% is the maximum pressure a Transaction can have on a stock.

					final Double minusNpc = Utility.round( ids.getClose() - ( ids.getClose() * (nPC/100.0) ) );

					if( min > minusNpc ){
						sCheckBoxChecked = "checked";
					}else{
						sCheckBoxChecked = "title='Min (" +min+ ") crossed below minus10pc (" +minusNpc+ ")'";
					}
					
					sbDumpPressureMonitor.append("<tr>");
					sbDumpPressureMonitor.append("<td>" +ids.getClose()+ "</td>");
					sbDumpPressureMonitor.append("<td title='$" +Utility.getFormattedInteger(CxV)+"'>" +minusNpc+ " (-" +Utility.round(nPC)+ "%)</td>");
					Double tgt1 = (minusNpc >= min ? 0.0 : minusNpc);
					//sbDumpPressureMonitor.append("<td>" +tgt1+ "</td>");
					
					Double dPressure = 0.0; //, dPressureRealtime = 0.0;
					if( tgt1 != 0.0 ){
						dPressure = Utility.round( ((tgt1 - min)/min)*100.0 );
						dSumDumpPressureCxV += (ids.getClose() * ids.getVolume());
						
						//dPressureRealtime = Utility.round( ((tgt1 - realTime)/realTime)*100.0 );
					}
					
					sbDumpPressureMonitor.append("<td>" +dPressure+ "%</td>");
					//sbDumpPressureMonitor.append("<td>" +dPressureRealtime+ "%</td>");
					if( tgt1 != 0.0 ){
						sbDumpPressureMonitor.append("<td>" +Utility.round(Math.abs(nPC) - Math.abs(dPressure))+ "%</td>");
					}else{
						sbDumpPressureMonitor.append("<td>&nbsp;</td>");
					}
					sbDumpPressureMonitor.append("</tr>");
					
					dSumDumpPressure += Math.abs(dPressure);
					dTotalDumpPressureCxV += CxV;
					//dSumDumpPressureRealtime += Math.abs(dPressureRealtime);
				}else{
					// Pump
					Double nPC = (((CxV-highestPumpCxV)/highestPumpCxV)*10.0)+10.0; // Assuming 10% is the maximum pressure a Transaction can have on a stock.
					
					final Double plusNpc = Utility.round( ids.getClose() + ( ids.getClose() * (nPC/100.0) ) );

					if( max < plusNpc ){
						sCheckBoxChecked = "checked";
					}else{
						sCheckBoxChecked = "title='Max (" +max+ ") crossed above plus10pc (" +plusNpc+ ")'";
					}

					sbPumpPressureMonitor.append("<tr>");
					sbPumpPressureMonitor.append("<td>" +ids.getClose()+ "</td>");
					sbPumpPressureMonitor.append("<td title='$" +Utility.getFormattedInteger(CxV)+ "'>" +plusNpc+ " (" +Utility.round(nPC)+ "%)</td>");
					Double tgt1 = (plusNpc <= max ? 0.0 : plusNpc);
					//sbPumpPressureMonitor.append("<td>" +tgt1+ "</td>");
					
					Double pPressure = 0.0; //, pPressureRealtime = 0.0;
					if( tgt1 != 0.0 ){
						pPressure = Utility.round( ((tgt1 - max)/max)*100.0 );
						dSumPumpPressureCxV += (ids.getClose() * ids.getVolume());
						
						//pPressureRealtime = Utility.round( ((tgt1 - realTime)/realTime)*100.0 );
					}
					
					sbPumpPressureMonitor.append("<td>" +pPressure+ "%</td>");
					//sbPumpPressureMonitor.append("<td>" +pPressureRealtime+ "%</td>");
					if( tgt1 != 0.0 ){
						sbPumpPressureMonitor.append("<td>" +Utility.round(Math.abs(nPC) - Math.abs(pPressure))+ "%</td>");
					}else{
						sbPumpPressureMonitor.append("<td>&nbsp;</td>");
					}
					sbPumpPressureMonitor.append("</tr>");
					
					dSumPumpPressure += Math.abs(pPressure);
					dTotalPumpPressureCxV += CxV;
					//dSumPumpPressureRealtime += Math.abs(pPressureRealtime);
				}
				// End

				sbAll.append("<tr>" +
						"<td valign=middle align=center><input type=checkbox name='c" +ids.getIndex()+ "' " +sCheckBoxChecked+ "></td>" +
						"<td valign=middle align=center>" +
						"D <input type=radio name='r" +ids.getIndex()+ "' onclick='setBackground(this, \"#FFDDFF\")' value='D " +label+ "' " +(ids.getClose() < idsPrev.getClose() ? "checked" : "")+ "> " +
						"P <input type=radio name='r" +ids.getIndex()+ "' onclick='setBackground(this, \"#99EEDD\")' value='P " +label+ "' " +(ids.getClose() >= idsPrev.getClose() ? "checked" : "")+ "> " +
						"<BR>" +change+ "%</td>");
				sbAll.append("<td>").append( idsPrev.toStringFor() );
				sbAll.append("<BR>").append( ids.toStringFor() );
				sbAll.append("</td></tr>");
			}
		}
		
		sbAll.append("</table>");
		
		final Double dumpPcAgainstWhole = Utility.round((((dSumDumpPressureCxV-dTotalDumpPressureCxV)/dTotalDumpPressureCxV)*100.0)+100.0);
		final Double pumpPcAgainstWhole = Utility.round((((dSumPumpPressureCxV-dTotalPumpPressureCxV)/dTotalPumpPressureCxV)*100.0)+100.0);
		
		// dTotalDumpPressureCxV
		//final String dumpString = "<tr><td colspan='2'>" +symbol+ " (Dump) Total Amount Involved (CxV): <B>$" +Utility.getFormattedInteger(dSumDumpPressureCxV)+ "</B></td><td>" +Utility.round(dSumDumpPressure)+ "%</td><td>&nbsp;</td></tr>";
		final String dumpString = "<tr><td colspan='2'>" +symbol+ " Total Dump Still to cover (CxV): <B>$" +Utility.getFormattedInteger(dSumDumpPressureCxV)+ "</B></td><td colspan='2' align='center'>(" +dumpPcAgainstWhole+ "% of Total)</td></tr>";
		sbDumpPressureMonitor.append( dumpString );
		sbDumpPressureMonitor.append("</table>");

		//final String pumpString = "<tr><td colspan='2'>" +symbol+ " (Pump) Total Amount Involved (CxV): <B>$" +Utility.getFormattedInteger(dSumPumpPressureCxV)+ "</B></td><td>" +Utility.round(dSumPumpPressure)+ "%</td><td>&nbsp;</td></tr>";
		final String pumpString = "<tr><td colspan='2'>" +symbol+ " Total Pump Still to realize (CxV): <B>$" +Utility.getFormattedInteger(dSumPumpPressureCxV)+ "</B></td><td colspan='2' align='center'>(" +pumpPcAgainstWhole+ "% of Total)</td></tr>";
		sbPumpPressureMonitor.append( pumpString );
		sbPumpPressureMonitor.append("</table>");
		
		//sbSummaryOnly.append( dumpString ).append("\n").append( pumpString ).append("\n");
		//sbSummaryOnly.append( "<tr><td colspan='5'>&nbsp;</td></tr>" ).append("\n");
		//sbSummaryOnly.append("Symbol,D Pressure(Min),P Pressure(Min),D Pressure(Realtime),P Pressure(Realtime),Total D Amount Involved (CxV),Total P Amount Involved (CxV),x Times (CxV)").append("\n");
		sbSummaryOnly.append("\"").append(symbol).append("\",");
		sbSummaryOnly.append("\"").append(Utility.round(dumpPcAgainstWhole)).append("%\",");
		sbSummaryOnly.append("\"").append(Utility.round(pumpPcAgainstWhole)).append("%\",");
		//sbSummaryOnly.append("\"").append(Utility.round(dSumDumpPressureRealtime)).append("%\",");
		//sbSummaryOnly.append("\"").append(Utility.round(dSumPumpPressureRealtime)).append("%\",");
		sbSummaryOnly.append("\"$").append(Utility.getFormattedInteger(dSumDumpPressureCxV)).append("\",");
		sbSummaryOnly.append("\"$").append(Utility.getFormattedInteger(dSumPumpPressureCxV)).append("\",");
		
		try{
			sbSummaryOnly.append(Utility.round(dSumPumpPressureCxV / dSumDumpPressureCxV));
		}
		catch(Exception e){
			sbSummaryOnly.append("0");
		}
		sbSummaryOnly.append(" times");
		sbSummaryOnly.append("\n");
		
		
		sb.append("\n<pre>All (Volume Toppers): <B>Note</B>: Consider them Pumps if %change is very low. \n").append( sbAll ).append("</pre>");
		sb.append("\nHighlight <select id='highlightOnly'>").append("\n");
		sb.append("\n<option value='P'>Pumps Only");
		sb.append("\n<option value='D'>Dumps Only");
		sb.append("\n<option value='All' selected>All");
		sb.append("\n</select>");
		sb.append("\n<input type='button' value='Update Chart' onclick='repaint(document.getElementById(\"highlightOnly\").value)'>");
		
		String chartHTMLAll = processSubList(idsList, symbol, "All", lowestVol, highestVol, ignoreBeforeIndex, considerAfterIndex);
		chartHTMLAll = chartHTMLAll.replace("hidden", "visible");
		chartHTMLAll = chartHTMLAll.replace("absolute", "relative");
		chartHTMLAll = chartHTMLAll.replace("appletName", "appletAll");
		chartHTMLAll = chartHTMLAll.replace("width='1000' height='350'", "width='1300' height='550'");
		chartHTMLAll = chartHTMLAll.replace("visibleSamples", "visibleSamplesNOTUSED");
		
		sb.append( chartHTMLAll );
		
		sb.append("<script>");
		sb.append( "arrExprDate = new Array();" ).append("\n");
		for( int i=0; i<javascriptToDetermineDateFromIndex.size(); i++ ){
			String[] arrJs = javascriptToDetermineDateFromIndex.get(i).split(";");
			sb.append( "arrExprDate[" +i+ "] = new Array();" ).append("\n");
			sb.append( "arrExprDate[" +i+ "][0] = '" +arrJs[0]+ "';" ).append("\n");
			sb.append( "arrExprDate[" +i+ "][1] = '" +arrJs[1]+ "';" ).append("\n");
		}
		sb.append("</script>");
		
		sb.append("<table><tr><td colspan=2 align=right>Current Price: " +realTime+ "</td></tr><tr><td>" +sbDumpPressureMonitor+ "</td><td>" +sbPumpPressureMonitor+ "</td></tr></table>");
		
		sb.append("</body></html>");
		
		final String rptPath = properties.getProperty("rpt.folder") + "/rpt_" +symbol+ ".html";
		Utility.saveContent( rptPath, sb.toString());
		System.out.println( "Report generated @ " +rptPath );
	}
	
	private static String processSubList(final List<IntraDayStructure> idsSubList, String symbol, String sDate, Long lowestVolume, Long highestVolume, int ignoreBeforeIndex, int considerAfterIndex) throws Exception{
		final StringBuilder sbCloseList = new StringBuilder();
		final StringBuilder sbVolumeList = new StringBuilder();
		final StringBuilder sbIndexList = new StringBuilder();

		for( final IntraDayStructure ids : idsSubList ){
			sbCloseList.append(",").append(ids.getClose());
			sbVolumeList.append(",").append(ids.getVolume());
			sbIndexList.append(",").append(ids.getIndex());
		}
		
		final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", new ArrayList<IntraDayStructure>(idsSubList) );
		final List<List<Double>> results = qrMinMax.getResults();
		final Double minClose = results.get(0).get(0);
		final Double maxClose = results.get(0).get(1);
		
		// Start: Top N Labels
		// http://josql.sourceforge.net/manual/limit-clause.html
		final QueryResults qrTopN = Query.parseAndExec("SELECT * FROM com.stocks.standalone.IntraDayStructure where index > " +considerAfterIndex+ " order by volume desc LIMIT 1, " +properties.getProperty("top.n"), new ArrayList<IntraDayStructure>(idsSubList) );
		final List<IntraDayStructure> resultsTopN = qrTopN.getResults();
		
		final List<Integer> indices = new ArrayList<Integer>(); 
		for( IntraDayStructure ids : resultsTopN ){
			indices.add( ids.getIndex() );
		}

		final ArrayList<String> labels = new ArrayList<String>();
		if( ignoreBeforeIndex != -1 ){
			labels.add("Ignore Before: " +ignoreBeforeIndex+ ",0.01,0.01," +(ignoreBeforeIndex-1)+ ",0");
		}
		
		int yIndex = 0;
		for( final IntraDayStructure ids : idsSubList ){
			if( yIndex == Y_LABEL_POS.size() ){
				yIndex = 0;
			}
			
			if( indices.contains( ids.getIndex() ) ){
				// highlight
				int relativeIndex = ids.getIndex() - idsSubList.get(0).getIndex(); // 1-based
				int relativePrevIndex = relativeIndex >= 1 ? relativeIndex-1 : relativeIndex;
				
				//final IntraDayStructure idsPrev = idsSubList.get(ids.getIndex() >= 2 ? ids.getIndex()-2 : ids.getIndex());
				final IntraDayStructure idsPrev = idsSubList.get(relativePrevIndex);
				final double change = Utility.round( ((ids.getClose() - idsPrev.getClose()) / idsPrev.getClose()) * 100.00 );
				String label = null;
				
				double x = 100.00 + ((((double)relativeIndex - (double)idsSubList.size()) / (double)idsSubList.size()) * 100.00);
				x = (x > 20.0 ? x-20.0 : x+40.0);
				String strX = "0." +(x+"").replace(".", "");
				label = (change < 0.0 ? "D" : "P")+ " Price: " +ids.getClose()+ " (" +idsPrev.getClose()+ " " +change+"%) Index: " +ids.getIndex()+ " Val $" +Utility.getFormattedInteger(ids.getClose()*ids.getVolume()).replaceAll(",", " ")+ "," +strX+ "," +Y_LABEL_POS.get(yIndex++)+ "," +(relativeIndex)+",0";
				
				labels.add(label);
			}
		}
		
		final StringBuilder sbLabels = new StringBuilder();
		for(int i=0; i<labels.size(); i++){
			sbLabels.append("\n<param name='label_" +i+ "' value='" +labels.get(i)+ "'>");
		}
		// End: Top N Labels

		String chartHTML = CHART_HTML;
		chartHTML = chartHTML.replace("~SYMBOL", symbol +" (" +sDate+ ")");
		chartHTML = chartHTML.replace("~CLOSELIST", sbCloseList.substring(1));
		chartHTML = chartHTML.replace("~VOLUMELIST", sbVolumeList.substring(1));
		chartHTML = chartHTML.replace("~INDICES", sbIndexList.substring(1));
		chartHTML = chartHTML.replace("~HIGHEST_CLOSE", maxClose.toString() );
		chartHTML = chartHTML.replace("~LOWEST_CLOSE", minClose.toString() );
		chartHTML = chartHTML.replace("~DATE", sDate );
		chartHTML = chartHTML.replaceAll("~HIGHEST_VOL", highestVolume.toString());
		chartHTML = chartHTML.replaceAll("~LOWEST_VOL", lowestVolume.toString());
		chartHTML = chartHTML.replace("<!-- LABELS -->", sbLabels.toString() );
		return chartHTML;
	}
	
	private static List<IntraDayStructure> getIntraDayStructureList(String content){
		final String[] rows = content.split("\n");
		boolean bDataStarted = false;
		int index = 0;
		final List<IntraDayStructure> idsList = new ArrayList<IntraDayStructure>();
		Date date = null;
		for( String row : rows ){
			if( row.startsWith("a") ){
				String[] cols = row.split(",");
				date = new Date( Long.parseLong(cols[INDX_D].substring(1)) * DATE_OFFSET );
				
				final Double close = Double.parseDouble(cols[INDX_C]);
				final Double high  = Double.parseDouble(cols[INDX_H]);
				final Double low   = Double.parseDouble(cols[INDX_L]);
				final Double open  = Double.parseDouble(cols[INDX_O]);
				final Long volume  = Long.parseLong(cols[INDX_V]);

				idsList.add(new IntraDayStructure(++index, date.getTime(), close, high, low, open, volume));

				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				
				if( cols.length != 6 ){
					System.out.println( "Error parsing current row. Data -> " +row );
					continue;
				}
				
				Date adjustedDate = new Date( date.getTime() +Long.parseLong(cols[INDX_D]) );
				
				final Double close = Double.parseDouble(cols[INDX_C]);
				final Double high  = Double.parseDouble(cols[INDX_H]);
				final Double low   = Double.parseDouble(cols[INDX_L]);
				final Double open  = Double.parseDouble(cols[INDX_O]);
				final Long volume  = Long.parseLong(cols[INDX_V]);

				idsList.add(new IntraDayStructure(++index, adjustedDate.getTime(), close, high, low, open, volume));
			}
		}
		return idsList;
	}
	
	
	
}
