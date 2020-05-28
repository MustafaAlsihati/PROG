/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTask extends AppCompatActivity {

    String ProjectID;
    EditText addTaskName, addTaskDesc;
    Spinner membersList;
    DatePicker taskDueDate;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);
        setTitle("Create New Task");

        addTaskName = (EditText)findViewById(R.id.addTaskNameField);
        addTaskDesc = (EditText)findViewById(R.id.addTaskDescField);
        membersList = (Spinner) findViewById(R.id.memberListSpinner);
        taskDueDate = (DatePicker) findViewById(R.id.TaskDatePicker);
        submit = (Button) findViewById(R.id.add_task_submit_btn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ProjectID = extras.getString("ProjectID");

            final DatabaseReference ProjectRef = FirebaseDatabase.getInstance()
                    .getReference().child("ProjectMembers/" + ProjectID);
            ProjectRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                //Get map of the projects in datasnapshot
                                collectMembers((Map<String, Object>) dataSnapshot.getValue());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(AddTask.this,
                                    "Error encountered, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });

            submitBtn(ProjectID);
        } else {
            Toast.makeText(AddTask.this, "Error encountered, please try again",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void collectMembers(Map<String,Object> members){
        final ArrayList<String> membersNames = new ArrayList<>();
        membersNames.add("Choose A Group Member");

        for (Map.Entry<String, Object> entry : members.entrySet()){
            //Get Projects map
            Map singleMember = (Map) entry.getValue();
            membersNames.add((String) singleMember.get("UserName"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, membersNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        membersList.setAdapter(adapter);
    }

    public String getUID(Map<String,Object> members){
        Map singleMember = null;
        for (Map.Entry<String, Object> entry : members.entrySet()){
            //Get Projects map
            singleMember = (Map) entry.getValue();
        }
        return (String) singleMember.get("UID");
    }

    public void submitBtn(final String pid){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = addTaskName.getText().toString();
                final String desc = addTaskDesc.getText().toString();
                final String member = membersList.getSelectedItem().toString();
                final String dueDate = taskDueDate.getDayOfMonth() + "/" +
                        (taskDueDate.getMonth()+1) + "/" +
                        taskDueDate.getYear();

                if( name.isEmpty() ){
                    Toast.makeText(AddTask.this, "Please enter task name",
                            Toast.LENGTH_SHORT).show();
                    return;
                } if( desc.isEmpty() ){
                    Toast.makeText(AddTask.this, "Please enter task description",
                            Toast.LENGTH_SHORT).show();
                    return;
                } if(member.equals("Choose A Group Member")){
                    Toast.makeText(AddTask.this, "Please Choose member to assign task to",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseDatabase.getInstance()
                    .getReference().child("ProjectMembers")
                    .child(pid).orderByChild("UserName").equalTo(member).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String uid = getUID((Map<String, Object>) dataSnapshot.getValue());
                            Map<String, String> newTask = new HashMap<>();
                            DatabaseReference Tasks = FirebaseDatabase.getInstance()
                                    .getReference("ProjectTasks");
                            newTask.put("TaskName", name);
                            newTask.put("TaskDescription", desc);
                            newTask.put("AssignedMemberName", member);
                            newTask.put("TaskDueDate", dueDate);
                            newTask.put("Status", "uncompleted");
                            newTask.put("UserAssignedID", uid);
                            newTask.put("ProjectID", ProjectID);
                            Tasks.push().setValue(newTask).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    setResult(1);
                                    finish();
                                    Toast.makeText(AddTask.this,
                                            "Task Created Successfully", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(AddTask.this,
                                    "Error Encountered", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
