package com.stocks.standalone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.josql.Query;
import org.josql.QueryResults;

import com.stocks.util.Utility;

public class TopVolumeHighlighter {
	final static String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=~SYMBOL";
	final static double LEAST_CONSIDERABLE_CHANGE = 0.5;
	final static int INDX_D = 0;
	final static int INDX_C = 1;
	final static int INDX_H = 2;
	final static int INDX_L = 3;
	final static int INDX_O = 4;
	final static int INDX_V = 5;
	final static Long DATE_OFFSET = 1000L;
	final static String CHART_HTML = "\n<DIV id='~DATE' style='visibility:hidden;position:absolute;top:10;left:10'>" +
			"\n<applet code='com.objectplanet.chart.ChartApplet' archive='chart.jar' width='1000' height='350'>" +
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
			"\n</applet>" +
			"\n</DIV>";

	public static void main(String... args) throws Exception{
		final String symbol = "GTI";
		final String content = Utility.getContent( "file:///C:/Temp/ForSrid/tmp/" +symbol+ "-1M.txt" );
		//final String content = Utility.getContent( GOOGLE_URL_INTRA_DAY.replaceAll("~SYMBOL", symbol) );
		
		
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
		sb.append( "</script>" ).append("\n");
		sb.append( "</head>" ).append("\n");
		sb.append( "<body>" ).append("\n");
		sb.append( "<P>This report reads IntraDay data from Google Finance. It generates Intra Day Charts for each day in the Past Month." ).append("\n");
		sb.append( "It lays out 1 min close and volume for each day. Clicking the date link will display 1 min IntraDay Chart for that day." ).append("\n");
		sb.append( "<B>Note</B>: There is a consolidated chart displayed in the end of this report. This chart displays close and volume for the entire month." ).append("\n");
		sb.append( "This chart is the combined version of all individual date charts together. It highlights top volumes only that are dumps or pumps." ).append("\n");

		final StringBuilder sbTabPanel = new StringBuilder();
		
		final int TD_PER_TR = 5;
		int tdCounter = 0;
		for( final ArrayList key : treeSet ){
			final List<List<Integer>> minMax = resultsDates.get(key);
			final String sDate = key.get(0).toString().replaceAll("/", "_");
			
			System.out.println( sDate+ " -> " +minMax.get(0).get(0) +","+ minMax.get(0).get(1) );
			
			// Process SubLists
			int fromIndex = minMax.get(0).get(0)-1;
			int toIndex   = minMax.get(0).get(1);
			
			final List<IntraDayStructure> idsSubList = idsList.subList(fromIndex, toIndex);
			final QueryResults qrMinMax = Query.parseAndExec("SELECT min(volume), max(volume) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", new ArrayList<IntraDayStructure>(idsSubList) );
			final List<ArrayList<Long>> resultsMinMax = qrMinMax.getResults();
			final Long minVolume = resultsMinMax.get(0).get(0);
			final Long maxVolume = resultsMinMax.get(0).get(1);
			final String chartHTML = processSubList(idsSubList, symbol, sDate, minVolume, maxVolume);
			
			sb.append( chartHTML ).append( "<BR>" );;
			
			if( ( tdCounter++ % TD_PER_TR ) == 0 ){
				sbTabPanel.append( "<BR>" );
				tdCounter = 1;
			}
			sbTabPanel.append( "<span title='" +fromIndex+ " - " +toIndex+ "' onclick='show(\"" +sDate+ "\");' style='border:1px solid black;'>" ).append( sDate ).append( "</span> &nbsp;" );
		}
		sb.append( sbTabPanel );
		// End
		
		// http://josql.sourceforge.net/manual/limit-clause.html
		final QueryResults qrTopN = Query.parseAndExec("SELECT * FROM com.stocks.standalone.IntraDayStructure order by volume desc LIMIT 1, 40", new ArrayList<IntraDayStructure>(idsList) );
		final List<IntraDayStructure> results = qrTopN.getResults();
		
		final List<Integer> indices = new ArrayList<Integer>(); 
		for( IntraDayStructure ids : results ){
			indices.add( ids.getIndex() );
		}
		
		final Long highestVol = results.get(0).getVolume();
		final Long lowestVol  = results.get( results.size()-1 ).getVolume();

		final StringBuilder sbToppers = new StringBuilder();
		final StringBuilder sbDumps   = new StringBuilder("<table border=1>");
		final StringBuilder sbPumps   = new StringBuilder("<table border=1>");

		final ArrayList<String> labels = new ArrayList<String>();
		List<Double> yLabelPos = Arrays.asList(new Double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9});
		int yIndex = 0;
		for( final IntraDayStructure ids : idsList ){
			if( yIndex == yLabelPos.size() ){
				yIndex = 0;
			}
			
			if( indices.contains( ids.getIndex() ) ){
				// highlight
				final IntraDayStructure idsPrev = idsList.get(ids.getIndex() >= 2 ? ids.getIndex()-2 : ids.getIndex());
				final double change = Utility.round( ((ids.getClose() - idsPrev.getClose()) / idsPrev.getClose()) * 100.00 );
				String label = null;
				sbToppers.append("\n\n").append( idsPrev.toStringFor() );
				sbToppers.append("\n").append( ids.toStringFor() );
				
				if( ids.getClose() < idsPrev.getClose() ){
					sbDumps.append("<tr><td valign=middle align=center><input type=checkbox onclick='if(this.checked){this.parentNode.parentNode.style.backgroundColor=\"#FFDDFF\";}else{this.parentNode.parentNode.style.backgroundColor=\"#FFFFFF\";}'> <BR>" +change+ "%</td>");
					sbDumps.append("<td>").append( idsPrev.toStringFor() );
					sbDumps.append("<BR>").append( ids.toStringFor() );
					sbDumps.append("</td></tr>");
					
					if( Math.abs(change) > LEAST_CONSIDERABLE_CHANGE ){
						// Text,X,Y,index,series
						double x = 100.00 + ((((double)ids.getIndex() - (double)idsList.size()) / (double)idsList.size()) * 100.00);
						String strX = "0." +(x+"").replace(".", "");
						label = "D Price: " +ids.getClose()+ " Index: " +ids.getIndex()+ " Val $" +Utility.getFormattedInteger(ids.getClose()*ids.getVolume()).replaceAll(",", " ")+ "," +strX+ "," +yLabelPos.get(yIndex++)+ "," +(ids.getIndex()-1)+",0";
					}
				}else{
					sbPumps.append("<tr><td valign=middle align=center><input type=checkbox onclick='if(this.checked){this.parentNode.parentNode.style.backgroundColor=\"#99EEDD\";}else{this.parentNode.parentNode.style.backgroundColor=\"#FFFFFF\";}'> <BR>" +change+ "%</td>");
					sbPumps.append("<td>").append( idsPrev.toStringFor() );
					sbPumps.append("<BR>").append( ids.toStringFor() );
					sbPumps.append("</td></tr>");

					if( Math.abs(change) > LEAST_CONSIDERABLE_CHANGE ){
						// Text,X,Y,index,series
						double x = 100.00 + ((((double)ids.getIndex() - (double)idsList.size()) / (double)idsList.size()) * 100.00);
						String strX = "0." +(x+"").replace(".", "");
						label = "P Price: " +ids.getClose()+ " Index: " +ids.getIndex()+ " Val $" +Utility.getFormattedInteger(ids.getClose()*ids.getVolume()).replaceAll(",", " ")+ "," +strX+ "," +yLabelPos.get(yIndex++)+ "," +(ids.getIndex()-1)+",0";
					}
				}
				
				if( label != null ){
					labels.add(label);
				}
			}
		}
		
