package com.example.josephgallahan.chitchat;

/**
 * Created by joseph.gallahan on 12/6/2016.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;

public class PostFragment extends android.support.v4.app.Fragment
{
    private RecyclerView mRecyclerView;
    private PostAdapter mAdapter;

    private TextView mDateTextView, mMessageTextView,
                     mLikesTextView, mDislikesTextView;

    private Button mRefreshButton, mPostButton, mLikeButton, mDislikeButton;

    private EditText mMessageBox; //Where the user types their message

    private List<Post> mPosts; //All the posts on chitchat

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //Get all the posts right when we start up
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.recyclerview, container, false);

        //Set up the recycler view and its manager
        mRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Set up the EditText box for the user to type their message
        mMessageBox = (EditText) getActivity().findViewById(R.id.message_box);
        mMessageBox.setSelected(false);

        //Set up the refresh button
        mRefreshButton = (Button) getActivity().findViewById(R.id.refresh_button);
        mRefreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Get all the posts and update the page
                new FetchItemsTask().execute();
                updateUI();
            }
        });

        //Set up the post button
        mPostButton = (Button) getActivity().findViewById(R.id.post_button);
        mPostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Send a new post based off of the text in the message box and update the page
                new PostItemTask(mMessageBox.getText().toString()).execute();
                updateUI();
                mMessageBox.setText(""); //reset the text to nothing
            }
        });

        updateUI();

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateUI();
    }

    //Basically just sets up the adapter
    private void updateUI()
    {
        if (mAdapter == null)
        {
            mPosts = Board.get().getBoard();
            mAdapter = new PostAdapter(mPosts);
            mRecyclerView.setAdapter(mAdapter);
        }
        else
        {
            mAdapter.notifyDataSetChanged();
        }
    }
    private class PostHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener
    {
        private Post mPost;

        public PostHolder(View itemView)
        {
            super(itemView);

            //Set up the views
            mDateTextView = (TextView) itemView.findViewById(R.id.date_text);
            mMessageTextView = (TextView) itemView.findViewById(R.id.message_text);
            mLikesTextView = (TextView) itemView.findViewById(R.id.num_likes);
            mDislikesTextView = (TextView) itemView.findViewById(R.id.num_dislikes);
            mLikeButton = (Button) itemView.findViewById(R.id.like_button);
            mDislikeButton = (Button) itemView.findViewById(R.id.dislike_button);

            //Make the on click listeners use the viewholder's click listener
            mLikeButton.setOnClickListener(this);
            mDislikeButton.setOnClickListener(this);
        }

        public void bindPost(Post post)
        {
            mPost = post;

            mDateTextView.setText(mPost.getDate());
            mMessageTextView.setText(mPost.getMessage());

            //Have to convert the ints to strings
            mLikesTextView.setText(Integer.toString(mPost.getLikes()));
            mDislikesTextView.setText(Integer.toString(mPost.getDislikes()));
        }

        @Override
        public void onClick(View v)
        {
            //Checks to see if the user clicked on a particular button in the view
            if (v.getId() == mLikeButton.getId())
            {
                //Like the post and update the page
                new ActionItemTask("LIKE", mPost.getPosition()).execute();
                updateUI();
            }
            else if (v.getId() == mDislikeButton.getId())
            {
                //Dislike the post and update the page
                new ActionItemTask("DISLIKE", mPost.getPosition()).execute();
                updateUI();
            }
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder>
    {
        List<Post> mItems;
        public PostAdapter(List<Post> posts)
        {
            mItems = posts;
            setHasStableIds(false);
        }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.post_fragment, parent, false);
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(PostHolder holder, int position)
        {
            Post post = mItems.get(position);
            post.setPosition(position);

            //This next line fixed so many problems oh my god
            //Fixed duplicates, changing values when scrolling, etc, etc
            holder.setIsRecyclable(false);

            holder.bindPost(post);
        }

        @Override
        public int getItemCount() {
            return Board.get().getBoard().size();
        }
    }

    //Used to show all of the chitchat posts
    private class FetchItemsTask extends AsyncTask<Void,Void,List<Post>>
    {
        @Override
        protected List<Post> doInBackground(Void... params)
        {
            return Board.get().fetchItems();
        }

        @Override
        protected void onPostExecute(List<Post> items)
        {
            mPosts = items;
            updateUI();
        }
    }

    //Used to post to the server
    private class PostItemTask extends AsyncTask<Void,Void,List<Post>>
    {
        String mMessage; //The message the user typed in

        public PostItemTask(String message)
        {
            mMessage = message;
        }

        @Override
        protected List<Post> doInBackground(Void... params) {
            return Board.get().postItem(mMessage);
        }

        @Override
        protected void onPostExecute(List<Post> items)
        {
            mPosts = items;
            updateUI();
        }
    }

    //Used to like & dislike posts
    private class ActionItemTask extends AsyncTask<Void,Void,List<Post>>
    {
        String mAction; //either LIKE or DISLIKE
        int mPosition;  //index of the post they liked or disliked

        public ActionItemTask(String action, int position)
        {
            mAction = action;
            mPosition = position;
        }

        @Override
        protected List<Post> doInBackground(Void... params)
        {
            //If they hit the like button, send the request to like the post
            //If they hit the dislike button, send the request to dislike the post
            //If mMessage was set to anything else, then something went wrong so just refresh the page
            switch (mAction)
            {
                case "LIKE": return Board.get().likePost(mPosition);
                case "DISLIKE": return Board.get().dislikePost(mPosition);
                default: return Board.get().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(List<Post> items)
        {
            mPosts = items;
            updateUI();
        }
    }
}
