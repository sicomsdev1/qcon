package com.sicoms.smartplug.test.photo;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.camera.CropImageIntentBuilder;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.util.profile.CircleImageView;
import com.sicoms.smartplug.util.profile.MediaStoreUtils;
import com.sicoms.smartplug.util.progress.SPProgressDialog;

import java.io.File;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PhotoFragment extends Fragment {

    private static int REQUEST_PICTURE = 1;
    private static int REQUEST_CROP_PICTURE = 2;

    private Button mBtnChangeProfile;
    private CircleImageView mIvProfile;

    private SPProgressDialog mProgressDialog;

    public static PhotoFragment newInstance() {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        mProgressDialog = new SPProgressDialog(getActivity());

        mBtnChangeProfile = (Button) view.findViewById(R.id.btn_change_profile);
        mIvProfile = (CircleImageView) view.findViewById(R.id.iv_profile);

        mBtnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.equals(mBtnChangeProfile)) {
                    mProgressDialog.show();
                    startActivityForResult(MediaStoreUtils.getPickImageIntent(getActivity()), REQUEST_PICTURE);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File croppedImageFile = new File(getActivity().getFilesDir(), "test.jpg");

        if ((requestCode == REQUEST_PICTURE) && (resultCode == getActivity().RESULT_OK)) {
            // When the user is done picking a picture, let's start the CropImage Activity,
            // setting the output image file and size to 200x200 pixels square.
            Uri croppedImage = Uri.fromFile(croppedImageFile);

            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, croppedImage);
            cropImage.setOutlineColor(0xFF03A9F4);
            cropImage.setSourceImage(data.getData());

            startActivityForResult(cropImage.getIntent(getActivity()), REQUEST_CROP_PICTURE);
        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == getActivity().RESULT_OK)) {
            // When we are done cropping, display it in the ImageView.
            mIvProfile.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
        }
    }
}
