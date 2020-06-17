package com.example.newsapp.Utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.newsapp.MainActivity;
import com.example.newsapp.Model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class QueryUtils {

    public static final String LOG_TAG = MainActivity.class.getName();

    private QueryUtils() {
    }

    public static ArrayList<News> fetchNewsData(String requestUrl)
    {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        ArrayList<News> news = extractNews(jsonResponse);

        return news;
    }


    //Parsing the Json Response is done here
    private static ArrayList<News> extractNews(String jsonResponse)
    {
        ArrayList<News> news = new ArrayList<>();
        if(TextUtils.isEmpty(jsonResponse))
            return null;
        //Extracting Json
        try {
            JSONObject allInfo = new JSONObject(jsonResponse);
            JSONObject root = allInfo.getJSONObject("response");
            JSONArray resultsArray = root.getJSONArray("results");
            for(int i = 0;i<resultsArray.length();i++)
            {
                JSONObject innerObject = resultsArray.getJSONObject(i);
                String title = innerObject.getString("webTitle");
                String url = innerObject.getString("webUrl");
                String date = innerObject.getString("webPublicationDate");
                String section = innerObject.getString("sectionName");
                String author = "";
                JSONArray tagsArray = innerObject.getJSONArray("tags");

                if (tagsArray.length() == 0) {
                    author = null;
                } else {
                    for (int j = 0; j < tagsArray.length(); j++) {
                        JSONObject innerTagsObject = tagsArray.getJSONObject(j);
                        author += innerTagsObject.getString("webTitle") + ". ";
                    }
                }

                news.add(new News(title,author,url,date,section));
            }

        }catch (JSONException e){
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);

        }
        // Return the list of news
        return news;
    }

    private static URL createUrl(String s)
    {
        URL url = null;
        try {
            url = new URL(s);
        }catch (Exception e){
            Log.e(LOG_TAG,"Error with creating Url:"+e);
            return null;
        }
        return  url;
    }

    private static String makeHttpRequest(URL url) throws IOException
    {
        String jsonResponse = "";
        if(url == null)
            return jsonResponse;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            if(urlConnection.getResponseCode() == 200)
            {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else
            {
                Log.e(LOG_TAG,"Error Response Code:"+urlConnection.getResponseCode());
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
