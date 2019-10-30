package user.ai_camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class Upload_image extends Activity {
	@InjectView(R.id.iv_show)
	ImageView iv_show;
	@InjectView(R.id.button_back)
	Button ib_back;
	@InjectView(R.id.button_suggestion)
	Button ib_suggestion;
	@InjectView(R.id.scoreShow)
	TextView tv_scoreShow;
	@InjectView(R.id.ib_score_light)
	ImageButton ib_scoreLight;
	private final static String AICAM = "AICam_";
	private final static String CROP_CATUION = "✂ best crop suggest for you !";

	public PostUploadTask httpclient;
	public String keyword = "userUpload";
	private String authentication;
	private String account;
	private Bitmap bitmap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_image_layout);
		ButterKnife.inject(this);
		authentication = getSharedPreferences("AICam",MODE_PRIVATE).getString("AUTH",null);
		account = getSharedPreferences("AICam",MODE_PRIVATE).getString("ACCOUNT",null);

		try {
			Bitmap loadedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), getIntent().getData());
			String picturePath = getIntent().getStringExtra("path");
			ExifInterface exif = null;
			if(picturePath != null){
				File pictureFile = new File(picturePath);
				exif = new ExifInterface(pictureFile.getAbsolutePath());
			}
			int orientation = ExifInterface.ORIENTATION_NORMAL;
			if(exif != null){
				orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			}
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:{
					loadedBitmap = rotateBitmap(loadedBitmap, 90);
					break;
				}
				case ExifInterface.ORIENTATION_ROTATE_180:{
					loadedBitmap = rotateBitmap(loadedBitmap, 180);
					break;
				}
				case ExifInterface.ORIENTATION_ROTATE_270: {
					loadedBitmap = rotateBitmap(loadedBitmap, 270);
					break;
				}
			}
			bitmap = loadedBitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}catch (NullPointerException e2){
			e2.printStackTrace();
		}
		iv_show.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (iv_show.getMeasuredHeight() <= 0) {
					Log.d("Photo ", "Measure Fail");
					iv_show.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 600, 600, true));
				} else {
					int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
					int specWidth = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED);
					//ib_photo.measure(specWidth, specHeight);

					//Log.d("Photo ", "Measure Success" + ib_photo.getMeasuredWidth() + "   " + ib_photo.getMeasuredHeight());
					iv_show.setImageBitmap(Bitmap.createScaledBitmap(bitmap, iv_show.getMeasuredWidth(), iv_show.getMeasuredHeight(), true));
				}
			}
		});
		ib_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		Drawable drawable = new BitmapDrawable(getResources(), bitmap);
		JustPostForCriticTask justPostForCriticTask = new JustPostForCriticTask(drawable, new float[]{0,0,0}, keyword,authentication);

		final int[] index = {0};
		Handler samHandler = new Handler();
		Runnable run2 = new Runnable() {
			@Override
			public void run() {
				if (justPostForCriticTask.resultBack() != -1) {
					tv_scoreShow.setText(String.format("%.4f", justPostForCriticTask.resultBack()));
					if(justPostForCriticTask.resultBack() <= 4){
						ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.redlight32));
					}else if (justPostForCriticTask.resultBack() <= 6 && justPostForCriticTask.resultBack() > 4){
						ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.yellowlight32));
					}else{
						ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.greenlight32));
					}
					Log.d("Score In Runnable Test", justPostForCriticTask.resultBack() + "?");
				}else {
					String str  = getResources().getString(R.string.i_am_scoreshow);
					switch(index[0]){
						case 0:{
							ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.yellowlight32));
							tv_scoreShow.setText(str.substring(0,str.length()-2));
							index[0] = (++index[0])%3;
							break;
						}
						case 1:{
							ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.redlight32));
							tv_scoreShow.setText(str.substring(0,str.length()-1));
							index[0] = (++index[0])%3;
							break;
						}
						case 2 :{
							ib_scoreLight.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.greenlight32));
							tv_scoreShow.setText(str);
							index[0] = (++index[0])%3;
							break;
						}
					}
					samHandler.postDelayed(this, 500);
				}
			}
		};
		Runnable run = new Runnable() {
			@Override
			public void run() {
					final float[] accelerometerReading = new float[3];
					final float[] magnetometerReading = new float[3];
					final boolean[] record = new boolean[]{false,false};
					final float[] rotationMatrix = new float[9];
					final float[] orientationAngles = new float[3];

					SensorManager sensorManager =   (SensorManager) getSystemService(SENSOR_SERVICE);
					SensorEventListener accListener = new SensorEventListener() {
						@Override
						public void onSensorChanged(SensorEvent sensorEvent) {
							System.arraycopy(sensorEvent.values, 0, accelerometerReading,
									0, accelerometerReading.length);
							record[0] = true;
							sensorManager.unregisterListener(this);
						}

						@Override
						public void onAccuracyChanged(Sensor sensor, int i) {
							//nothing
						}
					};
					SensorEventListener magListener = new SensorEventListener() {
						@Override
						public void onSensorChanged(SensorEvent sensorEvent) {
							System.arraycopy(sensorEvent.values, 0, magnetometerReading,
									0, magnetometerReading.length);
							record[1] = true;
							sensorManager.unregisterListener(this);
						}

						@Override
						public void onAccuracyChanged(Sensor sensor, int i) {
							//nothing
						}
					};

					Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					if (accelerometer != null) {
						sensorManager.registerListener(accListener, accelerometer,
								SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
					}
					Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
					if (magneticField != null) {
						sensorManager.registerListener(magListener, magneticField,
								SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
					}
					samHandler.postDelayed(()->{
						if(record[0] && record[1] ){
							SensorManager.getRotationMatrix(rotationMatrix, null,
									accelerometerReading, magnetometerReading);
							SensorManager.getOrientation(rotationMatrix, orientationAngles);	// store orientation into orientationAngles
							orientationAngles[0] = (float)(Math.toDegrees(orientationAngles[0]) + 360) % 360;
							//Toast.makeText(getApplicationContext(),"Camera  direction" + orientationAngles[0],Toast.LENGTH_SHORT).show();

							justPostForCriticTask.updateData(iv_show.getDrawable(),orientationAngles,keyword);
							justPostForCriticTask.execute("http://140.115.51.177:8000/upload/api=upload_");
							samHandler.postDelayed(run2, 100);

						}else{
							samHandler.postDelayed(this,100);
						}
					},100);

			}
		};
		samHandler.postDelayed(run, 100);
		ib_suggestion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// show model
				httpclient = new PostUploadTask(iv_show.getDrawable(), new float[]{0,0,0}, keyword,authentication);

				View v = getLayoutInflater().inflate(R.layout.suggestion_pop_layout, null);
				View rootView = LayoutInflater.from(Upload_image.this).inflate(R.layout.camera_layout, null);

				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				BackgroundBlurPopupWindow mPopupWindow;
				mPopupWindow = new BackgroundBlurPopupWindow(v , (int)(size.x * 0.8), (int)(size.y * 0.75), Upload_image.this, true);

				TextView tv_suggestion = v.findViewById(R.id.tv_adjust_show);
				ImageView iv_suggestion = v.findViewById(R.id.iv_suggestion);
				ImageButton ib_save_suggestion = v.findViewById(R.id.ib_save_suggestion);

				Handler samHandler = new Handler();
				Runnable run2 = new Runnable() {
					@Override
					public void run() {
						if(httpclient.resultBack() != -1 ){
							List<List<Double>> suggestion = httpclient.suggestionBack();
							StringBuilder str = new StringBuilder();
							RetrieveImageTask retrieveImageTask;
							retrieveImageTask =  new RetrieveImageTask(size,false);
							Runnable run = new Runnable() {
								@Override
								public void run() {
									if(retrieveImageTask.resultBack()!= null){
										iv_suggestion.setImageBitmap(Bitmap.createScaledBitmap(retrieveImageTask.resultBack(),
												(int) (size.x * 0.8d),
												(int) (size.y * 0.4d),
												true));
										tv_suggestion.setText(str);
										tv_suggestion.setTextSize((int)(size.y * 0.02));
										ib_save_suggestion.setVisibility(View.VISIBLE);
										Log.d("Camera test","end");
									}else{
										samHandler.postDelayed(this,100);
									}
								}
							};
							if(suggestion != null){
								retrieveImageTask.execute("http://140.115.51.177:8000/media/User/" + account + "/" + suggestion.get(0).get(3));
								//retrieveImageTask.execute("http://140.115.51.177:8000/media/" + keyword + "/" + suggestion.get(0).get(3));
								samHandler.postDelayed(run,100);
							}

							if(suggestion != null){
								str.append(String.format("%.4f", suggestion.get(0).get(0)))
										.append("\n")
										.append("\n  Bright    ").append(suggestion.get(0).get(1).intValue() > 0 ? "+ " + suggestion.get(0).get(1).intValue() : suggestion.get(0).get(1).intValue())
										.append("\n  Contrast    ").append(suggestion.get(0).get(2).intValue() > 0 ? "+ " + suggestion.get(0).get(2).intValue() : suggestion.get(0).get(2).intValue())
								;
							}
							if(httpclient.isPhotoCrop){
								str.append("\n" + CROP_CATUION);
							}
						}else{
							samHandler.postDelayed(this,100);
						}

					}
				};
				Runnable run = new Runnable() {
					@Override
					public void run() {

						mPopupWindow.setFocusable(true);
						mPopupWindow.setBackgroundDrawable(new ColorDrawable());
						mPopupWindow.setDownScaleFactor(1.2f);
						mPopupWindow.setBlurRadius(10);//配置虚化比例
						mPopupWindow.setDarkAnimStyle(R.anim.dark_fade_in);//动画
						mPopupWindow.setDarkColor(Color.parseColor("#a0000000"));//颜色
						mPopupWindow.resetDarkPosition();
						mPopupWindow.darkFillScreen();//全屏
						mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, -30);//弹出PopupWindow
						//Log.d("Camera Image Button Test","click");
						ib_save_suggestion.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if(SaveIamge(((BitmapDrawable)iv_suggestion.getDrawable()).getBitmap())){
									mPopupWindow.dismiss();
								}
							}
						});

							final float[] accelerometerReading = new float[3];
							final float[] magnetometerReading = new float[3];
							final boolean[] record = new boolean[]{false,false};
							final float[] rotationMatrix = new float[9];
							final float[] orientationAngles = new float[3];

							SensorManager sensorManager =  (SensorManager) getSystemService(SENSOR_SERVICE);
							SensorEventListener accListener = new SensorEventListener() {
								@Override
								public void onSensorChanged(SensorEvent sensorEvent) {
									System.arraycopy(sensorEvent.values, 0, accelerometerReading,
											0, accelerometerReading.length);
									record[0] = true;
									sensorManager.unregisterListener(this);
								}

								@Override
								public void onAccuracyChanged(Sensor sensor, int i) {
									//nothing
								}
							};
							SensorEventListener magListener = new SensorEventListener() {
								@Override
								public void onSensorChanged(SensorEvent sensorEvent) {
									System.arraycopy(sensorEvent.values, 0, magnetometerReading,
											0, magnetometerReading.length);
									record[1] = true;
									sensorManager.unregisterListener(this);
								}

								@Override
								public void onAccuracyChanged(Sensor sensor, int i) {
									//nothing
								}
							};

							Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
							if (accelerometer != null) {
								sensorManager.registerListener(accListener, accelerometer,
										SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
							}
							Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
							if (magneticField != null) {
								sensorManager.registerListener(magListener, magneticField,
										SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
							}
							samHandler.postDelayed(()->{
								if(record[0] && record[1] ){
									SensorManager.getRotationMatrix(rotationMatrix, null,
											accelerometerReading, magnetometerReading);
									SensorManager.getOrientation(rotationMatrix, orientationAngles);	// store orientation into orientationAngles
									orientationAngles[0] = (float)(Math.toDegrees(orientationAngles[0]) + 360) % 360;
									//Toast.makeText(getApplicationContext(),"Camera  direction" + orientationAngles[0],Toast.LENGTH_SHORT).show();

									httpclient.updateData(iv_show.getDrawable(),orientationAngles,keyword);
									httpclient.execute("http://140.115.51.177:8000/upload/api=upload");


									samHandler.postDelayed(run2, 100);
								}else{
									samHandler.postDelayed(this,100);
								}
							},100);

					}
				};
				samHandler.postDelayed(run,100);
			}
		});
	}


	public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
	public boolean SaveIamge(Bitmap bitmap){

		boolean isSuccess = false;
		String fileName = AICAM + System.currentTimeMillis()+".jpg";
		FileOutputStream outStream;
		File sdCard = Environment.getExternalStorageDirectory();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(sdCard.getAbsolutePath());
		stringBuilder.append("/AI_Camera");
		File dir = new File(stringBuilder.toString());

		File file = new File(getApplicationContext().getFilesDir(), "AI_Camera");
				/*if(!file.exists()){
					Log.d("dir exist test","file true");

				}
				if(!dir.exists()) {
					Log.d("dir exist test","dir true");

				}*/
		file.mkdirs();
		dir.mkdirs();


		String path = sdCard.getAbsolutePath() + "/AI_Camera/";
		String filePath = path + fileName;

		//Log.d("test",dir.getAbsolutePath() + "  " + dir.getName());
		//Log.d("test",file.getAbsolutePath() + "  " + file.getName());
		try {
			if(!new File(filePath).exists())
				new File(filePath).createNewFile();
			outStream = new FileOutputStream(new File(dir, fileName));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();

			Toast.makeText(getApplicationContext(),"Save image success", Toast.LENGTH_LONG).show();
			isSuccess = true;
		} catch (FileNotFoundException e) {
			try {
				outStream = new FileOutputStream(new File(file, fileName + "2"));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				outStream.close();
				Toast.makeText(getApplicationContext(),"Save image success", Toast.LENGTH_LONG).show();
				isSuccess = true;
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
}
