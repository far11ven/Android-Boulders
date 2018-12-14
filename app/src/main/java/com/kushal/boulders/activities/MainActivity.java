package com.kushal.boulders.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.adapters.MemberAdapter;
import com.kushal.boulders.adapters.MemberRecycleViewAdapter;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.models.Member;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.ConfigStorage;
import com.kushal.boulders.utils.storage.ImageStorage;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import static com.kushal.boulders.misc.Constants.LOG_TAG;

public class MainActivity extends AuthenticatedActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Inject
    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    @Inject
    ImageStorage mImageStorage;

    @Inject
    ConfigStorage mConfigStorage;

    private static final int STORAGE_PERMISSION_CODE = 123;

    private RelativeLayout mProgressBar;
    private AutoCompleteTextView mSearchView;
    private MemberAdapter mAdapter;
    private RecyclerView mRecyclerViewAllMembers;
    private RecyclerView.Adapter mRecycleViewAdapterAllMembers;
    private RecyclerView mRecyclerViewUpcomingMembers;
    private RecyclerView.Adapter mRecycleViewAdapterUpcomingMembers;
    private TabHost mTabHost;

    private List<Member> memberListItems;
    private List<Member> upcomingMemberListItems;

    public static Intent createIntent(Activity activity) {
        return new Intent(activity, MainActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestStoragePermission();
        getMemebersToSearch();

        if(mSharedPrefStorage.getUserId() == null){
            saveLoggedInUser();
        }

        if (!isAuthenticated()) {
            startActivity(LoginActivity.createIntent(MainActivity.this));

        } else {

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            NavigationView nav = drawer.findViewById(R.id.nav_view);
            ((TextView) nav.getHeaderView(0).findViewById(R.id.nav_username)).setText("Hi, " + mSharedPrefStorage.getUser().getUserName());

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        mProgressBar = findViewById(R.id.rl_progressBar);
        mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddMemberActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSearchView = findViewById(R.id.atv_search);
        mSearchView.setDropDownAnchor(R.id.cv_searchContainer);

        mRecyclerViewAllMembers = findViewById(R.id.rv_membersList);
        mRecyclerViewAllMembers.setHasFixedSize(true);
        mRecyclerViewAllMembers.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerViewUpcomingMembers = findViewById(R.id.rv_UpcomingMembersList);
        mRecyclerViewUpcomingMembers.setHasFixedSize(true);
        mRecyclerViewUpcomingMembers.setLayoutManager(new LinearLayoutManager(this));

        memberListItems = new ArrayList<>();
        upcomingMemberListItems = new ArrayList<>();


        mTabHost =  findViewById(R.id.tabHost);
        mTabHost.setup();

        TabHost.TabSpec allMembersTab = mTabHost.newTabSpec("allMembers");
        allMembersTab.setContent(R.id.tab1);
        allMembersTab.setIndicator("All");

        TabHost.TabSpec upcomingMembersTab = mTabHost.newTabSpec("upcoming");
        upcomingMembersTab.setContent(R.id.tab2);
        upcomingMembersTab.setIndicator("Due");

        mTabHost.addTab(allMembersTab);
        mTabHost.addTab(upcomingMembersTab);

        getMemebersToSearch();


    }

    public void getMemebersToSearch(){

        if(mSharedPrefStorage.getUserId() == null){
            saveLoggedInUser();
        }

        System.out.println(" ====================================== Trying to fetch members for " + mSharedPrefStorage.getUserId() + mSharedPrefStorage.getUserId());

        mHttpClient.fetchMembers( mSharedPrefStorage.getUserId(),  mSharedPrefStorage.getUserOrg(), new HttpClient.MemberCallback() {
            @Override
            public void run() {

                if (mHttpClient.getResponseMessage().contains("Found members for org")) {

                    System.out.println(" ====================================== Trying to fetch Members");
                    mAdapter = new MemberAdapter(MainActivity.this, mMembers, new MemberAdapter.OnMemberClickCallback() {
                        @Override
                        public void execute(Member Member) {
                            startActivity(MemberProfileActivity.createIntent(MainActivity.this, Member));
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            System.out.println(" ====================================== Setting adapter");
                            mSearchView.setAdapter(mAdapter);

                            getMemebersList();

                        }
                    });
                } else if (mHttpClient.getResponseMessage().contains("No members found for org")){

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "You do not have any members yet", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });


                } else {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity. this, "Error connecting to server..", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            }
        });
    }

    public void getMemebersList(){

        mRecycleViewAdapterAllMembers=null;
        mRecycleViewAdapterUpcomingMembers=null;

        mRecyclerViewAllMembers.setAdapter(mRecycleViewAdapterAllMembers);
        mRecyclerViewUpcomingMembers.setAdapter(mRecycleViewAdapterUpcomingMembers);

        memberListItems = mHttpClient.getMembersList();

        Iterator<Member> iterator = memberListItems.iterator();

        while (iterator.hasNext()) {

            Member currMember = iterator.next();

            if (currMember.getCycleEndDate().before(new DateTime().plusDays(3).toDate())) {
                upcomingMemberListItems.add(currMember);
                System.out.println(currMember.getCycleStartDate() + " ================================ adding members to upcoming" + currMember.getCycleEndDate().before(new DateTime().plusMonths(1).toDate()));


            }
        }

        System.out.println(" ====================================== Trying to fetch Members forRecyclerVIEW :" + memberListItems);

        mRecycleViewAdapterAllMembers = new MemberRecycleViewAdapter(memberListItems, this);   // for all members
        mRecycleViewAdapterUpcomingMembers = new MemberRecycleViewAdapter(upcomingMemberListItems, this);

        mRecyclerViewAllMembers.setAdapter(mRecycleViewAdapterAllMembers);
        mRecyclerViewUpcomingMembers.setAdapter(mRecycleViewAdapterUpcomingMembers);


        System.out.println(" ====================================== setting adapter forRecyclerVIEW COMPLETE");

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
       super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id){

            case R.id.nav_logout :
                mSharedPrefStorage.resetStorage();
                mImageStorage.resetStorage();
                Log.i(LOG_TAG, "User is logged out");
                startActivity(LoginActivity.createIntent(MainActivity.this));
                break;

            case R.id.nav_about :
                Log.i(LOG_TAG, "User is on about");
                startActivity(AboutActivity.createIntent(MainActivity.this));

                break;


            case R.id.nav_editProfile :
                Log.i(LOG_TAG, "User is on Edit User Profile");
                startActivity(UserProfileActivity.createIntent(MainActivity.this));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //saving User (Logged in) to SahredPref & display on navbar
    public void saveLoggedInUser(){

        mSharedPrefStorage.saveUserId(mHttpClient.userDetails.get("userId"));
        mSharedPrefStorage.saveUserDBDetails(mHttpClient.userDetails.get("userDBName"));
        mSharedPrefStorage.saveUserOrg(mHttpClient.userDetails.get("userOrgName"));
        mSharedPrefStorage.saveUserFirstName(mHttpClient.userDetails.get("userFirstName"));
        mSharedPrefStorage.saveUserLastName(mHttpClient.userDetails.get("userLastName"));
        mSharedPrefStorage.saveUserSecurityQuestion(mHttpClient.userSecurityDetails.get("userSecurityQuestion"));
        mSharedPrefStorage.saveUserSecurityAnswer(mHttpClient.userSecurityDetails.get("userSecurityAnswer"));

    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Now you can add images to your member profile", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops! You won't be able to add images to your member profile", Toast.LENGTH_LONG).show();
            }
        }
    }


}
