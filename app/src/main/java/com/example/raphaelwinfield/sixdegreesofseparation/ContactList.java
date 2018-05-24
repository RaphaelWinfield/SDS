package com.example.raphaelwinfield.sixdegreesofseparation;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;
import com.facebook.stetho.Stetho;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ContactList extends AppCompatActivity {

    private long exitTime = 0;//连续后退两次实现退出的标记
    private boolean mSearch = false; //是否开启搜索功能的标记
    private EditText etSearch;
    private List<Contact> mContactArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ContactListAdapter contactListAdapter;
    private ContactList mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Stetho.initializeWithDefaults(this);//对于未获取root的手机实现数据库内容访问

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_contact_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contact List");
        etSearch = (EditText) findViewById(R.id.et_contact_serach);
        recyclerView = (RecyclerView) findViewById(R.id.rv_contact_list);
        mContactList = this;

        //搜索功能的数据查询实现
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
                }
                linearLayoutManager = new LinearLayoutManager(ContactList.this);
                recyclerView.setLayoutManager(linearLayoutManager);
                contactListAdapter = new ContactListAdapter(mContactArrayList,mContactList.getApplicationContext(), mContactList);
                recyclerView.setAdapter(contactListAdapter);
            }
        };
        etSearch.addTextChangedListener(watcher);
        etSearch.setTag(watcher);

        //添加联系人按钮
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
        //回到此活动时根据先前是否进行过搜索显示相应的界面
        if (!mSearch){
            mContactArrayList = DataSupport.findAll(Contact.class);
            Collections.sort(mContactArrayList,
                    (Object o1, Object o2)-> (java.text.Collator.getInstance(java.util.Locale.getDefault()))
                            .compare(((Contact)o1).getContactName(), ((Contact)o2).getContactName()));//Lambda表达式
            /*将List中的每个实例按照指定的某个属性值进行排序的完整表达式
            new Comparator(){
                @Override
                public int compare(Object o1, Object o2) {
                    return ((java.text.RuleBasedCollator)java.text.Collator.getInstance(java.util.Locale.CHINA))
                    .compare(((Student)o1).getName(), ((Student)o2).getName());
            }*/
        } else {
            mContactArrayList = DataSupport.where("contactName like ? or contactPhoneNumber like ? or contactAdress like ? ",
                    "%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%","%" + etSearch.getText().toString() + "%")
                    .order("contactName asc")
                    .find(Contact.class);
        }
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
            //点击搜索或取消按钮
            case R.id.action_search:
                if (!mSearch){
                    //实现搜索界面
                    item.setIcon(R.drawable.ic_close_black_24dp);
                    etSearch.setVisibility(View.VISIBLE);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    mSearch = true;
                } else {
                    //实现退出搜索
                    item.setIcon(R.drawable.ic_search_black_24dp);
                    etSearch.setVisibility(View.GONE);
                    etSearch.getText().clear();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(ContactList.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);//自动隐藏输入法键盘
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    mContactArrayList.clear();
                    mContactArrayList = DataSupport.order("contactName asc").find(Contact.class);
                    linearLayoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    contactListAdapter = new ContactListAdapter(mContactArrayList,ContactList.this, this);
                    recyclerView.setAdapter(contactListAdapter);
                    mSearch = false;
                }
                break;
            //点击黄页目录项
            case R.id.action_yellow_pages:
                Intent yellowPages = new Intent(ContactList.this, YellowPages.class);
                startActivity(yellowPages);
                break;
            //点击保存目录项
            case R.id.action_save:
                //多选保存位置的信息提示框
                AlertDialog.Builder adSave = new AlertDialog.Builder(ContactList.this);
                adSave.setIcon(R.drawable.ic_file_upload_black_24dp);
                adSave.setTitle("Save");
                final String[] saveItem = { "Save to storage.", "Save to SIM card."};
                boolean[] saveChoice = {false, false};//记录选项选择情况的标记数组
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
                    public void onClick(DialogInterface dialog, int which) {
                        //以EXCEl文件格式写入手机外存
                        if (saveChoice[0]){
                            WritableWorkbook wwb;
                            WritableSheet sheet;
                            Label label;
                            List<Contact> mSaveContact = DataSupport.findAll(Contact.class);
                            String[] title = {"ID","name","phone number","address","photo","ringtone"};
                            try {
                                //EXCEl文件的路径
                                java.io.File saveFile = new java.io.File(Environment.getExternalStorageDirectory(), "Contact.xls");
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
                                //表头设置
                                for (int i = 0; i < title.length; i++) {
                                    label = new Label(i, 0, title[i],wcfF);//Label(x,y,z)表示第x+1列、第y+1行的单元格中的内容是z
                                    sheet.addCell(label);
                                }
                                //数据填写
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
                        //写入SIM卡
                        if (saveChoice[1]){ }
                    }
                });
                adSave.show();
                break;
            //点击读取目录项
            case R.id.action_load:
                //单选读取位置的信息提示框
                AlertDialog.Builder adLoad = new AlertDialog.Builder(ContactList.this);
                adLoad.setIcon(R.drawable.ic_file_download_black_24dp);
                adLoad.setTitle("Load");
                final String[] loadItem = { "Load from storage.", "Load from SIM card."};
                adLoad.setItems(loadItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    //从外存的EXCEL文件中读取信息
                                    case 0:
                                        mContactArrayList.clear();
                                        DataSupport.deleteAll(Contact.class);
                                        Workbook wb;
                                        Sheet sheet;
                                        try {
                                            //读入EXCEL文件的路径
                                            java.io.File loadFile = new java.io.File(Environment.getExternalStorageDirectory(), "contact.xls");
                                            wb = Workbook.getWorkbook(loadFile);
                                            sheet = wb.getSheet(0);
                                            //读入数据
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
                                    //从SIM卡中读取信息
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
            //点击删除全部目录项
            case R.id.action_delete_all:
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
        //连续后退两次实现退出功能
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "Press again to exit.", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
