package com.kushal.boulders.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.models.Member;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.ImageStorage;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;
import static com.kushal.boulders.misc.Constants.LOG_TAG;

public class EditMemberProfileActivity extends AppCompatActivity {

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    @Inject
    ImageStorage imageStorage;


    private RelativeLayout mProgressBar;
    private ImageView mMemberAvatar;
    private EditText mEditMemberFirstName;
    private EditText mEditMemberLastName;

    private TextInputLayout mEditMemberCycleStartDateHolder;
    private TextInputLayout mEditMemberCycleEndDateHolder;
    private EditText mEditMemberCycleStartDate;
    private EditText mEditMemberCycleEndDate;

    private EditText mEditMemberPhone;
    private EditText mEditMemberEmail;
    private EditText mEditMemberAddress;

    private Snackbar snackBar;
    private View snackview;

    private Member mMember = null;

    private int PICK_IMAGE_REQUEST = 1;

    private static final String EXTRA_MEMBER = "Member";
    
    //Bitmap to get image from gallery
    private Bitmap bitmap;
    private String imageBase64String =null;

    //Uri to store the image uri
    private Uri filePath;
    private FloatingActionButton fab;
    private FloatingActionButton fabAddImage;
    private FloatingActionButton fabRotateImage;

    private static int rotationValue = 1;

    SimpleDateFormat myDateFormat =  new SimpleDateFormat("yyyy-MM-dd"); // my format

    public static Intent createIntent(Activity activity) {
        Intent intent = new Intent(activity, EditMemberProfileActivity.class);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        setContentView(R.layout.activity_edit_member_profile);
        snackview = findViewById(android.R.id.content);
        mProgressBar = findViewById(R.id.rl_progressBar);
        mProgressBar.bringToFront();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        fab = findViewById(R.id.fab);
        fabAddImage = findViewById(R.id.fab_emf_addImage);
        fabRotateImage = findViewById(R.id.fab_emf_rotateImage);

        mMemberAvatar = findViewById(R.id.iv_emf_memberAvatar);
        mEditMemberFirstName = findViewById(R.id.edt_emf_memberFirstName);
        mEditMemberLastName = findViewById(R.id.edt_emf_memberLastName);
        mEditMemberPhone = findViewById(R.id.edt_emf_memberPhone);
        mEditMemberEmail = findViewById(R.id.edt_emf_memberEmail);
        mEditMemberAddress = findViewById(R.id.edt_emf_memberAddress);
        mEditMemberCycleStartDate = findViewById(R.id.edt_emf_memberCycleStartDate);
        mEditMemberCycleStartDateHolder = findViewById(R.id.holder_emf_memberCycleStartDate);
        mEditMemberCycleEndDate = findViewById(R.id.edt_emf_memberCycleEndDate);
        mEditMemberCycleEndDateHolder = findViewById(R.id.holder_emf_memberCycleEndDate);

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
                new DatePickerDialog(EditMemberProfileActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

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
                new DatePickerDialog(EditMemberProfileActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }

        });

