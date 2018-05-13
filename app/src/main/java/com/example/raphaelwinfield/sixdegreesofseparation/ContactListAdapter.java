package com.example.raphaelwinfield.sixdegreesofseparation;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> mContactList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View contactView;
        TextView contactItemName;

        public ViewHolder(View itemView) {
            super(itemView);
            contactView = itemView;
            contactItemName = (TextView) itemView.findViewById(R.id.i);
        }
    }

    public ContactsAdapter(List<Contacts> contactsList,Context context) {
        mContactsList = contactsList;
        mContext = context;
        // mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.contact_item, parent, false);
        ViewHolder holder = new ViewHolder(view);   //书上加了final关键字
        holder.contactView.setOnClickListener( v -> {
            int position = holder.getAdapterPosition();
            Contacts contacts = mContactsList.get(position);
            /*Toast.makeText(v.getContext(),contacts.getNumber(),Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent(mContext,ScrollingActivity.class);
            /*intent.putExtra("name",contacts.getName());
            intent.putExtra("number",contacts.getNumber());*/
            intent.putExtra("contacts",contacts);
            mContext.startActivity(intent);
            //mActivity.finish();
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contacts contacts = mContactsList.get(position);
        holder.contactItemName.setText(contacts.getName());
    }

    @Override
    public int getItemCount() {
        return mContactsList.size();
    }

}
