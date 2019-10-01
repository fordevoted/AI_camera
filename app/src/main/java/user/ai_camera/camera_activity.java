package user.ai_camera;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class camera_activity extends Activity  implements View.OnClickListener{

	private String authentication;
	private String account;
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
	private final static String AICAM = "AICam_";
	private final static String TAG = "camera test";
    // show the photo in  correct direction
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //Camera
	private CaptureRequest mPreviewRequest;
	private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
	private CaptureRequest.Builder mPreviewRequestBuilder;
	private CameraCharacteristics characteristics;
	private SurfaceTexture texture;
	private Surface surface;

	// Thread
	private HandlerThread mBackgroundThread;
    private Handler mMainHandler, mChildHandler;
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);
	private PostUploadTask httpclient;

	//View
	private TextureView mTextureView;
	private ImageView iv_show;
	private ImageButton ib_camera,ib_image, ib_imageQuery;
	private ImageButton ib_scoreLight;
	private Button ib_suggestion, ib_back,ib_save;
	private ImageButton ib_iso, ib_contrast, ib_bright;
	private TextView  tv_scoreShow;
	private ConstraintLayout containter;
	private SeekBar seekBar;


	private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
             configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
			closeCamera();
			return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }

    };

	// Variable

	private static final int ISO = 0;
	private static final int BRIGHT = 1;
	private static final int CONTRAST = 2;
	private static final int NOTSHOW = -1;
	private static final int MAX_PREVIEW_WIDTH = 1920;
	private static final int MAX_PREVIEW_HEIGHT = 1080;
	private static final  int REQUEST_CAMERA_PERMISSION = 1;
	private Size mPreviewSize;
	private Bitmap bitmap;
    private String mCameraID; //Id 0 is back   1 is front
	private String keyword;
	private Point size;
	private static double iso = 100;
	private static double bright = 0;
	private static double contrast = 0;
	private int seekBarFunctionShow = NOTSHOW;
	private Range<Integer> brightRangeResult;
	private boolean imageVisibleSemarphore;
    private boolean preview;
    private float finger_spacing = 0;
    private int zoom_level = 1;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
		authentication = getSharedPreferences("AICam",MODE_PRIVATE).getString("AUTH",null);
		account = getSharedPreferences("AICam",MODE_PRIVATE).getString("ACCOUNT",null);
        preview = false;

        if(getIntent().hasExtra("byteArray")) {
			bitmap = BitmapFactory.decodeByteArray(
					getIntent().getByteArrayExtra("byteArray"), 0, getIntent().getByteArrayExtra("byteArray").length);
		}
		if(getIntent().hasExtra("keyword")){
			keyword = getIntent().getStringExtra("keyword");
		}
		if(getIntent().hasExtra("ImageVisibleSemarphore")){
			imageVisibleSemarphore = getIntent().getBooleanExtra("ImageVisibleSemarphore",false);
		}
        initView();
		if(! imageVisibleSemarphore){
			ib_image.setVisibility(View.GONE);
		}
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
		//Log.d("Camera preview Test2", String.valueOf(preview));
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //open camera
            mCameraDevice = camera;
            mCameraOpenCloseLock.release();
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        	//close camera
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
        	//error occur
            Toast.makeText(camera_activity.this, "Error When Open Camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    /**
     *ini camera
     */
    private void initView() {
        iv_show = (ImageView) findViewById(R.id.iv_show);
        mTextureView = (TextureView) findViewById(R.id.texture_view_camera2_activity);
        ib_camera = (ImageButton)findViewById(R.id.camera_button);
        ib_image = (ImageButton)findViewById(R.id.imageButton);
        ib_back = (Button) findViewById(R.id.button_back);
        ib_save = (Button)findViewById(R.id.button_save);
        ib_imageQuery = (ImageButton) findViewById(R.id.imageButton_quary);
        tv_scoreShow = (TextView) findViewById(R.id.scoreShow);
        ib_suggestion = (Button) findViewById(R.id.button_suggestion);
        ib_scoreLight = (ImageButton) findViewById(R.id.ib_score_light);
		containter = (ConstraintLayout) findViewById(R.id.constraint4);
		ib_iso = findViewById(R.id.ib_iso);
		ib_bright = findViewById(R.id.ib_bright);
		ib_contrast = findViewById(R.id.ib_contrast);
		seekBar = findViewById(R.id.seekBar);


		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		// set photo  to imageButton first
		if(imageVisibleSemarphore){
			ib_image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if(bitmap != null){
						ib_image.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ib_image.getMeasuredWidth(), ib_image.getHeight(), true));
					}
				}
			});
		}

		ib_iso.setOnClickListener((view)->{
			Log.d(TAG,String.format("iso: %f, bright: %f, contrast: %f",iso,bright,contrast));
			if(seekBarFunctionShow != ISO){
				seekBar.setVisibility(View.VISIBLE);
				seekBarFunctionShow = ISO;
				seekBar.setProgress((int) (iso-100)/15);
			}else{
				seekBar.setVisibility(View.GONE);
				seekBarFunctionShow = NOTSHOW;

			}
			setCameraFunctionTint();
		});

		ib_bright.setOnClickListener((view)->{
			Log.d(TAG,String.format("iso: %f, bright: %f, contrast: %f",iso,bright,contrast));
			if(seekBarFunctionShow != BRIGHT){
				seekBar.setVisibility(View.VISIBLE);
				seekBarFunctionShow = BRIGHT;
				if(brightRangeResult== null){
					brightRangeResult = null;
					Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
					for (Range<Integer> range : ranges) {
						int upper = range.getUpper();
						// 10 - min range upper for my needs
						if (upper >= 10) {
							if (brightRangeResult == null || upper < brightRangeResult.getUpper().intValue()) {
								brightRangeResult = range;
							}
						}
					}
					if (brightRangeResult == null) {
						brightRangeResult = ranges[0];
					}
					seekBar.setProgress((int) (bright*100/brightRangeResult.getUpper()));
				}

			}else{
				seekBar.setVisibility(View.GONE);
				seekBarFunctionShow = NOTSHOW;
			}
			setCameraFunctionTint();
		});

		ib_contrast.setOnClickListener((view)->{
			Log.d(TAG,String.format("iso: %f, bright: %f, contrast: %f",iso,bright,contrast));
			if(seekBarFunctionShow != CONTRAST){
				seekBar.setVisibility(View.VISIBLE);
				seekBarFunctionShow = CONTRAST;
				seekBar.setProgress((int) (contrast * 100));
			}else{
				seekBar.setVisibility(View.GONE);
				seekBarFunctionShow = NOTSHOW;
			}
			setCameraFunctionTint();
		});

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				switch (seekBarFunctionShow){
					case ISO:{
						Range<Integer> range = characteristics.get((CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE));
						iso = progress * 15 + 100;
						break;
					}
					case BRIGHT:{
						bright = progress * brightRangeResult.getUpper() / 100.0 ;
						break;
					}
					case CONTRAST:{
						contrast = progress / 100.0;
						break;
					}
					default:{
					}
				}
				Log.d(TAG,String.format("after progress change iso: %f, bright: %f, contrast: %f",iso,bright,contrast));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});


        ib_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	takePicture();

                setViewVisible(true);   //suppose preview = true
				JustPostForCriticTask justPostForCriticTask = new JustPostForCriticTask(iv_show.getDrawable(), new float[]{0,0,0}, keyword,authentication);

				final int[] index = {0};
				Handler samHandler = new Handler();
				// the runnable run to wait asyncTask resultBack
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
				// the runnable run to get direction 
				Runnable run = new Runnable() {
					@Override
					public void run() {
						if (preview) {
							closeCamera();
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
						}else {
							Log.d("Camera test","take picture but preview still true");
							samHandler.postDelayed(this, 100);
						}
					}
				};
                samHandler.postDelayed(run, 100);
			}
        });
        ib_imageQuery.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                // Query Activity
				startActivity(new Intent(camera_activity.this, search_activity.class));
            }
        });
        ib_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // show model
				BackgroundBlurPopupWindow mPopupWindow;
				View v = getLayoutInflater().inflate(R.layout.popup_layout, null);
				ImageView iv = v.findViewById(R.id.imageView_browse);

				if(iv.getMeasuredHeight() > 0){
					iv.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int)(size.x * 0.95), (int)(size.y * 0.8), true));
				}else{
					iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
						@Override
						public void onGlobalLayout() {
							if(bitmap != null) {
								iv.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int)(size.x * 0.95), (int)(size.y * 0.8), true));
							}else{
								iv.setImageBitmap(Bitmap.createScaledBitmap(((BitmapDrawable)ib_image.getDrawable()).getBitmap(), (int)(size.x * 0.95), (int)(size.y * 0.8),true));
							}
						}
					});
				}
				iv.setAlpha((float) 0.5);
				View rootView = LayoutInflater.from(camera_activity.this).inflate(R.layout.camera_layout, null);
				mPopupWindow = new BackgroundBlurPopupWindow(v , (int)(size.x * 0.9d), (int)(size.y * 0.75), camera_activity.this, true);
				mPopupWindow.setFocusable(true);
				mPopupWindow.setBackgroundDrawable(new ColorDrawable());
				mPopupWindow.setDownScaleFactor(1.2f);
				mPopupWindow.setBlurRadius(10);//配置虚化比例
				mPopupWindow.setDarkAnimStyle(R.anim.dark_fade_in);//动画
				mPopupWindow.setDarkColor(Color.parseColor("#88333333"));//颜色
				mPopupWindow.resetDarkPosition();
				mPopupWindow.darkFillScreen();//全屏
				mPopupWindow.showAtLocation(rootView, Gravity.TOP, 0, 100);//弹出PopupWindow
				//Log.d("Camera Image Button Test","click");

                return false;
            }
        });
        
        
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preview = false;
                openCamera(mTextureView.getWidth(), mTextureView.getHeight());
                setViewVisible(preview);
                tv_scoreShow.setText(R.string.i_am_scoreshow);
                initView();
            }
        });
        ib_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if( saveImage(((BitmapDrawable)iv_show.getDrawable()).getBitmap()) ){
               		finish();
			   }
            }
        });
        ib_suggestion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// show model
				httpclient = new PostUploadTask(iv_show.getDrawable(), new float[]{0,0,0}, keyword,authentication);

				View v = getLayoutInflater().inflate(R.layout.suggestion_pop_layout, null);
				View rootView = LayoutInflater.from(camera_activity.this).inflate(R.layout.camera_layout, null);
				
				BackgroundBlurPopupWindow mPopupWindow;
				mPopupWindow = new BackgroundBlurPopupWindow(v , (int)(size.x * 0.8), (int)(size.y * 0.75), camera_activity.this, true);

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
										tv_suggestion.setTextSize((int)(size.x * 0.03));
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
						}else{
							samHandler.postDelayed(this,100);
						}

					}
				};
				Runnable run = new Runnable() {
					@Override
					public void run() {
						if (preview) {
							closeCamera();

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
									if(saveImage(((BitmapDrawable)iv_suggestion.getDrawable()).getBitmap())){
										mPopupWindow.dismiss();
									}
								}
							});

							httpclient.execute("http://140.115.51.177:8000/upload/api=upload");
							samHandler.postDelayed(run2, 100);

						}else {
							Log.d("Camera test","get suggestion but preview still true");
							samHandler.postDelayed(this, 100);
						}
					}
				};
				samHandler.postDelayed(run,100);
			}
		});
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        setUpCameraOutputs(width, height);
        configureTransform(width, height);

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                Toast.makeText(getApplicationContext(), "Time out waiting to lock camera opening.", Toast.LENGTH_SHORT).show();
            	throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraID, stateCallback, mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraId);
                Integer t = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
				Log.d(TAG, "level " + t);
                // We don't use a front facing camera
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

                	// deal with take picture
                    @Override
                    public void onImageAvailable(ImageReader reader) {
						//Toast.makeText(getApplicationContext(), " Enter imageReaderListener.onImageAvailable", Toast.LENGTH_SHORT).show();
                        Image image = reader.acquireNextImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bitmap != null) {
                            Matrix matrix = new Matrix();

                            int rotation = camera_activity.this.getWindowManager().getDefaultDisplay().getRotation();
                            // rotate the image
                            Log.d("Camera Orientation Test","CaptureRequest.JPEG_ORIENTATION : " +
									CaptureRequest.JPEG_ORIENTATION + "   ORIENTATIONS.get(rotation) : " + ORIENTATIONS.get(rotation));
                            matrix.postRotate(ORIENTATIONS.get(rotation));
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
							iv_show.setImageBitmap(rotatedBitmap);
                            preview = true;
						}else{
							Toast.makeText(getApplicationContext(), "ImageReader fail", Toast.LENGTH_SHORT).show();
						}
                    }
                }, mMainHandler);

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
				int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger! Attempting to use too large a preview size could exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
						width, height, maxPreviewWidth,
                        maxPreviewHeight, largest);

                mCameraID = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Toast.makeText(getApplicationContext(), "Camera2 API not supported on this device", Toast.LENGTH_LONG).show();
        }
    }

    private void createCameraPreviewSession() {
    	Log.d("createdCameraPreviewSession test","enter ");
    	try {
            texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
			surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.

			mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCameraCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.

                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_OFF);
								mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);

                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,(int) iso);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,(int)bright);

								TonemapCurve tc = mPreviewRequestBuilder.get(CaptureRequest.TONEMAP_CURVE);
								float[][] channels = null;
								if (tc != null) {
									channels = new float[3][];
									for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
										float[] array = new float[tc.getPointCount(chanel) * 2];
										tc.copyColorCurve(chanel, array, 0);
										channels[chanel] = array;
									}
									float[][] newValues = new float[3][];
									for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
										float[] array = new float [channels[chanel].length];
										System.arraycopy(channels[chanel], 0, array, 0, array.length);
										for (int i = 0; i < array.length; i++) {
											array[i] *= contrast;
										}
										newValues[chanel] = array;
									}
									TonemapCurve tcNew = new TonemapCurve(newValues[TonemapCurve.CHANNEL_RED], newValues[TonemapCurve.CHANNEL_GREEN], newValues[TonemapCurve.CHANNEL_BLUE]);
									mPreviewRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
									mPreviewRequestBuilder.set(CaptureRequest.TONEMAP_CURVE, tcNew);
								}

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCameraCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mChildHandler);
                                mCameraCaptureSession.capture(mPreviewRequest,
										null, mChildHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(camera_activity.this, "Failed to created preview", Toast.LENGTH_LONG).show();
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(camera_activity.this, "ERROR: Camera permissions not granted", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("Camera Preview Notification", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }



    /**
        * Override  BackPress
        */
    @Override
    public void onBackPressed() {
        if(preview){
            preview = false;
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            setViewVisible(preview);
            tv_scoreShow.setText(R.string.i_am_scoreshow);
            initView();
        }
        else{
        	finish();
        }
    }
    @Override
    public void onClick(View view) {
    }
    //Determine the space between the first two fingers
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;
        return jpegOrientation;
    }
    private void setViewVisible(boolean set){
        if(set){
            ib_camera.setVisibility(View.GONE);
            ib_bright.setVisibility(View.GONE);
			ib_iso.setVisibility(View.GONE);
			ib_contrast.setVisibility(View.GONE);
            ib_image.setVisibility(View.GONE);
            ib_back.setVisibility(View.VISIBLE);
            ib_save.setVisibility(View.VISIBLE);
            ib_imageQuery.setVisibility(View.GONE);
            mTextureView.setVisibility(View.GONE);
            iv_show.setVisibility(View.VISIBLE);
			tv_scoreShow.setVisibility(View.VISIBLE);
			ib_suggestion.setVisibility(View.VISIBLE);
			ib_scoreLight.setVisibility(View.VISIBLE);
			containter.setVisibility(View.VISIBLE);

			seekBar.setVisibility(View.GONE);
        }
        else{
            ib_camera.setVisibility(View.VISIBLE);
            if(imageVisibleSemarphore){
				ib_image.setVisibility(View.VISIBLE);
			}
			ib_bright.setVisibility(View.VISIBLE);
			ib_iso.setVisibility(View.VISIBLE);
			ib_contrast.setVisibility(View.VISIBLE);
			ib_back.setVisibility(View.GONE);
            ib_save.setVisibility(View.GONE);
            ib_imageQuery.setVisibility(View.VISIBLE);
            mTextureView.setVisibility(View.VISIBLE);
            iv_show.setVisibility(View.GONE);
			tv_scoreShow.setVisibility(View.GONE);
			ib_suggestion.setVisibility(View.GONE);
			ib_scoreLight.setVisibility(View.GONE);
			containter.setVisibility(View.GONE);
			iv_show.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.loadingg));
        }
    }
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mMainHandler = new Handler(getMainLooper());
        mChildHandler = new Handler(mBackgroundThread.getLooper());
    }
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mChildHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void takePicture() {
    	seekBarFunctionShow = NOTSHOW;
		if (mCameraDevice == null) return;
		// 创建拍照需要的CaptureRequest.Builder
		final CaptureRequest.Builder captureRequestBuilder;
		try {
			captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			// 将imageReader的surface作为CaptureRequest.Builder的目标
			captureRequestBuilder.addTarget(mImageReader.getSurface());
			// 自动对焦
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			// 自动曝光
			//captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_OFF);
			captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);

			captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,(int) iso);
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,(int)bright);
			Log.d(TAG,String.format("take picture iso: %f, bright: %f",iso,bright));
			TonemapCurve tc = captureRequestBuilder.get(CaptureRequest.TONEMAP_CURVE);
			float[][] channels = null;
			if (tc != null) {
				channels = new float[3][];
				for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
					float[] array = new float[tc.getPointCount(chanel) * 2];
					tc.copyColorCurve(chanel, array, 0);
					channels[chanel] = array;
				}
				float[][] newValues = new float[3][];
				for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
					float[] array = new float [channels[chanel].length];
					System.arraycopy(channels[chanel], 0, array, 0, array.length);
					for (int i = 0; i < array.length; i++) {
						array[i] *= contrast;
					}
					newValues[chanel] = array;
				}
				TonemapCurve tcNew = new TonemapCurve(newValues[TonemapCurve.CHANNEL_RED], newValues[TonemapCurve.CHANNEL_GREEN], newValues[TonemapCurve.CHANNEL_BLUE]);
				captureRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
				captureRequestBuilder.set(CaptureRequest.TONEMAP_CURVE, tcNew);
			}
			// 获取手机方向
			///int rotation = camera_activity.this.getWindowManager().getDefaultDisplay().getRotation();
			// 根据设备方向计算设置照片的方向
			///CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraID);
			///captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics,rotation));
			///Log.d("test","CaptureRequest.JPEG_ORIENTATION : "+CaptureRequest.JPEG_ORIENTATION+"   ORIENTATIONS.get(rotation) : "+ORIENTATIONS.get(rotation));

			//拍照
			CaptureRequest mCaptureRequest = captureRequestBuilder.build();
			mCameraCaptureSession.capture(mCaptureRequest, null, mChildHandler);
			//Toast.makeText(getApplicationContext()," take picture success ",Toast.LENGTH_SHORT).show();
		} catch (CameraAccessException e) {
			Toast.makeText(getApplicationContext()," take picture fail ",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	private void requestCameraPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
			new AlertDialog.Builder(camera_activity.this)
					.setMessage("Grant To Access Camera permission?")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(camera_activity.this,
									new String[]{Manifest.permission.CAMERA},
									REQUEST_CAMERA_PERMISSION);
						}
					})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							})
					.create();

		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
					REQUEST_CAMERA_PERMISSION);
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

	public boolean saveImage(Bitmap bitmap){

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

	private void setCameraFunctionTint(){
    	switch (seekBarFunctionShow){
			case ISO:{
				ib_iso.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),R.color.lightBlue)));
				ib_bright.setImageTintList(null);
				ib_contrast.setImageTintList(null);
				break;
			}
			case BRIGHT:{
				ib_iso.setImageTintList(null);
				ib_bright.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),R.color.lightBlue)));
				ib_contrast.setImageTintList(null);
				break;
			}
			case CONTRAST:{
				ib_iso.setImageTintList(null);
				ib_bright.setImageTintList(null);
				ib_contrast.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(),R.color.lightBlue)));
				break;
			}
			default:{
				ib_iso.setImageTintList(null);
				ib_bright.setImageTintList(null);
				ib_contrast.setImageTintList(null);
			}
		}

	}
}

