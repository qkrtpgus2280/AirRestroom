package com.air.restroom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private Button btn_find;
    double[] coords;
    JSONArray toilet;
    GetGeoData geoData;
    parseData data;
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("6e83ed1e757e4911a78748828489f975");

        ViewGroup viewGroup = (ViewGroup)findViewById(R.id.mapView);
        viewGroup.addView(mapView);

        btn_find = (Button)findViewById(R.id.btn_find);
        btn_find.setOnClickListener(this);
    }

    public void init() {
        data = new parseData(getApplicationContext());
        toilet = new JSONArray();
        getToiletData();
        JSONArray toilet = getToilet();
        Log.d("Init", toilet.toString());

        new Thread() {
            @Override
            public void run() {
                try {
                    coords = getUserLocation();
                    mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(coords[0], coords[1]), 1, true);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    public void onStartService() {
        bindService(new Intent(this, GetGeoData.class), mConnect, Context.BIND_AUTO_CREATE);
    }

    public void onStopService() {
        unbindService(mConnect);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private ServiceConnection mConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GetGeoData.LocalBinder b = (GetGeoData.LocalBinder)service;
            geoData = b.getSystem();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public double[] getUserLocation() throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(data.SendGeoInform());
        JSONObject co = (JSONObject) jsonObject.get("location");
        double[] c;

        c = new double[]{
                Double.parseDouble(co.get("lat").toString()),
                Double.parseDouble(co.get("lng").toString())
        };

        return c;
    }

    public void getToiletData() {
        String url = "http://openapi.seoul.go.kr:8088/496b4f4f726b6d73313031447776697a/json/SearchPublicToiletPOIService/1/1000/";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONObject j;
                JSONArray row = null;
                try {
                    j = response.getJSONObject("SearchPublicToiletPOIService");
                    row = j.getJSONArray("row");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("lala", row.toString());
                setToilet(row);
            }
        });
    }

    public void setToilet(JSONArray toilet) {
        this.toilet = toilet;
    }

    public JSONArray getToilet() {
        return this.toilet;
    }
}
//        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(coords[0], coords[1]), 7, true);
//        CloudAdapter adapter = new CloudAdapter();
//        adapter.init(this, "lala", R.drawable.abc_btn_radio_material, R.drawable.abc_btn_radio_material,
//                R.drawable.abc_btn_radio_material, "lala", "lala", "lala");
//        mapView.setCalloutBalloonAdapter(adapter);
//
//        MapPOIItem marker = new MapPOIItem();
//        marker.setItemName("Default Marker");
//        marker.setTag(0);
//        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.56621, 126.9779));
//        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
//        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
//
//        mapView.addPOIItem(marker);