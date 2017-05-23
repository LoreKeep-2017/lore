package com.example.ilya.lorekeep.topic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ilya.lorekeep.R;
import com.example.ilya.lorekeep.auth.LoginFragment;
import com.example.ilya.lorekeep.config.HelperFactory;
import com.example.ilya.lorekeep.config.NetworkThread;
import com.example.ilya.lorekeep.config.RetrofitFactory;
import com.example.ilya.lorekeep.topic.dao.Topic;
import com.example.ilya.lorekeep.topic.draw_and_colorpicker.ColorPicker;
import com.example.ilya.lorekeep.topic.draw_and_colorpicker.DrawActivity;
import com.example.ilya.lorekeep.topic.draw_and_colorpicker.DrawPicture;
import com.example.ilya.lorekeep.topic.image_flickr.CapturePhotoUtils;
import com.example.ilya.lorekeep.topic.image_flickr.FlickrFetchr;
import com.example.ilya.lorekeep.topic.image_flickr.FlickrItem;
import com.example.ilya.lorekeep.topic.image_flickr.SearchFragment;
import com.example.ilya.lorekeep.topic.image_flickr.ThumbnailDownloader;
import com.example.ilya.lorekeep.topic.topicApi.TopicAnswerDelModel;
import com.example.ilya.lorekeep.topic.topicApi.TopicApi;
import com.example.ilya.lorekeep.topic.topicApi.models.TopicAnswer;
import com.example.ilya.lorekeep.topic.topicApi.models.TopicModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class NewTopicFragment extends Fragment {

    static final String REQUEST_IMAGE_FRAGMENT = "SearchFragment";

    static final int PICK_COLOR_REQUEST = 1;
    static final int DRAW_PICTURE_REQUEST = 2;
    static final int REQUEST_IMAGE = 3;

    public static NewTopicFragment newInstance() {
        return new NewTopicFragment();
    }

    private Button mImageTopicButton;
    private RecyclerView mPhotoRecyclerView;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private List<FlickrItem> mItems = new ArrayList<>();
    private EditText mTopicTitle;
    private TextView mCreateTopic;

    private ImageView mSearchImage;
    private ImageView mRemoveTopic;
    private ImageView mColorPicker;
    private ImageView mDraw;

    private Integer topicId;
    private Boolean update;
    private Integer color;
    private Topic mTopic = new Topic();
    private Topic editTopic;
    private DrawPicture mDrawPicture;
    private Boolean isAllreadPressed = false;
    private Date creationUpdateDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        HelperFactory.setHelper(getActivity().getApplicationContext());

        Handler responseHandler = new Handler();

        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder,
                                                      Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(),
                                bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_topic, container,
                false);

        topicId = getActivity().getIntent().getIntExtra("topic_id", -1);
        update = getActivity().getIntent().getBooleanExtra("topic_update", false);

        mTopicTitle = (EditText) v.findViewById(R.id.set_topic_title);
        mImageTopicButton = (Button) v.findViewById(R.id.set_topic_image);

        if (topicId != -1) {
            try {
                editTopic = HelperFactory.getHelper().getTopicDAO().getTopic(topicId);
                Log.d("on Edit", "edit topic title " + editTopic.getTopicTitle());
                mTopicTitle.setText(editTopic.getTopicTitle(), TextView.BufferType.EDITABLE);
                creationUpdateDate = editTopic.getTopicCreationDate();

                if (editTopic.getTopicImage() != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getActivity()
                                .getContentResolver().openInputStream(Uri.parse(editTopic.getTopicImage())));
                        BitmapDrawable bdrawable = new BitmapDrawable(getContext().getResources(), bitmap);
                        mImageTopicButton.setBackground(bdrawable);
                    } catch (FileNotFoundException e) {
                        // TODO : write catch
                    }
                } else if (editTopic.getTopicColor() < 0){
                    mImageTopicButton.setBackgroundColor(editTopic.getTopicColor());
                }
            } catch (SQLException e) {
                Log.d(TAG, "onClick: " + e.toString());
            }
        }

        mImageTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        mTopicTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mTopic.setTopicTitle(c.toString());
            }

            @Override
            public void afterTextChanged(Editable c) {

            }
        });

        Toolbar bottomToolbar = (Toolbar) v.findViewById(R.id.toolbar_topic_bottom);

        mRemoveTopic = (ImageView) bottomToolbar.findViewById(R.id.button_remove_topic);
        if(!update){
            mRemoveTopic.setVisibility(View.GONE);
        }
        mRemoveTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    SharedPreferences sharedPreferences = getContext()
                            .getSharedPreferences(getString(R.string.sharedTitle), Context.MODE_PRIVATE);
                    String sessionId = sharedPreferences.getString(getString(R.string.sessionId), "");

                    HelperFactory.getHelper().getTopicDAO().updateDeleted(topicId);
                    final TopicApi topic = RetrofitFactory.retrofitLore().create(TopicApi.class);
                    int serverTopicId = HelperFactory.getHelper().getTopicDAO().getServerTopicId(topicId);
                    final Call<TopicAnswerDelModel> call = topic.deleteTopic(serverTopicId, "sessionId=" + sessionId);
                    NetworkThread.getInstance().execute(call, new NetworkThread.ExecuteCallback<TopicAnswerDelModel>() {
                        @Override
                        public void onSuccess(TopicAnswerDelModel result, Response<TopicAnswerDelModel> response) {
                            try {
//                                Log.d("on Delete", "message: " + response.body());
                                HelperFactory.getHelper().getTopicDAO().deleteTopicById(topicId);
                            } catch (SQLException ex) {
//                                 TODO write exception
                            }
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception ex) {
                            Log.d("onError", "Error " + ex.toString());
                            getActivity().finish();
                        }
                    });
                } catch (SQLException ex){
                // TODO write exception
                }


