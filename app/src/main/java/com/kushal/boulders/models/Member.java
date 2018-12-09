package com.kushal.boulders.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.joda.time.DateTime;

import java.util.Date;

public class Member implements Parcelable {

    private String mMeberId;
    private String mFirstName;
    private String mLastName;
    private Date mCycleStartDate;
    private Date mCycleEndDate;
    private String mPhone;
    private String mEmail;
    private String mParent;
    private String mLinkedTo;
    private String mAddress;

    private String mMemberImage;
    private Date mCreationDate;
    private Date mlastUpdateDate;

    public Member(String memberId, String firstName, String lastName, DateTime cycleStartDate, DateTime cycleEndDate, String phone, String email,
                            String parent, String linkedTo, String address, String memberImage, DateTime creationDate, DateTime lastUpdateDate) {
        mMeberId = memberId;
        mFirstName = firstName;
        mLastName = lastName;
        mCycleStartDate = cycleStartDate.toDate();
        mCycleEndDate = cycleEndDate.toDate();
        mPhone = phone;
        mEmail = email;
        mParent = parent;
        mLinkedTo = linkedTo;
        mAddress = address;
        mMemberImage = memberImage;
        mCreationDate = creationDate.toDate();
        mlastUpdateDate = lastUpdateDate.toDate();

    }

    private Member(Parcel parcel) {
        mMeberId = parcel.readString();
        mFirstName = parcel.readString();
        mLastName = parcel.readString();
        mCycleStartDate = new Date(parcel.readLong());
        mCycleEndDate = new Date(parcel.readLong());
        mPhone = parcel.readString();
        mEmail = parcel.readString();
        mParent = parcel.readString();
        mLinkedTo = parcel.readString();
        mAddress = parcel.readString();
        mMemberImage = parcel.readString();
        mCreationDate = new Date(parcel.readLong());
        mlastUpdateDate = new Date(parcel.readLong());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mMeberId);
        parcel.writeString(mFirstName);
        parcel.writeString(mLastName);
        parcel.writeLong(mCycleStartDate.getTime());
        parcel.writeLong(mCycleEndDate.getTime());
        parcel.writeString(mPhone);
        parcel.writeString(mEmail);
        parcel.writeString(mParent);
        parcel.writeString(mAddress);
        parcel.writeString(mAddress);
        parcel.writeString(mMemberImage);
        parcel.writeLong(mCreationDate.getTime());
        parcel.writeLong(mlastUpdateDate.getTime());
    }

    public String getMemberId() {
        return mMeberId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public Date getCycleStartDate() { return mCycleStartDate;}

    public Date getCycleEndDate() { return mCycleEndDate;}

    public String getPhone() {
        return mPhone;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getParent() {
        return mParent;
    }

    public String getMemberImage() {
        return mMemberImage;
    }

    public String getLinkedTo() {
        return mLinkedTo;
    }

    public Date getCreationDate() { return mCreationDate;}
    public Date getLatUpdateDate() { return mlastUpdateDate;}

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel parcel) {
            return new Member(parcel);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void setMemberImage(String val) {
        this.mMemberImage = val;
    }

    public void removeMemberImage(){

        this.mMemberImage = null;

    }


}
