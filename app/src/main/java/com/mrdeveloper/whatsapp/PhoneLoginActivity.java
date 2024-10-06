package com.mrdeveloper.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.StartupTime;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    TextInputEditText edNumber, edVerificationCode;
    Button sendCodeButton, verificationButton;

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    TextInputLayout numberInputLayout, codeInputLayout;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edNumber = findViewById(R.id.edNumber);
        edVerificationCode = findViewById(R.id.edVerificationCode);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verificationButton = findViewById(R.id.verificationButton);
        numberInputLayout = findViewById(R.id.numberInputLayout);
        codeInputLayout = findViewById(R.id.codeInputLayout);



        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(PhoneLoginActivity.this);


        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setTitle("Sent Verification Code");
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                String phoneNumber = edNumber.getText().toString();

                if (phoneNumber.isEmpty()) {
                    edNumber.setError("Input Your Phone Number First");
                } else {
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // (optional) Activity for callback binding
                                    // If no activity is passed, reCAPTCHA verification can not be used.
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }


            }
        });


        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edNumber.setVisibility(View.GONE);
                sendCodeButton.setVisibility(View.GONE);

                String code = edVerificationCode.getText().toString();
                if (code.isEmpty()) {
                    edVerificationCode.setError("Enter Code First");
                } else {

                    progressDialog.setTitle("Code Verification Process");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                numberInputLayout.setVisibility(View.VISIBLE);
                sendCodeButton.setVisibility(View.VISIBLE);

                codeInputLayout.setVisibility(View.GONE);
                verificationButton.setVisibility(View.GONE);

                progressDialog.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Verification Code Not Sent", Toast.LENGTH_SHORT).show();
                
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has Been Sent", Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();

                numberInputLayout.setVisibility(View.GONE);
                sendCodeButton.setVisibility(View.GONE);

                codeInputLayout.setVisibility(View.VISIBLE);
                verificationButton.setVisibility(View.VISIBLE);

            }

        };



    } // ======================================= On Create End ==================

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String currentUID = mAuth.getCurrentUser().getUid();
                            dbRef.child("Users").child(currentUID).setValue("");

                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Phone Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhoneLoginActivity.this,MainActivity.class));
                            finish();

                        } else {

                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }




}