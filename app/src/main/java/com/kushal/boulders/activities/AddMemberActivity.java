package com.kushal.boulders.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;

import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static com.kushal.boulders.misc.Constants.LOG_TAG;

public class AddMemberActivity extends AppCompatActivity {

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    private EditText mEditMemberFirstName;
    private EditText mEditMemberLastName;

    private TextInputLayout mEditMemberCycleStartDateHolder;
    private TextInputLayout mEditMemberCycleEndDateHolder;
    private EditText mEditMemberCycleStartDate;
    private EditText mEditMemberCycleEndDate;

    private EditText mEditMemberPhone;
    private EditText mEditMemberEmail;
    private EditText mEditMemberAddress;

    private TextView mImageBase64String;
    private ImageView mMemberAvatar;
    private FloatingActionButton buttonCreateMember;
    private FloatingActionButton buttonSelectMemberImage;
    private FloatingActionButton fabRotateImage;

    private Snackbar snackBar;
    private View snackview;

    private int PICK_IMAGE_REQUEST = 1;

    //Bitmap to get image from gallery
    private Bitmap bitmap;
    private String imageBase64String =null;
    private static int rotationValue = 1;

    //Uri to store the image uri
    private Uri filePath;

    public static Intent createIntent(Activity activity) {
        Intent intent = new Intent(activity, AddMemberActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        snackview = findViewById(android.R.id.content);
        mMemberAvatar = findViewById(R.id.iv_amf_memberAvatar);
        //mImageBase64String = findViewById(R.id.tv_memberImage);
        mEditMemberFirstName = findViewById(R.id.edt_amf_memberFirstName);
        mEditMemberLastName = findViewById(R.id.edt_amf_memberLastName);
        mEditMemberPhone = findViewById(R.id.edt_amf_memberPhone);
        mEditMemberEmail = findViewById(R.id.edt_amf_memberEmail);
        mEditMemberAddress = findViewById(R.id.edt_amf_memberAddress);
        mEditMemberCycleStartDate = findViewById(R.id.edt_amf_memberCycleStartDate);
        mEditMemberCycleStartDateHolder = findViewById(R.id.holder_amf_memberCycleStartDate);
        mEditMemberCycleEndDate = findViewById(R.id.edt_amf_memberCycleEndDate);
        mEditMemberCycleEndDateHolder = findViewById(R.id.holder_amf_memberCycleEndDate);

        mEditMemberCycleStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "yyyy-MM-dd"; // your format
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                        mEditMemberCycleStartDate.setText(sdf.format(myCalendar.getTime()));
                        System.out.println(" ====================================== selected Date : " + sdf.format(myCalendar.getTime()) );
                    }

                };
                new DatePickerDialog(AddMemberActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }

        });

        mEditMemberCycleEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "yyyy-MM-dd"; // your format
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                        mEditMemberCycleEndDate.setText(sdf.format(myCalendar.getTime()));
                        System.out.println(" ====================================== selected Date : " + sdf.format(myCalendar.getTime()) );
                    }

                };
                new DatePickerDialog(AddMemberActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }

        });

        buttonCreateMember = findViewById(R.id.fab_addMember);
        buttonCreateMember.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick (View view) {
                createNewMember();

            }
        });

        buttonSelectMemberImage = findViewById(R.id.fab_amf_addImage);
        buttonSelectMemberImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick (View view) {
                showFileChooser();

            }
        });

        fabRotateImage = findViewById(R.id.fab_rotateImage);
        fabRotateImage.setVisibility(View.INVISIBLE);
        fabRotateImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                rotateImage();

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createNewMember() {

        System.out.println(" ====================================== selected postbody : " + getPostBody());

        if(validateForm()) {
            mHttpClient.createMember(getPostBody(), new HttpClient.MemberCreationCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("New member created successfully!!")) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "Member added successfully..";
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                                clearForm();
                            }
                        });
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();

                        Log.i(LOG_TAG, "Member created Successful");

                    } else {

                        makeSnackbar(getResources().getString(R.string.message_member_creation_failed), Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "Member creation failed..");
                    }
                }
            });
        }
    }

    public  void clearForm() {
        mEditMemberFirstName.setText("");
        mEditMemberLastName.setText("");
        mEditMemberPhone.setText("");
        mEditMemberEmail.setText("");
        mEditMemberCycleStartDate.setText("");
        mEditMemberCycleEndDate.setText("");
        mEditMemberAddress.setText("");
    }

    public  boolean validateForm() {

        boolean firstname = mEditMemberFirstName.getText().toString().isEmpty();
        boolean lastname = mEditMemberLastName.getText().toString().isEmpty();
        boolean phone = mEditMemberPhone.getText().toString().isEmpty();
        boolean email = mEditMemberEmail.getText().toString().isEmpty();
        boolean cyclestartdate = mEditMemberCycleStartDate.getText().toString().isEmpty();
        boolean cycleenddate = mEditMemberCycleEndDate.getText().toString().isEmpty();
        boolean address = mEditMemberAddress.getText().toString().isEmpty();

        DateTime cycleStartDate = new DateTime( mEditMemberCycleStartDate.getText().toString() );
        DateTime cycleEndDate = new DateTime(  mEditMemberCycleEndDate.getText().toString());

        boolean validateDates = cycleStartDate.isBefore(cycleEndDate);

        String emailId = mEditMemberEmail.getText().toString();

        boolean validEmail = EmailValidator.getInstance().isValid(emailId);

        if (((firstname || lastname || phone || email || cyclestartdate || cycleenddate|| address) == false) && validEmail && validateDates) {

            return true;

        }else if (validEmail == false){
            makeSnackbar( " Please enter a valid email address ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Wrong email format");
            return false;

        } else if (validateDates == false){
            makeSnackbar( " Cycle End Date can't start before Cycle Start Date ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Cycle End Date is less Than Cycle Start Date");
            return false;

        } else {
            makeSnackbar( " Please fill all details ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Fields are empty");
            return false;

        }
    }

    public JSONObject getPostBody(){

        final String firstname = mEditMemberFirstName.getText().toString();
        final String lastname = mEditMemberLastName.getText().toString();
        final String phone = mEditMemberPhone.getText().toString();
        final String email = mEditMemberEmail.getText().toString();
        final String cycle_startdate = mEditMemberCycleStartDate.getText().toString();
        final String cycle_enddate = mEditMemberCycleEndDate.getText().toString();
        final String address = mEditMemberAddress.getText().toString();

        final String parent = mSharedPrefStorage.getUserId();
        final String linked_to = mSharedPrefStorage.getUserOrg();

        JSONObject postBody = new JSONObject();
        try {
            postBody.put("first_name", firstname);
            postBody.put("last_name", lastname);
            postBody.put("phone", phone);
            postBody.put("email", email);
            postBody.put("address", address);
            postBody.put("parent", parent);
            postBody.put("linked_to", linked_to);
            postBody.put("cycle_startdate", cycle_startdate);
            postBody.put("cycle_enddate", cycle_enddate);

            if( imageBase64String == null) {
                postBody.put("member_image", "");
            } else {
                postBody.put("member_image", imageBase64String);
            }

        } catch(JSONException e){
            e.printStackTrace();

        }
        return postBody;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AddMemberActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //rotate bitmap
                Matrix matrix = new Matrix();
                matrix.postRotate(0);
                //create new rotated bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                imageBase64String = encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 10);
                mMemberAvatar.setImageBitmap(bitmap);
                fabRotateImage.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    public void rotateImage(){
        mMemberAvatar.setDrawingCacheEnabled(false);
        mMemberAvatar.setDrawingCacheEnabled(true);

        Bitmap fetchedBitmapFromImageView = mMemberAvatar.getDrawingCache();

        //rotate bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(90 * rotationValue);
        rotationValue = rotationValue +1;
        //create new rotated bitmap
        bitmap = Bitmap.createBitmap(fetchedBitmapFromImageView, 0, 0,fetchedBitmapFromImageView.getWidth(), fetchedBitmapFromImageView.getHeight(), matrix, true);

        imageBase64String = encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100);
        mMemberAvatar.setImageBitmap(bitmap);

    }

    public void makeSnackbar(String snackMsg, int duration){

        snackBar = Snackbar.make(snackview, snackMsg, duration)
                .setAction("X", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                });

        View view = snackBar.getView();
        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snackBar.show();
    }

}
