package com.example.raphaelwinfield.sixdegreesofseparation.db;

import android.os.Parcelable;
import org.litepal.crud.DataSupport;

public class Contact extends DataSupport implements Parcelable {

    private String contactId;
    private String contactName;
    private String contactPhoneNumber;
    private String contactAdress;
    private String contactPhoto;



}
