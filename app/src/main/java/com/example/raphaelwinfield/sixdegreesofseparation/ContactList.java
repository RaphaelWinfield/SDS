package com.example.raphaelwinfield.sixdegreesofseparation;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;
import com.facebook.stetho.Stetho;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactList extends AppCompatActivity {

    private boolean mSearch = true;
    private EditText etSearch;
    private List<Contact> mContactArrayList = new ArrayList<>();
    private long exitTime = 0;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ContactListAdapter contactListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        Stetho.initializeWithDefaults(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contact List");
        etSearch = (EditText) findViewById(R.id.et_contact_serach);
        recyclerView = (RecyclerView) findViewById(R.id.rv_contact_list);

        ContactList contactList = this;
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                mContactArrayList.clear();
                if ( etSearch.getText().toString().trim().equals("")){
                    mContactArrayList = DataSupport.order("contactName asc").find(Contact.class);
                } else {
                    /*mContactArrayList.add(DataSupport.findFirst(Contact.class));*/
                    mContactArrayList = DataSupport.where("contactName like ? or contactPhoneNumber like ? or contactAdress like ? ",
                            "%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%")
                            .order("contactName asc")
                            .find(Contact.class);
                }   //若search->info->edit后的数据不满足此查询，但回到list后仍在search中显示
                linearLayoutManager = new LinearLayoutManager(ContactList.this);
                recyclerView.setLayoutManager(linearLayoutManager);
                contactListAdapter = new ContactListAdapter(mContactArrayList,contactList.getApplicationContext(), contactList);
                recyclerView.setAdapter(contactListAdapter);
            }
        };
        etSearch.addTextChangedListener(watcher);
        etSearch.setTag(watcher);

        //home键后点击应用总是打开启动活动
        FloatingActionButton fabAddContact = (FloatingActionButton) findViewById(R.id.fab_contact_list);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addContact = new Intent(ContactList.this, AddContact.class);
                startActivity(addContact);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onStart() {
        super.onStart();
        if (mSearch){
            mContactArrayList = DataSupport.findAll(Contact.class);
        }
        Collections.sort(mContactArrayList,
                (Object o1, Object o2)-> (java.text.Collator.getInstance(java.util.Locale.getDefault()))
                        .compare(((Contact)o1).getContactName(), ((Contact)o2).getContactName())
        );
        /*
        new Comparator(){
            @Override
            public int compare(Object o1, Object o2) {
                return ((java.text.RuleBasedCollator)java.text.Collator.getInstance(java.util.Locale.CHINA))
                .compare(((Student)o1).getName(), ((Student)o2).getName());
        }
        */
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        contactListAdapter = new ContactListAdapter(mContactArrayList,ContactList.this, this);
        recyclerView.setAdapter(contactListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_yellow_pages:
                Intent yellowPages = new Intent(ContactList.this, YellowPages.class);
                startActivity(yellowPages);
                break;
            case R.id.action_search:
                if (mSearch == true){

                    item.setIcon(R.drawable.ic_close_black_24dp);
                    //自动获取焦点的实现
                    etSearch.setVisibility(View.VISIBLE);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);

                    mSearch = false;
                } else {
                    item.setIcon(R.drawable.ic_search_black_24dp);
                    etSearch.setVisibility(View.GONE);
                    etSearch.getText().clear();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(ContactList.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    getSupportActionBar().setDisplayShowTitleEnabled(true);

                    mContactArrayList.clear();
                    mContactArrayList = DataSupport.order("contactName asc").find(Contact.class);
                    linearLayoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    contactListAdapter = new ContactListAdapter(mContactArrayList,ContactList.this, this);
                    recyclerView.setAdapter(contactListAdapter);
                    mSearch = true;
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "Press again to exit.",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
