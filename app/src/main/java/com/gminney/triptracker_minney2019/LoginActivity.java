package com.gminney.triptracker_minney2019;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    Button mLoginButton;
    TextView mSignUpTextView;
    Button mSignUpButton;
    String be_app_id;
    String be_android_api_key;
    private final String TAG = this.getClass().getName();
    BackendlessUser mBackendlessUser = new BackendlessUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmailEditText = (EditText)findViewById(R.id.enter_email);
        mPasswordEditText = (EditText)findViewById(R.id.enter_password);
        mNameEditText = (EditText)findViewById(R.id.enter_name);
        mLoginButton = (Button)findViewById(R.id.login_button);
        mSignUpTextView = (TextView)findViewById(R.id.sign_up_text);
        mSignUpButton = (Button)findViewById(R.id.signup_button);
        be_app_id = getString(R.string.be_app_id);
        be_android_api_key = getString(R.string.be_android_api_key);

        Backendless.initApp(this, be_app_id, be_android_api_key);

        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (mSignUpTextView.getText() == getString(R.string.sign_up_text)) {
                    mNameEditText.setVisibility(View.VISIBLE);
                    mSignUpButton.setVisibility(View.VISIBLE);
                    mLoginButton.setVisibility(View.GONE);
                    mSignUpTextView.setText("Cancel Sign Up");
                }
                else{
                    mNameEditText.setVisibility(View.GONE);
                    mSignUpButton.setVisibility(View.GONE);
                    mLoginButton.setVisibility(View.VISIBLE);
                    mSignUpTextView.setText(getString(R.string.sign_up_text));
                }
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                String name = mNameEditText.getText().toString().trim();
                if (!userEmail.isEmpty() &&!password.isEmpty() && !name.isEmpty()) {
                    mBackendlessUser = new BackendlessUser();
                    mBackendlessUser.setEmail(userEmail);
                    mBackendlessUser.setPassword(password);
                    mBackendlessUser.setProperty("name", name);
                    final ProgressDialog pDialog = ProgressDialog.show(LoginActivity.this,
                            "Please Wait!",
                            "Creating a new account...",
                            true);
                    Backendless.UserService.login(userEmail, password, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Log.i (TAG, "Registration successful for " + response.getEmail());
                            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i (TAG, "Registration failed " + fault.getMessage());
                        }
                    });
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.empty_field_signup_error);
                    builder.setTitle(R.string.authentication_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                if (!userEmail.isEmpty() &&!password.isEmpty()) {
                    final ProgressDialog pDialog = ProgressDialog.show(LoginActivity.this,
                            "Please Wait!",
                            "Creating a new account...",
                            true);
                    Backendless.UserService.login(userEmail, password, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Log.i (TAG, "Login Successful for  " + response.getEmail());
                            Intent intent = new Intent(LoginActivity.this, TripListActivity.class);
                            startActivity(intent);
                        }
                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i (TAG, "Login failed " + fault.getMessage());
                        }
                    });
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.empty_field_login_error);
                    builder.setTitle(R.string.login_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
}
