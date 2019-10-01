package user.ai_camera;

public class garbageclass {
        /*
        from MainActivity.onCreate();
        searchView.onActionViewExpanded();
        InputMethodManager imm = (InputMethodManager) searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchView.clearFocus();
        hideKeyboard(MainActivity.super.getParent(),this.getCurrentFocus());
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    String keyword="國立中央大學";
                    if (scrollView.getScrollY()==0) {
                        searchView.setQueryHint(keyword);
                    } else {
                        searchView.setQueryHint(keyword+"附近景點");
                    }
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                ByteArrayOutputstream bytearraystream = new ByteArrayOutputStream();
                Bitmap bitmap = ((BitmapDrawable)imageButton.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bytearraystream);
                intent.putExtra("byteArray", bytearraystream.toByteArray());
                intent.setClass(MainActivity.this,Photo_detail.class);
                Toast.makeText(getApplicationContext(),"From Main to Photo_detail",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SimpleSearchView", "Submit:" + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SimpleSearchView", "Text changed:" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                Log.d("SimpleSearchView", "Text cleared");
                return false;
            }
        });*/

        /* from MainActivity.initView()
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navgationHeight = resources.getDimensionPixelSize(resourceId);
        }

        lp.height = ((size.y*95/100-actionBarHeight)-navgationHeight);
        lp.width = size.x;
        linearLayout.setLayoutParams(lp);

        Log.d("size","size.y : "+size.y+" toolbar.getHeight() : "+actionBarHeight+" navigation.getHeight() : "+navgationHeight+" lp.height: "+lp.height);
         */

        //From MainActivity onCreate()
        /*Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final double[] longitude = {location.getLongitude()};
        final double[] latitude = {location.getLatitude()};
        Log.d("lat long","lat long printout start\n");
        for(int i = 0 ; i < longitude.length ; i++) {
            Log.d("lat long",String.format("%d lat :%f  long :%f",i, longitude[i], latitude[i]));
            Toast.makeText(this, String.format("%d lat :%f  long :%f",i, longitude[i], latitude[i]), Toast.LENGTH_LONG).show();
        }*/



        //Form Camera_activity use to zoom in
/*   mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d("zoom","on touch");
                try {
                    final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    Activity activity = camera_activity.this;
                    CameraManager manager = mCameraManager;
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraID);
                    float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM))*10;

                    Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int action = event.getAction();
                    float current_finger_spacing;

                    if (event.getPointerCount() > 1) {
                        // Multi touch logic
                        current_finger_spacing = getFingerSpacing(event);
                        if(finger_spacing != 0){
                            if(current_finger_spacing > finger_spacing && maxzoom > zoom_level){
                                zoom_level++;
                            } else if (current_finger_spacing < finger_spacing && zoom_level > 1){
                                zoom_level--;
                            }
                            int minW = (int) (m.width() / maxzoom);
                            int minH = (int) (m.height() / maxzoom);
                            int difW = m.width() - minW;
                            int difH = m.height() - minH;
                            int cropW = difW /100 *(int)zoom_level;
                            int cropH = difH /100 *(int)zoom_level;
                            cropW -= cropW & 3;
                            cropH -= cropH & 3;
                            Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
                            previewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                        }
                        finger_spacing = current_finger_spacing;
                    } else{
                        if (action == MotionEvent.ACTION_UP) {
                            //single touch logic
                        }
                    }

                    try {
                        previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
                        mCameraCaptureSession
                                .setRepeatingRequest(previewRequestBuilder.build(),mSessionCaptureCallback, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                } catch (CameraAccessException e) {
                    throw new RuntimeException("can not access camera.", e);
                }
                return true;
            }
        });*/


/* @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {

        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();

        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;//后摄像头
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地

            @Override
            public void onImageAvailable(ImageReader reader) {

                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    Matrix matrix = new Matrix();

                    int rotation = camera_activity.this.getWindowManager().getDefaultDisplay().getRotation();
                    // 根据设备方向计算设置照片的方向
                    Log.d("test","CaptureRequest.JPEG_ORIENTATION : "+CaptureRequest.JPEG_ORIENTATION+"   ORIENTATIONS.get(rotation) : "+ORIENTATIONS.get(rotation));
                    matrix.postRotate(ORIENTATIONS.get(rotation));
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    iv_show.setImageBitmap(rotatedBitmap);

                }
            }
        }, mainHandler);
        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 摄像头创建监听
     *//*


	/**
	 * capture callback
	 *//*
	private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
			new CameraCaptureSession.CaptureCallback() {

				@Override
				public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
											   TotalCaptureResult result) {
					//            Log.d("linc","mSessionCaptureCallback, onCaptureCompleted");
					mCameraCaptureSession = session;
					//checkState(result);
				}

				@Override
				public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
												CaptureResult partialResult) {
					Log.d("linc","mSessionCaptureCallback,  onCaptureProgressed");
					mCameraCaptureSession = session;
					//checkState(partialResult);
				}



			};
	/**
	 * preview
	 *//*
	private void takePreview() {
		try {
			// 创建预览需要的CaptureRequest.Builder
			final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			// 将SurfaceView的surface作为CaptureRequest.Builder的目标
			previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
			// 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
			mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
			{
				@Override
				public void onConfigured(CameraCaptureSession cameraCaptureSession) {
					if (null == mCameraDevice) return;
					// 当摄像头已经准备好时，开始显示预览
					mCameraCaptureSession = cameraCaptureSession;
					try {
						// 自动对焦
						previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
						// 打开闪光灯
						previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
						// 显示预览
						CaptureRequest previewRequest = previewRequestBuilder.build();
						mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
					Toast.makeText(camera_activity.this, "配置失败", Toast.LENGTH_SHORT).show();
				}
			}, childHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}



	/**
	 * take picture
	 *//*

*/


