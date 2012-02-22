package com.stocks.standalone;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.stocks.model.NyseTx;
import com.stocks.util.Utility;

public class TaxPreparerTest extends TestCase {
	List<NyseTx> nyseTxList = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		LineNumberReader lnr = null;
		try{
			lnr = new LineNumberReader(new InputStreamReader( TaxPreparerTest.class.getResourceAsStream("/NyseTxTest.txt")));
			String line = lnr.readLine(); // Ignore first line
			nyseTxList = new ArrayList<NyseTx>();
			while( (line = lnr.readLine()) != null ){
				nyseTxList.add( strToNyseTx(line) );
			}
		}
		finally{
			if(lnr != null){
				lnr.close();
			}
		}
	}
	
	private NyseTx strToNyseTx(String line) throws Exception{
		final String[] arr = line.split(",", -1);
		final NyseTx nyseTx = new NyseTx();
		nyseTx.setNyseTxId( new Long(arr[0]) );
		nyseTx.setTrxType(arr[1]);
		nyseTx.setSymbol(arr[2]);
		nyseTx.setTxDate( Utility.getDate(arr[3]) );
		nyseTx.setPrice(new Double(arr[4]));
		nyseTx.setQty(new Integer(arr[5]));
		nyseTx.setTxFee(new Double(arr[6]));
		nyseTx.setComments(arr[7]);

		return nyseTx;
	}
	
	@Test
	public void testProcess(){
		TaxPreparer taxPreparer = new TaxPreparer();
		try {
			taxPreparer.process(nyseTxList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
