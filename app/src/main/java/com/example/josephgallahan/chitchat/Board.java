package com.example.josephgallahan.chitchat;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joey on 12/6/2016.
 */

public class Board
{
    private static Board sBoard;
    private List<Post> mBoard;

    //All the important server constants
    private static final String CLIENT = "Gallygoo"; //Don't ask
    private static final String KEY = "champlainrocks1878";
    private static final String URL = "https://www.stepoutnyc.com/chitchat";

    public static Board get()
    {
        if (sBoard == null)
        {
            sBoard = new Board();
        }
        return sBoard;
    }

    public Board()
    {
        mBoard = new ArrayList<>();
    }

    public List<Post> getBoard() { return mBoard; }

    /*
        Performs a GET request for a specific post on the "like"
        portion of the server.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public List<Post> likePost(int position)
    {
        Post like = mBoard.get(position);

        List<Post> items = new ArrayList<>();

        mBoard.clear(); //Just in case

        //Change the url around a bit to what it needs to be
        String likeURL = URL + "/like/" + like.getID();

        try
        {
            String url = Uri.parse(likeURL)
                    .buildUpon()
                    .appendQueryParameter("key", KEY)
                    .build().toString();
            getUrlString(url, false);
            fetchItems(); //Update the posts
        }
        catch (IOException ioe)
        {
            Log.e("IOException", "Failed to fetch items", ioe);
        }

        return items;
    }

    /*
        Performs a GET request for a specific post on the "dislike"
        portion of the server.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public List<Post> dislikePost(int position)
    {
        Post dislike = mBoard.get(position);

        List<Post> items = new ArrayList<>();

        mBoard.clear(); //Just in case

        //Change the url around a bit to what it needs to be
        String dislikeURL = URL + "/dislike/" + dislike.getID();

        try
        {
            String url = Uri.parse(dislikeURL)
                    .buildUpon()
                    .appendQueryParameter("key", KEY)
                    .build().toString();
            getUrlString(url, false);
            fetchItems(); //Update the posts
        }
        catch (IOException ioe)
        {
            Log.e("IOException", "Failed to fetch items", ioe);
        }

        return items;
    }

    /*
        Sends either a GET or POST request to the chitchat server based
        off of the "posting" param. "posting" is true if you are posting
        a new message, false if you are just getting the posts.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public byte[] getUrlBytes(String urlSpec, boolean posting) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if (posting)
        {
            connection.setRequestMethod("POST");
        }
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally
        {
            connection.disconnect();
        }
    }

    /*
        Converts and returns a String from the output of the getUrlString(...)
        function. "posting" is true if you are posting a new message,
        false if you are just getting the posts.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public String getUrlString(String urlSpec, boolean posting) throws IOException {
        return new String(getUrlBytes(urlSpec, posting));
    }

    /*
        Sends a regular GET request to the chitchat server to just
        show all of the posts.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public List<Post> fetchItems()
    {
        List<Post> items = new ArrayList<>();

        mBoard.clear(); //Just in case

        try
        {
            String url = Uri.parse(URL)
                    .buildUpon()
                    .appendQueryParameter("key", KEY)
                    .build().toString();
            String jsonString = getUrlString(url, false);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        }
        catch (IOException ioe)
        {
            Log.e("IOException", "Failed to fetch items", ioe);
        }
        catch (JSONException je)
        {
            Log.e("JSONException", "Failed to parse JSON", je);
        }

        return items;
    }

    /*
        Parses the JSON from the server and sets the data of a new
        Post object based off of the information received.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    private void parseItems(List<Post> items, JSONObject jsonBody)
            throws IOException, JSONException
    {
        JSONArray postJsonArray = jsonBody.getJSONArray("messages");

        for (int i = 0; i < postJsonArray.length(); i++)
        {
            JSONObject postJsonObject = postJsonArray.getJSONObject(i);

            Post item = new Post();
            item.setMessage(postJsonObject.getString("message"));
            item.setDate(postJsonObject.getString("date"));
            item.setLikes(Integer.parseInt(postJsonObject.getString("likes")));
            item.setDislikes(Integer.parseInt(postJsonObject.getString("dislikes")));
            item.setID(postJsonObject.getString("_id")); //we only really need this for liking & disliking

            //Add to both the output list and the board list in case something goes wrong
            items.add(item);
            mBoard.add(item);
        }
    }

    /*
        Sends a POST request to the chitchat server to add a new post
        based off of input from the user.

        -Taken and modified from Big Nerd Ranch Android Programming
         Chapter 23 "FlickrFetchr" program-
    */
    public List<Post> postItem(String message)
    {
        List<Post> items = new ArrayList<>();
        mBoard.clear(); //Just in case

        try
        {
            String url = Uri.parse(URL)
                    .buildUpon()
                    .appendQueryParameter("key", KEY)
                    .appendQueryParameter("client", CLIENT)     //When posting, we need to add the client
                    .appendQueryParameter("message", message)   //Have to put in what the user wrote
                    .build().toString();
            getUrlString(url, true);
            fetchItems(); //Update the posts list
        }
        catch (IOException ioe)
        {
            Log.e("IOException", "Failed to fetch items", ioe);
        }

        return items;
    }
}