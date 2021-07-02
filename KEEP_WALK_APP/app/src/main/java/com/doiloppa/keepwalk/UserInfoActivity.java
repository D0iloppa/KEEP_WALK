package com.doiloppa.keepwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserInfoActivity extends Activity {

    MemberInfo memberInfo;
    ImageView iv_Prof;
    EditText edt_UserName;
    TextView txt_email;
    Uri filePath;
    Button btn_Confirm;

    static String name,profUrl = null;
    static String pw;
    boolean isImgChanged = false;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        memberInfo = intent.getParcelableExtra("userInfo");

        name = memberInfo.getName();
        profUrl = memberInfo.getImg();

        db.collection("member").document(memberInfo.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                pw = documentSnapshot.get("pw").toString();
            }
        });

        txt_email = findViewById(R.id.txt_UserEmail);
        txt_email.setText(memberInfo.getId());
        iv_Prof = findViewById(R.id.img_UserInfo_Prof);
        Glide.with(this).load(R.raw.loading_img).into(iv_Prof);
        imgTask();

        iv_Prof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                // 하드웨어에 있는 내용을 가져올 수 있는 액션 지정
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 선택창을 띄워줌
                startActivityForResult(Intent.createChooser(intent,
                        "Get Choose the image"), 1000);
            }
        });

        edt_UserName = findViewById(R.id.edt_User_Name);
        edt_UserName.setText(memberInfo.getName());

        btn_Confirm = findViewById(R.id.btn_UserInfo_Confirm);
        btn_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = memberInfo.getId();
                name = edt_UserName.getText().toString();

                // 이미지가 변경되었을 경우 이미지 업로드
                if(isImgChanged) imgUpload(id);



                Map<String, Object> member = new HashMap<>();
                member.put("id", id);
                member.put("name", name);
                member.put("pw",pw);
                member.put("profImg", profUrl);



                db.collection("member").document(id).set(member)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserInfoActivity.this, "회원정보 변경 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });





            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getData(); // 이미지를 가져옴
            try {
                // 우선은 파일형식으로 가져오기 때문에 비트맵 형식으로 변환시켜줘야 한다.
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), filePath);
                iv_Prof.setImageBitmap(bitmap);
                isImgChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void imgTask(){
        ImageLoadTask task = new ImageLoadTask(profUrl, iv_Prof);
        task.execute();
    }

    public void imgUpload(String id) {

        if (filePath != null) { // 파일패스가 지정되어있는 경우에만 동작되도록 해준다.

            FirebaseStorage storage = FirebaseStorage.getInstance();
            String filename = id + ".jpg";
            StorageReference reference = storage
                    .getReferenceFromUrl("gs://keepwalk-9930e.appspot.com")
                    .child("prof_img/" + filename);


            // 성공,실패,처리중에 해당되는 이벤트리스너를 각각 달아준다.
            reference.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String[] urls = {
                            "https://firebasestorage.googleapis.com/v0/b/",
                            taskSnapshot.getMetadata().getBucket().toString(),
                            "/o/",
                            "prof_img%2F",
                            taskSnapshot.getMetadata().getName(),
                            "?alt=media"
                    };
                    String url = "";
                    for (int i = 0; i < urls.length; i++) url += urls[i];
                    profUrl = url;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Upload Fail", Toast.LENGTH_SHORT).show();
                }
            });

        }


    }



    public void onClose(View view) {
        finish();
    }

}