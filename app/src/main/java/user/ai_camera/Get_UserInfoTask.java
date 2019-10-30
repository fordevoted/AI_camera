package user.ai_camera;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Get_UserInfoTask extends AsyncTask<String, Void, String> {
	private final static  String REQUEST_SUCCESS = "Success";
	private boolean isRequestSuccess = false;
	private String authentication;
	private String nickname;
	private String user_state;
	private String profile;
	private List<Map<String, String>> totalResult = new ArrayList();

	public Get_UserInfoTask(String authentication, String account, String default_userState) {
		this.nickname = account;
		this.user_state = default_userState;
		this.authentication = authentication;
	}
	/* Access modifiers changed, original: protected|varargs */
	public String doInBackground(String... urls) {
		Log.d("FAVTASK test","do in background");
		return GET(urls[0]);
	}

	public void onPostExecute(String result) {
		//Log.d("FAVTask End Of Execute", "processing json ");
		String str = result + "?";
		Log.d("FAVTask End Of Execute","processing json result :" + str);
		try {
			 JSONObject jObject = new JSONObject(result);
			 if(!jObject.getString("nickname").equals("None")){
				 nickname = jObject.getString("nickname");
			 }
			 if(!jObject.getString("user_state").equals("None")){
				 user_state = jObject.getString("user_state");
			 }if(!jObject.getString("face_image").equals("None")){
				profile = "http://140.115.51.177:8000/media/" + jObject.getString("face_image");
			}
			 for (int i = 0 ; i < jObject.getJSONArray("favorite").length() ; i++) {
				Log.d("FAVTask Results Name", jObject.getJSONArray("favorite").getString(i));
				Map<String, String> m = new HashMap();
				String info = jObject.getJSONArray("favorite").getString(i);
				String[] value = info.split("/");
				m.put("url", "http://140.115.51.177:8000/media/" + info);
				m.put("position","999");
				m.put("keyword", value[0]);
				m.put("name",value[1]);
				totalResult.add(m);
			}
			isRequestSuccess = jObject.getString("results").contains(REQUEST_SUCCESS);
			Log.d("FAVTask JSONEmpty Test", String.valueOf(isRequestSuccess));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}


	public boolean resultBack(){
		return isRequestSuccess;
	}

	public String NicknameBack(){
		return  nickname;
	}

	public String UserStateBack(){
		return user_state;
	}

	public String ProfileBack(){
		return profile;
	}

	public List collectionBack() {
		if (isRequestSuccess) {
			for (int i = 0 ; i < this.totalResult.size() ; i++) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("print name[i]: ");
				stringBuilder.append((String) ((Map) totalResult.get(i)).get("name"));
				Log.d("FAVTask Receive Name Test", stringBuilder.toString());
			}
			return totalResult;
		}
		return null;
	}



	private String GET(String url) {
		String result = "";
		try {
            /*HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();*/

			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader("authentication",authentication);
			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpget);

			// receive response as inputStream
			InputStream inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if(inputStream != null) {
				result = convertInputStreamToString(inputStream);
			}
			else{
				result = "Did not work!";
			}
			return  result;
		} catch (Exception e) {
			Log.d("FAVTask InputStream url is ", url);
			Log.d("FAVTask InputStream", e.getLocalizedMessage());
			return result;
		}
	}

	private String convertInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while (true) {
			String readLine = bufferedReader.readLine();
			line = readLine;
			if (readLine != null) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(result);
				stringBuilder.append(line);
				result = stringBuilder.toString();
			} else {
				inputStream.close();
				return result;
			}
		}
	}
}
