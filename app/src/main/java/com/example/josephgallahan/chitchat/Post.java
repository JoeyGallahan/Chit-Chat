package com.example.josephgallahan.chitchat;

/**
 * Created by Joey on 12/6/2016.
 */

public class Post
{
    private String mMessage, mDate, mID;
    private int mDislikes, mLikes;
    private int mPosition;

    public Post()
    {
        mMessage = "";
        mDate = "";
        mLikes = 0;
        mDislikes = 0;
        mID = "";
        mPosition = 0;
    }

    //Accessors
    public String getMessage()
    {
        return mMessage;
    }
    public String getDate() { return mDate; }
    public String getID() { return mID; }
    public int getLikes() { return mLikes; }
    public int getDislikes() { return mDislikes; }
    public int getPosition() { return mPosition; }

    //Mutators
    public void setMessage(String message) { mMessage = message; }
    public void setDate(String date) { mDate = date; }
    public void setID(String ID){ mID = ID; }
    public void setLikes(int likes) { mLikes = likes; }
    public void setDislikes(int dislikes) { mDislikes = dislikes; }
    public void setPosition(int position) { mPosition = position; }
}
