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
import java.util.ArrayList;
import java.util.List;

public class PostUploadTask extends AsyncTask<String, Void, String> {
	public List<List<Double>> scoreResult = new ArrayList<>();
	public boolean isJSONEmpty = true;
	public Drawable img ;
	public float[] direction;
	public String keyword;
	public String filename;
	private String authentication;
	public double score;

	public PostUploadTask(Drawable img, float[] direction, String keyword,String authentication){
		this.img = img;
		this.direction = direction;
		this.keyword = keyword;
		this.authentication = authentication;
		for(int i = 0 ; i < direction.length ; i ++){
			direction[i] =  (float)(Math.toDegrees(direction[i]) + 360) % 360;
		}

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
		Log.d("PTask End Of Execute", "processing json result: " + result);
		try {
			JSONObject jObject = new JSONObject(result);

			Log.d("PTask Results Score", jObject.getJSONArray("results").getJSONObject(1).getString("score"));
			ArrayList arrayList = new ArrayList();
			score = jObject.getJSONArray("results").getJSONObject(0).getDouble("score");
			filename = jObject.getJSONArray("results").getJSONObject(1).getString("name");
			arrayList.add(jObject.getJSONArray("results").getJSONObject(1).getDouble("score"));
			arrayList.add(jObject.getJSONArray("results").getJSONObject(1).getDouble("b"));
			arrayList.add(jObject.getJSONArray("results").getJSONObject(1).getDouble("c"));
			arrayList.add(filename);
			scoreResult.add(arrayList);
					//b														//c
				//if(scoreResult.get(scoreResult.size()-1).get(1) == 0 && scoreResult.get(scoreResult.size()-1).get(2) == 0){
				//	break;
				//}

			isJSONEmpty = jObject.getJSONArray("results").getJSONObject(1).length() == 0;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public double resultBack(){
		if(!isJSONEmpty) {
			return score;
		}else{
			return -1;
		}
	}

	public List<List<Double>> suggestionBack(){
		if(!isJSONEmpty) {
			return scoreResult;
		}else{
			return null;
		}
	}
	private String POST(String url) {
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			Bitmap bm = ((BitmapDrawable)img).getBitmap();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			byte[] data = bos.toByteArray();
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(System.currentTimeMillis());
			stringBuilder.append(".jpg");
			httppost.setHeader("authentication",authentication);
			httppost.setEntity(MultipartEntityBuilder.create().addPart("image", new ByteArrayBody(data, stringBuilder.toString()))
					.addTextBody("location",keyword)
					.addTextBody("direction", String.valueOf(direction[0]))
					.build()
			);

			InputStream inputStream = httpclient.execute(httppost).getEntity().getContent();
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
			} else {
				result = "Did not work!";
			}
		} catch (Exception e) {
			Log.d("PTask InputStream", e.getLocalizedMessage());
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
	public void updateData(Drawable i, float[] d, String k){
		img = i;
		direction = d ;
		keyword = k;
	}
}