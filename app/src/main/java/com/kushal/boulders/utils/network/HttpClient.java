package com.kushal.boulders.utils.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kushal.boulders.activities.MainActivity;
import com.kushal.boulders.models.Member;
import com.kushal.boulders.models.User;
import com.kushal.boulders.utils.storage.ConfigStorage;
import com.kushal.boulders.utils.storage.ImageStorage;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.kushal.boulders.misc.Constants.LOG_TAG;
import static com.kushal.boulders.misc.Constants.SOCKET_TIMEOUT;

public class HttpClient {

    ConfigStorage configStorage;

    SharedPrefStorage mSharedPrefStorage;

    ImageStorage mImageStorage ;

    private String USER_AUTH_URL = null ;
    private String USER_GET_SECURITY_URL = null ;
    private String USER_EDIT_SECURITY_URL = null ;
    private String USER_CHANGE_PASSWORD_URL = null ;
    private String USER_UPDATE_URL = null ;
    private String USER_REGISTER_URL = null ;

    private Context mContext;
    private final OkHttpClient mClient;
    private final JsonParser mJsonParser;
    private static String responseMessage = null;
    private ArrayList<Member> mMembersList;

    private final String DB_NAME = "DB01";     // DB For User Member Storage

    public static HashMap<String,String> userDetails =new HashMap<>();
    public static HashMap<String,String> userSecurityDetails =new HashMap<>();

    public HttpClient(Context context) {
        mClient = new OkHttpClient.Builder().readTimeout(SOCKET_TIMEOUT, TimeUnit.SECONDS).build();
        mJsonParser = new JsonParser();
        mContext = context;
        configStorage = new ConfigStorage(mContext);
        mImageStorage = new ImageStorage(mContext);
        mSharedPrefStorage = new SharedPrefStorage(mContext);

        USER_AUTH_URL = configStorage.getConfigValue("DB00", "USER_AUTH_URL") ;
        USER_GET_SECURITY_URL = configStorage.getConfigValue("DB00", "USER_GET_SECURITY_URL") ;
        USER_EDIT_SECURITY_URL = configStorage.getConfigValue("DB00", "USER_EDIT_SECURITY_URL") ;
        USER_CHANGE_PASSWORD_URL = configStorage.getConfigValue("DB00", "USER_CHANGE_PASSWORD_URL") ;
        USER_UPDATE_URL = configStorage.getConfigValue("DB00", "USER_UPDATE_URL") ;
        USER_REGISTER_URL = configStorage.getConfigValue("DB00", "USER_REGISTER_URL") ;

    }

