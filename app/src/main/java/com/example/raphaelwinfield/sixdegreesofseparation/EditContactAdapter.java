package com.example.raphaelwinfield.sixdegreesofseparation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class EditContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum ITEM_TYPE {
        ITEM_TYPE_IMAGE,
        ITEM_TYPE_TEXT
    }

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private Contact mContact;
    private String[] mTitles;
    private EditContact mEditContact;

    public EditContactAdapter(String[] titles, Contact contact, Context context, EditContact editContact) {
        mTitles = titles;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mContact = contact;
        mEditContact = editContact;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (ITEM_TYPE.values()[viewType]) {
            case ITEM_TYPE_IMAGE:
                return new ImageViewHolder(mLayoutInflater
                        .inflate(R.layout.item_image_contact, parent, false));
            case ITEM_TYPE_TEXT:
                return new TextViewHolder(mLayoutInflater
                        .inflate(R.layout.item_text_contact, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).mImageView.setImageURI(Uri.parse(mContact.getContactPhoto()));
            mEditContact.setImageView(((ImageViewHolder) holder).getImageView());
            ((ImageViewHolder) holder).mImageView.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setIcon(R.drawable.ic_insert_photo_black_24dp);
                builder.setTitle("Personal Photo");
                final String[] AddPhoto = {"Random Photo", "Take Photo", "Choose Photo"};
                builder.setItems(AddPhoto, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mContact.setContactPhoto("android.resource://com.example.raphaelwinfield.sixdegreesofseparation/drawable/photo_" + String.valueOf(new Random().nextInt(8) + 1));
                                ((ImageViewHolder) holder).mImageView.setImageURI(Uri.parse(mContact.getContactPhoto()));
                                break;
                            case 1:
                                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(mEditContact, new String[]{Manifest.permission.CAMERA}, mEditContact.TAKE_PHOTO);
                                } else {
                                    File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
                                    new File(mContact.getContactPhoto()).delete();
                                    try {
                                        if (outputImage.exists()) {
                                            outputImage.delete();
                                        }
                                        outputImage.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // 启动相机程序
                                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputImage));
                                    mEditContact.startActivityForResult(intent, mEditContact.TAKE_PHOTO);
                                    mEditContact.setImageUri(Uri.fromFile(outputImage));
                                }
                                break;
                            case 2:
                                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(mEditContact, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, mEditContact.CHOOSE_PHOTO);
                                } else {
                                    new File(mContact.getContactPhoto()).delete();
                                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                                    intent.setType("image/*");
                                    mEditContact.startActivityForResult(intent, mEditContact.CHOOSE_PHOTO);
                                }
                                break;
                            default:
                                break;
                        }
                        ;
                    }
                });
                builder.show();
            });
        } else if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).mTextView.setText(mTitles[position]);
            switch (position) {
                case 1:
                    ((TextViewHolder) holder).mEditText.setText(mContact.getContactName());
                    break;
                case 2:
                    ((TextViewHolder) holder).mEditText.setText(mContact.getContactPhoneNumber());
                break;
                case 3:
                    ((TextViewHolder) holder).mEditText.setText(mContact.getContactAdress());
                    break;
                case 4:
                    ((TextViewHolder) holder).mEditText.setText(mContact.getContactRingtone());
                    break;
                default:
                    break;
            }
            /*if (((TextViewHolder) holder).mEditText.getTag() instanceof TextWatcher) {
                ((TextViewHolder) holder).mEditText.removeTextChangedListener((TextWatcher) holder.mEditText.getTag());
            }*/
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    switch (position) {
                        case 1:
                            mContact.setContactName(((TextViewHolder) holder).mEditText.getText().toString());
                            break;
                        case 2:
                            mContact.setContactPhoneNumber(((TextViewHolder) holder).mEditText.getText().toString());
                            break;
                        case 3:
                            mContact.setContactAdress(((TextViewHolder) holder).mEditText.getText().toString());
                            break;
                        case 4:
                            mContact.setContactRingtone(((TextViewHolder) holder).mEditText.getText().toString());
                            break;
                        default:
                            break;
                    }
                }
            };
            ((TextViewHolder) holder).mEditText.addTextChangedListener(watcher);
            ((TextViewHolder) holder).mEditText.setTag(watcher);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal();
        } else if (position < 5 && position > 0) {
            return ITEM_TYPE.ITEM_TYPE_TEXT.ordinal();
        } else {
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.iv_contact);
        }

        public ImageView getImageView() {
            return mImageView;
        }
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;
        EditText mEditText;

        public TextViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_contact_name);
            mEditText = (EditText) itemView.findViewById(R.id.et_contact_name);
        }
    }

    public Contact getContact() {
        return mContact;
    }
}