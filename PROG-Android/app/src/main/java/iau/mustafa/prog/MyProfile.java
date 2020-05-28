/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MyProfile extends AppCompatActivity {

    int REQUEST_CODE;
    Context context;
    TabHost tabHost;
    String myUID;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private TextView profileUserName, profileBio;
    private Button editProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context=this;
        setTitle("PROFILE");

        progressBar = (ProgressBar)findViewById(R.id.profile_prgBar);
        profileUserName = (TextView) findViewById(R.id.profileUserName);
        profileBio = (TextView) findViewById(R.id.profileBio);
        editProfile = (Button)findViewById(R.id.profile_edit_btn);

        tabHost = (TabHost) findViewById(R.id.profile_tabs);
        tabHost.setup();

        //Tab 1: Completed Tasks
        TabHost.TabSpec spec = tabHost.newTabSpec("Projects Enrolled");
        spec.setContent(R.id.profile_tab1);
        spec.setIndicator("Projects Enrolled");
        tabHost.addTab(spec);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateProfile.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        if(NetworkCheck.IsNetworkConnected(context)) {
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth = FirebaseAuth.getInstance();
            myUID = firebaseAuth.getCurrentUser().getUid();

            Query UserRef = FirebaseDatabase.getInstance()
                    .getReference("Users").child(myUID);
            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        profileUserName.setText(user.username);
                        profileBio.setText(user.bio);
                    } catch (Exception e){
                        //exception
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Query allProjectsRef = FirebaseDatabase.getInstance()
                    .getReference("Membership").child(myUID);
            // Start of Retrieve Projects:
            allProjectsRef.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                collectProjects((Map<String, Object>) dataSnapshot.getValue());
                                progressBar.setVisibility(View.GONE);
                            } else {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            // End of Retrieve Projects

        } else {
            Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void collectProjects(Map<String,Object> projects) {
        final ArrayList<String> ProjectIDs = new ArrayList<>();
        final ArrayList<String> ProjectNames = new ArrayList<>();

        for (Map.Entry<String, Object> entry : projects.entrySet()){
            Map singleProject = (Map) entry.getValue();
            ProjectIDs.add((String) singleProject.get("PID"));
        }

        DatabaseReference getProjectRef = FirebaseDatabase.getInstance()
                .getReference("Projects");
        getProjectRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            String pName;
                            for (int i = 0; i < ProjectIDs.size(); i++) {
                                Project p = dataSnapshot.child(ProjectIDs.get(i)).getValue(Project.class);
                                pName = Objects.requireNonNull(p).projectName;
                                ProjectNames.add(pName);
                            }
                            final ListView lv = (ListView) findViewById(R.id.profile_projects_list);
                            final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                                    (context, android.R.layout.simple_list_item_1, ProjectNames);
                            lv.setAdapter(arrayAdapter1);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(context, Project_page.class);
                                    intent.putExtra("ProjectID", ProjectIDs.get(position));
                                    startActivityForResult(intent, REQUEST_CODE);
                                }
                            });
                        } catch (Exception e){
                            //Exception
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "Error Encountered",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            Intent intent = new Intent(context, MainMenu.class);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() { finish(); }
}
