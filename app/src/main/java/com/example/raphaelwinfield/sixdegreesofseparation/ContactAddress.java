package com.example.raphaelwinfield.sixdegreesofseparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.raphaelwinfield.sixdegreesofseparation.db.Contact;

import java.util.Random;

public class ContactAddress extends AppCompatActivity {

    private MapView mMapView;
    private GeoCoder mGeoCoder;
    private BaiduMap mBaiduMap;
    private Contact mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_address);

        Intent intent_Address = getIntent();
        mContact = (Contact) intent_Address.getParcelableExtra("ContactAddress");
        mMapView = (MapView) findViewById(R.id.contact_address);
        mBaiduMap = mMapView.getMap();

        getSupportActionBar().setTitle(mContact.getContactName()+ "'s Address");
        String mAddress = mContact.getContactAdress();
        int i = 0;
        StringBuffer sb = new StringBuffer();
        do{
            sb.append(String.valueOf(mAddress.charAt(i)));
        } while ( mAddress.charAt(i++) != '市' );
        String mCity = sb.toString();
        String mStreet = mAddress.substring(i);

        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(ContactAddress.this, "Wrong Search!", Toast.LENGTH_SHORT).show();
                } else {
                    LatLng pos = geoCodeResult.getLocation();
                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(pos);
                    mBaiduMap.animateMapStatus(update);
                    update = MapStatusUpdateFactory.zoomTo(18f);
                    mBaiduMap.animateMapStatus(update);
                }
            }
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) { }
        });
        mGeoCoder.geocode(new GeoCodeOption().city(mCity).address(mStreet));
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
    }
}
