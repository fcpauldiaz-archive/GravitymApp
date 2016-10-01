package uvg.app.gravitymapp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	/* Datos Teóricos */
	double G = 0.00000000006674;
	double mTierra = 5.972*Math.pow(10, 24);
	double vAngular = Math.PI/86400;
	double rEcuatorial = 0;
	WebView webView1,webView2;
	Button getLocationButton;
	TextView lati,longi,alti;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        lati =  (TextView)findViewById(R.id.textView3);
        longi =  (TextView)findViewById(R.id.textView4);
        alti =  (TextView)findViewById(R.id.textView5);
        getLocationButton = (Button) findViewById(R.id.button1);
        

        getLocationButton.setOnClickListener(new OnClickListener() 
		  {
			public void onClick(View v) 
			{
                GPSTracker gps;
                gps = new GPSTracker(MainActivity.this);
                 
                // check if GPS enabled     
                if(gps.canGetLocation()){

                    // \n is for new line
                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show(); 
                    try{
	                	lati.setText("" + gps.getLatitude());
	                	
	                    longi.setText("" + gps.getLongitude());
	                    Toast.makeText(getApplicationContext(),"altitud: " + gps.getAltitud(), Toast.LENGTH_LONG).show();
	                    alti.setText("" + getAltitude(gps.getLongitude(), gps.getLatitude()));
                    }catch(Exception e){
                    	Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show(); 
                    }
                }else{
                    // no puede obtener la ubicación
                    // GPS o Network no está habilitado
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
                
			}
        });
		
	}
    
    private double calcularGravedad(){
    	
    	
    	return 0;
    }

    private double getAltitude(Double longitude, Double latitude) {
        double result = Double.NaN;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://maps.googleapis.com/maps/api/elevation/"
                + "xml?locations=" + String.valueOf(latitude)
                + "," + String.valueOf(longitude)
                + "&sensor=true";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                int r = -1;
                StringBuffer respStr = new StringBuffer();
                while ((r = instream.read()) != -1)
                    respStr.append((char) r);
                String tagOpen = "<elevation>";
                String tagClose = "</elevation>";
                if (respStr.indexOf(tagOpen) != -1) {
                    int start = respStr.indexOf(tagOpen) + tagOpen.length();
                    int end = respStr.indexOf(tagClose);
                    String value = respStr.substring(start, end);
                    result = (double)(Double.parseDouble(value)*3.2808399); // convert from meters to feet
                }
                instream.close();
            }
        } catch (ClientProtocolException e) {} 
        catch (IOException e) {}

        return result;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
	
	public class GPSTracker extends Service implements LocationListener {

	    private final Context mContext;

	    // flag for GPS status
	    boolean isGPSEnabled = false;

	    // flag for network status
	    boolean isNetworkEnabled = false;

	    // flag for GPS status
	    boolean canGetLocation = false;

	    Location location; // location
	    double latitude; // latitude
	    double longitude; // longitude
	    double altitud; // longitude

	    // The minimum distance to change Updates in meters
	    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	    // The minimum time between updates in milliseconds
	    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	    // Declaring a Location Manager
	    protected LocationManager locationManager;

	    public GPSTracker(Context context) {
	        this.mContext = context;
	        getLocation();
	    }

	    public Location getLocation() {
	        try {
	            locationManager = (LocationManager) mContext
	                    .getSystemService(LOCATION_SERVICE);

	            // getting GPS status
	            isGPSEnabled = locationManager
	                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

	            // getting network status
	            isNetworkEnabled = locationManager
	                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	            if (!isGPSEnabled && !isNetworkEnabled) {
	                // no network provider is enabled
	            } else {
	                this.canGetLocation = true;
	                if (isNetworkEnabled) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.NETWORK_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                    Log.d("Network", "Network");
	                    if (locationManager != null) {
	                        location = locationManager
	                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                        if (location != null) {
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                            altitud = location.getAltitude();
	                        }
	                    }
	                }
	                // if GPS Enabled get lat/long using GPS Services
	                if (isGPSEnabled) 
	                {
	                    if (location == null) {
	                        locationManager.requestLocationUpdates(
	                                LocationManager.GPS_PROVIDER,
	                                MIN_TIME_BW_UPDATES,
	                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                        Log.d("GPS Enabled", "GPS Enabled");
	                        if (locationManager != null) 
	                        {
	                            location = locationManager
	                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                            if (location != null) 
	                            {
	                                latitude = location.getLatitude();
	                                longitude = location.getLongitude();
	                                altitud = location.getAltitude();
	                            }
	                        }
	                    }
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return location;
	    }
	 
	    /**
	     * Stop using GPS listener
	     * Calling this function will stop using GPS in your app
	     * */
	    public void stopUsingGPS(){
	        if(locationManager != null){
	            locationManager.removeUpdates(GPSTracker.this);
	        }     
	    }
	 
	    /**
	     * Function to get latitude
	     * */
	    public double getLatitude(){
	        if(location != null){
	            latitude = location.getLatitude();
	        }
	     
	        // return latitude
	        return latitude;
	    }
	 
	    /**
	     * Function to get longitude
	     * */
	    public double getLongitude(){
	        if(location != null){
	            longitude = location.getLongitude();
	        }
	     
	        // return longitude
	        return longitude;
	    }
	    
	    /**
	     * Function to get altitud
	     * */
	    public double getAltitud(){
	        if(location != null){
	            altitud = location.getAltitude();
	        }
	        // return longitude
	        return altitud;
	    }
	 
	    /**
	     * Function to check GPS/wifi enabled
	     * @return boolean
	     * */
	    public boolean canGetLocation() {
	        return this.canGetLocation;
	    }
	 
	    /**
	     * Function to show settings alert dialog
	     * On pressing Settings button will lauch Settings Options
	     * */
	    public void showSettingsAlert(){
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
	      
	        // Setting Dialog Title
	        alertDialog.setTitle("GPS is settings");

	        // Setting Dialog Message
	        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

	        // On pressing Settings button
	        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                mContext.startActivity(intent);
	            }
	        });

	        // on pressing cancel button
	        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	            }
	        });

	        // Showing Alert Message
	        alertDialog.show();
	    }

	    @Override
	    public void onLocationChanged(Location location) {
	    }

	    @Override
	    public void onProviderDisabled(String provider) {
	    }

	    @Override
	    public void onProviderEnabled(String provider) {
	    }

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }



		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    /***********************************************************************************/
    

}
