package user.ai_camera;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    @InjectView(R.id.toolbar1)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.SwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView((R.id.navigation))
	BottomNavigationView navigation;
    @InjectView((R.id.searchImage))
    ImageButton searchImage;
    @InjectView(R.id.textView4)
    TextView textView;
    @InjectView(R.id.tabhost)
	TabHost tabHost;
    /*@InjectView(R.id.searchView)
    SimpleSearchView simpleSearchView;
    @InjectView(R.id.scrollView2)
    ScrollView scrollView;
    @InjectView(R.id.imageButton)
    ImageButton imageButton;
  */

    public String account = null;
    public String authentication = null;
	public SharedPreferences mSharedPreferences;
	public Handler backgroundhandler = new Handler();
	public Runnable background_run;
	private final static int LOGIN_SIGNUP = 2;
	public final static String KEY_MAP = "AIzaSyBsbznj_CQUJicINQbL1eZ4B6F5JOemHMU";
	public final static String KEY = "AIzaSyDrfOr8hxLrb1LhLn28fJvQvdfHu53oXPc";

	private String[] name = null;
	private final String[] supportTypeSet = {
			"accounting"
	};
	private final boolean[] endFlag = {false};

	private String keyword="Taipei101", user_keyword, selectedFromList;
	private boolean isLoading, isLoadingFirst = true;

	public List<Map<String,String>> imgUrl = new ArrayList<>();
 	private List<Map<String, Object>> data = new ArrayList<>();
	public List<Map<String,String>> collection = new ArrayList<>();
	private KeywordFindingTask httpConnect;
	private AskNimaTask httpConnect3 = new AskNimaTask();
	private Handler samHandler = new Handler();
	private Handler handler = new Handler();
	private GoogleMap mGoogleMap;
	private List<Marker> markerList = new ArrayList();
	private List<Bitmap> bitmapList = new ArrayList<>();

    public RecyclerViewAdapter adapter;
    private SearchHelper searchHelper = new SearchHelper();

	public FragmentTransaction mFragmentTransaction;
	public FragmentManager mFragmentManager;
	public List<Fragment> mainFragmentStack ;
	public List<Fragment> personalFragmentStack;
	public Photo_detail mPhotoFragment;
	public Personal_Page mPersonalFragment = new Personal_Page();
	public RecyclerViewAdapter.ItemViewHolder tempHolder;
	public HashMap<Integer,Photo_detail> mPhotoFragmentMap = new HashMap<Integer, Photo_detail>();
	public boolean IsPersonal = false;



    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home: {
                	if(IsPersonal && personalFragmentStack.get(personalFragmentStack.size()-1) != null){
						onBackPressedHome(true);
						Log.d("home click","case1");
					}else if(!IsPersonal && (mainFragmentStack.get(mainFragmentStack.size()-1) == null)){
						//recyclerView.smoothScrollToPosition(0);
						//Log.d("home click test2",String.valueOf(mainFragmentStack.size()));
						searchImage.setVisibility(View.VISIBLE);
						Log.d("home click","case2");
					}else if(!IsPersonal && (mainFragmentStack.get(mainFragmentStack.size()-1) != null)){
						//Log.d("home click test3",String.valueOf(mainFragmentStack.size()));
						Handler handler = new Handler();
						handler.postDelayed(()->{
							updateCollection(mainFragmentStack);
							mFragmentManager.beginTransaction().remove(mainFragmentStack.get(mainFragmentStack.size()-1)).commit();
							mainFragmentStack.remove(mainFragmentStack.size()-1);
							if (mainFragmentStack.size() == 1){
								backToHome();
							}
							Log.d("home click","case3");
						},100);
					}
					IsPersonal = false;
					Log.d("main fragment test",String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
							mainFragmentStack.size(),personalFragmentStack.size(),String.valueOf(IsPersonal)));
					return true;
                }

                case R.id.navigation_circle: {
					//hideKeyboard(MainActivity.super.getParent(),searchView);
					Handler handler = new Handler();
					handler.postDelayed(()->{
						Intent intent1 = new Intent(MainActivity.this,camera_activity.class);
						if(mainFragmentStack.get(mainFragmentStack.size()-1) instanceof Photo_detail){
							ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
							Bitmap bitmap = ((BitmapDrawable)((Photo_detail)mainFragmentStack.get(mainFragmentStack.size()-1)).ib_photo.getDrawable())
									.getBitmap();
							bitmap = Bitmap.createScaledBitmap(bitmap,600,600,true);
							bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayStream);
							Log.d("test byteArray size before", String.valueOf(byteArrayStream.toByteArray().length));
							intent1.putExtra("byteArray", byteArrayStream.toByteArray());
							intent1.putExtra("ImageVisibleSemarphore",true);
							//intent1.putExtra("test","tesrsadasdasdaasjkdhlsanlfd");
							//onBackPressedHome();
							//mainFragmentStack.remove(mainFragmentStack.size()-1);
							Log.d("test byteArray size after", String.valueOf(byteArrayStream.toByteArray().length));
						}else{
							intent1.putExtra("ImageVisibleSemarphore",false);
						}
						intent1.putExtra("keyword", keyword);
						Log.d("test startactivity test","before" );
						//Toast.makeText(getApplicationContext(),R.string.title_notifications,Toast.LENGTH_SHORT).show();
						startActivity(intent1);
						Log.d("test startactivity test","after" );

					},100);

					return false;
                }
                case R.id.navigation_personal: {

					if(!IsPersonal && personalFragmentStack.get(personalFragmentStack.size()-1) ==null){
						searchImage.setVisibility(View.GONE);
						//swipeRefreshLayout.setVisibility(View.GONE);
						Handler personalFrameHandler = new Handler();

						// if the page is photo detail and press profile without leaving photo detail
						if(mainFragmentStack.get(mainFragmentStack.size()-1 ) instanceof Photo_detail){
							updateCollection(mainFragmentStack);
						}
						if(!personalFragmentStack.contains(mPersonalFragment)){
							IsPersonal = true;
							personalFrameHandler.postDelayed(()->{
								mFragmentTransaction = mFragmentManager.beginTransaction();
								if (mFragmentManager.findFragmentByTag("Personal") != null) {
									mFragmentManager.popBackStack();
								}
								//if (mPersonalFragment == null) mPersonalFragment = new Personal_Page();
								Bundle bundle = new Bundle();
								//Log.d("Main Put collection", String.valueOf(bundle.containsKey("collection")));
								//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();

								mPersonalFragment.setArguments(bundle);
								if (mPersonalFragment.isAdded()) {
									mFragmentTransaction.remove(mPersonalFragment);
									Log.d("personal show test","personal is added");
								}
								mFragmentTransaction.add(android.R.id.tabcontent, mPersonalFragment, "Personal");
								mFragmentManager.executePendingTransactions();
								mFragmentTransaction.commit();
								personalFragmentStack.add(mPersonalFragment);
								String s = "Profile";
								textView.setText(s);
								Log.d("main fragment test",String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
										mainFragmentStack.size(),personalFragmentStack.size(),String.valueOf(IsPersonal)));
							}, 100);
						}
						Log.d("personal click","case1");
					}else if(!IsPersonal && mainFragmentStack.get(mainFragmentStack.size()-1) != null){
						onBackPressedPersonal(true);
						Log.d("personal click","case2");
					}else if(!IsPersonal && mainFragmentStack.get(mainFragmentStack.size()-1) == null){
						if(personalFragmentStack.get(personalFragmentStack.size()-1) instanceof Photo_detail) {
							//navigation.setSelectedItemId(R.id.navigation_home);
							updateCollection(personalFragmentStack);
							textView.setText("Photo Detail");
						}else if(personalFragmentStack.get(personalFragmentStack.size()-1) instanceof Personal_Page){
							textView.setText("Profile");
						}else if (personalFragmentStack.size() == 1){
							backToHome();
							navigation.setSelectedItemId(R.id.navigation_home);
							IsPersonal = false;
						}
						mFragmentManager.beginTransaction().show(personalFragmentStack.get(personalFragmentStack.size()-1)).commit();
						Log.d("personal click","case3");
					}else if(IsPersonal && personalFragmentStack.get(personalFragmentStack.size()-1) != null && personalFragmentStack.size() > 2){
						if(personalFragmentStack.get(personalFragmentStack.size()-1) instanceof  Photo_detail){
							updateCollection(personalFragmentStack);
						}
						mFragmentManager.beginTransaction().remove(personalFragmentStack.get(personalFragmentStack.size()-1)).commit();
						personalFragmentStack.remove(personalFragmentStack.size()-1);
						Log.d("personal click","case4");
					}
					IsPersonal = true;
					//navigation.setItemIconTintList(null);
					Log.d("main fragment test",String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
							mainFragmentStack.size(),personalFragmentStack.size(),String.valueOf(IsPersonal)));
					return true;
                }
            }
            return false;
        }
    };

	@Override
	public void onResume(){
		super.onResume();
		if(account == null && background_run != null){
			backgroundhandler.postDelayed(background_run,100);
			endFlag[0]= false;
			Log.d("Main test ","onResume test");
		}
	}
	@Override
	public void onPause(){
		super.onPause();
		if(account == null && background_run != null){
			endFlag[0]= true;
			backgroundhandler.removeCallbacks(background_run);
		}
	}

    @SuppressLint("ApplySharedPref")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		mSharedPreferences = getSharedPreferences("AICam",MODE_PRIVATE);
		mSharedPreferences.edit()
				//.putString("ACCOUNT","test1")
				//.putString("AUTH","thisIsTheTestAuthorization")
				//.remove("ACCOUNT")
				//.remove("AUTH")
				.commit();

		int PERMISSION_ALL = 2;
		String[] PERMISSIONS = {
				android.Manifest.permission.CAMERA,
				android.Manifest.permission.ACCESS_NETWORK_STATE,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.INTERNET,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.ACCESS_COARSE_LOCATION
		};

		if(!hasPermissions(this, PERMISSIONS)){
			ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
		}
		if (bundle != null) {
			user_keyword = bundle.getString("keyword");
			selectedFromList = bundle.getString("selectedFromList");
		}
		account = mSharedPreferences.getString("ACCOUNT",null);
		authentication = mSharedPreferences.getString("AUTH",null);

        if(account!= null){
        	initWithAccount();
		}else{
			setContentView(R.layout.main_layout_without_login);
			Button bn_login = findViewById(R.id.bn_login);
			Button bn_signup = findViewById(R.id.bn_signup);
			ConstraintLayout container = findViewById(R.id.container);

			final int[] color = {(int)Math.pow(16,6)-1};
			String[] hex_color = new String[1];
			int background[] = {R.drawable.image_2065, R.drawable.image_2066, R.drawable.image_2067, R.drawable.image_2068};
			final int[] index = {0};

			hex_color[0] = Integer.toHexString(color[0]);

			Log.d("color test","#" + hex_color[0]);
			background_run = new Runnable() {
				@Override
				public void run() {
					if(!endFlag[0]){
						if(color[0] > 5 * (int)Math.pow(16,7) + 6 * (int)Math.pow(16,6)){
							container.setBackgroundResource(background[index[0]]);
							index[0] = (++index[0]) % background.length;
							color[0] = (int)Math.pow(16,7) + 2 * (int)Math.pow(16,6)-1;
						}else{
							Log.d("color test","#" + hex_color[0]);
							container.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#" + hex_color[0] )));
							color[0] += (int)Math.pow(16,6);
							hex_color[0] = Integer.toHexString(color[0]);
							if(hex_color[0].length() < 8){
								hex_color[0] = "0"+hex_color[0];
							}
						}
						backgroundhandler.postDelayed(this,100);
					}else{
						Log.d("log in page end","end loop");
					}
				}
			};
			backgroundhandler.postDelayed(background_run,100);
			bn_login.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					endFlag[0] = true;
					Intent intent = new Intent(MainActivity.this,Login_activity.class);
					startActivityForResult(intent,LOGIN_SIGNUP);
				}
			});
			bn_signup.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					endFlag[0] = true;
					Intent intent = new Intent(MainActivity.this,Signup_activity.class);
					startActivityForResult(intent,LOGIN_SIGNUP);
				}
			});

		}


    }

    public void initView() {

        setSupportActionBar(toolbar);
        //toolbar.setTitle(keyword);
		//toolbar.setLogo(R.drawable.ic_camera_white_24p);
		textView.setText(keyword);


        //no title (AI camera)
		getSupportActionBar().setDisplayShowTitleEnabled(false);

        // to adjust the word size, need to get the mobie size
        Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
        // set size to fit to mobile
		Log.d("Main Size test",String.format("  size.x %d , size.y %d",size.x,size.y));
        textView.setTextSize((float)((size.x)/60.0));
        //searchView.setQueryHint(keyword);

		tabHost.setup();
		TabHost.TabSpec spec;
		spec =tabHost.newTabSpec("Tab 1");
		spec.setContent(R.id.SwipeRefreshLayout);
		spec.setIndicator("List");
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec("Tab 2");
		spec.setContent(R.id.maps);
		spec.setIndicator("Maps");
		tabHost.addTab(spec);

		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
					tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#FFFFFF")); // unselected
					TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
					tv.setTextColor(getColor(R.color.titleSelected));
				}

				tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#22000000")); // selected
				TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title); //for Selected Tab
				tv.setTextColor(getColor(R.color.lightBlue));

			}
		});


        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        // enable refresh
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //TODO  refreshing
						return;
                    	//data.clear();
						//adapter = new RecyclerViewAdapter(MainActivity.this,size, data,imgUrl);
						//recyclerView.setAdapter(adapter);
						//getData();
                    }
                }, 1000);*/
                //Toast.makeText(getApplicationContext(),"not found data",Toast.LENGTH_SHORT).show();
				swipeRefreshLayout.setRefreshing(false);
            }
        });


		adapter = new RecyclerViewAdapter(this,size, data,imgUrl);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //Log.d("RecyclerView Scrolled Test", "StateChanged = " + newState+"recyclerView.getScaleY()"+recyclerView.computeVerticalScrollOffset());
                // if on the top, then set word keyword, else set keyword plus POI nearby 
				if (!recyclerView.canScrollVertically(1) && adapter.index >= imgUrl.size() - 1) {
					Toast.makeText(MainActivity.this,"親，已經沒東西了",Toast.LENGTH_LONG).show();
				}
				if(recyclerView.computeVerticalScrollOffset()==0 ){
                        //searchView.setQueryHint(keyword);
                        textView.setText(keyword);
                    }
                    else{
                        //searchView.setQueryHint("POI nearby " + keyword);
                        textView.setText(("POI nearby " + keyword));
                    }
                }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                /*Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);*/

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisibleItemPosition +1 == adapter.getItemCount()) {
					if (!isLoadingFirst) {
						Log.d("RecyclerView Scroll Index Notification",String.valueOf(adapter.maxindex)+" "+imgUrl.size());
						if(adapter.maxindex >= imgUrl.size() - 1){
        					return;
						}
						Log.d("RecyclerView Loading Notification", "loading executed");
						boolean isRefreshing = swipeRefreshLayout.isRefreshing();
						if (isRefreshing) {
							adapter.notifyItemRemoved(adapter.getItemCount());
							return;
						}
						if (!isLoading) {
							isLoading = true;
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									getData();
									Log.d("RecyclerView Loading Notification", "load more completed");
									isLoading = false;
								}
							}, 100);
						}
					}
				}
            }
        });

        // add click event on item on recycleView
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(RecyclerViewAdapter.ItemViewHolder holder, ImageButton ib, int position) {
				tempHolder = holder;
            	Handler photoFrameHandler = new Handler();
				photoFrameHandler.postDelayed(new Runnable() {
					@Override
					public void run() {

						searchImage.setVisibility(View.GONE);
						//swipeRefreshLayout.setVisibility(View.GONE);
						mFragmentTransaction = mFragmentManager.beginTransaction();
						if (mFragmentManager.findFragmentByTag("photo") != null) {
							mFragmentManager.popBackStack();
						}
						if (mPhotoFragment == null){
							if(!mPhotoFragmentMap.containsKey(position)){
								mPhotoFragment = new Photo_detail();
								mPhotoFragmentMap.put(position,mPhotoFragment);
							}else{
								mPhotoFragment = mPhotoFragmentMap.get(position);
							}
						}

						Bundle bundle = new Bundle();
						ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

						if(!(ib.getDrawable() instanceof  pl.droidsonroids.gif.GifDrawable)){
							bundle.putString("byteArray",
									"http://140.115.51.177:8000/media/" + keyword + "/" + imgUrl.get(position + 1).get("name"));
						}else{
							mPhotoFragment.retrieveImageTask = holder.retrieveImageTask;
							mPhotoFragment.retrieveImageTask.update_ib_photo = true;
						}

						ArrayList<Integer> visitList = new ArrayList<Integer>();
						for(int i = 2 ; i < 5 ; i++){
							String img_name= null;
							if(position + i < imgUrl.size()){
								img_name = imgUrl.get(position + i).get("name");
							}
							if(img_name == null){
								int j = 1;
								int count = 0;
								//Log.d("Main Bytearray Test","suggestion"+String.valueOf(i-1)+" size is  :"+adapter.dictionary.size());
								while(img_name == null && count < adapter.dictionary.size()){
									j = (int)(Math.random() * adapter.dictionary.size());
									count++;
									if(!visitList.contains(j)){
										img_name = (imgUrl.get(j).get("name"));
									}
									//Log.d("Main Bytearray Test","count :"+ count +"j  :"+j+" size is  :"+adapter.dictionary.size());
								}
								visitList.add(j);
								Log.d("Main Bytearray Test","suggestion"+String.valueOf(i-1) + " j is  :" + j);
								bundle.putString("suggestion"+String.valueOf(i-1)+" position",String.valueOf(j-1));
							}else{
								bundle.putString("suggestion"+String.valueOf(i-1)+" position",String.valueOf(position+i-1));
							}
							if(byteArrayStream.toByteArray().length == 0){
								Log.d("Photo delivery","Failed");
							}
							img_name = "http://140.115.51.177:8000/media/" + keyword + "/" + img_name;
							bundle.putString("suggestion"+String.valueOf(i-1), img_name);
						}
						bundle.putString("position",String.valueOf(position));
						Log.d("main adapter position test",String.format("position: %s",bundle.getString("position")));
						bundle.putString("score", String.valueOf(holder.score));
						bundle.putString("keyword", keyword);
						bundle.putString("name",imgUrl.get(position+1).get("name"));

						//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();

						mainFragmentStack.add(mPhotoFragment);
						mPhotoFragment.setArguments(bundle);
						if (mPhotoFragment.isAdded()) {
							mFragmentTransaction.remove(mPhotoFragment);
						}
						mFragmentTransaction.add(android.R.id.tabcontent, mPhotoFragment, "photo");
						mFragmentTransaction.commit();
						String s = "Photo Detail";
						textView.setText(s);
						mPhotoFragment = null;
						Log.d("main fragment test",String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
								mainFragmentStack.size(),personalFragmentStack.size(),String.valueOf(IsPersonal)));
					}
				}, 100);
			}

			@Override
			public void onCameraClick(RecyclerViewAdapter.ItemViewHolder holder, ImageButton ib, int position) {
				Handler handler = new Handler();
				handler.postDelayed(()->{
					Intent intent1 = new Intent(MainActivity.this,camera_activity.class);

					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					Bitmap bitmap = ((BitmapDrawable)(ib.getDrawable()))
							.getBitmap();
					bitmap = Bitmap.createScaledBitmap(bitmap,600,600,true);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayStream);
					Log.d("test byteArray size before", String.valueOf(byteArrayStream.toByteArray().length));
					intent1.putExtra("byteArray", byteArrayStream.toByteArray());
					Log.d("test byteArray size after", String.valueOf(byteArrayStream.toByteArray().length));
					intent1.putExtra("keyword", keyword);
					intent1.putExtra("ImageVisibleSemarphore",true);
					Log.d("test startactivity test","before" );
					//Toast.makeText(getApplicationContext(),R.string.title_notifications,Toast.LENGTH_SHORT).show();
					startActivity(intent1);
					Log.d("test startactivity test","after" );

				},100);
			}

			@Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });


		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		searchImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this,search_activity.class);
				name = searchHelper.httpConnect.resultBack();
				if(name!=null){
					Log.d("Name pass Test","name pass to search_activity is NON-NULL!");
					//Log.d("test",name[0]+"  "+name[1]);
				}
				intent.putExtra("name",name);
				startActivity(intent);
			}
		});
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.maps);
		mapFragment.getMapAsync(this);


    }
    public void initData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 1; i++) {
                    Map<String, Object> map = new HashMap<>();
                    data.add(map);
                }
                swipeRefreshLayout.setRefreshing(false);
				Log.d("Main Refresh Test","In");
                //Log.d("test", "size is "+adapter.getItemCount());
                adapter.notifyDataSetChanged();
            }
        }, 1000);

    }
    private void getData() {
    	int temp;
		if(adapter.maxindex + 6 >= imgUrl.size()){
			temp = (imgUrl.size() - 1) - adapter.maxindex  ;
    		Log.d("GetData test",String.format(" temp is %d, adapter.maxindex is %d  , max %d",temp, adapter.maxindex ,imgUrl.size()));
		}else{
    		temp = 6;
		}
		//Toast.makeText(getApplicationContext(),"GetData test" + String.format(" temp is %d, adapter.maxindex is %d  , max %d",temp, adapter.maxindex ,imgUrl.size()),Toast.LENGTH_SHORT).show();

		for (int i = 0; i < temp; i++) {
            Map<String, Object> map = new HashMap<>();
            data.add(map);
        }

        swipeRefreshLayout.setRefreshing(false);
        //Log.d("test", "size is "+adapter.getItemCount());

        data.remove(data.size()-1);
        adapter.data = data;
        adapter.imgUrl = imgUrl;
        adapter.notifyDataSetChanged();
        adapter.notifyItemRemoved(data.size());
        adapter.notifyItemRangeChanged(data.size(), adapter.getItemCount());
        //Log.d("test", "after pruning size is "+adapter.getItemCount());
    }
	private void initImage(){
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			searchHelper.requestLocationAccessPermission(this);
		}
		List<String> providers = locationManager.getProviders(true);
		Location location = null;
		for (String provider : providers) {
			Location l = locationManager.getLastKnownLocation(provider);
			if (l == null) {
				continue;
			}
			if (location == null || l.getAccuracy() < location.getAccuracy()) {
				// Found best last known location: %s", l);
				location = l;
			}
		}

		final double[] longitude = new double[]{0};
		//longitude[0] = location.getLongitude();
		final double[] latitude = new double[]{0};
		//longitude[0] = location.getLatitude();
		latitude[0] = 24.9668147;
		longitude[0] = 121.1922626;
		StringBuilder searchURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
				latitude[0]+ "," +longitude[0] + "&radius=800&types=");
		//searchURL.append(supportTypeSet[0]);
		searchURL.append("point_of_interest");
		for(int i = 1 ; i < 1; i++){
			searchURL.append("|").append(supportTypeSet[i]);
		}
		searchURL.append("&key="+KEY);

		httpConnect = new KeywordFindingTask(latitude[0],longitude[0]);
		httpConnect.execute(searchURL.toString());
		Log.d("Main Location URL test",searchURL.toString());
	}
	private void imageNotFound(){
		swipeRefreshLayout.setRefreshing(false);
		//Log.d("test", "size is "+adapter.getItemCount());

		data.remove(data.size()-1);
		adapter.data = data;
		adapter.notifyDataSetChanged();
		adapter.notifyItemRemoved(data.size());
		adapter.notifyItemRangeChanged(data.size(), adapter.getItemCount());
		//Log.d("test", "after pruning size is "+adapter.getItemCount());
	}

    public static void hideKeyboard(Activity activity, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

	@Override
	public void onBackPressed() {
		if(account == null){
			finish();
			return;
		}
    	if(personalFragmentStack.size()!= 1 && IsPersonal){
			onBackPressedPersonal(false);
			Log.d("back pressed","case1");
		}else if(mainFragmentStack.size()!= 1 && !IsPersonal) {
			onBackPressedHome(false);
			Log.d("back pressed","case2");
		}else{
			Log.d("back pressed","case3");
			finish();
		}
    }

	public void onBackPressedHome(boolean switchFromPersonal) {
		//super.onBackPressed();
		IsPersonal = false;
		if (switchFromPersonal) {
			for (int i = personalFragmentStack.size() - 1; i >= 0; i--) {
				Fragment fragment = personalFragmentStack.get(i);
				if (fragment != null) {
					mFragmentManager.beginTransaction().remove(personalFragmentStack.get(i)).commit();
					personalFragmentStack.remove(i);
					Log.d("main kill fragment test", "kill personal fragment");
				}
			}
		} else {
			mFragmentManager.beginTransaction().remove(mainFragmentStack.get(mainFragmentStack.size() - 1)).commit();
		}
		// switchFromPersonal
		if (mainFragmentStack.get(mainFragmentStack.size() - 1) != null) {
			if (mainFragmentStack.get(mainFragmentStack.size() - 1) instanceof Photo_detail) {
				//navigation.setSelectedItemId(R.id.navigation_home);
				updateCollection(mainFragmentStack);
				textView.setText("Photo Detail");
			}

			if (!switchFromPersonal) {
				mainFragmentStack.remove(mainFragmentStack.size() - 1);
			}
			if(mainFragmentStack.get(mainFragmentStack.size()-1) != null){
				mFragmentManager.beginTransaction().show(mainFragmentStack.get(mainFragmentStack.size() - 1)).commit();
			}else if (mainFragmentStack.size() == 1) {
				backToHome();
			}
		}else if (mainFragmentStack.size() == 1) {
			backToHome();
		}

		Log.d("main fragment test", String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
				mainFragmentStack.size(), personalFragmentStack.size(), String.valueOf(IsPersonal)));
	}
	public void onBackPressedPersonal(boolean switchFromMain){
		IsPersonal = true;
    	if(!switchFromMain){
			mFragmentManager.beginTransaction().remove(personalFragmentStack.get(personalFragmentStack.size()-1)).commit();
			personalFragmentStack.remove(personalFragmentStack.size()-1);
		}
    	if (personalFragmentStack.get(personalFragmentStack.size()-1)!= null && IsPersonal){
			if(personalFragmentStack.get(personalFragmentStack.size()-1) instanceof Photo_detail) {
				//navigation.setSelectedItemId(R.id.navigation_home);
				updateCollection(personalFragmentStack);
				textView.setText("Photo Detail");
			}else if(personalFragmentStack.get(personalFragmentStack.size()-1) instanceof Personal_Page){
				textView.setText("Profile");
			}
			mFragmentManager.beginTransaction().show(personalFragmentStack.get(personalFragmentStack.size()-1)).commit();
		}else if (personalFragmentStack.size() == 1){
			backToHome();
			navigation.setSelectedItemId(R.id.navigation_home);
			IsPersonal = false;
		}
		Log.d("main fragment test",String.format("main stack size: %d, personal stack size: %d, isPersonal: %s, ",
				mainFragmentStack.size(),personalFragmentStack.size(),String.valueOf(IsPersonal)));
	}

	public void updateCollection(List fragmentStack){
		Map<String,String> m = new HashMap();
		String fav_keyword =  ((Photo_detail) fragmentStack.get(fragmentStack.size()-1)).keyword;
		String fav_name = ((Photo_detail) fragmentStack.get(fragmentStack.size()-1)).name;
    	String str = "http://140.115.51.177:8000/media/" + fav_keyword + "/" + fav_name;
    	String position = String.valueOf(((Photo_detail) fragmentStack.get(fragmentStack.size()-1)).position);
    	m.put("url",str);
    	m.put("position",position);
    	m.put("keyword",fav_keyword);
    	m.put("name",fav_name);
    	Log.d("main update collection position & name test",String.format("position: %s, name: %s",position,fav_name));
		if(((Photo_detail) fragmentStack.get(fragmentStack.size()-1)).heart){
			if(!collection.contains(m)) {
				collection.add(m);
				Favorite_postTask favoritePostTask = new Favorite_postTask(authentication,fav_keyword,fav_name,false);
				favoritePostTask.execute("http://140.115.51.177:8000/userfunction/api=favorite");
			}
		}else{
			Favorite_postTask favoritePostTask = new Favorite_postTask(authentication,fav_keyword,fav_name,true);
			favoritePostTask.execute("http://140.115.51.177:8000/userfunction/api=unfavorite");
			collection.remove(m);
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_SIGNUP){
			if (resultCode == RESULT_OK) {
				if (data != null) {
					account = data.getStringExtra("ACCOUNT");
					authentication = data.getStringExtra("AUTH");
					initWithAccount();
				}
			}else if (resultCode == RESULT_CANCELED)  {
				Toast.makeText(getApplicationContext(), "Canceled LOG IN / SIGN UP ", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void backToHome(){
		if (recyclerView.computeVerticalScrollOffset() == 0) {
			textView.setText(keyword);
		} else {
			textView.setText(("POI nearby " + keyword));
		}
		searchImage.setVisibility(View.VISIBLE);
	}

	@SuppressLint("ApplySharedPref")
	private void initWithAccount(){
		{
			setContentView(R.layout.main_layout);
			ButterKnife.inject(this);
			mSharedPreferences.edit()
					.remove("ACCOUNT")
					.remove("AUTH")
					.commit();
			mSharedPreferences.edit()
					.putString("ACCOUNT",account)
					.putString("AUTH",authentication)
					.commit();

			Runnable samRun2 = new Runnable() {
				@Override
				public void run() {
					if(isLoadingFirst) {
						if (httpConnect3.resultBack().size() > 0) {
							Log.d("Main ATask ResultBack Test", String.valueOf(httpConnect3.resultBack().size()));
							imgUrl = httpConnect3.resultBack();
							if(imgUrl.get(0).get("name").equals("Not Found") ){
								imageNotFound();
								Toast.makeText(getApplicationContext(),"Location Data Not Found, Please Try Another Locations", Toast.LENGTH_LONG).show();
								imgUrl.clear();
							}else {
								Map<String,String> m = new HashMap<>();
								m.put("name",keyword);
								m.put("score","N/A");
								imgUrl.add(0, m);
								Log.d("Keyword In Runnable Test", imgUrl.get(0).get("name"));
								adapter.notifyDataSetChanged();
								getData();
								isLoadingFirst = false;
							}
						}else {
							samHandler.postDelayed(this, 100);
						}
					}
				}
			};
			Runnable samRun = new Runnable() {
				@Override
				public void run() {
					if(isLoadingFirst) {
						//Log.d("delivery test","in");
						if (httpConnect.resultBack() != null) {
							//keyword = httpConnect.resultBack();
							//keyword = keyword.replace(' ','+');
							httpConnect3.execute("http://140.115.51.177:8000/search/api=search/location="+keyword);
							Log.d("Keyword In Runnable Test",keyword+"?");
							samHandler.postDelayed(samRun2,100);
						}else {
							samHandler.postDelayed(this, 100);
						}
					}
				}
			};
			if(user_keyword != null){
				//httpConnect3.execute("http://140.115.51.177:8000/search/api=search/location="+selectedFromList);
				selectedFromList = selectedFromList.replace(' ','+');
				httpConnect3.execute("http://140.115.51.177:8000/search/api=search/location="+user_keyword);
				Log.d("Main htttConnect3 Test","http://140.115.51.177:8000/search/api=search/location="+user_keyword);
				textView.setText(user_keyword);
				keyword = user_keyword;
				initData();
				initView();
				samHandler.postDelayed(samRun2,100);
			}else{
				isLoadingFirst = true;
				initImage();
				initData();
				initView();
				name = searchHelper.FindNearbyPlace(this);
				samHandler.postDelayed(samRun,100);
			}

			mFragmentManager = getSupportFragmentManager();
			mainFragmentStack = new ArrayList<Fragment>();
			personalFragmentStack = new ArrayList<Fragment>();
			//bias base to 1
			mainFragmentStack.add(null);
			personalFragmentStack.add(null);
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		final int TEST_DATA_SIZE = 5;
		double[] latitude = {24.9745606,24.957537,24.9727711,24.9581984,24.9548231};
		double[] longtitude = {121.1837838,121.2388328,121.2158845,121.2278894,121.2250999};
		String[] description = {"中央大學","中原大學","舊社","中壢藝術館","中壢車站"};
		String[] iconName = {"maps_ncu","maps_cycu","maps_night_market","maps_art_museum","maps_train_station"};
		List<Map<String,Object>> data_list = new ArrayList<>();

		Button bn_test = findViewById(R.id.bn_test);
		bn_test.setOnClickListener((view)->{
			double lat = latitude[0] +Math.random()*0.1;
			double longt = longtitude[0] + Math.random()*0.1;
			addMarker(String.valueOf(lat),String.valueOf(longt),"test","cat1",Math.random());
			moveCamera();
		});
		mGoogleMap = googleMap;
		googleMap.setInfoWindowAdapter(this);
		for(int i = 0 ; i <TEST_DATA_SIZE ;i++){
			Map m = new HashMap<>();
			m.put("description",description[i]);
			m.put("lat",latitude[i]);
			m.put("long",longtitude[i]);
			data_list.add(m);
		}
		for(int i = 0; i < TEST_DATA_SIZE ; i++){
			addMarker(data_list.get(i).get("lat").toString(),
					data_list.get(i).get("long").toString(),
					data_list.get(i).get("description").toString(),
					iconName[i],
					Math.random()
					);
		}
		moveCamera();
		//googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),15));
	}
	public void moveCamera(){
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(Marker marker : markerList){
			builder.include(marker.getPosition());
		}

		int width = getResources().getDisplayMetrics().widthPixels ;
		int height = getResources().getDisplayMetrics().heightPixels;
		int padding = (int) (width * 0.30); // offset from edges of the map 12% of screen

		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding);
		// end of new code
		Log.d("Main Maps Test",String.format("width: %d, height: %d, padding: %d\n",width,height,padding));
		mGoogleMap.animateCamera(cu);
	}

	private void addMarker(String latitude, String longtitude, String title, String iconName,double direction){
		BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(),getResources().
				getIdentifier(iconName,"drawable",getApplicationContext().getPackageName()));
		Bitmap bitmap = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),100,150,true);
		markerList.add(mGoogleMap.addMarker(new MarkerOptions().position(
				new LatLng(Double.parseDouble(latitude),
						Double.parseDouble(longtitude)))
				.title(title)
				.snippet("經緯度 : "+latitude + ", " + longtitude)
				.zIndex((float)direction)
				.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
		));
		bitmapList.add(bitmap);
	}
	@Override
	public View getInfoWindow(Marker marker) {
		// map will call this first
		View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.marker_info_window,null);
		TextView tv_title = view.findViewById(R.id.tv_title);
		TextView tv_content = view.findViewById(R.id.tv_content);
		TextView tv_angleDirection = view.findViewById(R.id.tv_angleDirection);
		ImageView iv_poi_image = view.findViewById(R.id.iv_poi_image);
		tv_title.setText(marker.getTitle());
		tv_content.setText(marker.getSnippet());
		tv_angleDirection.setText(("傾斜角 : " + marker.getZIndex()));

		BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(),R.drawable.cat1);
		Bitmap bitmap = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),150,150,true);
		iv_poi_image.setImageBitmap(bitmap);
		for(int i = 0 ; i < markerList.size() ; i++){
			if(marker.equals(markerList.get(i))){
				iv_poi_image.setImageBitmap(bitmapList.get(i));
				break;
			}
		}
		return view;
	}

	@Override
	public View getInfoContents(Marker marker) {
		// and map will call this
		View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.marker_info_window,null);
		TextView tv_title = view.findViewById(R.id.tv_title);
		TextView tv_content = view.findViewById(R.id.tv_content);
		TextView tv_angleDirection = view.findViewById(R.id.tv_angleDirection);
		tv_title.setText(marker.getTitle());
		tv_content.setText(marker.getSnippet());
		tv_angleDirection.setText(("傾斜角 : " + Math.random()));
		return view;
	}
}
