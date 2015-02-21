package tud.kom.dss6.localsiri.knowuraddress;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import tud.kom.dss6.localsiri.R;
import tud.kom.dss6.localsiri.localservice.LocationMain;
import tud.kom.dss6.localsiri.localservice.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class KnowYourAddress extends Activity {

	private TextView mAddress;
	private ImageView mAddressImage;
	private ProgressBar mActivityIndicator;
	protected LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_know_your_address);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mAddress = (TextView) findViewById(R.id.address);
		mAddressImage = (ImageView) findViewById(R.id.ask_me_result);
		mActivityIndicator = (ProgressBar) findViewById(R.id.address_progress);
	}

	public void findAddress(View view) {
		Location mLocation;

		mLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if(mLocation == null){
		Log.e("Change in provider:", "Trying Network Provider...");
		mLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent()) {
			if(mLocation != null){
				// Show the activity indicator
				mActivityIndicator.setVisibility(View.VISIBLE);
				/*
				 * Reverse geocoding is long-running and synchronous. Run it on a
				 * background thread. Pass the current location to the background
				 * task. When the task finishes, onPostExecute() displays the
				 * address.
				 */
				(new GetAddressTask(this)).execute(mLocation);
			}else{
				mActivityIndicator.setVisibility(View.GONE);
				
				Toast.makeText(this,
						"Location is Empty. Please reset GPS Switch",
						Toast.LENGTH_SHORT).show();
				
				
			}
			
		} else {
			Toast.makeText(this,
					"Sorry! Your Device does not support this feature",
					Toast.LENGTH_SHORT).show();
		}

		mActivityIndicator.setVisibility(View.VISIBLE);
	}

	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		/**
		 * Get a Geocoder instance, get the latitude and longitude look up the
		 * address, and return it
		 * 
		 * @params params One or more Location objects
		 * @return A string containing the address of the current location, or
		 *         an empty string if no address can be found, or an error
		 *         message
		 */
		@Override
		protected String doInBackground(Location... params) {

			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				/*
				 * Return 1 address.
				 */
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception trying to get address");
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(loc.getLatitude()) + " , "
						+ Double.toString(loc.getLongitude())
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				e2.printStackTrace();
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				/*
				 * Format the first line of address (if available), city, and
				 * country name.
				 */
				String addressText = String.format(
						"%s, %s, %s",
						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "",
						// Locality is usually a city
						address.getLocality(),
						// The country of the address
						address.getCountryName());
				// Return the text
				return addressText;
			} else {
				return "No address found";
			}

		}

		@Override
		protected void onPostExecute(String address) {
			// Set activity indicator visibility to "gone"
			mActivityIndicator.setVisibility(View.GONE);
			// Display the results of the lookup.
			mAddress.setText(address);
			mAddressImage.setVisibility(View.VISIBLE);
			mAddress.setVisibility(View.VISIBLE);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.location_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.d("Menu", "Clicked Home");
			Intent intent = new Intent(this, LocationMain.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.menu_settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		}
		return false;
	}
}
