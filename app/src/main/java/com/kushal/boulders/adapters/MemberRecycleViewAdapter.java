package com.kushal.boulders.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kushal.boulders.R;
import com.kushal.boulders.activities.MainActivity;
import com.kushal.boulders.activities.MemberProfileActivity;
import com.kushal.boulders.models.Member;
import com.kushal.boulders.utils.network.HttpClient;
import com.kushal.boulders.utils.storage.ImageStorage;
import com.kushal.boulders.utils.storage.SharedPrefStorage;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;


public class MemberRecycleViewAdapter extends RecyclerView.Adapter<MemberRecycleViewAdapter.ViewHolder> {

    private List<Member> mListItems;
    private Context context;

    ImageStorage imageStorage;

    HttpClient mHttpClient;

    @Inject
    SharedPrefStorage mSharedPrefStorage;

    Bitmap imageBitmap;


    public MemberRecycleViewAdapter(List<Member> listItems, Context context) {
        this.mListItems = listItems;
        this.context = context;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mMemberAvatar;
        public TextView mFirstName;
        public TextView mLastName;
        public TextView mCycleEndDate;

        public TextView mMemberStatus;


        public ViewHolder(View itemView) {

            super(itemView);
            mMemberAvatar = itemView.findViewById(R.id.iv_MemberAvatar);
            mFirstName = itemView.findViewById(R.id.tv_MemberFirstName);
            mLastName = itemView.findViewById(R.id.tv_MemberLastName);
            mCycleEndDate = itemView.findViewById(R.id.tv_MemberCycleEndDate);
            mMemberStatus = itemView.findViewById(R.id.tv_MemberStatus);


        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_listeitem, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Member memberItem = mListItems.get(position);


        mHttpClient = new HttpClient(context);
        imageStorage = new ImageStorage(context);


        try {
            imageBitmap = decodeBase64(imageStorage.getMemberImage(memberItem.getMemberId()));
            holder.mMemberAvatar.setImageBitmap(imageBitmap);

        } catch (Exception e) {

            holder.mMemberAvatar.setImageResource(R.drawable.ic_member);
        }

        //imageStorage.resetMemberImage(memberItem.getMemberId());
        // imageStorage.saveMemberImage(memberItem.getMemberId(), memberItem.getMemberImage());

        holder.mFirstName.setText(memberItem.getFirstName());
        holder.mLastName.setText(memberItem.getLastName());
        holder.mCycleEndDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(memberItem.getCycleEndDate()));

        DateTime now = new DateTime();
        now.toDate().setTime(0000000000000L);
        String currentDate  = now.toString().split("T")[0];

        if (new SimpleDateFormat("yyyy-MM-dd").format(memberItem.getCycleEndDate()).equals(currentDate)) {
            holder.mMemberStatus.setTextAppearance(context, R.style.MemberItemText_DUE);
            holder.mMemberStatus.setText("DUE TODAY");

        } else if (memberItem.getCycleEndDate().before(new DateTime().plusDays(1).toDate())) {

            if (memberItem.getCycleEndDate().before(new DateTime().toDate())) {

                    holder.mMemberStatus.setTextAppearance(context, R.style.MemberItemText_OVERDUE);
                    holder.mMemberStatus.setText("OVERDUE");
            } else {

                holder.mMemberStatus.setTextAppearance(context, R.style.MemberItemText_DUE);
                holder.mMemberStatus.setText("DUE TOMORROW");
            }

        } else {
            holder.mMemberStatus.setTextAppearance(context, R.style.MemberItemText_SETTLED);
            holder.mMemberStatus.setText("SETTLED");

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("Member", memberItem);
                memberItem.removeMemberImage();
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {

        return mListItems.size();
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
