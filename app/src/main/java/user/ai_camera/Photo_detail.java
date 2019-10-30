package user.ai_camera;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class Photo_detail extends Fragment {

	private Bitmap bitmap;
	private static final BitmapFactory BitmapFactory = null;
	private static final int VIEW_BIAS = 950;

	public ImageButton ib_photo, ib_heart, ib_light, ib_share;
	public TextView tv_suggestion, tv_introduce;
	public TextView tv_score, tv_location;
	public TextView tv_publicRating, tv_privateRating;
	public ImageButton privateRatingBar, publicRatingBar;
	public ImageButton ib_suggestion1, ib_suggestion2, ib_suggestion3;

	public Point size = new Point();
	public  boolean  heart;
	public String keyword,name;
	public double  rating ;
	public String position;
	public boolean personal;
	public int suggestion1_position,suggestion2_position,suggestion3_position; 



	public RetrieveImageTask retrieveImageTask;
	public boolean onClicked_withoutImage = false;

	public Photo_detail(){
		heart = false;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.photo_detail, container, false);

		ib_photo = root.findViewById(R.id.ib_show);
		ib_heart = root.findViewById(R.id.ib_heart);
		ib_share = root.findViewById(R.id.ib_share);
		tv_suggestion = root.findViewById(R.id.textView);
		tv_introduce = root.findViewById(R.id.textView2);
		tv_score =  root.findViewById(R.id.tv_score);
		privateRatingBar =  root.findViewById(R.id.private_rating);
		publicRatingBar =  root.findViewById(R.id.public_rating);
		tv_privateRating = root.findViewById(R.id.tv_privateRating);
		tv_publicRating = root.findViewById(R.id.tv_publicRating);
		tv_location = root.findViewById(R.id.tv_location);
		ib_light = root.findViewById(R.id.ib_light);
		ib_suggestion1 = root.findViewById(R.id.ib_suggestion1);
		ib_suggestion2 = root.findViewById(R.id.ib_suggestion2);
		ib_suggestion3 = root.findViewById(R.id.ib_suggestion3);
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		size = new Point();
		display.getSize(size);


		return root;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);


		Bundle bundle = this.getArguments();
		if(bundle != null) {
			if(bundle.getString("byteArray")!= null){
				Handler samHandler = new Handler();
				RetrieveImageTask retrieveImageTask =  new RetrieveImageTask(ib_photo,size,false,false);
				retrieveImageTask.execute(bundle.getString("byteArray"));
				Runnable run = new Runnable() {
					@Override
					public void run() {
						if(retrieveImageTask.resultBack()!=null){
							Log.d("Photo ", "Measuring");
							if(ib_photo.getMeasuredHeight() > 0){
								bitmap = retrieveImageTask.resultBack();
								ib_photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ib_photo.getMeasuredWidth(), ib_photo.getMeasuredHeight(), true));
							}else{
								ib_photo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
									@Override
									public void onGlobalLayout() {
										bitmap = retrieveImageTask.resultBack();
										if (ib_photo.getMeasuredHeight() <= 0) {
											Log.d("Photo ", "Measure Fail");
											ib_photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, true));
										} else {
											int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
											int specWidth = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED);
											//ib_photo.measure(specWidth, specHeight);

											Log.d("Photo ", "Measure Success" + ib_photo.getMeasuredWidth() + "   " + ib_photo.getMeasuredHeight());
											ib_photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ib_photo.getMeasuredWidth(), ib_photo.getMeasuredHeight(), true));
										}
									}
								});
							}
						}else{
							samHandler.postDelayed(this,100);
						}

					}
				};
				samHandler.postDelayed(run,100);
				//Log.d("Photo Photo Size Test", String.valueOf(ib_photo.getMeasuredWidth())+"  "+ib_photo.getMeasuredHeight());
			}else{
				InformClickedWithoutImage();
			}


			//Log.d("Photo Photo Size Test", String.valueOf(ib_photo.getMeasuredWidth())+"  "+ib_photo.getMeasuredHeight());
			if(bundle.getString("suggestion1") != null){
				RetrieveImageTask retrieveImageTask =  new RetrieveImageTask(ib_suggestion1,size,false,true);
				retrieveImageTask.execute(bundle.getString("suggestion1"));
				suggestion1_position = Integer.parseInt(bundle.getString("suggestion1 position"));

			}else{
				ib_suggestion1.setVisibility(View.GONE);
			}

			if(bundle.getString("suggestion2") != null){
				RetrieveImageTask retrieveImageTask =  new RetrieveImageTask(ib_suggestion2,size,false,true);
				retrieveImageTask.execute(bundle.getString("suggestion2"));
				suggestion2_position = Integer.parseInt(bundle.getString("suggestion2 position"));
			}else{
				ib_suggestion2.setVisibility(View.GONE);
			}

			if(bundle.getString("suggestion3") != null){
				RetrieveImageTask retrieveImageTask =  new RetrieveImageTask(ib_suggestion3,size,false,true);
				retrieveImageTask.execute(bundle.getString("suggestion3"));
				suggestion3_position = Integer.parseInt(bundle.getString("suggestion3 position"));
			}else{
				ib_suggestion3.setVisibility(View.GONE);
			}

			position = bundle.getString("position");
			keyword = bundle.getString("keyword");
			name = bundle.getString("name");
			heart = (bundle.getBoolean("heart") || heart);
			personal = bundle.getString("personal") != null;

			Log.d("photo detail position & name test",String.format("position: %s name: %s",position,name));
			//String temp = tv_score.getText() +"   " + getIntent().getStringExtra("keyword");
			String str = "This image own by Instragram user, and it is downloaded from Saveig.com by using search location " + keyword;
			tv_introduce.setText(str);
			str = "Other Photo you may like❤";
			tv_suggestion.setText(str);

			tv_location.setText(keyword);
			tv_score.setText(String.format("%.4f",Double.parseDouble(bundle.getString("score"))));
			if(Double.parseDouble(tv_score.getText().toString()) <= 4){
				ib_light.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.redlight32));
			}else if ( (Double.parseDouble(tv_score.getText().toString()) > 4) &&
					Double.parseDouble(tv_score.getText().toString()) <= 6){
				ib_light.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.yellowlight32));
			}else{
				ib_light.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.greenlight32));
			}
			if(rating != 0){
				privateRatingBar.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.star_fill));
				tv_privateRating.setText(String.valueOf(rating));
			}
			if(heart) {
				ib_heart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart1, null));
				heart = true;
			}else{
				ib_heart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart, null));
				heart = false;
			}
		}



		ib_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				BackgroundBlurPopupWindow mPopupWindow;
				View v = getActivity().getLayoutInflater().inflate(R.layout.popup_layout, null);
				ImageView iv = v.findViewById(R.id.imageView_browse);
				iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						iv.setImageBitmap(Bitmap.createScaledBitmap(((BitmapDrawable)(ib_photo.getDrawable())).getBitmap(), iv.getMeasuredWidth(), iv.getMeasuredHeight(),true));
					}
				});
				View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.photo_detail,null);
				mPopupWindow = new BackgroundBlurPopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, getActivity(), true);
				mPopupWindow.setFocusable(true);
				mPopupWindow.setBackgroundDrawable(new ColorDrawable());
				mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
			}
		});

		ib_heart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!heart) {
					ib_heart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart1, null));
					Toast.makeText(getContext(),"Added to Liked pictures",Toast.LENGTH_SHORT).show();
					heart = true;
				}else{
					ib_heart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart, null));
					Toast.makeText(getContext(),"Removed from Liked pictures",Toast.LENGTH_SHORT).show();
					heart = false;
				}

			}
		});

		ib_share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				//BitmapDrawable bitmapDrawable = (BitmapDrawable) ib_photo.getDrawable();
				//Bitmap shareBody = bitmapDrawable.getBitmap();
				String shareInstroduce = "Look at this photo on AI Camera ! Check more detail on: "
						+"\nhttp://140.115.51.177:8000/media/" + keyword + "/" + name;
				sharingIntent.putExtra(Intent.EXTRA_TEXT, shareInstroduce);
				startActivity(Intent.createChooser(sharingIntent, "Share To"));
			}
		});
		Float score = Float.parseFloat(tv_score.getText().toString());
		switch((int)(Math.random()*2) % 2 ){
			case 0 :{
				score += (float)(Math.random()*2);
				if(score > 10){score = 10- score;}
				score = Math.abs(score);
				break;
			}

			case 1:{
				score -= (float)(Math.random()*2);
				if(score > 10){score = 10- score;}
				score = Math.abs(score);
				break;
			}
		}
		tv_publicRating.setText(String.format("%.1f",score));


		privateRatingBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PopupWindow mPopupWindow;
				View v = getActivity().getLayoutInflater().inflate(R.layout.popup_layout_rating, null);
				RatingBar ratingBar = v.findViewById(R.id.ratingBar);

				ratingBar.setMax(10);
				ratingBar.setNumStars(10);
				ratingBar.setStepSize(1);

				View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.photo_detail, null);
				mPopupWindow = new PopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
				mPopupWindow.setFocusable(true);
				mPopupWindow.setBackgroundDrawable(new ColorDrawable());
				int[] ratingBarlocation = new int[2];
				privateRatingBar.getLocationOnScreen(ratingBarlocation);
				mPopupWindow.showAtLocation(rootView, Gravity.START, 10, ratingBarlocation[1] - VIEW_BIAS);//弹出PopupWindow
				Log.d("temp test now ", String.valueOf(ratingBarlocation[1]));
				if(!tv_privateRating.getText().equals("")) {
					ratingBar.setRating((int)Double.parseDouble(tv_privateRating.getText().toString()));
				}
				ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
						rating = ratingBar.getRating();
						String str = String.valueOf(rating);
						tv_privateRating.setText(str);
						privateRatingBar.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.star_fill));
						//Log.d("Rating detail", String.valueOf(privateRatingBar.getRating()));
					}
				});
			}
		});
		privateRatingBar.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				privateRatingBar.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.star));
				tv_privateRating.setText("");
				return false;
			}
		});
		ib_suggestion1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Handler photoFrameHandler = new Handler();
				photoFrameHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//swipeRefreshLayout.setVisibility(View.GONE);
						if(personal){
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).personalFragmentStack);
						}else{
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).mainFragmentStack);
						}
						((MainActivity)getActivity()).mFragmentTransaction = ((MainActivity)getActivity()).mFragmentManager.beginTransaction();
						if (((MainActivity)getActivity()).mPhotoFragment == null){
							if(!((MainActivity)getActivity()).mPhotoFragmentMap.containsKey(suggestion1_position)){
								((MainActivity)getActivity()).mPhotoFragment = new Photo_detail();
								((MainActivity)getActivity()).mPhotoFragmentMap.put(suggestion1_position,((MainActivity)getActivity()).mPhotoFragment);
							}else{
								((MainActivity)getActivity()).mPhotoFragment = ((MainActivity)getActivity()).mPhotoFragmentMap.get(suggestion1_position);
							}
						}

						Bundle bundle = new Bundle();
						ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
						bundle.putString("byteArray",
								"http://140.115.51.177:8000/media/" + keyword + "/" + ((MainActivity)getActivity()).imgUrl.get(suggestion1_position + 1).get("name"));
						ArrayList<Integer> visitList = new ArrayList<Integer>();
						for(int i = 2 ; i < 5 ; i++){
							String img_name= null;
							if(suggestion1_position + i < ((MainActivity)getActivity()).imgUrl.size()){
								img_name = ((MainActivity)getActivity()).imgUrl.get(suggestion1_position + i).get("name");
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
								bundle.putString("suggestion"+String.valueOf(i-1)+" position",String.valueOf(suggestion1_position+i-1));
							}
							if(byteArrayStream.toByteArray().length == 0){
								Log.d("Photo delivery","Failed");
							}
							img_name = "http://140.115.51.177:8000/media/" + keyword + "/" + img_name;
							bundle.putString("suggestion"+String.valueOf(i-1), img_name);

						}

						bundle.putString("position",String.valueOf(suggestion1_position));
						bundle.putString("score",((MainActivity)getActivity()).imgUrl.get(suggestion1_position+1).get("score") );
						bundle.putString("keyword", keyword);
						bundle.putString("name",((MainActivity)getActivity()).imgUrl.get(suggestion1_position+1).get("name"));

						//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();
						if(personal){
							((MainActivity)getActivity()).personalFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}else{
							((MainActivity)getActivity()).mainFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}
						((MainActivity)getActivity()).mPhotoFragment.setArguments(bundle);
						if (((MainActivity)getActivity()).mPhotoFragment.isAdded()) {
							((MainActivity)getActivity()).mFragmentTransaction.show(((MainActivity)getActivity()).mPhotoFragment);
						} else {
							((MainActivity)getActivity()).mFragmentTransaction.add(android.R.id.tabcontent, ((MainActivity)getActivity()).mPhotoFragment, "photo");
						}
						((MainActivity)getActivity()).mFragmentTransaction.addToBackStack(null).commit();
						String s = "Photo Detail";
						((MainActivity)getActivity()).textView.setText(s);
						((MainActivity)getActivity()).mPhotoFragment = null;
						if(personal){
							bundle.putString("personal","true");
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).personalFragmentStack.size()));
						}else{
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).mainFragmentStack.size()));
						}

					}
				}, 100);
			}
		});
		ib_suggestion2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Handler photoFrameHandler = new Handler();
				photoFrameHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//swipeRefreshLayout.setVisibility(View.GONE);
						if(personal){
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).personalFragmentStack);
						}else{
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).mainFragmentStack);
						}
						((MainActivity)getActivity()).mFragmentTransaction = ((MainActivity)getActivity()).mFragmentManager.beginTransaction();

						if (((MainActivity)getActivity()).mPhotoFragment == null){
							if(!((MainActivity)getActivity()).mPhotoFragmentMap.containsKey(suggestion2_position)){
								((MainActivity)getActivity()).mPhotoFragment = new Photo_detail();
								((MainActivity)getActivity()).mPhotoFragmentMap.put(suggestion2_position,((MainActivity)getActivity()).mPhotoFragment);
							}else{
								((MainActivity)getActivity()).mPhotoFragment = ((MainActivity)getActivity()).mPhotoFragmentMap.get(suggestion2_position);
							}
						}

						Bundle bundle = new Bundle();
						ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
						bundle.putString("byteArray",
								"http://140.115.51.177:8000/media/" + keyword + "/" + ((MainActivity)getActivity()).imgUrl.get(suggestion2_position + 1).get("name"));
						ArrayList<Integer> visitList = new ArrayList<Integer>();
						for(int i = 2 ; i < 5 ; i++){
							String img_name= null;
							if(suggestion2_position + i < ((MainActivity)getActivity()).imgUrl.size()){
								img_name = ((MainActivity)getActivity()).imgUrl.get(suggestion2_position + i).get("name");
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
								bundle.putString("suggestion"+String.valueOf(i-1)+" position",String.valueOf(suggestion2_position+i-1));
							}
							if(byteArrayStream.toByteArray().length == 0){
								Log.d("Photo delivery","Failed");
							}
							img_name = "http://140.115.51.177:8000/media/" + keyword + "/" + img_name;
							bundle.putString("suggestion"+String.valueOf(i-1), img_name);

						}

						bundle.putString("position",String.valueOf(suggestion2_position));
						bundle.putString("score",((MainActivity)getActivity()).imgUrl.get(suggestion2_position+1).get("score") );
						bundle.putString("keyword", keyword);
						bundle.putString("name",((MainActivity)getActivity()).imgUrl.get(suggestion2_position+1).get("name"));

						//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();

						if(personal){
							((MainActivity)getActivity()).personalFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}else{
							((MainActivity)getActivity()).mainFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}
						((MainActivity)getActivity()).mPhotoFragment.setArguments(bundle);
						if (((MainActivity)getActivity()).mPhotoFragment.isAdded()) {
							((MainActivity)getActivity()).mFragmentTransaction.show(((MainActivity)getActivity()).mPhotoFragment);
						} else {
							((MainActivity)getActivity()).mFragmentTransaction.add(android.R.id.tabcontent, ((MainActivity)getActivity()).mPhotoFragment, "photo");
						}
						((MainActivity)getActivity()).mFragmentTransaction.addToBackStack(null).commit();
						String s = "Photo Detail";
						((MainActivity)getActivity()).textView.setText(s);
						((MainActivity)getActivity()).mPhotoFragment = null;
						if(personal){
							bundle.putString("personal","true");
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).personalFragmentStack.size()));
						}else{
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).mainFragmentStack.size()));
						}
					}
				}, 100);
			}
		});
		ib_suggestion3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Handler photoFrameHandler = new Handler();
				photoFrameHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//swipeRefreshLayout.setVisibility(View.GONE);
						if(personal){
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).personalFragmentStack);
						}else{
							((MainActivity)getActivity()).updateCollection(((MainActivity)getActivity()).mainFragmentStack);
						}
						((MainActivity)getActivity()).mFragmentTransaction = ((MainActivity)getActivity()).mFragmentManager.beginTransaction();

						if (((MainActivity)getActivity()).mPhotoFragment == null){
							if(!((MainActivity)getActivity()).mPhotoFragmentMap.containsKey(suggestion3_position)){
								((MainActivity)getActivity()).mPhotoFragment = new Photo_detail();
								((MainActivity)getActivity()).mPhotoFragmentMap.put(suggestion3_position,((MainActivity)getActivity()).mPhotoFragment);
							}else{
								((MainActivity)getActivity()).mPhotoFragment = ((MainActivity)getActivity()).mPhotoFragmentMap.get(suggestion3_position);
							}
						}

						Bundle bundle = new Bundle();
						ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
						bundle.putString("byteArray",
								"http://140.115.51.177:8000/media/" + keyword + "/" + ((MainActivity)getActivity()).imgUrl.get(suggestion3_position + 1).get("name"));
						ArrayList<Integer> visitList = new ArrayList<Integer>();
						for(int i = 2 ; i < 5 ; i++){
							String img_name= null;
							if(suggestion3_position + i < ((MainActivity)getActivity()).imgUrl.size()){
								img_name = ((MainActivity)getActivity()).imgUrl.get(suggestion3_position + i).get("name");
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
								bundle.putString("suggestion"+String.valueOf(i-1)+" position",String.valueOf(suggestion3_position+i-1));
							}
							if(byteArrayStream.toByteArray().length == 0){
								Log.d("Photo delivery","Failed");
							}
							img_name = "http://140.115.51.177:8000/media/" + keyword + "/" + img_name;
							bundle.putString("suggestion"+String.valueOf(i-1), img_name);

						}

						bundle.putString("position",String.valueOf(suggestion3_position));
						bundle.putString("score",((MainActivity)getActivity()).imgUrl.get(suggestion3_position+1).get("score") );
						bundle.putString("keyword", keyword);
						bundle.putString("name",((MainActivity)getActivity()).imgUrl.get(suggestion3_position+1).get("name"));

						//Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();

						if(personal){
							((MainActivity)getActivity()).personalFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}else{
							((MainActivity)getActivity()).mainFragmentStack.add(((MainActivity)getActivity()).mPhotoFragment);
						}
						((MainActivity)getActivity()).mPhotoFragment.setArguments(bundle);
						if (((MainActivity)getActivity()).mPhotoFragment.isAdded()) {
							((MainActivity)getActivity()).mFragmentTransaction.show(((MainActivity)getActivity()).mPhotoFragment);
						} else {
							((MainActivity)getActivity()).mFragmentTransaction.add(android.R.id.tabcontent, ((MainActivity)getActivity()).mPhotoFragment, "photo");
						}
						((MainActivity)getActivity()).mFragmentTransaction.addToBackStack(null).commit();
						String s = "Photo Detail";
						((MainActivity)getActivity()).textView.setText(s);
						((MainActivity)getActivity()).mPhotoFragment = null;
						if(personal){
							bundle.putString("personal","true");
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).personalFragmentStack.size()));
						}else{
							Log.d("photo detail test",String.format("main stack size: %d, ",((MainActivity)getActivity()).mainFragmentStack.size()));
						}
					}
				}, 100);
			}
		});



	}
	public void InformClickedWithoutImage(){
		retrieveImageTask.InformClickedWithoutImage(ib_photo);
	}
}
