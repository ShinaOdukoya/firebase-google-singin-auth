package com.example.googlesigninauthentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

                //For the requestIdToken, use getString(R.string.default_web_client_id), this is in the values.xml file that
                //is generated from your google-services.json file (data from your firebase project), uses the google-sign-in method
                //web api key
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Build a GoogleSignIn client with options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Set the dimensions of the sign-in button
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            Log.d(TAG,"Currently signed in" + currentUser.getEmail());
            Toast.makeText(this,"Currently logged in" + currentUser.getEmail(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google signIn was successful, authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this,"Google sign in was successful", Toast.LENGTH_LONG).show();
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                //Google sign in failed, update UI appropriately
                Log.w(TAG,"Google sign in failed", e);
                Toast.makeText(this,"Google sign in failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d(TAG,"firebaseAuthWithGoogle: " + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Sign-in successful, upate UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG,"signInWithCredential: success: getCurrentuser: " + user.getEmail());
                            Toast.makeText(getApplicationContext(), "Firebase Authentication Succeeded", Toast.LENGTH_LONG).show();
                        }else {
                            //if sign-in fail display a message to the user
                            Log.w(TAG,"signInWithCredential: failure", task.getException());
                            Toast.makeText(MainActivity.this,"Firebase Authentication Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void signInToGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button){
            signInToGoogle();
        }
        else if(i == R.id.signOutButton){
            signOut();
        }
        else if (i == R.id.disconnectButton){
            revokeAccess();
        }
    }

    private void signOut(){
        //Firebase sign out
        mAuth.signOut();

        //Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Google sign in failed, update UI appropriately
                Log.w(TAG,"Signed out of Google");
            }
        });
    }
    private void revokeAccess(){
        //Firebase sign out
        mAuth.signOut();

        //Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Google sign in failed, update UI appropriately
                Log.w(TAG,"Revoked Access");
            }
        });
    }

    //    public void onGoogleSignInClicked(View view){
//
//    }
}