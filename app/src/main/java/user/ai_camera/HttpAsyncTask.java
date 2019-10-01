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
import java.util.List;

public class HttpAsyncTask extends AsyncTask<String, Void, String> {
    public boolean isJSONEmpty = false;
    public List<String> nearbyResult = new ArrayList();

    /* Access modifiers changed, original: protected|varargs */
    public String doInBackground(String... urls) {
        return GET(urls[0]);
    }

   public void onPostExecute(String result) {
        //Log.d("HTask End Of Execute", "processing json ");
        Log.d("HTask End Of Execute","processing json resulr:" + result);
        try {
            JSONObject jObject = new JSONObject(result);
            for (int i = 0; i < jObject.getJSONArray("results").length(); i++) {
                Log.d("HTask Results Name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
                this.nearbyResult.add(jObject.getJSONArray("results").getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        isJSONEmpty = this.nearbyResult.size() == 0;
        Log.d("HTask JSONEmpty Test", String.valueOf(isJSONEmpty));
    }

    public String[] resultBack() {
        String[] name = (String[]) this.nearbyResult.toArray(new String[this.nearbyResult.size()]);
        for (String append : name) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("print name[i]: ");
            stringBuilder.append(append);
            Log.d("HTask Receive Name Test", stringBuilder.toString());
        }
        if (isJSONEmpty) {
            return new String[]{"Not Found"};
        }
        return name;
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

			// make GET request to the given URL
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

			// receive response as inputStream
			InputStream inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if(inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";
			return  result;
        } catch (Exception e) {
            Log.d("HTask InputStream", e.getLocalizedMessage());
            return result;
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine() )!= null) {
            result += line;
        }
        inputStream.close();
        return result;
    }
}
