package com.kushal.boulders.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kushal.boulders.App;
import com.kushal.boulders.R;
import com.kushal.boulders.dependencies.component.AppComponent;
import com.kushal.boulders.models.Member;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.ImageStorage;

import java.text.SimpleDateFormat;

import javax.inject.Inject;

import static com.kushal.boulders.misc.Constants.LOG_TAG;


public class MemberProfileActivity extends AppCompatActivity {

    @Inject
    HttpClient mHttpClient;

    @Inject
    ImageStorage imageStorage;

    private Member mMember;
    private View snackview;

    private static final String EXTRA_MEMBER = "Member";

    public static Intent createIntent(Activity activity, Member Member) {
        Intent intent = new Intent(activity, MemberProfileActivity.class);
        intent.putExtra(EXTRA_MEMBER, Member);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);
        AppComponent appComponent = App.getApplicationContext(this).getAppComponent();
        appComponent.inject(this);

        snackview = findViewById(android.R.id.content);
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
        mMember = intent.getParcelableExtra(EXTRA_MEMBER);
        setProfile(mMember);

        FloatingActionButton editMember = findViewById(R.id.fab_editMember);

        editMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(" ====================================== Launching Edit Member Profile =========================================");
                Intent intent = new Intent(MemberProfileActivity.this, EditMemberProfileActivity.class);
                intent.putExtra(EXTRA_MEMBER, mMember);
                startActivity(intent);


            }
        });

        FloatingActionButton deleteMember = findViewById(R.id.fab_deleteMember);

        deleteMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(" ====================================== Launching Delete Member  =========================================");

                showDeleteConfirmationDialog();

            }
        });
    }

    private void deleteMember(final Member mCurrMember) {

        System.out.println(" ====================================== in trying to delete member : " );

        mHttpClient.deleteMember(mCurrMember.getMemberId(), new HttpClient.MemberDeletionCallback() {
                @Override
                public void run() {

                    if (mHttpClient.getResponseMessage().contains("Member removed successfully!!")) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String toastMsg = "Member has been removed";
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                        imageStorage.resetMemberImage(mMember.getMemberId());

                        startActivity(intent);
                        finish();


                        Log.i(LOG_TAG, "Member details removal is Successful");

                    } else {

                        Snackbar.make(snackview, R.string.message_member_creation_failed, Snackbar.LENGTH_LONG).show();
                        Log.i(LOG_TAG, "Not able to remove member at this time");
                    }
                }
            });
        }

    private void setProfile(final Member Member) {

        ImageView memberAvatar = findViewById(R.id.iv_memberAvatar);
        TextView memberFirstName = findViewById(R.id.tv_memberFirstName);
        TextView memberLastName = findViewById(R.id.tv_memberLastName);
        TextView memberPhone = findViewById(R.id.tv_memberPhone);
        TextView memberEmail = findViewById(R.id.tv_memberEmail);
        TextView memberAddress = findViewById(R.id.tv_memberAddress);
        TextView memberCycleStartDate = findViewById(R.id.tv_memberCycleStartDate);
        TextView memberCycleEndDate = findViewById(R.id.tv_memberCycleEndDate);

        memberFirstName.setText(Member.getFirstName());
        memberLastName.setText(Member.getLastName());
        memberPhone.setText(Member.getPhone());
        memberEmail.setText(Member.getEmail());
        memberCycleStartDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Member.getCycleStartDate()));
        memberCycleEndDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(Member.getCycleEndDate()));
        memberAddress.setText(Member.getAddress());

        System.out.println(" ===================================== Member.getMemberImage()" + Member.getMemberImage() );

        if(imageStorage.getMemberImage(Member.getMemberId()) != null && !imageStorage.getMemberImage(Member.getMemberId()).isEmpty()) {
            Bitmap imageBitmap = decodeBase64(imageStorage.getMemberImage(Member.getMemberId()));

            memberAvatar.setImageBitmap(imageBitmap);
        }

    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i= new Intent(MemberProfileActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MemberProfileActivity.this);
        builder.setTitle("Remove Member Confirmation");
        builder.setMessage("This will remove Member details forever. Are you sure you want to delete this member?");

        String positiveText = getString(android.R.string.yes);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        deleteMember(mMember);

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

}
