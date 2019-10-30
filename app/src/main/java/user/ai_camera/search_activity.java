package user.ai_camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class search_activity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView((R.id.searchView))
    SearchView searchView;
    @InjectView(R.id.listView)
    ListView listView;


    private ListAdapter listAdapter;
    private PlacesClient placesClient;
    private SearchHelper searchHelper;
	private HttpAsyncTask httpConnect;
	private Handler samHandler;

    private String keyword;
    private String name[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
		ButterKnife.inject(this);


		Bundle bundle = getIntent().getExtras();
        if(getIntent().hasExtra("name")) {
			name = bundle.getStringArray("name");
		}

		searchHelper = new SearchHelper();
		httpConnect = new HttpAsyncTask();
		samHandler = new Handler();
		name = new String[]{};
		initView();
		int PERMISSION_ALL = 2;
		String[] PERMISSIONS = {
				android.Manifest.permission.CAMERA,
				android.Manifest.permission.ACCESS_NETWORK_STATE,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.INTERNET
		};

		if(!hasPermissions(this, PERMISSIONS)){
			ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
		}

    }


    private void getCurrentPlaceItems() {
        if (searchHelper.isLocationAccessPermitted(this)) {
            getCurrentPlaceData();
        } else {
            searchHelper.requestLocationAccessPermission(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentPlaceData() {
       /* // Define a Place ID.
        String placeId = "ChIJy02Q7MEjaDQRVuRcRdQpwc0";
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG);
        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .build();
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            nameList.add(place.getLatLng().toString());
            nameList.add(place.getId());
            String name[] = new String[nameList.size()];
            name = nameList.toArray(name);
            listAdapter = new ArrayAdapter<String>(search_activity.this, android.R.layout.simple_list_item_1, name);
            listView.setAdapter(listAdapter);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());

            }
        });
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).*/
		String url ="https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + keyword + "&key="+MainActivity.KEY;
		httpConnect.execute(url);
		Runnable samRun = new Runnable() {
			@Override
			public void run() {
				if(httpConnect.resultBack().length > 0) {
					Log.d("Search Keyword ResultBack Test","in");
					listAdapter = new ArrayAdapter<String>(search_activity.this, android.R.layout.simple_list_item_1, httpConnect.resultBack());
					listView.setAdapter(listAdapter);
					httpConnect = new HttpAsyncTask();
					samHandler.removeCallbacks(this);
				}else{
					samHandler.postDelayed(this, 100);
				}
			}
		};
		samHandler.postDelayed(samRun,100);




    }
	private  void initView(){
		setSupportActionBar(toolbar);
		//toolbar.setTitle(R.string.NCU);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		listAdapter = new ArrayAdapter<String>(search_activity.this, android.R.layout.simple_list_item_1, name);
		listView.setAdapter(listAdapter);
		//places = new String[]{"1", "2"};
		//listAdapter = new ArrayAdapter<String>(search_activity.this, android.R.layout.simple_list_item_1, places);
		//listView.setAdapter(listAdapter);
		searchView.onActionViewExpanded();
		searchView.setQueryHint(keyword);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				keyword = searchView.getQuery().toString();
				//Toast.makeText(getApplicationContext(), keyword, Toast.LENGTH_SHORT).show();
				searchView.setQueryHint(keyword);
				//searchView.setIconified(true);
				getCurrentPlaceItems();
				// used to get places id
				return false;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				keyword = searchView.getQuery().toString();
				return false;
			}
		});
		Places.initialize(getApplicationContext(), "AIzaSyDrfOr8hxLrb1LhLn28fJvQvdfHu53oXPc");
		placesClient = Places.createClient(this);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				String selectedFromList = (String) listView.getItemAtPosition(position);
				Intent intent = new Intent(search_activity.this,MainActivity.class);
				intent.putExtra("selectedFromList",selectedFromList);
				intent.putExtra("keyword",keyword);
				startActivity(intent);
				finish();
			}
		});
	}
	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}

}
