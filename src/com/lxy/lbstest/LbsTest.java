package com.lxy.lbstest;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;
import android.location.Location;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TabHost; 
import android.widget.TextView;
//import android.widget.Toast;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.view.KeyEvent; 
import 	android.graphics.Bitmap; 

public class LbsTest extends Activity implements GeolocationPermissions.Callback {
	WebView browser; 
	TextView outputWindow; 
	TabHost tabhost; 
	Locater mLocater; 
	private String PROVIDER; // LocationManager.GPS_PROVIDER;
	private LocationManager myLocationManager=null;
	private ProgressDialog progressBar;
	private static final String LOG = "LbsTest";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((tabhost.getCurrentTab()==1) && (keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
        	browser.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onButtonGetGeo(View theButton){
    	double latitude = mLocater.getLatitude();
    	double longtitude = mLocater.getLongitude();
    	if (latitude !=0 && longtitude !=0){
    		outputWindow.append("\nLatidue is "+latitude);
    		outputWindow.append("\nLongtitude is "+longtitude);
    		tabhost.setCurrentTab(1);
    	}
    	else{
    		outputWindow.append("\nGet Geo position failed");
    	}
    }
    
    public void onButtonGetGeoJS2Native(View theButton){
    	browser.loadUrl("file:///android_asset/geo.html");
    	tabhost.setCurrentTab(0);
    }
    
    public void onButtonDisplayGoogleMapGPS(View theButton){
    	browser.loadUrl("file:///android_asset/map_gps.html");
    	tabhost.setCurrentTab(0);
    }
    
    // to load a URL input from user
    public void onButtonLoadUrl(View theButton){    	
    	EditText edit = (EditText)findViewById(R.id.editUrl);
    	String url = edit.getText().toString();
    	if (url.length()>0){
    		browser.loadUrl(url);
    		outputWindow.append("\nLoading "+url);
    		tabhost.setCurrentTab(0);
    	}else {
    		outputWindow.append("\nURL is null");
    	}   	 
    }
    
    private void init(){
        // Doesn't work
    	//getWindow().requestFeature(Window.FEATURE_PROGRESS);
    	
    	// initialize tabs and tabs contents
    	tabhost = (TabHost)findViewById(R.id.tabhost);
        tabhost.setup();
        
        TabHost.TabSpec tabspec = tabhost.newTabSpec("webview");
        tabspec.setContent(R.id.webview);
        tabspec.setIndicator("Main");
        tabhost.addTab(tabspec);
        
        tabspec = tabhost.newTabSpec("OutputWindow");
        tabspec.setContent(R.id.OutputWindow);
        tabspec.setIndicator("Output");
        tabhost.addTab(tabspec);
        
        tabspec = tabhost.newTabSpec("console");
        tabspec.setContent(R.id.console);
        tabspec.setIndicator("Console");
        tabhost.addTab(tabspec);
        
        //initialize output window
        outputWindow = (TextView)findViewById(R.id.OutputWindow);
        
        // initialize location stuffs. 
        myLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        // List all providers:
		List<String> providers = myLocationManager.getAllProviders();
		outputWindow.append("\nAll location service providers:");
		for (String pro : providers) {
			outputWindow.append("\n" + pro);
		}
		Criteria criteria = new Criteria();  
		criteria.setAccuracy(Criteria.ACCURACY_FINE);  
		criteria.setAltitudeRequired(false);  
		criteria.setBearingRequired(false);  
		criteria.setCostAllowed(true);  
		criteria.setPowerRequirement(Criteria.POWER_LOW);  
		PROVIDER = myLocationManager.getBestProvider(criteria, false);
		outputWindow.append("\nBEST Provider:" + PROVIDER);
		
		// register location update listener .
        mLocater = new Locater();         
        myLocationManager.requestLocationUpdates(PROVIDER, 2000,10.0f,onLocationChange);
        
        //initialize webview
        //Activity.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        browser = (WebView)findViewById(R.id.webview);
        browser.setWebViewClient(new WebViewClient() {     
            @Override     
            public boolean shouldOverrideUrlLoading(WebView view, String url){     
            	view.loadUrl(url);     
            	return true; // to open URL in the same web view     
            }            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(LOG, "onPageStarted: " + url);
                if (!progressBar.isShowing()) {
                	progressBar.show();
                }
            }             
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(LOG, "onPageFinished: " + url);
                if (progressBar.isShowing()) {
                	progressBar.dismiss();
                }
            }            
        });   
        
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        browser.addJavascriptInterface(mLocater, "locater");
        //seems like if i set this, the webview should prompt when I call navigator.geolocation.getCurrentPosition
        browser.getSettings().setGeolocationEnabled(true); 
        //GeolocationPermissions geoPerm = new GeolocationPermissions(); //added in API Level 5 but no methods exposed until API level 7
        GeoClient geo = new GeoClient();
        browser.setWebChromeClient(geo);        
        String origin = ""; //how to get origin in correct format?
        geo.onGeolocationPermissionsShowPrompt(origin, this);  
        
        browser.loadUrl("file:///android_asset/map_gps.html");
        //browser.loadUrl("file:///android_asset/tracker.html");
        //browser.loadUrl("http://10.0.2.2:3000");
        //browser.loadUrl("http://10.0.2.2:8080"); //"http://www.baidu.com"        
        progressBar = ProgressDialog.show(this, "", "Loading...");
    }
    
    final class GeoClient extends WebChromeClient {
    	@Override
    	public void onGeolocationPermissionsShowPrompt(String origin,Callback callback) {
	    	// TODO Auto-generated method stub
	    	super.onGeolocationPermissionsShowPrompt(origin, callback);
	    	callback.invoke(origin, true, false);
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	myLocationManager.requestLocationUpdates(PROVIDER, 2000,10.0f,onLocationChange);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	myLocationManager.removeUpdates(onLocationChange);
    }
    
    LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location location) {        	 
        	 // call javacript to update html page. for (geo.html) only 
        	 StringBuilder buf=new StringBuilder("javascript:whereami(");      
             buf.append(String.valueOf(location.getLatitude()));
             buf.append(",");
             buf.append(String.valueOf(location.getLongitude()));
             buf.append(")");             
             outputWindow.append("\nStart to call JS:" + buf.toString());
             browser.loadUrl(buf.toString());
        }        
        public void onProviderDisabled(String provider) {
          // required for interface, not used
        	outputWindow.append("\n onProviderDisabled" + provider);
        }        
        public void onProviderEnabled(String provider) {
          // required for interface, not used
        	outputWindow.append("\n onProviderEnabled" + provider);
        }        
        public void onStatusChanged(String provider, int status,
                                     Bundle extras) {
          // required for interface, not used
        	outputWindow.append("\n onStatusChanged" + provider + "status=" + status);
        }
     };
    
    // class to get GPS position
    public class Locater {
        public double getLatitude() {
        	Location loc=myLocationManager.getLastKnownLocation(PROVIDER);
        	if (loc==null) {
        		outputWindow.append("\ngetLatitude return 0");
        		return(0);
        	}
        	return(loc.getLatitude());
        }        
        public double getLongitude() {
        	Location loc=myLocationManager.getLastKnownLocation(PROVIDER);
        	if (loc==null) {
        		outputWindow.append("\ngetLongitude return 0");
        		return(0);
        	}
        	return(loc.getLongitude());
        }
    }

	@Override
	public void invoke(String origin, boolean allow, boolean remember) {
		// TODO Auto-generated method stub
		
	}
}