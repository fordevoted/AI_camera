package  user.ai_camera;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


public class Favorite_postTask extends AsyncTask<String, Void, String> {
	public String keyword;
	public String imageName;
	public String token;
	public boolean isRemove;
	public boolean isFinish;
	private final static String ADD_SUCCESS = "Success add favorite";
	private final static String REMOVE_SUCCESS = "Success remove favorite";
	private final static String IMAGE_DO_NOT_EXIST = "The image doesn't exist.";


	public Favorite_postTask(String token,String keyword, String imageName, boolean isRemove){
		this.keyword = keyword;
		this.imageName = imageName;
		this.isRemove = isRemove;
		this.token = token;
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
		//TODO response
		Log.d("FAVPOSTTask End Of Execute", "processing json result: " + result);
		if(result.equals(ADD_SUCCESS + keyword + "/" + imageName)){

		}else if (result.equals(REMOVE_SUCCESS  + keyword + "/" + imageName )){

		}else if (result.equals(IMAGE_DO_NOT_EXIST)){

		}
		isFinish = true;
	}

	public boolean resultBack(){
		return isFinish;
	}
	public String responseBack(){
		//TODO response
		return "";
	}

	private String POST(String url) {
		String result = "";
		try {
			HttpClient httpclient;
			HttpPost httppost;
			if(isRemove){
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(url);
				httppost.setHeader("authentication",token);
				ContentType contentType = ContentType.create("text/plain", Charset.forName("UTF-8"));
				httppost.setEntity(MultipartEntityBuilder.create()
						.addTextBody("favoriteimage",keyword + "/" + imageName,contentType)
						.build()
				);
				Log.d("FAVPOSTTASK test","keyword"+ keyword );
			}else{
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(url);
				ContentType contentType = ContentType.create("text/plain", Charset.forName("UTF-8"));
				httppost.setHeader("authentication",token);
				httppost.setEntity(MultipartEntityBuilder.create()
						.addTextBody("favoriteimage",keyword + "/" + imageName,contentType)
						.build()
				);
				Log.d("FAVPOSTTASK test","keyword"+ keyword );
			}
			InputStream inputStream = httpclient.execute(httppost).getEntity().getContent();
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
			} else {
				result = "Did not work!";
			}
		} catch (Exception e) {
			Log.d("LTask InputStream", e.getLocalizedMessage());
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