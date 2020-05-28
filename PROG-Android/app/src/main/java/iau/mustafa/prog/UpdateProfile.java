/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                    Pattern.CASE_INSENSITIVE);

    private FirebaseAuth firebaseAuth;
    private EditText emailAddress, password, bio;
    private Button submit;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
        setTitle("Update Email");
        context = this;

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        emailAddress = (EditText)findViewById(R.id.email_update);
        bio = (EditText)findViewById(R.id.bio_update);
        password = (EditText)findViewById(R.id.password_email_update);
        submit = (Button)findViewById(R.id.update_email_Btn);

        if(firebaseAuth!=null) {
            emailAddress.setText(firebaseAuth.getCurrentUser().getEmail());
        }

        Query UserRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(user.getUid());
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if(dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        bio.setText(user.bio);
                    }
                } catch (Exception e){
                    //exception
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkCheck.IsNetworkConnected(context)){
                    final String email = emailAddress.getText().toString().trim();
                    String pass = password.getText().toString().trim();
                    final String userBio = bio.getText().toString();

                    if (email.isEmpty()){
                        Toast.makeText(context, "Enter your new email address",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } if (pass.isEmpty()) {
                        Toast.makeText(context, "Enter your password to confirm updating",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } if ( !validate(email) ){
                        Toast.makeText(context, "Invalid New Email Address",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } if( bio.length() > 150 ){
                        Toast.makeText(context, "Bio should be 150 character maximum",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthCredential credential = EmailAuthProvider
                            .getCredential(firebaseAuth.getCurrentUser().getEmail(), pass);
                    user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            user.updateEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            DatabaseReference UserRef = FirebaseDatabase.getInstance()
                                                    .getReference("Users").child(user.getUid());
                                            Map<String, Object> updates = new HashMap<String, Object>();
                                            updates.put("bio", userBio);
                                            UserRef.updateChildren(updates)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(context, "Profile Updated",
                                                            Toast.LENGTH_SHORT).show();
                                                    setResult(RESULT_OK, null);
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Unable to update, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}
