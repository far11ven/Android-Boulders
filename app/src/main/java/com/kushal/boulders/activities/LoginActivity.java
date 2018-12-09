package com.kushal.boulders.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;

import com.kushal.boulders.utils.OnSwipeTouchListener;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.SharedPrefStorage;


import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import static com.kushal.boulders.misc.Constants.LOG_TAG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    private EditText mEditTextUsername;
    private EditText mEditTextPassword;
    private EditText mEditTextFindEmail;
    private EditText mEditTextSecurityAnswer;
    private EditText mEditTextNewPassword;
    private EditText mEditTextConfirmNewPassword;

    private Button mFindUser;
    private Button mVerifyUser;
    private Button mChangeUserPassword;
    private ProgressBar mWaitingIndicator;

    private LinearLayout mUserLoginLayout;
    private LinearLayout mUserForgotPasswordLayout;
    private LinearLayout mUserVerifySecurityQuestionLayout;
    private LinearLayout mUserChangePasswordLayout;

    private Boolean isPasswordChanged = null;
    private String securityToken = null;

    private View snackview;
    private Snackbar snackBar;

    private final JsonParser mJsonParser = new JsonParser();

    public static Intent createIntent(Activity activity) {
        return new Intent(activity, LoginActivity.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        snackview = findViewById(android.R.id.content);

        mUserLoginLayout = findViewById(R.id.ll_LoginLayout);
        mEditTextUsername = findViewById(R.id.edt_username);
        mEditTextPassword = findViewById(R.id.edt_password);
        mEditTextFindEmail = findViewById(R.id.edt_findEmail);
        mEditTextFindEmail.setText("");

        mEditTextNewPassword= findViewById(R.id.edt_userNewPassword);
        mEditTextConfirmNewPassword= findViewById(R.id.edt_userNewConfirmPassword);

        Button buttonLogin = findViewById(R.id.btn_login);
        buttonLogin.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mUserForgotPasswordLayout = findViewById(R.id.ll_userforgotpassword_layout);
        mUserVerifySecurityQuestionLayout = findViewById(R.id.ll_userverifysecurity_layout);
        mUserChangePasswordLayout = findViewById(R.id.ll_userchangepassword_layout);

        TextView registrationLink = findViewById(R.id.tv_linkToRegistration);
        registrationLink.setClickable(true);
        registrationLink.setMovementMethod(LinkMovementMethod.getInstance());
        String registrationLinkText = "Don't have an account? <a href=''> Register Now </a>";
        registrationLink.setText(Html.fromHtml(registrationLinkText));
        registrationLink.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        TextView forgotPasswordLink = findViewById(R.id.tv_linkToForgetPassword);
        forgotPasswordLink.setClickable(true);
        forgotPasswordLink.setMovementMethod(LinkMovementMethod.getInstance());
        String forgotPasswordLinkText = "<a href=''> Forgot Password? </a>";
        forgotPasswordLink.setText(Html.fromHtml(forgotPasswordLinkText));

        forgotPasswordLink.setOnClickListener(this);

        if(mVerifyUser != null){
            System.out.println(" ================================ mVerifyUser is not null");
            mVerifyUser.setOnClickListener(this);
        }

        CoordinatorLayout activityLayout = findViewById(R.id.layout_login);
        activityLayout.setOnTouchListener(new OnSwipeTouchListener(LoginActivity.this) {
            public void onSwipeLeft() {

                finish();
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_linkToForgetPassword:

                mUserLoginLayout.setVisibility(LinearLayout.GONE);
                mUserForgotPasswordLayout.setVisibility(LinearLayout.VISIBLE);

                mFindUser = findViewById(R.id.btn_findUserDetails);
                mFindUser.setOnClickListener(this);

                break;

            case R.id.btn_findUserDetails:

                mWaitingIndicator = findViewById(R.id.pb_progressBar);
                mWaitingIndicator.bringToFront();
                mWaitingIndicator.setVisibility(View.VISIBLE);

                findUserSecurityDetailsByEmail(mEditTextFindEmail.getText().toString());

                mVerifyUser = findViewById(R.id.btn_verifyUserDetails);

                mVerifyUser.setOnClickListener(this);

                break;

            case R.id.btn_verifyUserDetails:

                if(mEditTextSecurityAnswer.getText().toString().toLowerCase().equals(securityToken.toLowerCase())) {

                    mUserVerifySecurityQuestionLayout.setVisibility(View.GONE);
                    mUserChangePasswordLayout.setVisibility(View.VISIBLE);

                    mEditTextNewPassword.setText("");
                    mEditTextConfirmNewPassword.setText("");

                    mChangeUserPassword = findViewById(R.id.btn_changePassword);

                    mChangeUserPassword.setOnClickListener(this);
                } else {

                    makeSnackbar("Sorry, Your answer doesn't match", Snackbar.LENGTH_LONG);
                    Log.i(LOG_TAG, "User Security answer doesn't match");
                }


                break;

            case R.id.btn_changePassword:

                mEditTextNewPassword  = findViewById(R.id.edt_userNewPassword);;
                mEditTextConfirmNewPassword  = findViewById(R.id.edt_userNewConfirmPassword);

                if(mEditTextNewPassword.getText().toString().equals(mEditTextConfirmNewPassword.getText().toString()))
                {
                    if(mEditTextNewPassword.getText().toString().length() >= 6){

                        isPasswordChanged = userChangePassword(getPasswordChangePostBody());

                        while (isPasswordChanged == null){
                            runOnUiThread(new Runnable() {
                                public void run() {

                                    mWaitingIndicator.getProgressDrawable()
                                        .setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorBlue), PorterDuff.Mode.MULTIPLY);
                                    mWaitingIndicator.setVisibility(View.VISIBLE);

                                }
                            });
                        }

                        if(isPasswordChanged) {

                            makeSnackbar( "Password has been changed", Snackbar.LENGTH_LONG);
                            Log.i(LOG_TAG, "Password changed successfully");

                            mUserChangePasswordLayout.setVisibility(View.GONE);
                            mUserLoginLayout.setVisibility(View.VISIBLE);

                        }

                    } else {

                        makeSnackbar( "Password should have 6 or more characters", Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "Password length is < 6");

                    }

                } else {

                    makeSnackbar( "Both passwords do-not match", Snackbar.LENGTH_LONG);
                    Log.i(LOG_TAG, "Passwords doesn't match");

                }
                break;

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void attemptLogin() {
        final String username = mEditTextUsername.getText().toString();
        final String password = mEditTextPassword.getText().toString();

        if(password.length() > 5 ) {
            mHttpClient.fetchUser(username, new HttpClient.UserLoginCallback() {

                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("User found")){
                         if (mUser.match(username, password)) {
                              mSharedPrefStorage.saveUserName(mUser);

                         // fetching SecurityQuestion
                          mHttpClient.fetchUserSecurityDetails(username, new HttpClient.UserSecurityCallback() {
                            @Override
                            public void run() {

                                if (mHttpClient.getResponseMessage().contains("User security details found")) {

                                    mHttpClient.saveSecurityDetails(mHttpClient.getResponseMessage());

                                    runOnUiThread(new Runnable() {
                                        public void run() {

                                            finish();

                                            String toastMsg = "Welcome, " + mUser.getUserName();
                                            Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);

                                        }
                                    });

                                } else {

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            String toastMsg = "Welcome, " + mUser.getUserName();
                                            Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }

                            }
                        });


                           finish();
                           Log.i(LOG_TAG, "Successful login with user: " + username);
                         } else {

                             makeSnackbar( "Incorrect Username/Password", Snackbar.LENGTH_INDEFINITE);
                             Log.i(LOG_TAG, "Failed login with user: " + username);
                         }

                    } else {

                        makeSnackbar( "Incorrect Username/Password", Snackbar.LENGTH_INDEFINITE);
                        Log.i(LOG_TAG, "Failed login with user: " + username);
                    }
                }
            });

        } else {

            makeSnackbar( "Password should have atleast 6 characters", Snackbar.LENGTH_INDEFINITE);

        }
    }

    private void findUserSecurityDetailsByEmail (String userEmail){
            System.out.println(" passed userEmail ============= " + userEmail);

            mHttpClient.fetchUserSecurityDetails(userEmail, new HttpClient.UserSecurityCallback() {
                @Override
                public void run() {

                    mWaitingIndicator.setVisibility(View.INVISIBLE);

                    if (mHttpClient.getResponseMessage().contains("User security details found")) {

                        runOnUiThread(new Runnable() {
                            public void run() {

                                mUserForgotPasswordLayout.setVisibility(View.GONE);
                                mUserVerifySecurityQuestionLayout.setVisibility(View.VISIBLE);


                                TextView mUserFetchedSecurityQuestion = findViewById(R.id.tv_userSequrityQuestion);
                                mUserFetchedSecurityQuestion.setText(getSecurityDetails(mHttpClient.getResponseMessage()));

                                mEditTextSecurityAnswer = findViewById(R.id.edt_userAnswerInput);
                                mEditTextSecurityAnswer.setText("");
                            }
                        });


                    } else {

                        makeSnackbar( "User does not exist", Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "User does not exist");

                    }

                }
            });

    }

    private Boolean userChangePassword (JSONObject body){
        System.out.println(" passed userEmail ============= " + body);

        mHttpClient.changeUserPassword(body, new HttpClient.UserUpdationCallback() {
            @Override
            public void run() {

                if (mHttpClient.getResponseMessage().contains("User password updated successfully!!")) {

                    isPasswordChanged = true;

                } else {

                    isPasswordChanged = false;

                    makeSnackbar( "Password not updated", Snackbar.LENGTH_LONG);
                    Log.i(LOG_TAG, "Password not updated");

                }

            }
        });

        return isPasswordChanged;

    }

    @Override
    public void onBackPressed() {

        if (mUserLoginLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
            finish();
        } else if (mUserForgotPasswordLayout.getVisibility() == View.VISIBLE) {

            mUserForgotPasswordLayout.setVisibility(View.GONE);
            mUserLoginLayout.setVisibility(View.VISIBLE);

        } else if (mUserVerifySecurityQuestionLayout.getVisibility() == View.VISIBLE) {

            mUserVerifySecurityQuestionLayout.setVisibility(View.GONE);
            mUserForgotPasswordLayout.setVisibility(View.VISIBLE);
        } else if (mUserChangePasswordLayout.getVisibility() == View.VISIBLE) {

            mUserChangePasswordLayout.setVisibility(View.GONE);
            mUserVerifySecurityQuestionLayout.setVisibility(View.VISIBLE);
        }
    }

    private String getSecurityDetails(String jsonResponse) {

        System.out.println(" ====================== =============== Fetched Members ..." + jsonResponse);
        JsonObject jsonObject = mJsonParser.parse(jsonResponse).getAsJsonObject();

        JsonObject result = jsonObject.getAsJsonObject("result");

        JsonElement securityQuestion = result.get("userSecurityQues");

        securityToken = result.get("userSecurityValue").getAsString();

        System.out.println(" ====================================== " + securityQuestion.getAsString());


        return securityQuestion.getAsString();

    }

    public JSONObject getPasswordChangePostBody(){

        final String email = mEditTextFindEmail.getText().toString();
        final String newpassword = mEditTextNewPassword.getText().toString();


        JSONObject postBody = new JSONObject();
        try {
            postBody.put("username", email);
            postBody.put("new_password", newpassword);
        } catch(JSONException e){
            e.printStackTrace();

        }
        return postBody;
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
