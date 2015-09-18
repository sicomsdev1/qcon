package com.sicoms.smartplug.login.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.UserVo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by pc-11-user on 2015-04-02.
 */
public class FacebookServiceManager {

    public static final String TAG = FacebookServiceManager.class.getSimpleName();
    public static final int FACEBOOK_LOGIN_SUCCESS = 1;
    public static final int FACEBOOK_LOGOUT_SUCCESS = 2;

    private static final String PERMISSION = "publish_actions";

    private Activity mActivity;
    private ProfilePictureView mProfilePictureView;
    public CallbackManager mCallbackManager;
    private PendingAction mPendingAction = PendingAction.NONE;
    private ProfileTracker mProfileTracker;
    private ShareDialog shareDialog;
    private boolean mCanPresentShareDialog;
    private boolean mCanPresentShareDialogWithPhotos;

    private FacebookLoginCallbacks mCallbacks;

    public interface FacebookLoginCallbacks {
        void onFacebookLoginResultStatus(int result, UserVo userVo);
    }

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = mActivity.getString(R.string.facebook_error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = mActivity.getString(R.string.facebook_success);
                String id = result.getPostId();
                String alertMessage = mActivity.getString(R.string.facebook_successfully_posted_post, id);
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    };

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    public FacebookServiceManager(Activity activity, FacebookLoginCallbacks callbacks){
        mActivity = activity;
        mCallbacks = callbacks;

        initialize();
    }

    public void setProfilePictureView(ProfilePictureView profilePictureView){
        mProfilePictureView = profilePictureView;
    }

    private void initialize(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager manager = LoginManager.getInstance();
        manager.logInWithReadPermissions(mActivity, Arrays.asList("public_profile, email"));
        manager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handlePendingAction();
                updateUI();
                final AccessToken accessToken = loginResult.getAccessToken();
                GraphRequestAsyncTask request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        String email = jsonObject.optString("email");
                        String name = jsonObject.optString("name");
                        UserVo userVo = new UserVo(email, "facebook", name, 1, "", true);
                        LoginService.saveLastLoginUser(mActivity, userVo);
                        mCallbacks.onFacebookLoginResultStatus(FACEBOOK_LOGIN_SUCCESS, userVo);
                    }
                }).executeAsync();
            }

            @Override
            public void onCancel() {
                if (mPendingAction != PendingAction.NONE) {
                    showAlert();
                    mPendingAction = PendingAction.NONE;
                }
                updateUI();
            }

            @Override
            public void onError(FacebookException exception) {
                if (mPendingAction != PendingAction.NONE
                        && exception instanceof FacebookAuthorizationException) {
                    showAlert();
                    mPendingAction = PendingAction.NONE;
                }
                updateUI();
            }

            private void showAlert() {
                new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.facebook_cancelled)
                        .setMessage(R.string.facebook_permission_not_granted)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        });
        shareDialog = new ShareDialog(mActivity);
        shareDialog.registerCallback(
                mCallbackManager,
                shareCallback);

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
                // It's possible that we were waiting for Profile to be populated in order to
                // post a status update.
                handlePendingAction();
            }
        };
        // Can we present the share dialog for regular links?
        mCanPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);

        // Can we present the share dialog for photos?
        mCanPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);
    }

    public void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            // Login
            //mCallbacks.onFacebookLoginResultStatus(FACEBOOK_LOGIN_SUCCESS, profile.getId());
            //mProfilePictureView.setProfileId(profile.getId());
        } else {
            // Logout
            //mCallbacks.onFacebookLoginResultStatus(FACEBOOK_LOGOUT_SUCCESS, "");
            //mProfilePictureView.setProfileId(null);
        }
    }

    private void postPhoto() {
        Bitmap image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.gudnam);
        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(image).build();
        ArrayList<SharePhoto> photos = new ArrayList<SharePhoto>();
        photos.add(sharePhoto);

        SharePhotoContent sharePhotoContent =
                new SharePhotoContent.Builder().setPhotos(photos).build();
        if (mCanPresentShareDialogWithPhotos) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPublishPermission()) {
            ShareApi.share(sharePhotoContent, shareCallback);
        } else {
            mPendingAction = PendingAction.POST_PHOTO;
        }
    }

    public void uploadImage() {
        performPublish(PendingAction.POST_PHOTO, mCanPresentShareDialogWithPhotos);
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action, boolean allowNoToken) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            mPendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else {
                // We need to get new permissions, then complete the action when we get called back.
                LoginManager.getInstance().logInWithPublishPermissions(
                        mActivity,
                        Arrays.asList(PERMISSION));
                return;
            }
        }

        if (allowNoToken) {
            mPendingAction = action;
            handlePendingAction();
        }
    }

    private void handlePendingAction() {
        PendingAction previouslyPendingAction = mPendingAction;
        // These actions may re-set mPendingAction if they are still pending, but we assume they
        // will succeed.
        mPendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case NONE:
                break;
            case POST_PHOTO:
                postPhoto();
                break;
        }
    }

    public void onSetCallbackManager(int requestCode, int resultCode, Intent data){
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    
    public void setPendingAction(String name){
        mPendingAction = PendingAction.valueOf(name);
    }

    public String getPendingActionName(){
        return mPendingAction.name();
    }
    
    public void setProfileTrackerStop(){
        mProfileTracker.stopTracking();
    }

    public void setOnFacebookLoginResultCallbacks(final FacebookLoginCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
