package  user.ai_camera;

import android.os.AsyncTask;
import android.util.Log;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.Header;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Login_SignupTask extends AsyncTask<String, Void, String> {

	private String authentication = "empty";
	public String account;
	public String password;
	public String password_again;
	public String response;
	public String error_detail = "No Response";
	public boolean isFinish = false;
	public int response_state = -1 ;

	public Login_SignupTask(String account, String password){
		this.account = account;
		this.password = password;

	}
	public Login_SignupTask(String account, String password,String password_again){
		this.account = account;
		this.password = password;
		this.password_again = password_again;

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
		Log.d("LTask End Of Execute", "processing json result: " + result);
		try {
			JSONObject jObject = new JSONObject(result);
			 response = jObject.getString("results");
			Log.d("LTask Results Response State", jObject.getString("results"));
			if(response.equals("Fault")){
				error_detail = jObject.getString("problem");
				response_state = -1;
			}else if (response.equals("Success")||response.equals("Success login")){
				response_state = 0;
				authentication = jObject.getString("token");
			}


		} catch (JSONException e) {
			e.printStackTrace();
		}
		isFinish = true;
	}

	public boolean resultBack(){
		return isFinish;
	}
	public String[] responseBack(){
		return new String[]{String.valueOf(response_state),error_detail,authentication};
	}

	private String POST(String url) {
		String result = "";
		try {
			HttpClient httpclient;
			HttpPost httppost;
			if(password_again != null){
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(url);
				httppost.setEntity(MultipartEntityBuilder.create()
						.addTextBody("username",account)
						.addTextBody("password", password)
						.addTextBody("checkpassword",password_again)
						.addTextBody("email",String.valueOf(System.currentTimeMillis()))
						.build()
				);
			}else{
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(url);
				httppost.setEntity(MultipartEntityBuilder.create()
						.addTextBody("username",account)
						.addTextBody("password", password)
						.build()
				);
			}
			HttpResponse response = httpclient.execute(httppost);
			InputStream inputStream = response.getEntity().getContent();
			//get all headers
			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				Log.d("LOG IN ","Key : " + header.getName()
						+ " ,Value : " + header.getValue());
			}

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