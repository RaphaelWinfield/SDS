package com.example.raphaelwinfield.sixdegreesofseparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class ContactAddress extends AppCompatActivity {

    private MapView mMapView;
    private GeoCoder mGeoCoder;
    private BaiduMap mBaiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_address);
        mMapView = (MapView) findViewById(R.id.contact_address);
        mBaiduMap = mMapView.getMap();
        mGeoCoder = GeoCoder.newInstance();
        mBaiduMap.setMyLocationEnabled(true);

        Intent intent_Address = getIntent();
        Contact contact = (Contact) intent_Address.getParcelableExtra("ContactAddress");
        getSupportActionBar().setTitle(contact.getContactName()+ "'s Address");
        String addressInfo = contact.getContactAdress();

        if (addressInfo == null){
            Toast.makeText(ContactAddress.this, "Wrong Search!", Toast.LENGTH_SHORT).show();
            //finish();  若有此句则报错：E/JVMContainer VMSG: RegisterNatives  可能和SDKInitializer.initialize(getApplicationContext())有关
        } else {
            //根据联系人地址取出所需的City与Street信息
            int i = 0;
            StringBuffer sb = new StringBuffer();
            do{
                sb.append(String.valueOf(addressInfo.charAt(i)));
            } while ( addressInfo.charAt(i++) != '市' );
            String mCity = sb.toString();
            String mStreet = addressInfo.substring(i);

            mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                @Override
                public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                    if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                        Toast.makeText(ContactAddress.this, "Wrong Search!", Toast.LENGTH_SHORT).show();
                    } else {
                        //地址转换为坐标
                        LatLng pos = geoCodeResult.getLocation();
                        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(pos);
                        mBaiduMap.animateMapStatus(update);
                        //设置缩放范围
                        update = MapStatusUpdateFactory.zoomTo(18f);
                        mBaiduMap.animateMapStatus(update);
                        //显示定点
                        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
                        locationBuilder.latitude(pos.latitude);
                        locationBuilder.longitude(pos.longitude);
                        MyLocationData locationData = locationBuilder.build();
                        mBaiduMap.setMyLocationData(locationData);
                    }
                }
                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) { }
            });
            mGeoCoder.geocode(new GeoCodeOption().city(mCity).address(mStreet));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mGeoCoder.destroy();
        mBaiduMap.setMyLocationEnabled(false);
    }
}
