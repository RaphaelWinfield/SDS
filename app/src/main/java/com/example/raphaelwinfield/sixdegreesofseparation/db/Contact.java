package com.example.raphaelwinfield.sixdegreesofseparation.db;

import android.os.Parcel;
import android.os.Parcelable;
import org.litepal.crud.DataSupport;
import java.util.Random;

public class Contact extends DataSupport implements Parcelable {

    private String contactId;
    private String contactName;
    private String contactPhoneNumber;
    private String contactAdress;
    private String contactPhoto;
    private String contactRingtone;

    public void setContactId() {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 2; i++) {
            sb.append(String.valueOf(str.charAt(new Random().nextInt(26))));
        }
        contactId = sb.toString();
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public void setContactAdress(String contactAdress) {
        this.contactAdress = contactAdress;
    }

    public void setContactPhoto(String contactPhoto) {
        this.contactPhoto = contactPhoto;
    }

    public void setContactRingtone(String contactRingtone) {
        this.contactRingtone = contactRingtone;
    }

    public String getContactId() {
        return contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public String getContactAdress() {
        return contactAdress;
    }

    public String getContactPhoto() {
        return contactPhoto;
    }

    public String getContactRingtone() {
        return contactRingtone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contactId);
        dest.writeString(contactName);
        dest.writeString(contactPhoneNumber);
        dest.writeString(contactAdress);
        dest.writeString(contactPhoto);
        dest.writeString(contactRingtone);
    }

    public static final  Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>(){
        @Override
        public Contact createFromParcel(Parcel source) {
            Contact contact = new Contact();
            //注意读写顺序一致
            contact.contactId = source.readString();
            contact.contactName = source.readString();
            contact.contactPhoneNumber = source.readString();
            contact.contactAdress = source.readString();
            contact.contactPhoto = source.readString();
            contact.contactRingtone = source.readString();
            return contact;
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
