package com.doiloppa.keepwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.Iterator;
import java.util.Locale;

public class SearchActivity extends Activity implements OnMapReadyCallback {

    FirebaseFirestore db;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    MapView mapView;
    private double lat, lon; // 좌표

    EditText edt_Search;

    // 검색결과 영역
    LinearLayout layout_search_result;
    ImageView img_Food;
    TextView txt_FoodName,txt_Kcal,txt_Dist;
    String query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        edt_Search = findViewById(R.id.edt_Search);

        // 검색 결과 영역
        layout_search_result = findViewById(R.id.layout_search_result);
        layout_search_result.setVisibility(View.GONE); // 처음에는 보이지 않도록 설정
        img_Food = findViewById(R.id.img_Food);
        txt_FoodName = findViewById(R.id.txt_FoodName);
        txt_Kcal = findViewById(R.id.txt_kcal);
        txt_Dist = findViewById(R.id.txt_distGoal);





        // 네이버 지도 로딩
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("iir66c6l9n"));

        NaverMapOptions options = new NaverMapOptions()
                .locationButtonEnabled(true)
                .tiltGesturesEnabled(false);


        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(this);

        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


        //////////
        // 검색버튼 구현

        Button btn_Search = findViewById(R.id.btn_areaSearch);
        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edt_Search.getText().toString().isEmpty()){
                    Toast.makeText(SearchActivity.this, "검색어를 먼저 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 검색어를 통해 질의하여 결과를 가져옴
                query = edt_Search.getText().toString();
                db.collection("food")
                        .whereArrayContains("aka",query).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                      Iterator<QueryDocumentSnapshot> it = queryDocumentSnapshots.iterator();
                      if(it.hasNext()){
                          DocumentSnapshot result = it.next();
                          String foodName = query;
                          String imgUrl = result.get("img").toString();
                          double kcal = Double.parseDouble(result.get("kcal").toString());
                          search(foodName,imgUrl,kcal);
                      }else{
                          layout_search_result.setVisibility(View.GONE);
                          Toast.makeText(SearchActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                      }

                    }
                });
            }
        });







    }

    private void search(String foodName, String imgUrl, double kcal) {
        layout_search_result.setVisibility(View.VISIBLE);

        // 사전 표시 이미지
        Glide.with(this).load(R.raw.loading_img).into(img_Food);

        // 정보 표시
        txt_FoodName.setText(foodName);
        txt_Kcal.setText(kcal + " kCal");
        double dist = new GetDist().getDist((int)kcal);
        String dist_Text = String.format("이동해야 할 거리 : %.3f KM",dist);
        txt_Dist.setText(dist_Text);

        // 이미지 로드
        imgLoad(imgUrl);
    }

    private void imgLoad(String imgUrl) {
        Glide.with(this).load(imgUrl).into(img_Food);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);



        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();




            }
        });



    }

    public void on_sch_Close(View view) {
        finish();
    }
}