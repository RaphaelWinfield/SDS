package com.example.raphaelwinfield.sixdegreesofseparation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;

import org.litepal.crud.DataSupport;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private List<Contact> mContactArrayList;
    private Context mContext;
    private ContactList mContactList;

    public ContactListAdapter(List<Contact> contactArrayList, Context context,ContactList contactList ) {
        mContactArrayList = contactArrayList;
        mContext = context;
        mContactList = contactList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
       TextView mTextView;
        ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_contact_list_name);
            mImageView = (ImageView) itemView.findViewById(R.id.iv_contact_list_photo);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_contact_list, parent, false);
        ViewHolder holder = new ViewHolder(view);

        //单点实现查看具体信息
        holder.itemView.setOnClickListener( v -> {
            int position = holder.getAdapterPosition();
            Contact contact = mContactArrayList.get(position);
            Intent intent = new Intent(mContext,ContactInfo.class);
            intent.putExtra("ContactInfo",contact);
            mContactList.startActivity(intent);
        });
        //长按实现删除联系人
        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getAdapterPosition();
            Contact contact = mContactArrayList.get(position);
            //确认删除提示框
            AlertDialog.Builder deleteWarning = new AlertDialog.Builder(mContext);
            deleteWarning.setIcon(R.drawable.ic_delete_black_24dp);
            deleteWarning.setTitle("Delete");
            deleteWarning.setMessage("Are you sure to delete your contact " + contact.getContactName() + " ?");
            deleteWarning.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });
            deleteWarning.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataSupport.deleteAll(Contact.class, "contactId = ?", contact.getContactId());
                    notifyItemRemoved(position);
                    mContactArrayList.remove(position);
                }
            });
            deleteWarning.show();
            return true;
            }
        );
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = mContactArrayList.get(position);
        holder.mTextView.setText(contact.getContactName());
        /*对于随机图片无法压缩
        Bitmap bitmap = BitmapFactory.decodeFile(contact.getContactPhoto());
        holder.mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, true));*/
        holder.mImageView.setImageURI(Uri.parse(contact.getContactPhoto()));
    }

    @Override
    public int getItemCount() {
        return mContactArrayList.size();
    }
}
