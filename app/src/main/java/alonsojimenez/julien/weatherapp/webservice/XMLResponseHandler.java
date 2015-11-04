package alonsojimenez.julien.weatherapp.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * Process the response to a GET request to the Web service 
 * http://www.webservicex.net/globalweather.asmx?op=GetWeather
 * 
 * @author Stephane Huet
 *
 */
public class XMLResponseHandler {

	private static final String TAG = XMLResponseHandler.class.getSimpleName();
	
	private static final String ROOT_TAG = "string";
	private static final String ROOT2_TAG = "CurrentWeather";
	private static final String TIME_TAG = "Time";
	private static final String WIND_TAG = "Wind";
	private static final String TEMPERATURE_TAG = "Temperature";
	private static final String PRESSURE_TAG = "Pressure";
	
	private boolean mIsParsingTime, mIsParsingWind, mIsParsingTemperature, mIsParsingPressure, mIsParsingString;
	private String mTime, mWind, mTemperature, mPressure, mString;
	private ArrayList<String> mRes;
	
	/**
	 * @param response done by the Web service
	 * @param encoding of the response
	 * @return A list of four Strings (wind, temperature, pressure, time) if response was
	 * successfully analyzed; a void list otherwise
	 */
	public List<String> handleResponse(InputStream response, String encoding)
			throws IOException {
		mRes = new ArrayList<String>();
		try {
            /* Format of the 2 embedded files
             * <?xml version="1.0" encoding="utf-8"?>
             * <string>
             * <?xml version="1.0" encoding="utf-16"?>
			 * <CurrentWeather>
             * <Location>Seoul / Kimp'O International Airport, Korea, South (RKSS) 37-33N 126-48E 18M</Location>
             * <Time>Nov 06, 2014 - 04:00 PM EST / 2014.11.06 2100 UTC</Time>
             * <Wind> from the NNW (340 degrees) at 2 MPH (2 KT):0</Wind>
             * <Visibility> greater than 7 mile(s):0</Visibility>
             * <Temperature> 35 F (2 C)</Temperature>
             * <DewPoint> 28 F (-2 C)</DewPoint>
             * <RelativeHumidity> 74%</RelativeHumidity>
             * <Pressure> 30.27 in. Hg (1025 hPa)</Pressure>
             * <Status>Success</Status>
             * </CurrentWeather>
             * </string>
             */
			// Create the Pull Parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(response, encoding);

            // start iterating over the first XML document
            iterate(xpp);

            // Create another Pull Parser
            xpp = factory.newPullParser();
            xpp.setInput(new StringReader(mString));
            
            // start iterating over the 2nd embedded XML document
            iterate(xpp);
            
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getMessage());
		}
		return mRes;
	
	}
	
	private void iterate(XmlPullParser xpp) throws XmlPullParserException, IOException {
		int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	if (eventType == XmlPullParser.START_TAG) {
        		startTag(xpp.getName());
        	} else if (eventType == XmlPullParser.END_TAG) {
        		endTag(xpp.getName());
        	} else if (eventType == XmlPullParser.TEXT) {
        		text(xpp.getText());
        	}
        	eventType = xpp.next();
        }
	}
	
	private void startTag(String localName) {
		if (localName.equals(ROOT_TAG)) {
			mIsParsingString = true;
		} else if (localName.equals(TIME_TAG)) {
			mIsParsingTime = true;
		} else if (localName.equals(WIND_TAG)) {
			mIsParsingWind = true;
		} else if (localName.equals(TEMPERATURE_TAG)) {
			mIsParsingTemperature = true;
		} else if (localName.equals(PRESSURE_TAG)) {
			mIsParsingPressure = true;
		}
	}
	
	private void text(String text) {
		//Log.d(TAG,"text="+text);
		if (mIsParsingString) {
			mString = text.trim();
		} else if (mIsParsingTime) {
			// Nov 06, 2014 - 04:00 PM EST / 2014.11.06 2100 UTC
			Pattern pattern = Pattern.compile(" / (\\d{4}\\.\\d{2}\\.\\d{2} \\d{4} UTC)");
			Matcher matcher = pattern.matcher(text.trim());
			if (matcher.find())
				mTime = matcher.group(1);
			Log.d(TAG,"time="+mTime);
		} else if (mIsParsingWind) {
			// from the NNW (340 degrees) at 2 MPH (2 KT):0
			Pattern pattern = Pattern.compile("from the ([NEWS]+) \\(\\d+ degrees\\) at (\\d+\\.?\\d*) MPH ");
			Matcher matcher = pattern.matcher(text.trim());
			if (matcher.find())
				mWind = mph2kmh(Integer.parseInt(matcher.group(2)))+"km/h ("+matcher.group(1)+")";
			else if (text.contains("Calm"))
				mWind = "0 km/h";
			Log.d(TAG,"wind="+mWind);
		} else if (mIsParsingTemperature) {
			//  35 F (2 C)
			Pattern pattern = Pattern.compile(" \\((-?\\d+\\.?\\d* C)\\)");
			Matcher matcher = pattern.matcher(text.trim());
			if (matcher.find())
				mTemperature = matcher.group(1);
			Log.d(TAG,"temperature="+mTemperature);
		} else if (mIsParsingPressure) {
			//  30.27 in. Hg (1025 hPa)
			Pattern pattern = Pattern.compile(" \\((\\d+ hPa)\\)");
			Matcher matcher = pattern.matcher(text.trim());
			if (matcher.find())
				mPressure = matcher.group(1);
			Log.d(TAG,"pressure="+mPressure);
		}
	}

	private int mph2kmh(int n) {
		return (int) (n/1.609344);
	}

	private void endTag(String localName) {
		if (localName.equals(ROOT_TAG)) {
			mIsParsingString = false;
		} else if (localName.equals(TIME_TAG)) {
			mIsParsingTime = false;
		} else if (localName.equals(WIND_TAG)) {
			mIsParsingWind = false;
		} else if (localName.equals(TEMPERATURE_TAG)) {
			mIsParsingTemperature = false;
		} else if (localName.equals(PRESSURE_TAG)) {
			mIsParsingPressure = false;
		} else if (localName.equals(ROOT2_TAG)) {
			mRes.add(mWind);
			mRes.add(mTemperature);
			mRes.add(mPressure);
			mRes.add(mTime);
			mWind = "";
			mTemperature = "";
			mPressure = "";
			mTime = "";
		}
	}

}