	/*
	* "airport",
			"amusement_park",
			"aquarium",
			"art_gallery",
			"atm",
			"bakery",
			"bank",
			"bar",
			"beauty_salon",
			"bicycle_store",
			"book_store",
			"bowling_alley",
			"bus_station",
			"cafe",
			"campground",
			"car_dealer",
			"car_rental",
			"car_repair",
			"car_wash",
			"casino",
			"cemetery",
			"church",
			"city_hall",
			"clothing_store",
			"convenience_store",
			"courthouse",
			"dentist",
			"department_store",
			"doctor",
			"electrician",
			"electronics_store",
			"embassy",
			"fire_station",
			"florist",
			"funeral_home",
			"furniture_store",
			"gas_station",
			"gym",
			"hair_care",
			"hardware_store",
			"hindu_temple",
			"home_goods_store",
			"hospital",
			"insurance_agency",
			"jewelry_store",
			"laundry",
			"lawyer",
			"library",
			"liquor_store",
			"local_government_office",
			"locksmith",
			"lodging",
			"meal_delivery",
			"meal_takeaway",
			"mosque",
			"movie_rental",
			"movie_theater",
			"moving_company",
			"museum",
			"night_club",
			"painter",
			"park",
			"parking",
			"pet_store",
			"pharmacy",
			"physiotherapist",
			"plumber",
			"police",
			"post_office",
			"real_estate_agency",
			"restaurant",
			"roofing_contractor",
			"rv_park",
			"school",
			"shoe_store",
			"shopping_mall",
			"spa",
			"stadium",
			"storage",
			"store",
			"subway_station",
			"supermarket",
			"synagogue",
			"taxi_stand",
			"train_station",
			"transit_station",
			"travel_agency",
			"veterinary_care",
			"zoo"
	*
	* */
	//Home
	/*                    Toast.makeText(getApplicationContext(),"Homeee",Toast.LENGTH_SHORT).show();
					BackgroundBlurPopupWindow mPopupWindow;
					View v = getLayoutInflater().inflate(R.layout.popup_layout, null);
					View rootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_layout, null);
					mPopupWindow = new BackgroundBlurPopupWindow(v , WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, MainActivity.this, true);
					mPopupWindow.setFocusable(true);
					mPopupWindow.setBackgroundDrawable(new ColorDrawable());
					mPopupWindow.setDownScaleFactor(1.2f);
					mPopupWindow.setBlurRadius(10);//配置虚化比例
					mPopupWindow.setDarkAnimStyle(R.anim.dark_fade_in);//动画
					mPopupWindow.setDarkColor(Color.parseColor("#40000000"));//颜色
					mPopupWindow.resetDarkPosition();
					mPopupWindow.darkFillScreen();//全屏
					mPopupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);//弹出PopupWindow
		*/
		//System Camera
	/* DisplayMetrics mPhone = new DisplayMetrics();
                    ContentValues value = new ContentValues();
                    getWindowManager().getDefaultDisplay().getMetrics(mPhone);
                    // new a camera
                    value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    Uri uri = null;
                    try {
                        uri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                value);
                    }catch (Exception e){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                                    Toast.makeText(getApplicationContext(),"permission allowed",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                                }
                            }
                        }

                    }

                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri.getPath());
                    startActivityForResult(intent, CAMERA);
                    */


/*<TableRow
            android:id="@+id/collect_1"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:weightSum="1">
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.33333"
                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.33333"
                android:layout_margin="2dp"
                />
        </TableRow>

        <TableRow
            android:id="@+id/collect_2"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:collapseColumns="3"
            android:weightSum="1">
             <ImageButton
                android:contentDescription="@string/infomation"


                 android:layout_height="wrap_content"
                 android:layout_width="0dp"
                 android:layout_weight="0.33333"
                 android:layout_column="0"
                 android:layout_margin="2dp"
                 />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
        </TableRow>

        <TableRow
            android:id="@+id/collect_3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:weightSum="1">
        <ImageButton
            android:contentDescription="@string/infomation"


            android:layout_height="wrap_content"
            android:layout_width="0dp"
            android:layout_weight="0.33333"
            android:layout_column="0"
            android:layout_margin="2dp"
            />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
        </TableRow>

        <TableRow
            android:id="@+id/collect_4"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:weightSum="1">
            <ImageButton
                android:contentDescription="@string/infomation"


                android:layout_height="wrap_content"
                android:layout_width="0dp"
                android:layout_weight="0.33333"
                android:layout_column="0"
                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
            <ImageButton
                android:contentDescription="@string/infomation"

                android:layout_width="0dp"
                android:layout_weight="0.333333333"

                android:layout_margin="2dp"
                />
        </TableRow>
* */

// fire button
/* <ImageButton
                    android:id="@+id/ib_fire"
                    style="@style/SmallGreyTextView"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:adjustViewBounds="false"
                    android:background="@null"
                    android:contentDescription="@string/this_is_photo_is_used_to_test_and_it_is_cooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooool"
                    android:cropToPadding="false"
                    android:paddingStart="5dp"
                    android:paddingTop="10dp"
                    android:paddingEnd="5dp"
                    android:src="@drawable/fire24" />*/


}
