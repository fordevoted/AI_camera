package user.ai_camera;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class Personal_Page extends Fragment {
	private final static int SELECT_IMAGE = 1;
	private final static int UPLOAD_IMAGE = 2;

	private final static  int PROFILE = 0;
	private final static int USER_STATE = 1;
	private final static int NICKNAME = 2;

	public  TableLayout tableLayout, tableLayout_record;
	public  ImageButton setting, uploadImage;
	public  pl.droidsonroids.gif.GifImageButton ib_user_profile ;
	public  TextView tv_user_name, tv_user_status;
	public  String user_name, user_status;
	public String account;
	public String authentication;
	public  Bitmap user_profile;
	public pl.droidsonroids.gif.GifImageView iv_unlike;


	public Runnable run2;
	public Point size;
//TODO optimization dataflow (multiple set username user_state user profile now)
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.personal_layout, container, false);
		tableLayout  = root.findViewById(R.id.tablelayout);
		tableLayout_record = root.findViewById(R.id.tablelayout_record);
		setting = root.findViewById(R.id.setting);
		ib_user_profile = root.findViewById(R.id.ib_profile);
		tv_user_name = root.findViewById(R.id.user_name);
		tv_user_status = root.findViewById(R.id.user_status);
		iv_unlike = root.findViewById(R.id.iv_unlike);
		uploadImage = root.findViewById(R.id.uploadImage);
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		return root;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		SharedPreferences msharedPreferences = getActivity().getSharedPreferences("AICam",Context.MODE_PRIVATE);
		account = msharedPreferences.getString("ACCOUNT",null);
		authentication = msharedPreferences.getString("AUTH",null);

		Get_UserInfoTask get_userInfoTask = new Get_UserInfoTask(
				msharedPreferences.getString("AUTH","authentication not found"),
				msharedPreferences.getString("user_name", msharedPreferences.getString("ACCOUNT",getResources().getString(R.string.user))),
				msharedPreferences.getString("user_status", getResources().getString(R.string.user_status)));
		get_userInfoTask.execute("http://140.115.51.177:8000/userfunction/api=user_info");
		RetrieveImageTask retrieveImageTask = new RetrieveImageTask(size,false);
		Log.d("Personal onviewCreated test"," FAVTASK execute");
		Handler handler = new Handler();
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if(retrieveImageTask.resultBack() != null){
					if(ib_user_profile.getMeasuredHeight()> 0){
						ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(retrieveImageTask.resultBack(), ib_user_profile.getMeasuredWidth(),ib_user_profile.getMeasuredHeight(),true));
						Log.d("Personal test"," measure profile >0");
					}else {
						ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(retrieveImageTask.resultBack(), 150, 150, true));
					}
				}else{
					handler.postDelayed(this,100);
				}
			}
		};
		run2 = new Runnable() {
			@Override
			public void run() {
				if(get_userInfoTask.resultBack()){
					Log.d("Personal Collection test","resultback success");
					tv_user_name.setText(get_userInfoTask.NicknameBack());
					tv_user_status.setText(get_userInfoTask.UserStateBack());
					if(get_userInfoTask.ProfileBack() != null){
						retrieveImageTask.execute(get_userInfoTask.ProfileBack());
						handler.postDelayed(run,100);
					}
					initcollection(get_userInfoTask.collectionBack());
				}else{
					handler.postDelayed(this,100);
				}
			}
		};
		handler.postDelayed(run2,100);
		if(user_name != null){
			tv_user_name.setText(user_name);
		}
		if(user_status != null){
			tv_user_status.setText(user_status);
		}
		if(user_profile != null){
			ib_user_profile.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if(ib_user_profile.getMeasuredHeight()> 0){
						ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(user_profile, ib_user_profile.getMeasuredWidth(),ib_user_profile.getMeasuredHeight(),true));
					}else {
						ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(user_profile, 50, 50, true));
					}
				}
			});
		}
		uploadImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Handler handler = new Handler();
				handler.postDelayed(()-> {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent,UPLOAD_IMAGE);
				},100);
			}
		});


		setting.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			BackgroundBlurPopupWindow mPopupWindow;
			View v = getActivity().getLayoutInflater().inflate(R.layout.personal_setting_layout, null);
			View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.personal_layout, null);
			mPopupWindow = new BackgroundBlurPopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getActivity(), true);
			mPopupWindow.setWidth((int) (size.x*0.7));
			mPopupWindow.setFocusable(true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable());
			mPopupWindow.setDownScaleFactor(1.2f);
			mPopupWindow.setBlurRadius(10);//配置虚化比例
			mPopupWindow.setDarkAnimStyle(R.anim.dark_fade_in);//动画
			mPopupWindow.setDarkColor(Color.parseColor("#a0000000"));//颜色
			mPopupWindow.resetDarkPosition();
			mPopupWindow.darkFillScreen();//全屏
			mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
			Button bn_user_name = v.findViewById(R.id.bn_user_name);
			Button bn_user_status = v.findViewById(R.id.bn_user_status);
			Button bn_user_profile = v.findViewById(R.id.bn_profile);
			Button bn_user_login = v.findViewById(R.id.bn_sign);
			Button bn_user_signup = v.findViewById(R.id.bn_signup);
			LinearLayout linearLayout = v.findViewById(R.id.linearlayout);
			if(account != null){
				bn_user_signup.setVisibility(View.GONE);
				bn_user_login.setText("Log out");
			}
			bn_user_name.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					bn_user_name.setVisibility(View.GONE);
					bn_user_profile.setVisibility(View.GONE);
					bn_user_status.setVisibility(View.GONE);
					bn_user_signup.setVisibility(View.GONE);
					bn_user_login.setVisibility(View.GONE);
					EditText e = new EditText(getContext());
					e.setHint(tv_user_name.getText());
					e.setImeOptions(EditorInfo.IME_ACTION_DONE);
					e.setSingleLine();
					linearLayout.addView(e);
					e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@SuppressLint("ApplySharedPref")
						@Override
						public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
							if(keyCode == EditorInfo.IME_ACTION_DONE){
								user_name = e.getText().toString();
								getActivity().getSharedPreferences("AICam", Context.MODE_PRIVATE).edit()
										.putString("user_name",user_name)
										.commit();
								Post_UserinfoTask post_userinfoTask = new Post_UserinfoTask(user_name,account,authentication,NICKNAME);
								post_userinfoTask.execute("http://140.115.51.177:8000/userfunction/api=nickname");
								Handler handler = new Handler();
								Runnable run = new Runnable() {
									@Override
									public void run() {
										if(post_userinfoTask.resultBack() != -1){
											tv_user_name.setText(user_name);
											Log.d("Personal test","user name success");
										}else{
											handler.postDelayed(this,100);
										}
									}
								};
								handler.postDelayed(run,100);
								mPopupWindow.dismiss();
								return true;
							}
							Log.d("edit test","keycode "+keyCode +" Keyaction"+keyEvent.getAction());
							return false;
						}
					});
				}
			});
			bn_user_status.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					bn_user_name.setVisibility(View.GONE);
					bn_user_profile.setVisibility(View.GONE);
					bn_user_status.setVisibility(View.GONE);
					bn_user_signup.setVisibility(View.GONE);
					bn_user_login.setVisibility(View.GONE);
					EditText e = new EditText(getContext());
					e.setHint(tv_user_status.getText());
					e.setImeOptions(EditorInfo.IME_ACTION_DONE);
					e.setSingleLine();
					linearLayout.addView(e);
					e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@SuppressLint("ApplySharedPref")
						@Override
						public boolean onEditorAction(TextView textView, int keyCode, KeyEvent keyEvent) {
							if(keyCode == EditorInfo.IME_ACTION_DONE){
								user_status = e.getText().toString();
								getActivity().getSharedPreferences("AICam", Context.MODE_PRIVATE).edit()
									.putString("user_status",user_status)
									.commit();
								Post_UserinfoTask post_userinfoTask = new Post_UserinfoTask(user_status,account,authentication,USER_STATE);
								post_userinfoTask.execute("http://140.115.51.177:8000/userfunction/api=user_state");
								Handler handler = new Handler();
								Runnable run = new Runnable() {
									@Override
									public void run() {
										if(post_userinfoTask.resultBack() != -1){
											tv_user_status.setText(user_status);
											Log.d("Personal test","user state success");
										}else{
											handler.postDelayed(this,100);
										}
									}
								};
								handler.postDelayed(run,100);
								mPopupWindow.dismiss();
								return true;
							}
							Log.d("edit test","keycode "+keyCode +" Keyacyion"+keyEvent.getAction());
							return false;
						}
					});
				}
			});
			bn_user_profile.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_IMAGE);
					mPopupWindow.dismiss();
				}
			});
			bn_user_login.setOnClickListener(new View.OnClickListener() {
				@SuppressLint("ApplySharedPref")
				@Override
				public void onClick(View view) {
					if(bn_user_login.getText().equals("Log in")){
						Intent intent = new Intent(getActivity(), Login_activity.class);
						startActivity(intent);
						mPopupWindow.dismiss();
					}else{
						msharedPreferences.edit()
								.remove("ACCOUNT")
								.remove("AUTH")
								.commit();
						Intent intent = new Intent(getActivity(), MainActivity.class);
						startActivity(intent);
						mPopupWindow.dismiss();
						getActivity().finish();
					}

				}
			});
			bn_user_signup.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getActivity(), Signup_activity.class);
					startActivity(intent);
					mPopupWindow.dismiss();
				}
			});
			//Log.d("Camera Image Button Test","click");
		}});
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Handler handler = new Handler();
		if(run2 != null){

		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE) {
			if (resultCode == getActivity().RESULT_OK) {
				if (data != null) {
					try {
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
						user_profile = bitmap;
						Post_UserinfoTask post_userinfoTask = new Post_UserinfoTask(bitmap,account,authentication,PROFILE);
						post_userinfoTask.execute("http://140.115.51.177:8000/userfunction/api=faceimage");
						Handler handler = new Handler();
						Runnable run = new Runnable() {
							@Override
							public void run() {
								ib_user_profile.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.loading100));
								if(post_userinfoTask.resultBack() != -1){
									if(ib_user_profile.getMeasuredHeight()> 0){
										ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ib_user_profile.getMeasuredWidth(),ib_user_profile.getMeasuredHeight(),true));
									}else {
										ib_user_profile.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, true));
									}
									Toast.makeText(getContext(),"Change Profile",Toast.LENGTH_SHORT).show();
								}else{
									handler.postDelayed(this,100);
								}
							}
						};
						handler.postDelayed(run,100);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (resultCode == getActivity().RESULT_CANCELED)  {
				Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
			}
		}else if (requestCode == UPLOAD_IMAGE) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					String imagePath = null;
					Uri uri = data.getData();

					if (DocumentsContract.isDocumentUri(getContext(), uri)) {
						String docId = DocumentsContract.getDocumentId(uri);
						if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
							String id = docId.split(":")[1];
							String selection = MediaStore.Images.Media._ID + "=" + id;
							imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
						} else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
							Uri contentUri = ContentUris.withAppendedId(
									Uri.parse("content://downloads/public_downloads"),
									Long.valueOf(docId));
							imagePath = getImagePath(contentUri, null);
						}
					} else if ("content".equalsIgnoreCase(uri.getScheme())) {
						imagePath = getImagePath(uri, null);
					}
					data.putExtra("path", imagePath);
					data.setClass(getActivity(), Upload_image.class);
					startActivity(data);
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getContext(), "Canceled Upload Image ", Toast.LENGTH_SHORT).show();
			}
		}
	}
	private String getImagePath(Uri uri, String selection) {
		String path = null;
		Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			}
			cursor.close();
		}
		return path;
	}
	private void initcollection(List<Map<String,String>> collection){
		int index = 0;
		/*if(collection.size() != 0 ) {
			iv_unlike.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.loadingg));
		} else{
			iv_unlike.setVisibility(View.VISIBLE);
		}*/


		while(index < collection.size()){
			TableRow tableRow = new TableRow(getContext());
			TableRow.LayoutParams tablelayoutParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			tablelayoutParam.weight = 1 ;
			tableRow.setWeightSum(1);
			tableRow.setLayoutParams(tablelayoutParam);
			tableRow.setBackgroundColor(Color.parseColor("#EEEEEE"));
			ImageButton ib;

			for(int i = 0 ; i < 3 ; i ++){
				Log.d("Personal Put collection", (collection.get(index).get("url")));
				ib = new ImageButton(getContext());
				TableRow.LayoutParams layoutParam = new TableRow.LayoutParams();
				layoutParam.weight = 0.3333f;
				layoutParam.column = i;
				layoutParam.width = 0;
				layoutParam.height = TableRow.LayoutParams.WRAP_CONTENT;
				layoutParam.setMargins(2,2,2,2);
				ib.setLayoutParams(layoutParam);
				ib.setBackgroundResource(0);// background @null
				RetrieveImageTask retrieveImageTask;
				if(index ==0){
					retrieveImageTask =  new RetrieveImageTask(ib,size,true,true,iv_unlike);
				}else{
					retrieveImageTask =  new RetrieveImageTask(ib,size,false,true);
				}
				retrieveImageTask.execute(collection.get(index).get("url"));
				tableRow.addView(ib);
				ImageButton finalIb = ib;
				final int finalIndex = index;
				ib.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						BackgroundBlurPopupWindow mPopupWindow;
						View v = getActivity().getLayoutInflater().inflate(R.layout.popup_layout, null);
						ImageView iv = v.findViewById(R.id.imageView_browse);
						iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
								iv.setImageBitmap(Bitmap.createScaledBitmap(((BitmapDrawable)(finalIb.getDrawable())).getBitmap(), iv.getMeasuredWidth(), iv.getMeasuredHeight(),true));
							}
						});
						View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.main_layout, null);
						mPopupWindow = new BackgroundBlurPopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getActivity(), true);
						mPopupWindow.setFocusable(true);
						mPopupWindow.setBackgroundDrawable(new ColorDrawable());
						mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
						return false;
					}
				});
				ib.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Handler photoFrameHandler = new Handler();
						photoFrameHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								//swipeRefreshLayout.setVisibility(View.GONE);

								((MainActivity)getActivity()).mFragmentTransaction = ((MainActivity)getActivity()).mFragmentManager.beginTransaction();
								int position;
								String keyword;
								String url;
								String name;
								assert  (collection.get(finalIndex).get("url")) != null;
								{
									url = collection.get(finalIndex).get("url");
									position = (int)(Math.random()*((MainActivity)getActivity()).imgUrl.size()*0.8); //*0.8 for ensure it won't be the size of imgurl
									keyword = (collection.get(finalIndex).get("keyword"));
									name = collection.get(finalIndex).get("name");
								}

								Log.d("personal image button name test",name);
								Log.d("personal image button position test",String.format("index: %d, position: %d",finalIndex,position));
								if (((MainActivity)getActivity()).mPhotoFragment == null){
									if(!((MainActivity)getActivity()).mPhotoFragmentMap.containsKey(999)){
										((MainActivity)getActivity()).mPhotoFragment = new Photo_detail();
										Log.d("Personal new fragment test","Yes, new Photo_detail create");
										//((MainActivity)getActivity()).mPhotoFragmentMap.put(999,((MainActivity)getActivity()).mPhotoFragment);
									}else{
										((MainActivity)getActivity()).mPhotoFragment = ((MainActivity)getActivity()).mPhotoFragmentMap.get(999);
									}
								}


								Bundle bundle = new Bundle();
								ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
								bundle.putString("byteArray",
										url);
								ArrayList<Integer> visitList = new ArrayList<Integer>();
								for(int i = 2 ; i < 5 ; i++){
									String img_name= null;
									if(position + i < ((MainActivity)getActivity()).imgUrl.size()){
										img_name = ((MainActivity)getActivity()).imgUrl.get(position + i).get("name");
									}
									if(img_name == null){
										int j = 1;
										int count = 0;
										//Log.d("Main Bytearray Test","suggestion"+String.valueOf(i-1)+" size is  :"+adapter.dictionary.size());
										while(img_name == null && count < ((MainActivity)getActivity()).adapter.dictionary.size()){
											j = (int)(Math.random() * ((MainActivity)getActivity()).adapter.dictionary.size());
											count++;
											if(!visitList.contains(j)){
												img_name = (((MainActivity)getActivity()).imgUrl.get(j).get("name"));
											}
											//Log.d("Main Bytearray Test","count :"+ count +"j  :"+j+" size is  :"+adapter.dictionary.size());
										}
										visitList.add(j) ;
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
								bundle.putString("personal","true");
								bundle.putString("position",String.valueOf(position));
								bundle.putString("score",((MainActivity)getActivity()).imgUrl.get(position+1).get("score"));
								bundle.putString("keyword", keyword);
								bundle.putBoolean("heart",true);
								bundle.putString("name",name);
								Log.d("personal image test",String.format("image show: %s, suggestion1: %s, suggestion2: %s, suggestion3: %s"
										,bundle.getString("byteArray"),bundle.getString("suggestion1")
										,bundle.getString("suggestion2"),bundle.getString("suggestion3")));

								//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();

								((MainActivity)getActivity()).personalFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
								((MainActivity)getActivity()).mPhotoFragment.setArguments(bundle);
								if (((MainActivity)getActivity()).mPhotoFragment.isAdded()) {
									((MainActivity)getActivity()).mFragmentManager.beginTransaction().remove(((MainActivity)getActivity()).mPhotoFragment).commit();
									((MainActivity)getActivity()).mainFragmentStack.remove(((MainActivity)getActivity()).mPhotoFragment);
								}
								((MainActivity) getActivity()).mFragmentManager.executePendingTransactions();
								((MainActivity) getActivity()).mFragmentManager.beginTransaction().add(android.R.id.tabcontent, ((MainActivity)getActivity()).mPhotoFragment, "photo").commit();
								String s = "Photo Detail";
								((MainActivity)getActivity()).textView.setText(s);
								((MainActivity)getActivity()).mPhotoFragment = null;
								Log.d("personal setting test",String.format("personal stack size: %d, ",((MainActivity)getActivity()).personalFragmentStack.size()));
							}
						}, 100);
					}
				});
				index++;
				if(index >= collection.size()){ break; }
			}
			tableLayout.addView(tableRow);
		}
	}
}
/*
		while(index < Objects.requireNonNull(collection.size()){
			TableRow tableRow = new TableRow(getContext());
			TableRow.LayoutParams tablelayoutParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			tablelayoutParam.weight = 1 ;
			tableRow.setWeightSum(1);
			tableRow.setLayoutParams(tablelayoutParam);
			tableRow.setBackgroundColor(Color.parseColor("#EEEEEE"));
			ImageButton ib;
			for(int i = 0 ; i < 3 ; i ++){
				Log.d("Personal Put collection", ((MainActivity)getActivity()).collection.get(index).get("url"));
				ib = new ImageButton(getContext());
				TableRow.LayoutParams layoutParam = new TableRow.LayoutParams();
				layoutParam.weight = 0.3333f;
				layoutParam.column = i;
				layoutParam.width = 0;
				layoutParam.height = TableRow.LayoutParams.WRAP_CONTENT;
				layoutParam.setMargins(2,2,2,2);
				ib.setLayoutParams(layoutParam);
				ib.setBackgroundResource(0);// background @null
				RetrieveImageTask retrieveImageTask =  new RetrieveImageTask(ib,size,false,true);
				retrieveImageTask.execute(((MainActivity)getActivity()).collection.get(index).get("url"));
				tableRow.addView(ib);
				ImageButton finalIb = ib;
				final int finalIndex = index;
				Log.d("personal image button index test",String.valueOf(finalIndex));
				ImageButton finalIb1 = ib;
				ib.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						BackgroundBlurPopupWindow mPopupWindow;
						View v = getActivity().getLayoutInflater().inflate(R.layout.personal_record_popup_layout, null);
						View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.personal_layout, null);
						mPopupWindow = new BackgroundBlurPopupWindow(v, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getActivity(), true);
						mPopupWindow.setWidth((int) (size.x * 0.7));
						mPopupWindow.setFocusable(true);
						mPopupWindow.setBackgroundDrawable(new ColorDrawable());
						mPopupWindow.setDownScaleFactor(1.2f);
						mPopupWindow.setBlurRadius(10);//配置虚化比例
						mPopupWindow.setDarkAnimStyle(R.anim.dark_fade_in);//动画
						mPopupWindow.setDarkColor(Color.parseColor("#a0000000"));//颜色
						mPopupWindow.resetDarkPosition();
						mPopupWindow.darkFillScreen();//全屏
						mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
						Button bn_delete = v.findViewById(R.id.bn_delete);
						bn_delete.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								tableRow.removeView(finalIb1);
								// data clear ib
							}
						});
						return false;
					}
				});
				ib.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						BackgroundBlurPopupWindow mPopupWindow;
						View v = getActivity().getLayoutInflater().inflate(R.layout.popup_layout, null);
						ImageView iv = v.findViewById(R.id.imageView_browse);
						iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
								iv.setImageBitmap(Bitmap.createScaledBitmap(((BitmapDrawable)(finalIb.getDrawable())).getBitmap(), iv.getMeasuredWidth(), iv.getMeasuredHeight(),true));
							}
						});
						View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.main_layout, null);
						mPopupWindow = new BackgroundBlurPopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getActivity(), true);
						mPopupWindow.setFocusable(true);
						mPopupWindow.setBackgroundDrawable(new ColorDrawable());
						mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
					}
				});
				index++;
				if(index >= ((MainActivity)getActivity()).collection.size()){ break; }
			}
			tableLayout_record.addView(tableRow);
		}
		*/