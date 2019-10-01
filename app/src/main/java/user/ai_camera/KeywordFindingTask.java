package user.ai_camera;

import android.os.AsyncTask;
import android.os.Handler;
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

public class KeywordFindingTask extends AsyncTask<String, Void, String> {
    public Handler handler = new Handler();
    HttpAsyncTask httpConnect = new HttpAsyncTask();
    public String keyword;
    public double latitude;
    public double longitude;
    public double maxScore = 0.0;

    public KeywordFindingTask(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /* Access modifiers changed, original: protected|varargs */
    public String doInBackground(String... urls) {
        //Log.d("FTask KeywordFinding Test", "enter background" + urls[0]);

        return GET(urls[0]);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(String result) {
        //Log.i("FTask Key End Of Execute", "processing json ");
        Log.i("FTask Key End Of Execute", "processing json result: " + result);
        try {
            JSONObject jObject = new JSONObject(result);
            for (int i = 0; i < jObject.getJSONArray("results").length(); i++) {
                Log.i("results name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
				double temp;
                if(jObject.getJSONArray("results").getJSONObject(i).has("user_ratings_total")){
					temp = ((1.0d - Sigmoid(Math.sqrt(Math.pow((jObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat")
							- this.longitude) * 1000.0d, 2.0d) + Math.pow((jObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng")
							- this.longitude) * 1000.0d, 2.0d)))) * 0.4d) + (Sigmoid((double) jObject.getJSONArray("results").getJSONObject(i).getInt("user_ratings_total")) * 0.6d);
				}else{
					temp = ((1.0d - Sigmoid(Math.sqrt(Math.pow((jObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat")
							- this.longitude) * 1000.0d, 2.0d) + Math.pow((jObject.getJSONArray("results").getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng")
							- this.longitude) * 1000.0d, 2.0d)))) * 1.0d);
				}
                if (temp > this.maxScore) {
                    maxScore = temp;
                    keyword = jObject.getJSONArray("results").getJSONObject(i).getString("name");
                }
            }
            String str = keyword + "?";
            Log.d("FTask KeywordFinding ", str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String resultBack() {
        return keyword;
    }

    private String GET(String url) {
        String result = "";
        try {
           /* HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();*/
			// create HttpClient
			HttpClient httpclient = new DefaultHttpClient();
			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			 InputStream inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if(inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";
			return result;
        } catch (Exception e) {
        	Log.d("FTask URL ",url);
            Log.d("FTask InputStream", e.getLocalizedMessage());
            return result;
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine() )!= null) {
                result+=line;
        }
        inputStream.close();
        return result;
    }

    public double Sigmoid(double x) {
        return 1.0d / (Math.pow(Math.E, -1.0d * x) + 1.0d);
    }
}
