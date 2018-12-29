package com.kushal.boulders.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.kushal.boulders.models.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends ArrayAdapter<Member> {

    private List<Member> mMembers;
    private final List<Member> mMembersCompleteSet;
    private LayoutInflater mLayoutInflater;
    private OnMemberClickCallback mOnMemberClickCallback;
    private static final int sResource = android.R.layout.select_dialog_item;

    public MemberAdapter(@NonNull Context context, List<Member> Members, OnMemberClickCallback onMemberClickCallback) {
        super(context, sResource, Members);
        mMembers = new ArrayList<>(Members);
        mMembersCompleteSet = new ArrayList<>(Members);
        mLayoutInflater = LayoutInflater.from(context);
        mOnMemberClickCallback = onMemberClickCallback;
    }

    @Override
    public int getCount() {
        return mMembers.size();
    }

    @Override
    public Member getItem(int i) {
        return mMembers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(sResource, parent, false);
        }
        Member Member = mMembers.get(position);
        convertView.setOnClickListener(new OnMemberClickListener(Member, mOnMemberClickCallback));
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(Member.getFirstName() + " " + Member.getLastName());
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Member) resultValue).getFirstName() + " " + ((Member) resultValue).getLastName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Member> suggestions = new ArrayList<>();
                if (charSequence != null) {
                    for (Member Member : mMembersCompleteSet) {
                        if (Member.getFirstName().toLowerCase().startsWith(charSequence.toString().toLowerCase()) || Member.getLastName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                            suggestions.add(Member);
                        }
                    }
                }
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mMembers.clear();
                if (filterResults != null && filterResults.count > 0) {
                    for (Object result : (List<?>) filterResults.values) {
                        if (result instanceof Member) {
                            mMembers.add((Member) result);
                        }
                    }
                    notifyDataSetChanged();
                } else {
                    mMembers.addAll(mMembersCompleteSet);
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public class OnMemberClickListener implements OnClickListener {

        private Member mMember;
        private OnMemberClickCallback mOnMemberClickCallback;

        OnMemberClickListener(Member Member, OnMemberClickCallback onMemberClickCallback) {
            mMember = Member;
            mOnMemberClickCallback = onMemberClickCallback;
        }

        @Override
        public void onClick(View view) {
            mOnMemberClickCallback.execute(mMember);
        }

    }

    public interface OnMemberClickCallback {

        void execute(Member Member);

    }

}
