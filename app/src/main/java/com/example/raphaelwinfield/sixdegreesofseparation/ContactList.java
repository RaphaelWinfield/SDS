package com.example.raphaelwinfield.sixdegreesofseparation;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.IccOpenLogicalChannelResponse;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.biff.File;

public class ContactList extends AppCompatActivity {

    private boolean mSearch = true;
    private EditText etSearch;
    private List<Contact> mContactArrayList = new ArrayList<>();
    private long exitTime = 0;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ContactListAdapter contactListAdapter;
    private ContactList mContactList;

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

        mContactList = this;
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
                    mContactArrayList = DataSupport.where("contactName like ? or contactPhoneNumber like ? or contactAdress like ? ",
                            "%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%")
                            .order("contactName asc")
                            .find(Contact.class);
                }   //若search->info->edit后的数据不满足此查询，但回到list后仍在search中显示
                linearLayoutManager = new LinearLayoutManager(ContactList.this);
                recyclerView.setLayoutManager(linearLayoutManager);
                contactListAdapter = new ContactListAdapter(mContactArrayList,mContactList.getApplicationContext(), mContactList);
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
            case R.id.action_yellow_pages:
                Intent yellowPages = new Intent(ContactList.this, YellowPages.class);
                startActivity(yellowPages);
                break;
            case R.id.action_save:
                AlertDialog.Builder adSave = new AlertDialog.Builder(ContactList.this);
                adSave.setIcon(R.drawable.ic_file_upload_black_24dp);
                adSave.setTitle("Save");
                final String[] saveItem = { "Save to storage.", "Save to SIM card."};
                boolean[] saveChoice = {false, false};
                adSave.setMultiChoiceItems(saveItem, null, (DialogInterface dialog, int which, boolean isChecked)->{
                    if (isChecked) {
                        saveChoice[which] = true;
                    }else {
                        saveChoice[which] = false;
                    }
                });
                adSave.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                adSave.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {  //此处的which为负值
                        if (saveChoice[0]){
                            List<Contact> mSaveContact= new ArrayList<>();
                            WritableSheet sheet;
                            WritableWorkbook wwb;
                            String[] title = {"ID","name","phone number","address","photo","ringtone"};
                            try {
                                /*输出的excel文件的路径*/
                                mSaveContact = DataSupport.findAll(Contact.class);
                                java.io.File saveFile = new java.io.File(Environment.getExternalStorageDirectory(), "contact.xls");
                                if (saveFile.exists()) {
                                    saveFile.delete();
                                }
                                saveFile.createNewFile();
                                wwb = Workbook.createWorkbook(saveFile);
                                jxl.write.WritableCellFormat wcfF = new jxl.write.WritableCellFormat();//设置单元格格式
                                wcfF.setWrap(true);// 自动换行
                                wcfF.setAlignment(jxl.format.Alignment.CENTRE);// 把水平对齐方式指定为居中
                                wcfF.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 把垂直对齐方式指定为居中
                                sheet = wwb.createSheet("Contact", 0);
                                Label label;
                                for (int i = 0; i < title.length; i++) {
                                    label = new Label(i, 0, title[i],wcfF);//Label(x,y,z)其中x代表单元格的第x+1列，第y+1行, 单元格的内容是y
                                    sheet.addCell(label);
                                }
                                for (int j = 0; j < mSaveContact.size(); j++) {
                                    label = new Label(0, j+1 ,mSaveContact.get(j).getContactId(),wcfF);
                                    sheet.addCell(label);
                                    label = new Label(1, j+1 ,mSaveContact.get(j).getContactName(),wcfF);
                                    sheet.addCell(label);
                                    label = new Label(2, j+1 ,mSaveContact.get(j).getContactPhoneNumber(),wcfF);
                                    sheet.addCell(label);
                                    label = new Label(3, j+1 ,mSaveContact.get(j).getContactAdress(),wcfF);
                                    sheet.addCell(label);
                                    label = new Label(4, j+1 ,mSaveContact.get(j).getContactPhoto(),wcfF);
                                    sheet.addCell(label);
                                    label = new Label(5, j+1 ,mSaveContact.get(j).getContactRingtone(),wcfF);
                                    sheet.addCell(label);
                                }
                                wwb.write();// 写入数据
                                wwb.close();// 关闭文件
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (saveChoice[1]){

                        }
                    }
                });
                adSave.show();
                break;
            case R.id.action_load:
                AlertDialog.Builder adLoad = new AlertDialog.Builder(ContactList.this);
                adLoad.setIcon(R.drawable.ic_file_download_black_24dp);
                adLoad.setTitle("Load");
                final String[] loadItem = { "Load from storage.", "Load from SIM card."};
                adLoad.setItems(loadItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mContactArrayList.clear();
                                        DataSupport.deleteAll(Contact.class);
                                        Sheet sheet;
                                        Workbook wb;
                                        try {
                                            /*输出的excel文件的路径*/
                                            java.io.File loadFile = new java.io.File(Environment.getExternalStorageDirectory(), "contact.xls");
                                            wb = Workbook.getWorkbook(loadFile);
                                            sheet = wb.getSheet(0);
                                            Label label;
                                            for (int j = 0; j < (sheet.getRows()-1); j++) {
                                                Contact loadContact = new Contact();
                                                loadContact.setContactId(sheet.getCell(0,j+1).getContents());
                                                loadContact.setContactName(sheet.getCell(1,j+1).getContents());
                                                loadContact.setContactPhoneNumber(sheet.getCell(2,j+1).getContents());
                                                loadContact.setContactAdress(sheet.getCell(3,j+1).getContents());
                                                loadContact.setContactPhoto(sheet.getCell(4,j+1).getContents());
                                                loadContact.setContactRingtone(sheet.getCell(5,j+1).getContents());
                                                mContactArrayList.add(loadContact);
                                            }
                                            wb.close();// 关闭文件
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        DataSupport.saveAll(mContactArrayList);
                                        Collections.sort(mContactArrayList,
                                                (Object o1, Object o2)-> (java.text.Collator.getInstance(java.util.Locale.getDefault()))
                                                        .compare(((Contact)o1).getContactName(), ((Contact)o2).getContactName()));
                                        linearLayoutManager = new LinearLayoutManager(mContactList.getApplicationContext());
                                        recyclerView.setLayoutManager(linearLayoutManager);
                                        contactListAdapter = new ContactListAdapter(mContactArrayList,mContactList.getApplicationContext(), mContactList);
                                        recyclerView.setAdapter(contactListAdapter);
                                        Toast.makeText(getApplicationContext(), "Successfully load from storage.", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(getApplicationContext(), "Successfully load from SIM card.", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                };
                            }
                        });
                adLoad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                adLoad.show();
                break;
            case R.id.action_delete_all: //可撤销删除的实现
                mContactArrayList.clear();
                DataSupport.deleteAll(Contact.class);
                linearLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(linearLayoutManager);
                contactListAdapter = new ContactListAdapter(mContactArrayList,ContactList.this, this);
                recyclerView.setAdapter(contactListAdapter);
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
                Toast.makeText(getApplicationContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
