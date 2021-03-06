package com.example.raphaelwinfield.sixdegreesofseparation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.SDKInitializer;
import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;
import org.litepal.crud.DataSupport;

public class ContactInfo extends AppCompatActivity {

    public static final int CALL_PHONE = 1;
    public static final int SEND_SMS = 2;
    public static final int CONTACT_NEW_INFO = 1;
    private Contact mContact;
    private CollapsingToolbarLayout ctContactInfoName;
    private TextView tvContactInfoPhoneNumber;
    private TextView tvContactInfoAddress;
    private TextView tvContactInfoRingtone;
    private ImageView ivContactInfoPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());//必须放在下面语句的上面；若放在CA类中时会存在地图显示延迟
        setContentView(R.layout.activity_contact_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContact = (Contact) getIntent().getParcelableExtra("ContactInfo");
        ctContactInfoName = (CollapsingToolbarLayout) findViewById(R.id.toolbar_contact_info);
        tvContactInfoPhoneNumber = (TextView) findViewById(R.id.tv_contact_info_phone_number);
        tvContactInfoAddress = (TextView) findViewById(R.id.tv_contact_info_address);
        tvContactInfoRingtone = (TextView) findViewById(R.id.tv_contact_info_ringtone);
        ivContactInfoPhoto = (ImageView) findViewById(R.id.iv_contact_info_photo);

        ctContactInfoName.setTitle(mContact.getContactName());
        tvContactInfoPhoneNumber.setText("Phone Number: " + mContact.getContactPhoneNumber());
        tvContactInfoAddress.setText("Address: " + mContact.getContactAdress());
        if ( mContact.getContactRingtone() == null || mContact.getContactRingtone().equals("") ){
            tvContactInfoRingtone.setVisibility(View.GONE);
        } else{
            tvContactInfoRingtone.setVisibility(View.VISIBLE);
            tvContactInfoRingtone.setText("Ringtone: " + mContact.getContactRingtone());
        }
        ivContactInfoPhoto.setImageURI(Uri.parse(mContact.getContactPhoto()));

        //点击编辑按钮
        FloatingActionButton fabEdit = (FloatingActionButton) findViewById(R.id.fab_contact_info_edit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_Edit = new Intent(ContactInfo.this,EditContact.class);
                intent_Edit.putExtra("EditContact",mContact);
                startActivityForResult(intent_Edit, CONTACT_NEW_INFO);
            }
        });

        //点击删除按钮
        FloatingActionButton fabDelete = (FloatingActionButton) findViewById(R.id.fab_contact_info_delete);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSupport.deleteAll(Contact.class, "contactId = ?", mContact.getContactId());
                finish();
            }
        });

        //点击电话按钮
        ImageView ivCall = (ImageView) findViewById(R.id.iv_contact_info_call);
        ivCall.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ContactInfo.this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContactInfo.this,
                        new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE);
            } else {
                Intent intent_callphone = new Intent(Intent.ACTION_CALL);
                intent_callphone.setData(Uri.parse("tel:" + mContact.getContactPhoneNumber()));
                startActivity(intent_callphone);
            }
        });

        //点击短信按钮
        ImageView ivMessage = (ImageView) findViewById(R.id.iv_contact_info_message);
        ivMessage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ContactInfo.this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContactInfo.this,
                        new String[]{Manifest.permission.SEND_SMS}, SEND_SMS);
            } else {
                Intent intent_sendsms = new Intent(Intent.ACTION_SENDTO);
                intent_sendsms.setData(Uri.parse("smsto:" + mContact.getContactPhoneNumber()));
                startActivity(intent_sendsms);
            }
        });

        //点击地图按钮
        ImageView ivAddress = (ImageView) findViewById(R.id.iv_contact_info_address);
        ivAddress.setOnClickListener(v -> {
            Intent intent_Address = new Intent(ContactInfo.this,ContactAddress.class);
            intent_Address.putExtra("ContactAddress",mContact);
            startActivity(intent_Address);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CONTACT_NEW_INFO:
                //显示编辑后的联系人信息,图片存在异常
                if (resultCode == RESULT_OK){
                    mContact = (Contact) data.getParcelableExtra("ContactNewInfo");
                    ctContactInfoName.setTitle(mContact.getContactName());
                    tvContactInfoPhoneNumber.setText("Phone Number: " + mContact.getContactPhoneNumber());
                    tvContactInfoAddress.setText("Address: " + mContact.getContactAdress());
                    if ( mContact.getContactRingtone() == null || mContact.getContactRingtone().equals("") ){
                        tvContactInfoRingtone.setVisibility(View.GONE);
                    } else{
                        tvContactInfoRingtone.setVisibility(View.VISIBLE);
                        tvContactInfoRingtone.setText("Ringtone: " + mContact.getContactRingtone());
                    }
                    ivContactInfoPhoto.setImageURI(Uri.parse(mContact.getContactPhoto()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CALL_PHONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent_callphone = new Intent(Intent.ACTION_CALL);
                    intent_callphone.setData(Uri.parse("tel:" + mContact.getContactPhoneNumber()));
                    startActivity(intent_callphone);
                } else {
                    Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent_sendsms = new Intent(Intent.ACTION_SENDTO);
                    intent_sendsms.setData(Uri.parse("smsto:" + mContact.getContactPhoneNumber()));
                    startActivity(intent_sendsms);
                } else {
                    Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
