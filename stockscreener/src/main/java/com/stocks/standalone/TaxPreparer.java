package com.stocks.standalone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.NyseTx;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class TaxPreparer {
	public static void main(String args[]) throws Exception {
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		StockService stockService = (StockService) context.getBean("stockService");

		// Step 1: Pull all Buy/Sell Transactions
		TaxPreparer taxPreparer = new TaxPreparer();
		taxPreparer.process(stockService.getAllNyseTransactions());

		System.out.println( "Done." );
		System.exit(0);
	}
	
	public void process(final List<NyseTx> allNyseTxList) throws Exception{
		// Step 2: Organize Buy/Sell Transactions in separate maps.
		final Map<String, List<NyseTx>> bNyseTxMap = getNyseTxMap("B", allNyseTxList);
		final Map<String, List<NyseTx>> sNyseTxMap = getNyseTxMap("S", allNyseTxList);
		
		// Step 3:
		final List<CsvFormat> csvFormatList = new ArrayList<CsvFormat>();
		for( Map.Entry<String, List<NyseTx>> me : sNyseTxMap.entrySet() ){
			final String symbol = me.getKey();
			final List<NyseTx> nyseTxList = me.getValue();
			
			for( final NyseTx sNyseTx : nyseTxList ){
				sNyseTx.setQty( Math.abs(sNyseTx.getQty()) );
				
				final CsvFormat csvFormat = new CsvFormat();
				csvFormat.setsNyseTx(sNyseTx);
				
				final List<NyseTx> bNyseTxFromMap = bNyseTxMap.get( symbol );
				if( bNyseTxFromMap != null && !bNyseTxFromMap.isEmpty() ){
					csvFormat.setbNyseTxList(new ArrayList<NyseTx>());
					
					Integer sQty = sNyseTx.getQty();
					
					for( final NyseTx bNyseTx : bNyseTxFromMap  ){
						Integer consumedQty = 0;
						if( bNyseTx.getQty() > 0 ){
							final NyseTx nyseTx = new NyseTx();
							if( bNyseTx.getQty() <= sQty  ){ // If equal or less qty bought than sold
								consumedQty = bNyseTx.getQty();
							}else if( bNyseTx.getQty() > sQty ){ // If more qty bought than sold
								consumedQty = sQty;
							}
							nyseTx.setNyseTxId(bNyseTx.getNyseTxId());
							nyseTx.setPrice(bNyseTx.getPrice());
							nyseTx.setSymbol(bNyseTx.getSymbol());
							nyseTx.setTrxType(bNyseTx.getTrxType());
							nyseTx.setTxDate(bNyseTx.getTxDate());
							nyseTx.setTxFee(bNyseTx.getTxFee());
							nyseTx.setQty(consumedQty);
							csvFormat.getbNyseTxList().add(nyseTx);
							
							// Recalculate Qty
							sQty -= consumedQty;
							bNyseTx.setQty( bNyseTx.getQty() - consumedQty );
						}
						
						if( sQty == 0 ){
							break;
						}
					}
					
					// Additional processing for Wash Sale
					if( csvFormatList != null && !csvFormatList.isEmpty() ){
						CsvFormat prevCsvFormat = csvFormatList.get( csvFormatList.size()-1 );
						if( prevCsvFormat.getsNyseTx().getSymbol().equals( sNyseTx.getSymbol() ) && prevCsvFormat.isWashSale() ){
							Double basis = 0.0;
							
							class QtyPrice{
								Integer qty;
								Double price;

								public QtyPrice(Integer qty, Double price) {
									super();
									this.qty = qty;
									this.price = price;
								}
							}
							
							List<QtyPrice> qpList = new ArrayList<QtyPrice>();
							for(final NyseTx prevbNyseTx : prevCsvFormat.getbNyseTxList()){
								qpList.add( new QtyPrice(prevbNyseTx.getQty(), prevbNyseTx.getPrice()) );
							}
							
							final StringBuilder basisComments = new StringBuilder();
							for(final NyseTx bNyseTx : csvFormat.getbNyseTxList()){
								Integer adjustedQty = bNyseTx.getQty();
								for(final QtyPrice qp : qpList){
									if( qp.qty > 0 ){
										final Double lossPerShare = Math.abs(prevCsvFormat.getsNyseTx().getPrice() - qp.price);
										if( qp.qty <= adjustedQty ){ // If equal or less qty was bought than required
											Double bs = qp.qty * lossPerShare;
											basis += bs;
											basisComments.append( qp.qty ).append(" * ").append(lossPerShare).append(" = ").append( bs ).append("<BR>");
											adjustedQty -= qp.qty;
											qp.qty = 0;
										}else{
											Double bs = adjustedQty * lossPerShare;
											basis += bs;
											basisComments.append( adjustedQty ).append(" * ").append(lossPerShare).append(" = ").append( bs ).append("<BR>");
											//adjustedQty = 0;
											qp.qty -= adjustedQty;
											break;
										}
									}
								}
							}
							csvFormat.setBasis( basis );
							csvFormat.setBasisComments( basisComments.toString() );
						}
					}
				}
				
				csvFormatList.add(csvFormat);
			}
		}
		
		// Step 4: Print it out
		//System.out.println( String.format("%s,%s,%s,%s,%s,%s,%s", "Description", "Date Sold", "Sales Proceeds", "Date Acquired", "Cost", "Wash Sale", "Adjustment Amount") );
		StringBuilder sb = new StringBuilder();
		sb.append("<TABLE BORDER=1>" );
		sb.append( "<TR><TD>Description</TD> <TD>Sales Proceeds</TD> <TD>Cost</TD> <TD>Wash Sale</TD> <TD>Adjustment Amount</TD><TD>Basis Comments</TD></TR>" );
		for(final CsvFormat csvFormat : csvFormatList){
			sb.append( csvFormat );
		}
		sb.append("</TABLE>" );
		Utility.saveContent("C:/Temp/Stk/tax.html", sb.toString());
	}
	
	private Map<String, List<NyseTx>> getNyseTxMap(final String trxType, final List<NyseTx> allNyseTxList){
		final Map<String, List<NyseTx>> map = new HashMap<String, List<NyseTx>>();
		for( final NyseTx nyseTx : allNyseTxList ){
			if( nyseTx.getTrxType().equals(trxType) ){
				List<NyseTx> nyseTxList = map.get( nyseTx.getSymbol() );
				if( nyseTxList == null ){
					nyseTxList = new ArrayList<NyseTx>();
					map.put(nyseTx.getSymbol(), nyseTxList);
				}

				nyseTxList.add( nyseTx );
			}
		}
		return map;
	}
	
	private class CsvFormat{
		List<NyseTx> bNyseTxList;
		NyseTx sNyseTx;
		Double basis;
		String basisComments;

		public List<NyseTx> getbNyseTxList() {
			return bNyseTxList;
		}
		public void setbNyseTxList(List<NyseTx> bNyseTxList) {
			this.bNyseTxList = bNyseTxList;
		}
		public NyseTx getsNyseTx() {
			return sNyseTx;
		}
		public void setsNyseTx(NyseTx sNyseTx) {
			this.sNyseTx = sNyseTx;
		}
		public Double getBasis() {
			return basis;
		}
		public void setBasis(Double basis) {
			this.basis = basis;
		}
		public String getBasisComments() {
			return basisComments;
		}
		public void setBasisComments(String basisComments) {
			this.basisComments = basisComments;
		}
		
		public Double getCost(){
			Double cost = 0.0;
			if( bNyseTxList != null && !bNyseTxList.isEmpty() ){
				for(final NyseTx nyseTx : bNyseTxList){
					cost += ((nyseTx.getPrice() * nyseTx.getQty()) + nyseTx.getTxFee());
				}
			}
			return cost;
		}
		public Double getSalesProceeds(){
			final Double salesProceeds = Math.abs(Utility.round((sNyseTx.getPrice() * sNyseTx.getQty()) - sNyseTx.getTxFee()));
			return salesProceeds;
		}
		public Double getAdjustmentAmount(){
			return getSalesProceeds() - getCost();
		}
		public boolean isWashSale(){
			if( getAdjustmentAmount() < 0 ){ // Loss
				if( getbNyseTxList() != null && !getbNyseTxList().isEmpty() ){
					Date firstDateAcquired = getbNyseTxList().get(0).getTxDate();
					if( Utility.getDaysDiffBetween( sNyseTx.getTxDate(), firstDateAcquired) <= 30 ){
						return true;
					}
				}
			}

			return false;
		}
		
		@Override
		public String toString() {
			//System.out.println( "<TD>Description</TD> <TD>Sales Proceeds</TD> <TD>Cost</TD> <TD>Wash Sale</TD> <TD>Adjustment Amount</TD>" );
			final Double salesProceeds = Math.abs(Utility.round((sNyseTx.getPrice() * sNyseTx.getQty()) - sNyseTx.getTxFee()));
			final StringBuilder sb = new StringBuilder();
			sb.append("<TR>");
			
			sb.append("<TD><TABLE><TR><TD>SYM</TD><TD>TYP</TD><TD>DT</TD><TD>P</TD><TD>QTY</TD></TR>");
			sb.append("<TR>").append( this.getsNyseTx() ).append("</TR>");
			if( this.getbNyseTxList() != null && !this.getbNyseTxList().isEmpty() ){
				for(final NyseTx nyseTx : this.getbNyseTxList()){
					sb.append("<TR>").append( nyseTx ).append("</TR>");
				}
			}else{
				sb.append( "<TR><TD COLSPAN='5'>NOT KNOWN</TD></TR>" );
			}
			sb.append("</TABLE></TD>");
			
			sb.append("<TD align=right>");
			sb.append(salesProceeds);
			sb.append("</TD>");

			sb.append("<TD align=right>");
			sb.append(this.getCost() +(this.getBasis() != null ? this.getBasis() : 0.0));
			sb.append("</TD>");

			sb.append("<TD>");
			sb.append(this.isWashSale() ? "W" : "&nbsp;");
			sb.append("</TD>");

			sb.append("<TD align=right>");
			sb.append(this.isWashSale() ? getAdjustmentAmount() : "&nbsp;");
			sb.append("</TD>");
			
			sb.append("<TD>");
			sb.append( (this.getBasisComments() != null ? this.getBasisComments() : "&nbsp;" ));
			sb.append("</TD></TR>");

			return sb.toString();
		}
	}
}
