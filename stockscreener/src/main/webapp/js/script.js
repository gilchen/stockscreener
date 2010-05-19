function round(value){
	var result = Math.round(value*100)/100;  //returns 28.45
	return result;
}

function showChart(imgObj, symbol){
	var yahooUrl = "http://ichart.finance.yahoo.com/z?s=" +symbol+ "&t=1y&q=l&l=on&z=l&p=s&a=v&p=s&lang=en-US&region=US";
	imgObj.style.display="block";
	imgObj.src = yahooUrl;
}

function showGoogleChart(imgObj, imgUrl){
	imgObj.style.display="block";
	imgObj.src = imgUrl;
}