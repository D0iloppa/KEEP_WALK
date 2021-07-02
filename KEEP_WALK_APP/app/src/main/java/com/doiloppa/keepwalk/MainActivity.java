package com.doiloppa.keepwalk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.map.NaverMapSdk;


import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static MemberInfo memberInfo = new MemberInfo();
    float myKG;
    int c_MET = 4;
    Intent intent;
    String id;
    FirebaseFirestore db;

    // info window 영역
    ConstraintLayout infoWindow; // 로그인 시, 인포창
    TextView txtName;
    ImageView iv_Prof;
    boolean isInfoOpend = true;

    /////////////////////

    ImageView btn_Search, btn_UserInfo;







    double getDist(int kcal) {
        double dist = 0.0;
        double min = kcal * 1000 / (c_MET * myKG * 3.5 * 5);
        dist = (6.0 / 60) * min;

        return dist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = getIntent();

        // 인포 창 영역
        TextView infoClose;

        infoWindow = findViewById(R.id.info_Window);
        infoClose = findViewById(R.id.btn_infoClose);
        infoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoWindow.setVisibility(View.GONE);
                isInfoOpend = false;
            }
        });
        txtName = findViewById(R.id.txt_NameInfo);

        iv_Prof = findViewById(R.id.iv_prof);
        Glide.with(this).load(R.raw.loading_img).into(iv_Prof);




        id = intent.getStringExtra("id");


        db = FirebaseFirestore.getInstance();
        db.collection("member").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                String memberId = documentSnapshot.get("id").toString();
                String memberName = documentSnapshot.get("name").toString();
                txtName.setText("Hello "+ memberName+"! It's time to Walk.");
                String memberProf = documentSnapshot.get("profImg").toString();
                memberInfo.inIt(memberId,memberName,memberProf);

                imgTask();
            }

        });


        //////////////////////////////


        btn_Search = findViewById(R.id.btn_SearchIcon);
        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInfoOpend) startActivity(new Intent(getApplicationContext(),SearchActivity.class));
            }
        });

        btn_UserInfo = findViewById(R.id.btn_UserIcon);
        btn_UserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInfoOpend)
                    startActivity(new Intent(getApplicationContext(),UserInfoActivity.class)
                            .putExtra("userInfo", memberInfo));

            }
        });




    }

    public void imgTask(){
        String imgUrl = memberInfo.getImg();

        ImageLoadTask task = new ImageLoadTask(imgUrl, iv_Prof);
        task.execute();


    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

}