        mMember = intent.getParcelableExtra(EXTRA_MEMBER);
        setProfile(mMember);

        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                saveMemberDetails(mMember);

            }
        });

        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                showFileChooser();

            }
        });

        fabRotateImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                rotateImage();

            }
        });
    }

    private void setProfile(final Member mMember) {

        mEditMemberFirstName.setText(mMember.getFirstName());
        mEditMemberLastName.setText(mMember.getLastName());
        mEditMemberPhone.setText(mMember.getPhone());
        mEditMemberEmail.setText(mMember.getEmail());
        mEditMemberAddress.setText(mMember.getAddress());

        String formattedStartDate = myDateFormat.format(mMember.getCycleStartDate());
        mEditMemberCycleStartDate.setText(formattedStartDate);

        String formattedEndDate = myDateFormat.format(mMember.getCycleEndDate());
        mEditMemberCycleEndDate.setText(formattedEndDate);

        System.out.println(" ===================================== Member.getMemberImage()" + mMember.getMemberImage() );

        if(imageStorage.getMemberImage(mMember.getMemberId()) != null && !imageStorage.getMemberImage(mMember.getMemberId()).isEmpty()) {
            Bitmap imageBitmap = decodeBase64(imageStorage.getMemberImage(mMember.getMemberId()));
            mMemberAvatar.setImageBitmap(imageBitmap);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveMemberDetails(final Member mCurrMember) {


        System.out.println(" ====================================== in saveMemberDetails : " + getUpdatePostBody(mCurrMember));

        if(validateForm()) {
            mHttpClient.updateMember(getUpdatePostBody(mCurrMember), new HttpClient.MemberUpdationCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("Member updated successfully!!")) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "Member details updated successfully..";
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                        Intent intent = new Intent(getApplicationContext(), MemberProfileActivity.class);

                        imageStorage.saveMemberImage(mMember.getMemberId(), imageBase64String);

                        Member savedMember = getEditedMember();

                        savedMember.removeMemberImage();    //remove after value has been set in shared preferences, and before putting in intent
                        intent.putExtra(EXTRA_MEMBER, savedMember);
                        startActivity(intent);
                        finish();

                        mProgressBar.setVisibility(View.INVISIBLE);
                        Log.i(LOG_TAG, "Member details updation Successful");

                    } else {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        makeSnackbar(getResources().getString(R.string.message_member_updation_failed), Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "Member updation failed..");

                    }
                }
            });
        }
    }

    public  boolean validateForm() {

        boolean firstname = mEditMemberFirstName.getText().toString().isEmpty();
        boolean lastname = mEditMemberLastName.getText().toString().isEmpty();
        boolean phone = mEditMemberPhone.getText().toString().isEmpty();
        boolean email = mEditMemberEmail.getText().toString().isEmpty();
        boolean cyclestartdate = mEditMemberCycleStartDate.getText().toString().isEmpty();
        boolean cycleenddate = mEditMemberCycleEndDate.getText().toString().isEmpty();
        boolean address = mEditMemberCycleStartDate.getText().toString().isEmpty();

        DateTime cycleStartDate = new DateTime( mEditMemberCycleStartDate.getText().toString() );
        DateTime cycleEndDate = new DateTime(  mEditMemberCycleEndDate.getText().toString());

        boolean isValidateDates = cycleStartDate.isBefore(cycleEndDate);

        String phoneNo = mEditMemberPhone.getText().toString();
        boolean isValidatePhoneNo = phoneNo.matches("[0-9]+");

        String emailId = mEditMemberEmail.getText().toString();
        boolean validEmail = EmailValidator.getInstance().isValid(emailId);
        boolean memberDetailsChanged = false;


        try{


        if(getUpdatePostBody(mMember).getJSONObject("updateFields").length() > 0){
            memberDetailsChanged = true;
        } else {
            memberDetailsChanged = false;
        }

        } catch(Exception e){
            e.printStackTrace();
        }


        if (((firstname || lastname || phone || email || cyclestartdate || cycleenddate || address) == false) && validEmail && isValidateDates &&  memberDetailsChanged) {

            return true;

        }else if (!validEmail){
            mProgressBar.setVisibility(View.INVISIBLE);
            makeSnackbar( " Please enter a valid email address " + emailId, Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Wrong email format");
            return false;

        } else if (!memberDetailsChanged) {

            mProgressBar.setVisibility(View.INVISIBLE);
            makeSnackbar( " No changes were made ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "No changes were made");
            return false;

        } else if (isValidateDates == false) {

            mProgressBar.setVisibility(View.INVISIBLE);
            makeSnackbar( " Cycle End Date can't start before Cycle Start Date ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Cycle End Date is less Than Cycle Start Date");
            return false;

        }else {

            mProgressBar.setVisibility(View.INVISIBLE);
            makeSnackbar( " Please fill all details ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Fields are empty");
            return false;


        }
    }

    public JSONObject getUpdatePostBody(Member mCurrMember){

        JSONObject updatePostBody = new JSONObject();

        final String firstname = mEditMemberFirstName.getText().toString();
        final String lastname = mEditMemberLastName.getText().toString();
        final String phone = mEditMemberPhone.getText().toString();
        final String email = mEditMemberEmail.getText().toString();
        final String cycle_startdate = mEditMemberCycleStartDate.getText().toString();
        final String cycle_enddate = mEditMemberCycleEndDate.getText().toString();
        final String address = mEditMemberAddress.getText().toString();


        JSONObject postBody = new JSONObject();
        try {

            if(!mCurrMember.getFirstName().equals(firstname)) {
                postBody.put("firstName", firstname);
            }

            if(!mCurrMember.getLastName().equals(lastname)) {
                postBody.put("lastName", lastname);
            }
            if(!mCurrMember.getPhone().equals(phone)) {
                postBody.put("phone", phone);
            }

            if(!mCurrMember.getEmail().equals(email)) {
                postBody.put("email", email);
            }

            if(!mCurrMember.getAddress().equals(address)) {
                postBody.put("address", address);
            }

            System.out.println(" ====================================== in saveMemberDates: " + myDateFormat.format(mCurrMember.getCycleStartDate()) + cycle_startdate);

            if(!myDateFormat.format(mCurrMember.getCycleStartDate()).equals(cycle_startdate)) {
                postBody.put("cycleStartDate", cycle_startdate);
            }

            if(!myDateFormat.format(mCurrMember.getCycleEndDate()).equals(cycle_enddate)) {
                postBody.put("cycleEndDate", cycle_enddate);
            }


            if(imageStorage.getMemberImage(mMember.getMemberId()) != null && !imageStorage.getMemberImage(mMember.getMemberId()).isEmpty()) {

                    if (!imageStorage.getMemberImage(mMember.getMemberId()).equals(imageBase64String)) {
                        postBody.put("memberImage", imageBase64String);

                   }
            } else {

                if (imageBase64String != null) {
                    postBody.put("memberImage", imageBase64String);
                }
            }

            updatePostBody.put("_id", mCurrMember.getMemberId());
            updatePostBody.put("updateFields", postBody);

        } catch(JSONException e){
            e.printStackTrace();

        }

        System.out.println(" ======================================== updatePostBody :" + updatePostBody);
        return updatePostBody;
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


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Member getEditedMember(){

        final String firstname = mEditMemberFirstName.getText().toString();
        final String lastname = mEditMemberLastName.getText().toString();
        final String phone = mEditMemberPhone.getText().toString();
        final String email = mEditMemberEmail.getText().toString();
        final String cycle_startdate = mEditMemberCycleStartDate.getText().toString();
        final String cycle_enddate = mEditMemberCycleEndDate.getText().toString();
        final String address = mEditMemberAddress.getText().toString();

        mSharedPrefStorage = new SharedPrefStorage(this);

        final String parent = mSharedPrefStorage.getUserId();
        final String linked_to = mSharedPrefStorage.getUserOrg();

        System.out.println(" ======================================== EditedMember :" + linked_to + " : " + mMember.getCreationDate().toString());

        DateTimeFormatter jtf = DateTimeFormat.forPattern("yyyy-MM-dd");

        return new Member(mMember.getMemberId(), firstname, lastname, new DateTime(cycle_startdate), new DateTime(cycle_enddate), phone, email, parent, linked_to, address, imageBase64String, new DateTime(mMember.getCreationDate()), new DateTime(mMember.getLatUpdateDate()));
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public void onBackPressed() {
        try {
        System.out.println(" ==========-============= back pressed" + getUpdatePostBody(mMember).getJSONObject("updateFields").length());

            if (getUpdatePostBody(mMember).getJSONObject("updateFields").length() > 0) {
                    showGoBackNavigationDialog();

            } else{

                super.onBackPressed();
            }
        } catch(Exception e){

        }

    }

    private void showGoBackNavigationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditMemberProfileActivity.this);
        builder.setTitle("Go back to Member Profile");
        builder.setMessage("Are you sure you want to go back without saving your changes?");

        String positiveText = getString(android.R.string.yes);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        Intent intent = new Intent(EditMemberProfileActivity.this, MemberProfileActivity.class);
                        intent.putExtra(EXTRA_MEMBER, mMember);
                        startActivity(intent);
                        finish();
                    }


                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }


    public void rotateImage(){

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
