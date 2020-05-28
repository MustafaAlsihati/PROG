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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddProject extends AppCompatActivity {

    EditText projectNameField, projectDescriptionField;
    DatePicker DueDateProject;
    Button submit;
    private DatabaseReference Projects;
    String myUid;
    String UserName;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        context=this;

        setTitle("Create new Project");

        if(!NetworkCheck.IsNetworkConnected(context)){
            Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Projects = FirebaseDatabase.getInstance().getReference("Projects");
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserID();

        projectNameField = (EditText) findViewById(R.id.projectNameField);
        projectDescriptionField = (EditText) findViewById(R.id.projectDescriptionField);
        DueDateProject = (DatePicker) findViewById(R.id.DueDateProject);
        submit = (Button) findViewById(R.id.add_task_submit_btn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = projectNameField.getText().toString();
                final String desc = projectDescriptionField.getText().toString();
                final String duedate = DueDateProject.getDayOfMonth() + "/" +
                                    (DueDateProject.getMonth()+1) + "/" +
                                    DueDateProject.getYear();

                if( name.isEmpty() ){
                    Toast.makeText(AddProject.this, "Please enter name", Toast.LENGTH_SHORT).show();
                    return;
                } if( desc.isEmpty() ){
                    Toast.makeText(AddProject.this, "Please add project description", Toast.LENGTH_SHORT).show();
                    return;
                }
                Project project = new Project(name,desc,duedate,myUid);
                final String pid = Projects.push().getKey();
                Projects.child(pid).setValue(project).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //Here Add the leader as a member method
                            DatabaseReference rootRef = FirebaseDatabase.getInstance()
                                    .getReference().getRoot().child("ProjectMembers").child(pid).child(myUid);
                            Map<String, Object> addLeader = new HashMap<String,Object>();
                            addLeader.put("UserName", UserName);
                            addLeader.put("PID", pid);
                            addLeader.put("UID", myUid);
                            rootRef.setValue(addLeader).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Here Add to membership
                                    DatabaseReference rootRef = FirebaseDatabase.getInstance()
                                            .getReference().getRoot().child("Membership").child(myUid).child(pid);
                                    Map<String, Object> addLeader = new HashMap<String,Object>();
                                    addLeader.put("PID", pid);
                                    addLeader.put("UID", myUid);
                                    rootRef.setValue(addLeader).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(AddProject.this, "Project Created", Toast.LENGTH_SHORT).show();
                                            setResult(1);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void getUserID(){
        DatabaseReference getUserNameRef = FirebaseDatabase.getInstance().getReference().child("Users/"
                + myUid );
        getUserNameRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User u = dataSnapshot.getValue(User.class);
                        UserName = u.username;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AddProject.this, "Error Encountered", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