		final StringBuilder sbLabels = new StringBuilder();
		for(int i=0; i<labels.size(); i++){
			sbLabels.append("\n<param name='label_" +i+ "' value='" +labels.get(i)+ "'>");
			System.out.println( "Label: " +labels.get(i) );
		}
		
		sbDumps.append("</table>");
		sbPumps.append("</table>");
		
		sb.append("\n<pre>Dumps: \n").append( sbDumps ).append("</pre>");
		sb.append("\n<pre>Pumps: \n").append( sbPumps ).append("</pre>");
		sb.append("\n<pre>All: \n").append( sbToppers ).append("</pre>");
		
		String chartHTMLAll = processSubList(idsList, symbol, "All", lowestVol, highestVol);
		chartHTMLAll = chartHTMLAll.replace("hidden", "visible");
		chartHTMLAll = chartHTMLAll.replace("absolute", "relative");
		chartHTMLAll = chartHTMLAll.replace("<!-- LABELS -->", sbLabels.toString() );
		chartHTMLAll = chartHTMLAll.replace("visibleSamples", "visibleSamplesNOTUSED");
		
		sb.append( chartHTMLAll );
		
		sb.append("</body></html>");
		
		//final String s = sb.toString().replaceAll("~HIGHEST_VOL", highestVol.toString()).replaceAll("~LOWEST_VOL", lowestVol.toString());
		
		Utility.saveContent("C:/Temp/ForSrid/tmp/rpt.html", sb.toString());
		System.out.println( "Done" );
		
	}
	
	private static String processSubList(final List<IntraDayStructure> idsSubList, String symbol, String sDate, Long lowestVolume, Long highestVolume) throws Exception{
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
