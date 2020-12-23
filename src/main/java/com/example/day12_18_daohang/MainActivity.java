package com.example.day12_18_daohang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, View.OnClickListener, PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener, RouteSearch.OnRouteSearchListener {
    MapView mMapView = null;
    private OnLocationChangedListener mListener;
    private Button btn_lu;
    private Button btn_wei;
    private Button btn_ye;
    private Button btn_mo;
    private Button btn_seach;
    private Button btn_xian;
    private Button btn_tianqi;
    private EditText et_sear;
    private EditText et_first;
    private EditText et_end;
    private AMap aMap;
    private PoiSearch poiSearch;
    private PoiSearch.Query queryy;
    private RouteSearch routeSearch;

    private WalkRouteResult mWalkRouteResult;
    private GeocodeSearch geocodeSearch;
    private LatLonPoint latLonPoint;
    private GeocodeSearch search;
    private LatLonPoint point;
    private final int ROUTE_TYPE_WALK = 3;

    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        btn_lu = findViewById(R.id.btn_lu);
        btn_wei = findViewById(R.id.btn_wei);
        btn_ye = findViewById(R.id.btn_ye);
        btn_mo = findViewById(R.id.btn_mo);
        et_sear = findViewById(R.id.et_sear);
        et_first = findViewById(R.id.et_first);
        et_end = findViewById(R.id.et_end);
        btn_seach = findViewById(R.id.btn_seach);
        btn_xian = findViewById(R.id.btn_xian);
        btn_tianqi = findViewById(R.id.btn_tianqi);
        mMapView.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
                == PackageManager.PERMISSION_GRANTED){
            initPermission();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION
            ,Manifest.permission.ACCESS_WIFI_STATE
            ,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS},100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED
        &&grantResults[1]==PackageManager.PERMISSION_GRANTED&&grantResults[2]==PackageManager.PERMISSION_GRANTED
        &&grantResults[3]==PackageManager.PERMISSION_GRANTED&&grantResults[4]==PackageManager.PERMISSION_GRANTED
                &&grantResults[5]==PackageManager.PERMISSION_GRANTED&&grantResults[6]==PackageManager.PERMISSION_GRANTED){
            initPermission();
        }else {
            Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPermission() {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        aMap = mMapView.getMap();
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        myLocationStyle.showMyLocation(true);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setLocationSource(this);
        aMap.showIndoorMap(true);

        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
// 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
//声明AMapLocationClient类对象
        AMapLocationClient mLocationClient = null;

        btn_lu.setOnClickListener(this);
        btn_wei.setOnClickListener(this);
        btn_ye.setOnClickListener(this);
        btn_mo.setOnClickListener(this);

//初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //声明AMapLocationClientOption对象
        AMapLocationClientOption mLocationOption = null;
//初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        AMapLocationClientOption option = new AMapLocationClientOption();
        /**
         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
         */
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        if(null != mLocationClient){
            mLocationClient.setLocationOption(option);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(1000);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
//启动定位
        mLocationClient.startLocation();

        btn_tianqi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aoiName = mlocationClient.getLastKnownLocation().getCity();
                Log.e("TAG", "onCreate: 当前位置:"+aoiName);
                Intent intent = new Intent(MainActivity.this, WeatherSearchActivity.class);
                intent.putExtra("tianqi",aoiName);
                startActivity(intent);
            }
        });

        InputtipsQuery query = new InputtipsQuery(et_sear.getText().toString(), "北京");
        query.setCityLimit(true);
        Inputtips inputtips = new Inputtips(this, query);
        inputtips.setInputtipsListener(this);
        inputtips.requestInputtipsAsyn();


        btn_seach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryy = new PoiSearch.Query(et_sear.getText().toString(), "", "北京");
//keyWord表示搜索字符串，
//第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
//cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
                queryy.setPageSize(10);// 设置每页最多返回多少条poiitem
                queryy.setPageNum(currentPage);//设置查询页码
                currentPage++;
                poiSearch = new PoiSearch(MainActivity.this, queryy);
                poiSearch.setOnPoiSearchListener(MainActivity.this);
                poiSearch.searchPOIAsyn();

            }
        });
        btn_xian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geocodeSearch = new GeocodeSearch(MainActivity.this);
                GeocodeQuery query1 = new GeocodeQuery(et_first.getText().toString(), "北京");
                geocodeSearch.getFromLocationNameAsyn(query1);
                geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                    @Override
                    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

                    }

                    @Override
                    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                        Log.e("TAG", "poi检索返回:"+i);
                        if (i==1000){
                            latLonPoint = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
//                        String s1 = et_end.getText().toString();
                            Log.e("TAG", "onGeocodeSearched: latLonPoint"+latLonPoint.toString());
                            searsh();
                        }
                    }
                });
            }
        });


    }

    public void searchRouteResult(int routeType, int mode) {
        if (latLonPoint == null) {
            ToastUtil.show(this, "定位中，稍后再试...");
            return;
        }
        if (point == null) {
            ToastUtil.show(this, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                latLonPoint, point);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            routeSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
        routeSearch = new RouteSearch(MainActivity.this);
        routeSearch.setRouteSearchListener(this);
    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(latLonPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(point))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }
    private void searsh() {
        GeocodeQuery query1 = new GeocodeQuery(et_end.getText().toString(), "北京");
        geocodeSearch.getFromLocationNameAsyn(query1);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                Log.e("TAG", "poi检索返回:"+i);
                if (i==1000){
                    point = geocodeResult.getGeocodeAddressList().get(0).getLatLonPoint();
                    Log.e("TAG", "point"+point.toString());
                    poResult();
                }
            }
        });
    }
    private void poResult() {
        setfromandtoMarker();
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
//        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
//            @Override
//            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
//
//            }
//
//            @Override
//            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
//                aMap.clear();
//                if (i==AMapException.CODE_AMAP_SUCCESS){
//                    if (driveRouteResult!=null&&driveRouteResult.getPaths()!=null){
//                        if (driveRouteResult.getPaths().size()>0){
//                            DrivePath drivePath = driveRouteResult.getPaths().get(0);
//                            if (drivePath==null){
//                                return;
//                            }
//
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
//
//            }
//
//            @Override
//            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
//
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null&&aMapLocation != null) {
            if (aMapLocation != null
                    &&aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    public void onClick(View v) {
        //       aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 设置卫星地图模式，aMap是地图控制器对象。
        //        aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图，aMap是地图控制器对象。
        //        aMap.setTrafficEnabled(true);//显示实时路况图层，aMap是地图控制器对象。
        switch (v.getId()){
            case R.id.btn_lu:
                aMap.setTrafficEnabled(true);
                break;
            case R.id.btn_wei:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.btn_ye:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                break;
            case R.id.btn_mo:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                break;
        }
    }
private int currentPage = 0;

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        Log.e("TAG", "poi检索返回:"+i);
        ArrayList<PoiItem> pois = poiResult.getPois();
        for (PoiItem item :
                pois) {
            Log.e("TAG", "当前返回地址:"+item.getAdName());
            Log.e("TAG", "当前返回地址:"+item.toString());
        }
        PoiOverlay poiOverlay = new PoiOverlay(aMap, pois);
        poiOverlay.removeFromMap();
        poiOverlay.addToMap();
        poiOverlay.zoomToSpan();
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {

    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    if(walkPath == null) {
                        return;
                    }
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(MainActivity.this, "对不起，没有搜索到相关数据！");
                }
            } else {
                ToastUtil.show(MainActivity.this,"对不起，没有搜索到相关数据！");
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
}