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

public class AskNimaTask extends AsyncTask<String, Void, String> {
    public boolean IsJSONEmpty = false;
    public List<Map<String, String>> totalResult = new ArrayList();

    /* Access modifiers changed, original: protected|varargs */
    public String doInBackground(String... urls) {
        return GET(urls[0]);
    }

    public void onPostExecute(String result) {
        //Log.d("ATask End Of Execute", "processing json ");
        String str = result + "?";
        Log.d("ATask End Of Execute","processing json result :" + str);
        try {
            JSONObject jObject = new JSONObject(result);
            for (int i = 0 ; i < jObject.getJSONArray("results").length() ; i++) {
                Log.d("ATask Results Name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
                Map<String, String> m = new HashMap();
                m.put("name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
                m.put("score", jObject.getJSONArray("results").getJSONObject(i).getString("score"));
                totalResult.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        IsJSONEmpty = this.totalResult.size() == 0;
        Log.d("ATask JSONEmpty Test", String.valueOf(IsJSONEmpty));
    }

    public List resultBack() {
        for (int i = 0 ; i < this.totalResult.size() ; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("print name[i]: ");
            stringBuilder.append((String) ((Map) totalResult.get(i)).get("name"));
            Log.d("ATask Receive Name Test", stringBuilder.toString());
        }
        if (!IsJSONEmpty) {
            return totalResult;
        }
        List<Map> l = new ArrayList();
        Map<String, String> m = new HashMap();
        m.put("name", "Not Found");
        m.put("score", "-1");
        l.add(m);
        return l;
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
			if(inputStream != null) {
				result = convertInputStreamToString(inputStream);
			}
			else{
				result = "Did not work!";
			}
			return  result;
        } catch (Exception e) {
			Log.d("ATask InputStream url is ", url);
			Log.d("ATask InputStream", e.getLocalizedMessage());
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