    public void fetchUser(final String username, final UserLoginCallback userLoginCallback) {
        String url = USER_AUTH_URL + "&email=" + username;

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String reponseString = responseBody.string();
                    if(!reponseString.contains("User not found")) {
                        userLoginCallback.setUser(getUser(reponseString));
                    }
                    userLoginCallback.setResponseMessage(reponseString);
                    userLoginCallback.run();
                }
            }
        });
    }

    public void changeUserPassword(JSONObject postBody, final UserUpdationCallback userUpdationCallback) {

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        String url = USER_CHANGE_PASSWORD_URL;
        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = postBody;

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    userUpdationCallback.setResponseMessage(responseBody.string());
                    userUpdationCallback.run();
                }
            }
        });
    }

    public void fetchUserSecurityDetails(String username, final UserSecurityCallback userSecurityCallback) {

        String url = USER_GET_SECURITY_URL + "&email=" + username;

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseDetail = responseBody.string();

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    userSecurityCallback.setResponseMessage(responseDetail);
                    userSecurityCallback.run();
                }
            }
        });
    }

    public void createUser(final JSONObject postBody, final UserCreationCallback userCreationCallback) {
        String url = USER_REGISTER_URL;

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = postBody;

        //following code decides which DB to use for member storage for user
        try {
            postdata.put("dbDetails", DB_NAME);
        } catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(" postBody ============= " + postBody);

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try (ResponseBody responseBody = response.body()) {

                    String responseData = responseBody.string();

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + responseData);
                    if(!responseData.contains("StitchError")){
                        userCreationCallback.setUser(getCreatedUser(responseData));

                    }

                    userCreationCallback.setResponseMessage(responseData);
                    userCreationCallback.setUserSecurityDetails(postBody);
                    userCreationCallback.run();

                }
            }
        });
    }

    public void updateUser(JSONObject postBody, final UserUpdationCallback userUpdationCallback) {
        String url = USER_UPDATE_URL ;

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(MEDIA_TYPE, postBody.toString());

        System.out.println(" ===================================== Member PostBody === "+ postBody.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println(" ===================================== inside failure");
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    userUpdationCallback.setResponseMessage(responseBody.string());
                    userUpdationCallback.run();
                }
            }
        });
    }


    public void updateUserSecurityDetails(JSONObject postBody, final UserSecurityCallback userSecurityCallback) {

        System.out.println(" getUserDBDetails ======================== " + "DB00");

        String url = USER_EDIT_SECURITY_URL ;

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(MEDIA_TYPE, postBody.toString());

        System.out.println(" ===================================== Member PostBody === "+ postBody.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println(" ===================================== inside failure");
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    userSecurityCallback.setResponseMessage(responseBody.string());
                    userSecurityCallback.run();
                }
            }
        });
    }

    public void fetchMembers(String userId, String userOrg, final MemberCallback memberCallback) {

        String url = configStorage.getConfigValue(mSharedPrefStorage.getUserDBDetails(), "MEMBER_GET_URL") + "&parent=" + userId + "&org_name=" + userOrg ;
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {

                    memberCallback.setResponseMessage(responseBody.string());

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    } else if(getResponseMessage().contains("Error fetching members for org")){

                        memberCallback.run();
                    }
                    else {
                        ArrayList<Member> members = getMembers(getResponseMessage());
                        Log.i(LOG_TAG, "Fetched successfully " + members.size() + " members.");

                        setMembersList(members);

                        System.out.println(" ====================================== members " + members);

                        memberCallback.setMembers(members);
                        memberCallback.run();

                    }
                }
            }
        });
    }

    public void fetchMemberImage(String parent_id, final String member_id, final MemberImageCallback memberImageCallback) {

        String url = configStorage.getConfigValue(mSharedPrefStorage.getUserDBDetails(), "MEMBER_IMAGE_GET_URL") + "&parent_id=" + parent_id + "&member_id=" + member_id ;
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {

                    String responseData = responseBody.string();

                    memberImageCallback.setResponseMessage(responseData);

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);

                    } else if(getResponseMessage().contains("Error fetching image for member :")){

                        memberImageCallback.run();
                    }
                    else if(getResponseMessage().contains("Found image for member :")){

                        Log.i(LOG_TAG, "Members Image Fetched successfully for " + member_id);

                        JsonObject results = mJsonParser.parse(responseData).getAsJsonObject();

                        JsonElement memberImage = results.getAsJsonPrimitive("result");

                        mImageStorage.saveMemberImage(member_id, memberImage.getAsString());

                        memberImageCallback.run();

                    }
                }
            }
        });
    }

    public void createMember(JSONObject postBody, final MemberCreationCallback memberCreationCallback) {

        String url = configStorage.getConfigValue(mSharedPrefStorage.getUserDBDetails(), "MEMBER_CREATE_URL") ;

        System.out.println(" getUserDBDetails ======================== " + mSharedPrefStorage.getUserDBDetails());

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(MEDIA_TYPE, postBody.toString());

        System.out.println(" ===================================== Member PostBody === "+ postBody.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println(" ===================================== inside failure");
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(" ===================================== inside success");

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                   // memberCreationCallback.setMember(getCreatedMember(response.body().string()));

                    memberCreationCallback.setResponseMessage(responseBody.string());
                    memberCreationCallback.run();
                }
            }
        });
    }


    public void updateMember(JSONObject postBody, final MemberUpdationCallback memberUpdationCallback) {
        String url = configStorage.getConfigValue(mSharedPrefStorage.getUserDBDetails(), "MEMBER_UPDATE_URL") ;

        System.out.println(" getUserDBDetails ======================== " + mSharedPrefStorage.getUserDBDetails());

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(MEDIA_TYPE, postBody.toString());

        System.out.println(" ===================================== Member PostBody === "+ postBody.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println(" ===================================== inside failure");
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(" ===================================== inside succss");

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    memberUpdationCallback.setResponseMessage(responseBody.string());
                    memberUpdationCallback.run();
                }
            }
        });
    }

    public void deleteMember(String memberId, final MemberDeletionCallback memberDeletionCallback) {
        String url = configStorage.getConfigValue(mSharedPrefStorage.getUserDBDetails(), "MEMBER_DELETE_URL") ;

        System.out.println(" getUserDBDetails ======================== " + mSharedPrefStorage.getUserDBDetails());

        final MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String postBody = "{\"member_id\":\"" + memberId +"\"}";
        RequestBody body = RequestBody.create(MEDIA_TYPE, postBody);

        System.out.println(" ===================================== Member PostBody === "+ postBody.toString());

        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println(" ===================================== inside failure");
                e.printStackTrace();
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(" ===================================== inside success");

                try (ResponseBody responseBody = response.body()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    memberDeletionCallback.setResponseMessage(responseBody.string());
                    memberDeletionCallback.run();
                }
            }
        });
    }

    private ArrayList<Member> getMembers(String jsonResponse) {

        System.out.println( " ====================== =============== Fetched Members ..." + jsonResponse);
        JsonObject results = mJsonParser.parse(jsonResponse).getAsJsonObject();

        JsonArray allMembers = results.getAsJsonArray("result");

        System.out.println(" ====================================== " + results);
        ArrayList<Member> members = new ArrayList<>();
        for (JsonElement jsonElement :allMembers) {

            JsonObject jsonMember = jsonElement.getAsJsonObject();

            JsonElement memberId = jsonMember.get("_id");
            String memberIdString = memberId.getAsString();

            JsonElement first = jsonMember.get("firstName");
            String firstName = first.getAsString();
            JsonElement last = jsonMember.get("lastName");
            String lastName = last.getAsString();
            JsonElement cycleStart = jsonMember.get("cycleStartDate");
            String startDateString = cycleStart.getAsString();
            DateTime cycleStartDate = new DateTime( startDateString );

            JsonElement cycleEnd = jsonMember.get("cycleEndDate");
            String endDateString = cycleEnd.getAsString();
            DateTime cycleEndDate = new DateTime( endDateString );

            JsonElement phone = jsonMember.get("phone");
            String phoneString = phone.getAsString();
            JsonElement email = jsonMember.get("email");
            String emailString = email.getAsString();

            JsonElement parent = jsonMember.get("parent");
            String parentString = parent.getAsString();

            JsonElement linkedTo = jsonMember.get("linkedTo");
            String linkedToString = linkedTo.getAsString();

            JsonElement address = jsonMember.get("address");
            String addressString = address.getAsString();

            String memberImageString =null;
            try {
                JsonElement memberImage = jsonMember.get("memberImage");
                memberImageString = memberImage.getAsString();
            }catch(Exception e){

                System.out.println(" ===================================== Image not present");

            }

            JsonElement memberCreation = jsonMember.get("createDate");
            String creationDateString = memberCreation.getAsString();
            DateTime memberCreationDate = new DateTime( creationDateString );

            JsonElement updateDate = jsonMember.get("updatedOn");
            String updateDateString = updateDate.getAsString();
            DateTime memberUpdateDate = new DateTime( updateDateString );

            members.add(new Member(memberIdString, firstName, lastName, cycleStartDate, cycleEndDate, phoneString, emailString, parentString, linkedToString, addressString, memberImageString,  memberCreationDate, memberUpdateDate));
        }
        return members;
    }

    private User getUser(String jsonResponse) {
        setResponseMessage(jsonResponse);
        JsonObject jsonObject = mJsonParser.parse(jsonResponse).getAsJsonObject();
        JsonArray results = jsonObject.getAsJsonArray("result");

        Iterator<JsonElement> iterator = results.iterator();
        JsonObject currObj = null;
        while(iterator.hasNext()) {
            currObj = iterator.next().getAsJsonObject();

        }

        System.out.println(" ====================================== " + jsonObject);

        JsonElement userFirstName = currObj.get("firstName");
        JsonElement userLastName = currObj.get("lastName");

        JsonElement username = currObj.get("email");
        JsonElement password = currObj.get("password");
        JsonElement user_Id = currObj.get("_id");

        JsonElement org_name = currObj.get("orgName");
        JsonElement db_name = currObj.get("dbDetails");

        saveUserDetails(username.getAsString(), user_Id.getAsString(), org_name.getAsString(), userFirstName.getAsString(), userLastName.getAsString(), db_name.getAsString());

        System.out.println(" ======= in http : password" + password.getAsString() );
        return new User(username.getAsString() , password.getAsString(), userFirstName.getAsString() );
    }

    private User getCreatedUser(String jsonResponse) {

        setResponseMessage(jsonResponse);
        JsonObject jsonObject = mJsonParser.parse(jsonResponse).getAsJsonObject();
        JsonObject results = jsonObject.getAsJsonObject("result");

        System.out.println(" ====================================== " + jsonObject);

        JsonElement username = results.get("email");
        JsonElement password = results.get("password");

        JsonElement first_name = results.get("firstName");
        JsonElement last_name = results.get("lastName");

        JsonElement user_Id = results.get("_id");
        JsonElement org_name = results.get("orgName");
        JsonElement db_name = results.get("dbDetails");
        saveUserDetails(username.getAsString(), user_Id.getAsString(), org_name.getAsString(), first_name.getAsString(), last_name.getAsString(), db_name.getAsString());

        return new User(username.getAsString() , password.getAsString(), first_name.getAsString());
    }

    private Member getCreatedMember(String jsonResponse) {

        setResponseMessage(jsonResponse);
        JsonObject jsonObject = mJsonParser.parse(jsonResponse).getAsJsonObject();
        JsonObject results = jsonObject.getAsJsonObject("result");

        System.out.println(" ====================================== " + jsonObject);

        JsonElement memberId = results.get("_id");
        String memberIdString = memberId.getAsString();

        JsonElement firstName = results.get("firstName");
        String firstNameString = firstName.getAsString();

        JsonElement lastName = results.get("lastName");
        String lastNameString = lastName.getAsString();

        JsonElement cycleStartDateElem = results.get("cycleStartDate");
        String startDateString = cycleStartDateElem.getAsString();
        DateTime cycleStartDate = new DateTime( startDateString );

        JsonElement cycleEndDateElem = results.get("cycleEndDate");
        String endDateString = cycleStartDateElem.getAsString();
        DateTime cycleEndDate = new DateTime( endDateString );

        JsonElement phone = results.get("phone");
        String phoneString = phone.getAsString();
        JsonElement email = results.get("email");
        String emailString = email.getAsString();

        JsonElement parent = results.get("parent");
        String parentString = parent.getAsString();

        JsonElement linkedTo = results.get("linkedTo");
        String linkedToString = linkedTo.getAsString();

        JsonElement address = results.get("address");
        String addressString = address.getAsString();

        String memberImageString =null;
        JsonElement memberImage = results.get("memberImage");
        memberImageString = memberImage.getAsString();

        JsonElement memberCreation = results.get("createDate");
        String creationDateString = memberCreation.getAsString();
        DateTime memberCreationDate = new DateTime( creationDateString );

        JsonElement updateDate = results.get("updatedOn");
        String updateDateString = updateDate.getAsString();
        DateTime memberUpdateDate = new DateTime( updateDateString );


        System.out.println(" ======= in http : password" + firstName.getAsString() );
        return new Member(memberIdString, firstNameString, lastNameString, cycleStartDate, cycleEndDate, phoneString, emailString, parentString, linkedToString, addressString, memberImageString, memberCreationDate, memberUpdateDate);

    }

    public abstract static class MemberCallback implements Runnable {

        protected ArrayList<Member> mMembers;

        void setMembers(ArrayList<Member> members) {
            mMembers = members;
        }

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class MemberImageCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class UserLoginCallback implements Runnable {

        protected User mUser;

        void setUser(User user) {
            mUser = user;
        }

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }


    public abstract static class UserSecurityCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class UserCreationCallback implements Runnable {

        protected User mCreatedUser;

        void setUser(User user) {
            mCreatedUser = user;
        }
        void setUserSecurityDetails(JSONObject postBody){

            try {
                saveUserSecurityDetails(postBody.get("security_question").toString(), postBody.get("security_token").toString());
            } catch (JSONException e){

                e.printStackTrace();
            }

        }
        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class UserUpdationCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class MemberCreationCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class MemberUpdationCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public abstract static class MemberDeletionCallback implements Runnable {

        void setResponseMessage(String msg) {
            HttpClient.responseMessage = msg;
        }

    }

    public String setResponseMessage(String responseBody){
            JsonObject jsonObject = mJsonParser.parse(responseBody).getAsJsonObject();

            System.out.println(" ========== jsonObject response" +  jsonObject);
            JsonElement message = jsonObject.get("message");

            System.out.println(" ========== new POST response" +  message.getAsString());
            responseMessage = message.getAsString();
            return responseMessage;
    }

    public String getResponseMessage(){
        System.out.println(" ========== getResponseMessage" +  responseMessage);
        return this.responseMessage;
    }

    public void setMembersList(ArrayList<Member> members) {
        mMembersList = members;
    }

    public ArrayList<Member> getMembersList() {
        return mMembersList;
    }

    public void saveUserDetails(String userName, String userId, String userOrgName, String userFirstName, String userLastName, String userDbName){
        userDetails.put("userFirstName", userFirstName);
        userDetails.put("userLastName", userLastName);
        userDetails.put("userName", userName);
        userDetails.put("userId", userId);
        userDetails.put("userOrgName", userOrgName);
        userDetails.put("userDBName", userDbName);

    }

    public void saveSecurityDetails(String jsonResponse) {

        System.out.println(" ====================== =============== saveSecurityDetails ..." + jsonResponse);
        JsonObject jsonObject = mJsonParser.parse(jsonResponse).getAsJsonObject();

        JsonObject result = jsonObject.getAsJsonObject("result");

        JsonElement securityQuestion = result.get("userSecurityQues");

        JsonElement securityToken = result.get("userSecurityValue");

        saveUserSecurityDetails(securityQuestion.getAsString(),securityToken.getAsString());

    }

    private static void saveUserSecurityDetails(String securityQuestion, String securityToken) {

        System.out.println(" ====================== =============== Saving saveSecurityDetails ..." + securityQuestion + " : " + securityToken);

        userSecurityDetails.put("userSecurityQuestion", securityQuestion);
        userSecurityDetails.put("userSecurityAnswer", securityToken);

    }






}
