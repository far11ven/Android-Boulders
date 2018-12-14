package com.kushal.boulders.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.validator.routines.EmailValidator;

import com.google.common.hash.Hashing;
import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.utils.OnSwipeTouchListener;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import static com.kushal.boulders.misc.Constants.LOG_TAG;

public class RegistrationActivity extends AppCompatActivity {

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    private EditText mEditTextFirstname;
    private EditText mEditTextlastname;
    private EditText mEditTextOrgname;
    private EditText mEditTextEmail;
    private EditText mEditTextSetPassword;
    private EditText mEditTextConfirmPassword;
    private EditText mEditTextSecurityAnswer;

    private Spinner mSecurityQuestions;

    private Button buttonRegisterNext;
    private Button buttonRegister;

    private LinearLayout mRegistrationLayout;
    private LinearLayout mRegistrationSecurityLayout;

    private View snackview;
    private Snackbar snackBar;


    public static Intent createIntent(Activity activity) {
        return new Intent(activity, RegistrationActivity.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        snackview = findViewById(android.R.id.content);

        mEditTextFirstname = findViewById(R.id.edt_firstname);
        mEditTextlastname = findViewById(R.id.edt_lastname);
        mEditTextOrgname = findViewById(R.id.edt_orgname);
        mEditTextEmail = findViewById(R.id.edt_email);
        mEditTextSetPassword = findViewById(R.id.edt_reg_setpass);
        mEditTextConfirmPassword = findViewById(R.id.edt_reg_confirmpass);

        mEditTextSecurityAnswer = findViewById(R.id.edt_reg_userAnswerInput);


        mSecurityQuestions = findViewById(R.id.dd_reg_userSequrityQuestion);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RegistrationActivity.this,
                R.array.security_ques_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityQuestions.setAdapter(adapter);


        mRegistrationLayout = findViewById(R.id.ll_layout_registration);                         //Main Registration Layout
        mRegistrationSecurityLayout = findViewById(R.id.ll_securityscreen_layout);               //Second Registration Screen Layout

        buttonRegisterNext = findViewById(R.id.btn_registerNext);
        buttonRegisterNext.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick (View view) {
                if(validateRegisterationForm1()) {
                     mRegistrationLayout.setVisibility(View.GONE);
                     mRegistrationSecurityLayout.setVisibility(View.VISIBLE);

                 }
            }
        });

