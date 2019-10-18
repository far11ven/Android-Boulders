package com.kushal.boulders.activities;

import android.app.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.hash.Hashing;
import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import static android.view.Gravity.CENTER;
import static com.kushal.boulders.misc.Constants.LOG_TAG;


public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    Spinner mUserSecurityQustions ;
    ImageView mUserAvatar ;
    EditText mUserFirstName;
    EditText mUserLastName ;
    EditText mUserEmail;
    EditText mUserOrgName;
    EditText mUserSecurityAnswer;

    TextView lblResult;
    ProgressBar mProgressIndicator;

    FloatingActionButton mSaveButton;

    Button mChangePasswordButton;

    Snackbar snackBar;
    View snackview;

    AlertDialog passwordChangeDialog  = null;

    private ArrayAdapter<CharSequence> securityQuestionAdapter;

    public static Intent createIntent(Activity activity) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

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

         snackview = findViewById(android.R.id.content);

         mUserAvatar = findViewById(R.id.iv_userAvatar);
         mUserFirstName = findViewById(R.id.edt_userFirstName);
         mUserLastName = findViewById(R.id.edt_userLastName);
         mUserEmail = findViewById(R.id.edt_userEmail);
         mUserOrgName = findViewById(R.id.edt_userOrgName);
         mUserSecurityQustions = findViewById(R.id.security_ques_spinner);
         mUserSecurityAnswer = findViewById(R.id.edt_securityAnswer);

        mUserFirstName.setFocusable(false); //to disable it
        mUserLastName.setFocusable(false); //to disable it
        mUserEmail.setFocusable(false); //to disable it
        mUserOrgName.setFocusable(false); //to disable it
        mUserSecurityQustions.setFocusable(false); //to disable it
        mUserSecurityAnswer.setFocusable(false); //to disable it

        mUserFirstName.setOnClickListener(this);
        mUserLastName.setOnClickListener(this);
        mUserEmail.setOnClickListener(this);
        mUserOrgName.setOnClickListener(this);
        mUserSecurityAnswer.setOnClickListener(this);


        securityQuestionAdapter = ArrayAdapter.createFromResource(this,
                R.array.security_ques_array, android.R.layout.simple_spinner_item);
        securityQuestionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUserSecurityQustions.setAdapter(securityQuestionAdapter);

        setUserProfile();

        mSaveButton = findViewById(R.id.fab_save);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                saveUserDetails();

            }
        });

        mChangePasswordButton = findViewById(R.id.btn_changePassword);
        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                onButtonShowPopupWindowClick(view);

            }
        });

    }


    //EditTexts OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edt_userFirstName:
                mUserFirstName.setFocusableInTouchMode(true); //to enable it
                break;

            case R.id.edt_userLastName:
                mUserLastName.setFocusableInTouchMode(true); //to enable it
                break;

            case R.id.edt_userEmail:
                mUserEmail.setFocusableInTouchMode(true); //to enable it
                break;

            case R.id.edt_userOrgName:
                mUserOrgName.setFocusableInTouchMode(true); //to enable it
                break;

            case R.id.security_ques_spinner:
                mUserSecurityQustions.setFocusableInTouchMode(true); //to enable it
                break;

            case R.id.edt_securityAnswer:
                mUserSecurityAnswer.setFocusableInTouchMode(true); //to enable it
                break;
        }

    }

    private void setUserProfile() {

        mSharedPrefStorage = new SharedPrefStorage(this);

        mUserFirstName.setText(mSharedPrefStorage.getUserFirstName());
        mUserLastName.setText(mSharedPrefStorage.getUserLastName());
        mUserEmail.setText(mSharedPrefStorage.getUser().getUserName());
        mUserOrgName.setText(mSharedPrefStorage.getUserOrg());
        //mUserSecurityAnswer.setText(mSharedPrefStorage.getUserSecurityAnswer());

        mUserSecurityQustions.setSelection(securityQuestionAdapter.getPosition(mSharedPrefStorage.getUserSecurityQuestion()));


    }

    public void saveUserDetails() {

        String userSecurityQuestion =  mUserSecurityQustions.getSelectedItem().toString();
        String userSecurityAnswer = mUserSecurityAnswer.getText().toString();
        String userFirstName = mUserFirstName.getText().toString();
        String userLastName = mUserLastName.getText().toString();
        String userOrgName = mUserOrgName.getText().toString();


        if (validateUserDetails()){

            if ((userSecurityQuestion != null && !userSecurityQuestion.equals(mSharedPrefStorage.getUserSecurityQuestion())
                    || userSecurityAnswer != null )){

                saveUserSeurityDetails();

            }

            if((userFirstName != null && !userFirstName.equals(mSharedPrefStorage.getUserFirstName()))
                    || (userLastName != null && !userLastName.equals(mSharedPrefStorage.getUserLastName()))
                    || (userOrgName != null && !userOrgName.equals(mSharedPrefStorage.getUserOrg()))){

            mHttpClient.updateUser(getUpdatePostBody(), new HttpClient.UserUpdationCallback() {
                    @Override
                    public void run() {

                        if (mHttpClient.getResponseMessage().contains("User updated successfully!!")) {

                            mSharedPrefStorage.saveUserFirstName(mUserFirstName.getText().toString());
                            mSharedPrefStorage.saveUserLastName(mUserLastName.getText().toString());
                            mSharedPrefStorage.saveUserOrg(mUserOrgName.getText().toString());

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    String toastMsg = "User details have been updated";
                                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            makeSnackbar( "Unable to save member details", Snackbar.LENGTH_LONG);
                            Log.i(LOG_TAG, "User updation failed..");
                        }

                    }

                });
           }


       }

    }


    public void saveUserSeurityDetails() {

        if (validateUserDetails()){
            mHttpClient.updateUserSecurityDetails(getUpdateSecurityPostBody(), new HttpClient.UserSecurityCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("User Seurity details updated successfully!!")) {

                        mSharedPrefStorage.saveUserSecurityQuestion(mUserSecurityQustions.getSelectedItem().toString());
                        mSharedPrefStorage.saveUserSecurityAnswer(mUserSecurityAnswer.getText().toString());

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "User details have been updated";
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        });

                    } else {

                        makeSnackbar( "Unable to save member details", Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "User updation failed..");
                    }

                }

            });

        }

    }

    private void userChangePassword (JSONObject postBody){

        System.out.println(" passed userEmail ============= " + postBody);

            mHttpClient.changeUserPassword(postBody, new HttpClient.UserUpdationCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("User password updated successfully!!")) {

                        mProgressIndicator.setVisibility(View.INVISIBLE);
                        passwordChangeDialog.dismiss();

                        makeSnackbar( "Password updated Successfully", Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "Password updated Successfully");




                    } else {
                        mProgressIndicator.setVisibility(View.INVISIBLE);
                        lblResult.setText("Password not updated, please check you are connected to internet");

                        makeSnackbar( "Password not updated", Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "Password not updated");

                    }

                }
            });



    }

    @Override
    public void onBackPressed() {

        if (validateUserDetails()) {
            showGoBackNavigationDialog();

        } else{

            super.onBackPressed();
        }

    }

    public JSONObject getPasswordChangePostBody(String newpassword){

        final String email = mUserEmail.getText().toString();

        String newpasswordHash = Hashing.sha256()
                .hashString(newpassword, StandardCharsets.UTF_8)
                .toString();

        JSONObject postBody = new JSONObject();
        try {
            postBody.put("username", email);
            postBody.put("new_password", newpasswordHash);
        } catch(JSONException e){
            e.printStackTrace();

        }
        return postBody;
    }

    public JSONObject getUpdateSecurityPostBody(){

        final String email = mUserEmail.getText().toString();
        final String security_question = mUserSecurityQustions.getSelectedItem().toString();
        final String securityAnswer = mUserSecurityAnswer.getText().toString().toLowerCase();

        String security_token = Hashing.sha256()
                .hashString(securityAnswer, StandardCharsets.UTF_8)
                .toString();


        JSONObject postBody = new JSONObject();
        try {
            postBody.put("email", email);
            postBody.put("security_question", security_question);
            postBody.put("security_token", security_token);
        } catch(JSONException e){
            e.printStackTrace();

        }
        return postBody;
    }

    public boolean validateUserDetails(){
        String userSecurityQuestion =  mUserSecurityQustions.getSelectedItem().toString();
        String userFirstName = mUserFirstName.getText().toString();
        String userLastName = mUserLastName.getText().toString();
        String userOrgName = mUserOrgName.getText().toString();
        String userSecurityAnswer = mUserSecurityAnswer.getText().toString();

        if((userFirstName != null && !userFirstName.equals(mSharedPrefStorage.getUserFirstName()))
                || (userLastName != null && !userLastName.equals(mSharedPrefStorage.getUserLastName()))
                || (userOrgName != null && !userOrgName.equals(mSharedPrefStorage.getUserOrg()))
                || (userSecurityQuestion != null && !userSecurityQuestion.equals(mSharedPrefStorage.getUserSecurityQuestion()))
                || (userSecurityAnswer != null && !userSecurityAnswer.equals(mSharedPrefStorage.getUserSecurityAnswer()))){


            return true;
        } else if (userFirstName == null){

            runOnUiThread(new Runnable() {
                public void run() {
                    String toastMsg = "First name can-not be empty";
                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                }
            });

            return false;


        } else if (userLastName == null){

            runOnUiThread(new Runnable() {
                public void run() {
                    String toastMsg = "Last name can-not be empty";
                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                }
            });

            return false;


        } else if (userOrgName == null){

            runOnUiThread(new Runnable() {
                public void run() {
                    String toastMsg = "Org name can-not be empty";
                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                }
            });

            return false;


        } else if (userSecurityAnswer == null){

            runOnUiThread(new Runnable() {
                public void run() {
                    String toastMsg = "Security Answer can-not be empty";
                    Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                }
            });

            return false;


        }

        return false;




    }

    public JSONObject getUpdatePostBody(){

        JSONObject postBody = new JSONObject();
        JSONObject updatePostBody = new JSONObject();

        String userFirstName = mUserFirstName.getText().toString();
        String userLastName = mUserLastName.getText().toString();
        String userOrgName = mUserOrgName.getText().toString();

        try {

            if(userFirstName != null && !userFirstName.equals(mSharedPrefStorage.getUserFirstName())){

                postBody.put("firstName", userFirstName);
            }
            if (userLastName != null && !userLastName.equals(mSharedPrefStorage.getUserLastName())){

                postBody.put("lastName", userLastName);

            }

            if (userOrgName != null && !userOrgName.equals(mSharedPrefStorage.getUserOrg())){

                postBody.put("orgName", userOrgName);
                postBody.put("oldOrgName", mSharedPrefStorage.getUserOrg());

            }

            updatePostBody.put("_id", mSharedPrefStorage.getUserId());
            updatePostBody.put("updateFields", postBody);


        } catch(JSONException e){
            e.printStackTrace();

        }

        System.out.println(" ======================================== updatePostBody :" + updatePostBody);

        return updatePostBody;


    }

    public void onButtonShowPopupWindowClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Your Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        lblResult = new TextView(this);
        lblResult.setTextColor(Color.RED);

        mProgressIndicator = new ProgressBar(this);
        mProgressIndicator.bringToFront();
        mProgressIndicator.setVisibility(View.INVISIBLE);

        final EditText edtNewPassword = new EditText(this);
        edtNewPassword.setHint("New Password");
        final EditText edtConfirmNewPassword = new EditText(this);
        edtConfirmNewPassword.setHint("Confirm New Password");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.5f);

        layout.setGravity(CENTER);
        layout.setPadding(50, 50, 50, 50);

        lblResult.setLayoutParams(lp);
        mProgressIndicator.setLayoutParams(lp);
        edtNewPassword.setLayoutParams(lp);
        edtConfirmNewPassword.setLayoutParams(lp);

        layout.addView(lblResult);
        layout.addView(mProgressIndicator);
        layout.addView(edtNewPassword);
        layout.addView(edtConfirmNewPassword);

        builder.setView(layout);

        String positiveText = getString(R.string.button_changePassword);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }


                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

        passwordChangeDialog= builder.create();
        // display dialog
        passwordChangeDialog.show();

        passwordChangeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                lblResult.setText("");
                String newpassword = edtNewPassword.getText().toString();
                String confirm_newpassword = edtConfirmNewPassword.getText().toString();

                if(newpassword!=null && newpassword.length() > 5 && newpassword.equals(confirm_newpassword)) {

                    mProgressIndicator.setVisibility(View.VISIBLE);
                    userChangePassword(getPasswordChangePostBody(newpassword));

                } else {
                    if(newpassword == null ){

                        lblResult.setText("New password can-not be null");
                        Log.i(LOG_TAG, "Password can-not be null");

                    } else if (newpassword.length() < 6 ){

                        lblResult.setText("New password should have atleast 6 characters");
                        Log.i(LOG_TAG, "Password length too short");

                    } else if(!newpassword.equals(confirm_newpassword)){

                        lblResult.setText("Password doesn't match");
                        Log.i(LOG_TAG, "Both passwords should match");

                    }

                    mProgressIndicator.setVisibility(View.INVISIBLE);

                }
            }
        });
    }




    private void showGoBackNavigationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Changes");
        builder.setMessage("Are you sure you want to go back without saving your changes?");

        String positiveText = getString(android.R.string.yes);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i= new Intent(UserProfileActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();

                        dialog.dismiss();

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
