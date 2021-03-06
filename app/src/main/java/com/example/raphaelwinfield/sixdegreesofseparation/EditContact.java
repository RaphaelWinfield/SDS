package com.example.raphaelwinfield.sixdegreesofseparation;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditContact extends AppCompatActivity {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_PHOTO = 3;
    private String[] mTitle = { null, "Name", "PhoneNumber", "Address", "Ringtone"};
    private Contact mContact;
    private ImageView mImageView;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_edit_contact);
        setSupportActionBar(toolbar);

        Intent intent_Edit = getIntent();
        mContact = (Contact) intent_Edit.getParcelableExtra("EditContact");
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_edit_contact);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        EditContactAdapter editContactAdapter = new EditContactAdapter(mTitle, mContact,EditContact.this, this);
        recyclerView.setAdapter(editContactAdapter);
        mContact = editContactAdapter.getContact();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_edit_contact, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.ic_done:
                if ( mContact.getContactName() == null || mContact.getContactName().equals("") ){
                    Toast.makeText(this, "Name can't be empty.", Toast.LENGTH_SHORT).show();
                } else if (mContact.getContactPhoneNumber() == null || mContact.getContactPhoneNumber().equals("")){
                    Toast.makeText(this, "PhoneNumber can't be empty.", Toast.LENGTH_SHORT).show();
                } else if (isNumeric(mContact.getContactPhoneNumber())){
                    mContact.updateAll("contactId = ?", mContact.getContactId());
                    Intent intent_NewInfo = new Intent();
                    intent_NewInfo.putExtra("ContactNewInfo", mContact);
                    setResult(RESULT_OK, intent_NewInfo);
                    finish();
                } else{
                    Toast.makeText(this, "PhoneNumber must be number.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ic_close:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    File cropImage = new File(Environment.getExternalStorageDirectory(), "o_crop_image.jpg");
                    try {
                        if (cropImage.exists()) {
                            cropImage.delete();
                        }
                        cropImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Uri cropUri = Uri.fromFile(cropImage);
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mImageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
                    startActivityForResult(intent, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = null;
                    File cropImage = new File(Environment.getExternalStorageDirectory(), "o_crop_image.jpg");
                    Uri uri = Uri.fromFile(cropImage);
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        // 如果是document类型的Uri，则通过document id处理
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1]; // 解析出数字格式的id
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                            imagePath = getImagePath(contentUri, null);
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        // 如果是content类型的Uri，则使用普通方式处理
                        imagePath = getImagePath(uri, null);
                    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                        // 如果是file类型的Uri，直接获取图片路径即可
                        imagePath = uri.getPath();
                    }
                    if (imagePath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        mImageView.setImageBitmap(bitmap);
                        try {
                            mContact.setContactPhoto("/data/data/com.example.raphaelwinfield.sixdegreesofseparation/files/"
                                    + mContact.getContactName() + "_" + mContact.getContactPhoneNumber());
                            File oldfile = new File(imagePath);
                            InputStream inputStream = new FileInputStream(oldfile); //读入原文件
                            FileOutputStream fileOutputStream = new FileOutputStream(mContact.getContactPhoto());
                            byte[] buffer = new byte[1024];
                            while ( inputStream.read(buffer) != -1) {
                                fileOutputStream.write(buffer);
                            }
                            inputStream.close();
                            cropImage.delete();
                            File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
                            outputImage.delete();
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = null;
                    Uri uri = data.getData();
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        // 如果是document类型的Uri，则通过document id处理
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1]; // 解析出数字格式的id
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                            imagePath = getImagePath(contentUri, null);
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        // 如果是content类型的Uri，则使用普通方式处理
                        imagePath = getImagePath(uri, null);
                    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                        // 如果是file类型的Uri，直接获取图片路径即可
                        imagePath = uri.getPath();
                    }
                    if (imagePath != null) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        mImageView.setImageBitmap(bitmap);
                        try {
                            mContact.setContactPhoto("/data/data/com.example.raphaelwinfield.sixdegreesofseparation/files/"
                                    + mContact.getContactName() + "_" + mContact.getContactPhoneNumber());
                            File oldfile = new File(imagePath);
                            InputStream inputStream = new FileInputStream(oldfile); //读入原文件
                            FileOutputStream fileOutputStream = new FileOutputStream(mContact.getContactPhoto());
                            byte[] buffer = new byte[1024];
                            while ( inputStream.read(buffer) != -1) {
                                fileOutputStream.write(buffer);
                            }
                            inputStream.close();
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You have had the permission of camera.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You denied the permission of camera.", Toast.LENGTH_SHORT).show();
                }
                break;

            case CHOOSE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You have had the permission of storage.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "You denied the permission of storage.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public void setImageUri(Uri imageUri) {
        mImageUri = imageUri;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder saveWarning = new AlertDialog.Builder(EditContact.this);
        saveWarning.setIcon(R.drawable.ic_save_black_24dp);
        saveWarning.setTitle("Save");
        saveWarning.setMessage("Discard your changes and quit editing?");
        saveWarning.setPositiveButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        saveWarning.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        saveWarning.show();
    }

    public boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
