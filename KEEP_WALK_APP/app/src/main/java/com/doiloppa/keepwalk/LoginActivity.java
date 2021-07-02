package com.doiloppa.keepwalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoginActivity extends Activity {

    EditText edt_Id, edt_Pw;
    Button btn_SignIn;
    TextView txt_SignUp;
    EncryptText encryptText;
    FirebaseFirestore db;
    Map<String, Object> member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        encryptText = new EncryptText();
        db = FirebaseFirestore.getInstance();
        member = new HashMap<>();

        edt_Id = findViewById(R.id.edt_Id);
        edt_Pw = findViewById(R.id.edt_Pw);
        btn_SignIn = findViewById(R.id.btn_SignIn);
        // 로그인 버튼
        btn_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(edt_Id.getText().toString().isEmpty() || edt_Pw.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "아이디와 암호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                    
                String id = edt_Id.getText().toString();
                String pw = encryptText.encryptSHA256(edt_Pw.getText().toString());

                db.collection("member").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String chk_Pw = documentSnapshot.get("pw").toString();
                            if(pw.equals(chk_Pw)) loginProcess(id);
                            else
                                Toast.makeText(getApplicationContext(), "아이디 또는 암호를 확인해주세요", Toast.LENGTH_SHORT).show();
                        }else{ // 해당 아이디가 존재하지 않는 경우
                            Toast.makeText(getApplicationContext(), "해당 아이디는 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                
            }
        }); // 로그인 버튼 온클릭리스너 끝


        txt_SignUp = findViewById(R.id.txt_SignUp);
        // sign up 글씨에 밑줄
        txt_SignUp.setPaintFlags(txt_SignUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txt_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });


    }

    private void loginProcess(String id) {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id",id);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


    public void onClose(View view) {
        finish();
    }
}