//                try {
//                    HelperFactory.getHelper().getTopicDAO().deleteTopicById(topicId);
//                } catch (SQLException e) {
//                    Log.d(TAG, "onClick: " + e.toString());
//                }
//                getActivity().finish();
            }
        });

        mDraw = (ImageView) bottomToolbar.findViewById(R.id.button_draw);
        mDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), DrawActivity.class);
                startActivityForResult(intent, DRAW_PICTURE_REQUEST);
            }
        });

        mColorPicker = (ImageView) bottomToolbar.findViewById(R.id.button_color_picker);
        mColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ColorPicker.class);
                startActivityForResult(intent, PICK_COLOR_REQUEST);
            }
        });

        mSearchImage = (ImageView) bottomToolbar.findViewById(R.id.flickr_search_image_button);
        mSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllreadPressed) {
                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.loading_text_fragment_search);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
                    isAllreadPressed = false;
                } else {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    Fragment searchFragment = SearchFragment.newInstance();
                    searchFragment.setTargetFragment(NewTopicFragment.this, REQUEST_IMAGE);
                    transaction.add(R.id.loading_text_fragment_search, searchFragment, REQUEST_IMAGE_FRAGMENT);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    isAllreadPressed = true;
                }
            }
        });


        mPhotoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager
                (getActivity(), 3));

        setupAdapter();

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_topic_top);
        mCreateTopic = (TextView) toolbar.findViewById(R.id.toolbar_topic_create);


        if(update){
            mCreateTopic.setText("Update");
        }

        mCreateTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.sharedTitle),
                        Context.MODE_PRIVATE);
                int userId = sharedPref.getInt(getString(R.string.userId), 0);
                Log.d("New Topic Fragment", "UserId " + userId);

                // TODO write some checks

                if (!update) {
                    try {

                        mTopic.setTopicUserId(userId);
                        mTopic.setTopicChanged(false);
                        mTopic.setTopicCreated(true);
                        mTopic.setTopicDeleted(false);
                        mTopic.setTopicCreationDate(new Date());
                        try {
                            HelperFactory.getHelper().getTopicDAO().setTopic(mTopic);
                        }catch(SQLException e){
                            Log.d("Error", e.toString());
                        }
                        int TopicId = mTopic.getTopicId();
                        Log.d("NewTopicFragment", "TopicId: " + TopicId);

                        TopicModel newTopic = new TopicModel();
                        newTopic.setUserId(userId);
                        newTopic.setTitle(mTopic.getTopicTitle());
                        newTopic.setCreationDate(new Date());
                        newTopic.setColor(mTopic.getTopicColor());
                        newTopic.setImage(mTopic.getTopicImage());

                        //                    try {
//                        Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver()
//                                .openInputStream(Uri.parse(mTopic.getTopicImage())));
//                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
//                        byte[] image = bao.toByteArray();
////                        newTopic.setImageBitmap(image);
//                        String ba1 = Base64.encodeToString(image, Base64.DEFAULT);
//                    } catch(FileNotFoundException ex){
//                        // TODO write exception
//                    }

                        SharedPreferences sharedPreferences = getContext()
                                .getSharedPreferences(getString(R.string.sharedTitle), Context.MODE_PRIVATE);
                        String sessionId = sharedPreferences.getString(getString(R.string.sessionId), "");

                        final TopicApi topic = RetrofitFactory.retrofitLore().create(TopicApi.class);
                        final Call<TopicAnswer> call = topic.createNewTopic(newTopic, "sessionId=" + sessionId);
                        NetworkThread.getInstance().execute(call, new NetworkThread.ExecuteCallback<TopicAnswer>() {
                            @Override
                            public void onSuccess(TopicAnswer result, Response<TopicAnswer> response) {
                                try {
                                    HelperFactory.getHelper().getTopicDAO().updateServerTopicId(mTopic.getTopicId(), result.getMessage());
                                    HelperFactory.getHelper().getTopicDAO().updateCreated(mTopic.getTopicId());
                                } catch (SQLException ex) {
//                                 TODO write exception
                                }
                                getActivity().finish();
                            }

                            @Override
                            public void onError(Exception ex) {
                                Log.d("onError", "Error " + ex.toString());
                                getActivity().finish();
                            }
                        });

                    } catch (Exception e) {
                        Log.d(TAG, "onClick: " + e.toString());
                    }
                }else{
                    try{
/*
                        Topic upTopic = HelperFactory.getHelper().getTopicDAO().getTopic(topicId);

                        if(mTopic.getTopicTitle() != null)
                            upTopic.setTopicTitle(mTopic.getTopicTitle());

                        upTopic.setTopicColor(mTopic.getTopicColor());
*/
                        Topic upTopic = new Topic();
                        upTopic.setTopicId(topicId);
                        upTopic.setTopicTitle(mTopicTitle.getText().toString());
                        upTopic.setTopicCreationDate(creationUpdateDate);
                        if(mTopic.getTopicColor() > 0) {
                            upTopic.setTopicColor(mTopic.getTopicColor());
                        } else {
                            upTopic.setTopicColor(editTopic.getTopicColor());
                        }
                        if(mTopic.getTopicImage() != null) {
                            upTopic.setTopicImage(mTopic.getTopicImage());
                        }else{
                            upTopic.setTopicImage(editTopic.getTopicImage());
                        }
                        upTopic.setTopicChanged(true);
                        upTopic.setTopicCreated(false);
                        upTopic.setTopicDeleted(false);

                        try {
                            HelperFactory.getHelper().getTopicDAO().updateTopic(upTopic);
                        }catch(SQLException e){
                            Log.d("Error", e.toString());
                        }
                        int TopicId = mTopic.getTopicId();
                        Log.d("NewTopicFragment", "TopicId: " + TopicId);

                        TopicModel newTopic = new TopicModel();
                        newTopic.setUserId(userId);
                        newTopic.setTopicId(upTopic.getTopicId());
                        newTopic.setServerTopicId(upTopic.getServerTopicId());
                        newTopic.setTitle(upTopic.getTopicTitle());
                        newTopic.setCreationDate(upTopic.getTopicCreationDate());
                        newTopic.setColor(upTopic.getTopicColor());
                        newTopic.setImage(upTopic.getTopicImage());

                        SharedPreferences sharedPreferences = getContext()
                                .getSharedPreferences(getString(R.string.sharedTitle), Context.MODE_PRIVATE);
                        String sessionId = sharedPreferences.getString(getString(R.string.sessionId), "");


                        final TopicApi topic = RetrofitFactory.retrofitLore().create(TopicApi.class);
                        final Call<TopicAnswer> call = topic.updateTopic(newTopic, "sessionId=" + sessionId);
                        NetworkThread.getInstance().execute(call, new NetworkThread.ExecuteCallback<TopicAnswer>() {
                            @Override
                            public void onSuccess(TopicAnswer result, Response<TopicAnswer> response) {
                                try {
                                    HelperFactory.getHelper().getTopicDAO().updateServerTopicId(mTopic.getTopicId(), result.getMessage());
                                    HelperFactory.getHelper().getTopicDAO().updateCreated(mTopic.getTopicId());
                                } catch (SQLException ex) {
    //                                 TODO write exception
                                }
                                getActivity().finish();
                            }

                            @Override
                            public void onError(Exception ex) {
                                Log.d("onError", "Error " + ex.toString());
                                getActivity().finish();
                            }
                        });

                    } catch (Exception e) {
                        Log.d(TAG, "onClick: " + e.toString());
                    }

                }
            }
        });

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        return v;
    }


    public void updateItems(String query) {
        new FetchItemsTask(query).execute();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_COLOR_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    return;
                }
                color = ColorPicker.getColor(data);
                mImageTopicButton.setBackgroundColor(color);
                mTopic.setTopicColor(color);

            }
        }

        if (requestCode == DRAW_PICTURE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    return;
                }
                Bitmap bitmap;
                String targetUri = DrawActivity.getImage(data);
                Log.d("ondraw", "picture" + targetUri);
                //TODO rewrite, doesn't work
                try {
                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver()
                            .openInputStream(Uri.parse(targetUri)));
                    BitmapDrawable bdrawable = new BitmapDrawable(getContext().getResources(), bitmap);
                    mImageTopicButton.setBackground(bdrawable);
                } catch (FileNotFoundException e) {
                    //TODO write catch block
                }
                Log.d("ondraw", "picture" + targetUri);
                mTopic.setTopicImage(targetUri);
            }
        }

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                Log.d("New Topic Fragment", targetUri.toString());
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(targetUri));
                    BitmapDrawable bdrawable = new BitmapDrawable(getContext().getResources(), bitmap);
                    mImageTopicButton.setBackground(bdrawable);
                } catch (FileNotFoundException e) {
                }
                mImageTopicButton.setText("");
                mTopic.setTopicImage(targetUri.toString());
            }
        }

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                String imageText = (String) data.getSerializableExtra(SearchFragment.EXTRA_REQUEST);
                updateItems(imageText);
            }
        }
    }


    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private Drawable mDrawable;

        public PhotoHolder(View ImageView) {
            super(ImageView);
            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
            mItemImageView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
            mDrawable = drawable;
        }

        @Override
        public void onClick(View v) {

            Bitmap bitmap = ((BitmapDrawable) mDrawable).getBitmap();
            BitmapDrawable bdrawable = new BitmapDrawable(getContext().getResources(), bitmap);
            mImageTopicButton.setBackground(bdrawable);
            mImageTopicButton.setText("");

            String targetUri = CapturePhotoUtils.insertImage(getActivity().getContentResolver(), bitmap, "hell", "hell");
            mTopic.setTopicImage(targetUri);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<FlickrItem> mGalleryItems;

        public PhotoAdapter(List<FlickrItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup,
                                              int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.flickr_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            FlickrItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable
                    (R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder,
                    galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<FlickrItem>> {

        private String mQuery = null;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<FlickrItem> doInBackground(Void... params) {

            if (mQuery == null) {
                return null;
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<FlickrItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }
}