        buttonRegister = findViewById(R.id.btn_register);
        buttonRegister.setOnClickListener(new OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick (View view) {

                    attemptRegistration();

                }
        });

        TextView loginLink = findViewById(R.id.tv_linkToLogin);
        loginLink.setClickable(true);
        loginLink.setMovementMethod(LinkMovementMethod.getInstance());
        String loginLinkText = "Already have an account? <a href=''> Login Now </a>";
        loginLink.setText(Html.fromHtml(loginLinkText));
        loginLink.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


        CoordinatorLayout activityLayout = findViewById(R.id.layout_registration);
        activityLayout.setOnTouchListener(new OnSwipeTouchListener(RegistrationActivity.this) {
            public void onSwipeRight() {

                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }


        });
    }

    @Override
    public void onBackPressed() {

        if (mRegistrationLayout.getVisibility() == View.VISIBLE) {
            finish();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        } else if (mRegistrationSecurityLayout.getVisibility() == View.VISIBLE) {

            mRegistrationSecurityLayout.setVisibility(View.GONE);
            mRegistrationLayout.setVisibility(View.VISIBLE);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void attemptRegistration() {

        if(validateAllForms()) {
            mHttpClient.createUser(getPostBody(), new HttpClient.UserCreationCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("New User created successfully!!")) {
                        mSharedPrefStorage.saveUserName(mCreatedUser);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "Welcome, " + mCreatedUser.getUserName();
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        Log.i(LOG_TAG, "User created Successful");

                    } else if (mHttpClient.getResponseMessage().contains("User added without security details")) {

                        mSharedPrefStorage.saveUserName(mCreatedUser);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "Welcome, " + mCreatedUser.getUserName();
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        Log.i(LOG_TAG, "User created Successful, sans security details");

                    } else if (mHttpClient.getResponseMessage().contains("Duplicate key error")) {

                        makeSnackbar( "User with this email already exists, try login.", Snackbar.LENGTH_INDEFINITE);
                        Log.i(LOG_TAG, "User with this email already exists, try login.");


                    } else {

                        makeSnackbar( getResources().getString(R.string.message_registration_fail), Snackbar.LENGTH_LONG);
                        Log.i(LOG_TAG, "User registration failed, please try again after sometime");
                    }
                }
            });
        }
    }

    public  boolean validateRegisterationForm1() {

        boolean firstname = mEditTextFirstname.getText().toString().isEmpty();
        boolean lastname = mEditTextlastname.getText().toString().isEmpty();
        boolean org_name = mEditTextOrgname.getText().toString().isEmpty();
        boolean email = mEditTextEmail.getText().toString().isEmpty();

        final String emailId = mEditTextEmail.getText().toString();


        boolean validEmail = EmailValidator.getInstance().isValid(emailId);

        if ((firstname || lastname || org_name || email ) == false) {

            if(validEmail) {
                return true;

            } else {
                makeSnackbar( " Please enter a valid email Id ", Snackbar.LENGTH_LONG);
                Log.i(LOG_TAG, "Email Id is not valid");
                return false;

            }


        } else {

                makeSnackbar( " All fields are required ", Snackbar.LENGTH_INDEFINITE);
                Log.i(LOG_TAG, "Fields are empty");
                return false; }

    }


    public  boolean validateAllForms() {

        boolean firstname = mEditTextFirstname.getText().toString().isEmpty();
        boolean lastname = mEditTextlastname.getText().toString().isEmpty();
        boolean org_name = mEditTextOrgname.getText().toString().isEmpty();
        boolean email = mEditTextEmail.getText().toString().isEmpty();
        boolean setPassword = mEditTextSetPassword.getText().toString().isEmpty();
        boolean confirmPassword = mEditTextConfirmPassword.getText().toString().isEmpty();

        boolean securityAnswer = mEditTextSecurityAnswer.getText().toString().isEmpty();

        final String setPasswordText = mEditTextSetPassword.getText().toString();
        final String confirmPasswordText = mEditTextConfirmPassword.getText().toString();
        final String emailId = mEditTextEmail.getText().toString();


        boolean validEmail = EmailValidator.getInstance().isValid(emailId);
        boolean passwordLength = mEditTextSetPassword.getText().toString().length() > 5;
        boolean passwordMatch = setPasswordText.equals(confirmPasswordText);

        if ((firstname || lastname || org_name || email || setPassword || confirmPassword || securityAnswer) == false) {

            if(passwordLength && passwordMatch && validEmail) {
                return true;

            } else if(passwordLength == false) {
                makeSnackbar( " Password should have atleast 6 characters ", Snackbar.LENGTH_LONG);
                Log.i(LOG_TAG, "Password length is too short");
                return false;

            } else if(passwordMatch == false) {
                makeSnackbar( " Both passwords should be same ", Snackbar.LENGTH_LONG);
                Log.i(LOG_TAG, "Passwords don't match");
                return false;

            } else {
                makeSnackbar( " Please enter a valid email Id ", Snackbar.LENGTH_LONG);
                Log.i(LOG_TAG, "Wrong email format");
                return false;
            }

        } else {
            makeSnackbar( " All fields are required ", Snackbar.LENGTH_LONG);
            Log.i(LOG_TAG, "Fields are empty");
            return false;

        }
    }

    public  JSONObject getPostBody(){

        final String firstname = mEditTextFirstname.getText().toString();
        final String lastname = mEditTextlastname.getText().toString();
        final String org_name = mEditTextOrgname.getText().toString();
        final String email = mEditTextEmail.getText().toString();
        final String password = mEditTextSetPassword.getText().toString();
        final String passwordHash = Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();

        final String securityQuestion = mSecurityQuestions.getSelectedItem().toString();
        final String securityAnswer = mEditTextSecurityAnswer.getText().toString().toLowerCase();
        final String securityToken = Hashing.sha256()
                .hashString(securityAnswer, StandardCharsets.UTF_8)
                .toString();

        JSONObject postBody = new JSONObject();
        try {
            postBody.put("first_name", firstname);
            postBody.put("last_name", lastname);
            postBody.put("org_name", org_name);
            postBody.put("email", email);
            postBody.put("password", passwordHash);
            postBody.put("security_question", securityQuestion);
            postBody.put("security_token", securityToken);
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
