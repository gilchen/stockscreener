function round(value){
	var result = Math.round(value*100)/100;  //returns 28.45
	return result;
}

function showChart(imgObj, symbol){
	var yahooUrl = "http://chart.finance.yahoo.com/c/6m/c/" +symbol+ "?lang=en-US&region=US";
	//alert( relativeObj + "\n" +yahooUrl );
	imgObj.style.visibility='visible';
	imgObj.src = yahooUrl;
}