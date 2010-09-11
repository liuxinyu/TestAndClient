package com.lxy.lbstest;

import android.app.Activity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TabHost; 
import android.widget.TextView;
//import android.widget.Toast;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.view.KeyEvent; 

public class LbsTest extends Activity {
	WebView browser; 
	TextView outputWindow; 
	TabHost tabhost; 
	private static String PROVIDER=LocationManager.GPS_PROVIDER;
	private LocationManager myLocationManager=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
        	browser.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onButtonGetGeo(View theButton){
    	outputWindow.append("\nGet Geo function is to be added");
    }
    

    
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
        
        //initialize webview
        browser = (WebView)findViewById(R.id.webview);
        browser.getSettings().setJavaScriptEnabled(true);
        
        browser.setWebViewClient(new WebViewClient() {     
            @Override     
            public boolean shouldOverrideUrlLoading(WebView view, String url){     
              view.loadUrl(url);     
              return true; // to open URL in the same web view     
            }     
          });    
        myLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(new Locater(), "locater");
        //browser.loadUrl("http://www.baidu.com");
        browser.loadUrl("file:///android_asset/Geo.html"); 
        //browser.loadUrl("http://10.0.2.2:8080"); //"http://www.baidu.com"        
        
        //initialize output window
        outputWindow = (TextView)findViewById(R.id.OutputWindow);
    }
    
    @Override
    public void onResume() {
      super.onResume();
      myLocationManager.requestLocationUpdates(PROVIDER, 
    		  									10000,
    		  									100.0f,
    		  									onLocationChange);
    }
    
    @Override
    public void onPause() {
      super.onPause();
      myLocationManager.removeUpdates(onLocationChange);
    }
    
    LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location location) {
          // ignore...for now
        }        
        public void onProviderDisabled(String provider) {
          // required for interface, not used
        }        
        public void onProviderEnabled(String provider) {
          // required for interface, not used
        }        
        public void onStatusChanged(String provider, int status,
                                     Bundle extras) {
          // required for interface, not used
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
}