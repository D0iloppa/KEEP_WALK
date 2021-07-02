package com.doiloppa.keepwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class SignupActivity extends Activity implements View.OnClickListener {

    EditText edt_Name, edt_Email, edt_Pw, edt_Repw;
    Button btn_Reg;
    ImageView img;
    Uri filePath;
    String filename, profUrl;

    FirebaseFirestore db;
    EncryptText encryptText;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getData(); // 이미지를 가져옴
            try {
                // 우선은 파일형식으로 가져오기 때문에 비트맵 형식으로 변환시켜줘야 한다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();

        edt_Name = findViewById(R.id.edt_SignUP_Name);
        edt_Email = findViewById(R.id.edt_SignUP_Email);
        edt_Pw = findViewById(R.id.edt_SignUP_Pw);
        edt_Repw = findViewById(R.id.edt_SignUP_repw);
        btn_Reg = findViewById(R.id.btn_SignUp);
        btn_Reg.setOnClickListener(this);
        img = findViewById(R.id.img_SignUp_Prof);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                // 하드웨어에 있는 내용을 가져올 수 있는 액션 지정
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // 선택창을 띄워줌
                startActivityForResult(Intent.createChooser(intent, "Get Choose the image"), 1000);
            }
        });

        encryptText = new EncryptText();


    }

    public void onClose(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {

        String name, email, pw, repw;
        name = edt_Name.getText().toString();
        email = edt_Email.getText().toString().trim();
        pw = edt_Pw.getText().toString();
        repw = edt_Repw.getText().toString();

        if (name.isEmpty() || email.isEmpty() || pw.isEmpty() || repw.isEmpty()) {
            Toast.makeText(getApplicationContext(), "입력란은 공란으로 둘 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pw.equals(repw)) {
            Toast.makeText(getApplicationContext(), "패스워드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }



        db.collection("member").whereEqualTo("id", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Iterator<QueryDocumentSnapshot> it = queryDocumentSnapshots.iterator();
                if (it.hasNext()) // 중복아이디 존재
                    Toast.makeText(getApplicationContext(), "이미 가입된 아이디 입니다.", Toast.LENGTH_SHORT).show();
                else {
                    imgUpload(email); // 이미지 업로드
//                    register(email, name, pw , profUrl);
                }
            }
        });

    }

    public void imgUpload(String id) {

        if (filePath != null) { // 파일패스가 지정되어있는 경우에만 동작되도록 해준다.

            FirebaseStorage storage = FirebaseStorage.getInstance();
            filename = id + ".jpg";
            StorageReference reference = storage.getReferenceFromUrl("gs://keepwalk-9930e.appspot.com").child("prof_img/" + filename);


            // 성공,실패,처리중에 해당되는 이벤트리스너를 각각 달아준다.
            reference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

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
                    // 등록된 사진정보를 가지고 회원가입을 마침
                    register(edt_Email.getText().toString(), edt_Name.getText().toString(), edt_Pw.getText().toString(), url);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload Fail", Toast.LENGTH_SHORT).show();
                }
            });

        } else { // 파일설정을 하지 않았을 경우
            final String defaultUrl ="https://firebasestorage.googleapis.com/v0/b/keepwalk-9930e.appspot.com/o/prof_img%2Fdefault-avatar.jpg?alt=media&token=250d447f-7949-49a8-96b3-03c6fbe26d4c";
           register(edt_Email.getText().toString(), edt_Name.getText().toString(), edt_Pw.getText().toString(), defaultUrl);
        }


    }


    void register(String id, String name, String pw, String url) {

        Map<String, Object> member = new HashMap<>();
        member.put("name", name);
        member.put("id", id);
        member.put("pw", encryptText.encryptSHA256(pw));
        member.put("profImg", url);

        db.collection("member").document(id).set(member)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
            }
        });


    }

}