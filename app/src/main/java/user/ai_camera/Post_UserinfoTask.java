package  user.ai_camera;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Post_UserinfoTask extends AsyncTask<String, Void, String> {
	private final static  String REQUEST_SUCCESS = "Success";
	private final static  int PROFILE = 0;
	private final static int USER_STATE = 1;
	private final static int NICKNAME = 2;

	private boolean isRequestSuccess = false;
	private int code;
	private Drawable img ;
	private Bitmap imgb;
	public String account;
	private String authentication;
	private String userinfo;
	public double score = 0;

	public Post_UserinfoTask(Drawable img, String account, String authentication, int code){
		this.img = img;
		this.account = account;
		this.authentication = authentication;
		this.code = code;
	}
	public Post_UserinfoTask(Bitmap imgb, String account, String authentication, int code){
		this.imgb = imgb;
		this.account = account;
		this.authentication = authentication;
		this.code = code;
	}
	public Post_UserinfoTask(String userinfo ,String account, String authentication, int code){
		this.userinfo = userinfo;
		this.account = account;
		this.authentication = authentication;
		this.code = code;
	}

	@Override
	protected String doInBackground(String... urls) {
		//Log.d("PTask direction test in background", String.valueOf(direction[0]));

		return POST(urls[0]);
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		//Log.d("PTask End Of Execute", "processing json ");
		Log.d("PROFTask End Of Execute", "processing json result: " + result);
		try {
			JSONObject jObject = new JSONObject(result);
			isRequestSuccess = jObject.getString("results").contains(REQUEST_SUCCESS);
			Log.d("PUITASK test in onPostexcuate","excuate end");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public double resultBack(){
		if(isRequestSuccess) {
			return score;
		}else{
			return -1;
		}
	}
	private String POST(String url) {
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			switch(code){
				case PROFILE:{
					Bitmap bm;
					if(img != null){
						bm = ((BitmapDrawable)img).getBitmap();
					}else{ bm = imgb; }
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					byte[] data = bos.toByteArray();
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(System.currentTimeMillis());
					stringBuilder.append(".jpg");
					httppost.setHeader("authentication",authentication);
					httppost.setEntity(MultipartEntityBuilder.create().addPart("image", new ByteArrayBody(data, stringBuilder.toString()))
							.build()
					);
					break;
				}
				case USER_STATE:{
					httppost.setHeader("authentication",authentication);
					httppost.setEntity(MultipartEntityBuilder.create()
							.addTextBody("user_state",userinfo)
							.build()
					);
					break;
				}
				case NICKNAME:{
					httppost.setHeader("authentication",authentication);
					httppost.setEntity(MultipartEntityBuilder.create()
							.addTextBody("nickname",userinfo)
							.build()
					);
					break;
				}
			}

			InputStream inputStream = httpclient.execute(httppost).getEntity().getContent();
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
			} else {
				result = "Did not work!";
			}
		} catch (Exception e) {
			Log.d("PROFTask InputStream", e.getLocalizedMessage());
		}
		return result;
	}

	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null) {
			result += line;
		}
		inputStream.close();
		return result;
	}

}