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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Project_page extends AppCompatActivity {

    Context context;
    String projectName, projectDesc, dueDate, creatorID;
    String UserName;
    TextView dueDateTextView, projectNameTextView, projectDescription, membersNumber, ProjectTaskList;
    View line2;
    LinearLayout memberList;
    Button join;
    private FirebaseAuth firebaseAuth;
    String ProjectID;
    ListView CompletedTasksView, UncompletedTasksView;
    TabHost tabHost;
    int currentposition;
    String myUID;
    int REQUEST_CODE;

    EditText nameUpdate;
    EditText descUpdate;
    Spinner memberUpdate;
    DatePicker DueDate;

    ArrayList<String> UncompletedTasksIDs = new ArrayList<>();
    ArrayList<String> UncompletedTasksNames = new ArrayList<>();
    ArrayList<String> UncompletedTasksDescs = new ArrayList<>();
    ArrayList<String> UncompletedTasksAssignation = new ArrayList<>();
    ArrayList<String> UncompletedTasksDates = new ArrayList<>();
    ArrayList<String> UncompletedTasksStatus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_page);
        context = this;

        if(!NetworkCheck.IsNetworkConnected(context)){
            Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        tabHost = (TabHost) findViewById(R.id.tasks_tabs);
        tabHost.setup();

        //Tab 1: Completed Tasks
        TabHost.TabSpec spec = tabHost.newTabSpec("Uncompleted Tasks");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Uncompleted Tasks");
        tabHost.addTab(spec);

        //Tab 2: Uncompleted Tasks
        spec = tabHost.newTabSpec("Completed Tasks");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Completed Tasks");
        tabHost.addTab(spec);

        firebaseAuth = FirebaseAuth.getInstance();
        myUID=firebaseAuth.getCurrentUser().getUid();
        getUserName();

        dueDateTextView = (TextView)findViewById(R.id.dueDate);
        projectNameTextView = (TextView)findViewById(R.id.projectName);
        projectDescription = (TextView)findViewById(R.id.projectDescription);
        membersNumber = (TextView)findViewById(R.id.membersNumber);
        join = (Button) findViewById(R.id.join_or_leave_btn);
        memberList = (LinearLayout)findViewById(R.id.ClickToMembersList);
        CompletedTasksView = (ListView)findViewById(R.id.completedTasksView);
        UncompletedTasksView = (ListView)findViewById(R.id.uncompletedTasksView);
        ProjectTaskList = (TextView)findViewById(R.id.ProjectTaskList);
        line2 = (View)findViewById(R.id.line2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ProjectID = extras.getString("ProjectID");

            //Uncompleted Tasks
            Query UncompletedTasksRef = FirebaseDatabase.getInstance()
                    .getReference("ProjectTasks").orderByChild("Status").equalTo("uncompleted");
            UncompletedTasksRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                showUncompletedTasks((Map<String, Object>) dataSnapshot.getValue(), UncompletedTasksView);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });
            registerForContextMenu(UncompletedTasksView);

            //Completed Tasks
            Query CompletedTasksRef = FirebaseDatabase.getInstance()
                    .getReference("ProjectTasks").orderByChild("Status").equalTo("completed");
            CompletedTasksRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                showCompletedTasks((Map<String, Object>) dataSnapshot.getValue(), CompletedTasksView);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });

            memberList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewMembers(ProjectID);
                }
            });
            if(NetworkCheck.IsNetworkConnected(this)) {
                membersCount();
                DatabaseReference ProjectRef = FirebaseDatabase.getInstance().getReference().child("Projects/" + ProjectID);
                ProjectRef.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    Project p = dataSnapshot.getValue(Project.class);
                                    projectName = p.projectName;
                                    projectDesc = p.description;
                                    dueDate = p.DueDate;
                                    creatorID = p.creator;
                                    setTitle(projectName);
                                    dueDateTextView.setText(dueDate);
                                    projectNameTextView.setText(projectName);
                                    projectDescription.setText(projectDesc);

                                    authority(creatorID);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "No Connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error Loading Page", Toast.LENGTH_SHORT).show();
        }

    }

    public void getUserName(){
        DatabaseReference getUserNameRef = FirebaseDatabase.getInstance().getReference().child("Users/"
                + firebaseAuth.getCurrentUser().getUid() );
        getUserNameRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User u = dataSnapshot.getValue(User.class);
                        UserName = u.username;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(context, "Error Encountered", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void authority(final String uid){
        FloatingActionButton fab = findViewById(R.id.addTask);
        if(Objects.equals(uid, firebaseAuth.getCurrentUser().getUid())){
            // Start of Floating Btn
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, AddTask.class);
                    intent.putExtra("ProjectID", ProjectID);
                    startActivityForResult(intent,REQUEST_CODE);
                }
            });
            // End of Floating Btn
            ProjectTaskList.setVisibility(View.VISIBLE);
            line2.setVisibility(View.VISIBLE);
            UncompletedTasksView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    currentposition = info.position;
                    menu.add(0, v.getId(), 0, "Edit");
                    menu.add(0, v.getId(), 0, "Delete");

                }
            });
            join.setText("Delete Project");
            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(1);
                    finish();
                    DatabaseReference deleteProject = FirebaseDatabase.getInstance().getReference()
                            .getRoot().child("Projects").child(ProjectID);
                    deleteProject.setValue(null);
                    DatabaseReference deleteMemberships = FirebaseDatabase.getInstance()
                            .getReference().getRoot().child("Membership")
                            .child(myUID).child(ProjectID);
                    deleteMemberships.setValue(null);
                    DatabaseReference deleteProjectMembers = FirebaseDatabase.getInstance()
                            .getReference().getRoot().child("ProjectMembers").child(ProjectID);
                    deleteProjectMembers.setValue(null);
                }
            });
        } else {
            fab.hide();
            final DatabaseReference refProject = FirebaseDatabase.getInstance()
                    .getReference("ProjectMembers").child(ProjectID);
            refProject.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                join.setText("Leave");
                                join.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        refProject.child(firebaseAuth.getCurrentUser().getUid())
                                                .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                DatabaseReference deleteMembership = FirebaseDatabase.getInstance()
                                                        .getReference().getRoot()
                                                        .child("Membership").child(myUID).child(ProjectID);
                                                deleteMembership.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            } else {
                                join.setText("Join");
                                join.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Map<String, String> userJoin = new HashMap<>();
                                        userJoin.put("UID", firebaseAuth.getCurrentUser().getUid());
                                        userJoin.put("PID", ProjectID);
                                        userJoin.put("UserName", UserName);

                                        refProject.child(firebaseAuth.getCurrentUser().getUid()).setValue(userJoin)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Map<String, String> userJoin = new HashMap<>();
                                                userJoin.put("UID", firebaseAuth.getCurrentUser().getUid());
                                                userJoin.put("PID", ProjectID);
                                                DatabaseReference addMembership = FirebaseDatabase.getInstance()
                                                        .getReference("Membership").child(myUID).child(ProjectID);
                                                addMembership.setValue(userJoin).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent intent = getIntent();
                                                        finish();
                                                        startActivity(intent);
                                                        Toast.makeText(context, "Joined Successfully", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });

        }
    }

    public void membersCount(){
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().getRoot();
        mDatabaseRef.child("ProjectMembers/" + ProjectID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count= dataSnapshot.getChildrenCount();
                membersNumber.setText(String.valueOf(count));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }



    public void viewMembers(String pid){
        Intent intent = new Intent(this, MembersActivity.class);
        intent.putExtra("PID", pid);
        startActivity(intent);
    }

    private void showCompletedTasks(@NonNull Map<String,Object> tasks, ListView lv) {

        ArrayList<String> CompletedTasksIDs = new ArrayList<>();
        final ArrayList<String> CompletedTasksNames = new ArrayList<>();
        final ArrayList<String> CompletedTasksDescs = new ArrayList<>();
        final ArrayList<String> CompletedTasksAssignation = new ArrayList<>();
        final ArrayList<String> CompletedTasksDates = new ArrayList<>();
        final ArrayList<String> CompletedTasksStatus = new ArrayList<>();

        for (Map.Entry<String, Object> entry : tasks.entrySet()){
            //Get Projects map
            Map singleRow = (Map) entry.getValue();
            if(((String) singleRow.get("ProjectID")).equals(ProjectID)) {
                CompletedTasksNames.add((String) singleRow.get("TaskName"));
                CompletedTasksDescs.add((String) singleRow.get("TaskDescription"));
                CompletedTasksAssignation.add((String) singleRow.get("UserAssignedID"));
                CompletedTasksDates.add((String) singleRow.get("TaskDueDate"));
                CompletedTasksStatus.add((String) singleRow.get("Status"));
                CompletedTasksIDs.add(entry.getKey());
            }
        }

        final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, CompletedTasksNames);
        lv.setAdapter(arrayAdapter1);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                DatabaseReference getUserNameRef = FirebaseDatabase.getInstance()
                        .getReference("Users").child(CompletedTasksAssignation.get(position));
                getUserNameRef.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User u = dataSnapshot.getValue(User.class);
                                String UserName = u.username;
                                String message = CompletedTasksDescs.get(position)
                                        + "\n\n" + getResources().getString(R.string.task_user_assigned) + UserName
                                        + "\n" + getResources().getString(R.string.task_due_date) + CompletedTasksDates.get(position)
                                        + "\n" + getResources().getString(R.string.Task_status) + CompletedTasksStatus.get(position);

                                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle(CompletedTasksNames.get(position));
                                alertDialog.setMessage(message);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, "Error Encountered", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void showUncompletedTasks(@NonNull Map<String,Object> tasks, ListView lv) {

        for (Map.Entry<String, Object> entry : tasks.entrySet()){
            //Get Projects map
            Map singleRow = (Map) entry.getValue();
            if(((String) singleRow.get("ProjectID")).equals(ProjectID)) {
                UncompletedTasksNames.add((String) singleRow.get("TaskName"));
                UncompletedTasksDescs.add((String) singleRow.get("TaskDescription"));
                UncompletedTasksAssignation.add((String) singleRow.get("UserAssignedID"));
                UncompletedTasksDates.add((String) singleRow.get("TaskDueDate"));
                UncompletedTasksStatus.add((String) singleRow.get("Status"));
                UncompletedTasksIDs.add(entry.getKey());
            }
        }

        final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, UncompletedTasksNames);
        lv.setAdapter(arrayAdapter1);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                DatabaseReference getUserNameRef = FirebaseDatabase.getInstance()
                        .getReference("Users").child(UncompletedTasksAssignation.get(position));
                getUserNameRef.addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User u = dataSnapshot.getValue(User.class);
                                String UserName = u.username;
                                String message = UncompletedTasksDescs.get(position)
                                        + "\n\n" + getResources().getString(R.string.task_user_assigned) + UserName
                                        + "\n" + getResources().getString(R.string.task_due_date) + UncompletedTasksDates.get(position)
                                        + "\n" + getResources().getString(R.string.Task_status) + UncompletedTasksStatus.get(position);

                                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle(UncompletedTasksNames.get(position));
                                alertDialog.setMessage(message);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, "Error Encountered", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final DatabaseReference refProject = FirebaseDatabase.getInstance()
                .getReference("ProjectTasks").child(UncompletedTasksIDs.get(currentposition));

        if(item.getTitle()=="Edit"){
            editTasks(currentposition);
        } else if(item.getTitle()=="Delete") {
            new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure? Data will not be restored!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        refProject.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }})
                .setNegativeButton(android.R.string.no, null).show();
        }
        return true;
    }

    public void editTasks(final int i){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Edit Task");
        builder.setMessage("Enter contact information below to update");

        LayoutInflater li = LayoutInflater.from(context);
        final View dialogView = li.inflate(R.layout.update_task, null);

        nameUpdate = (EditText) dialogView.findViewById(R.id.updateName);
        descUpdate = (EditText) dialogView.findViewById(R.id.updateDesc);
        memberUpdate = (Spinner) dialogView.findViewById(R.id.memberList2Spinner);
        DueDate = (DatePicker) dialogView.findViewById(R.id.TaskDatePicker2);
        final DatabaseReference ProjectRef = FirebaseDatabase.getInstance()
                .getReference().child("ProjectMembers/" + ProjectID);
        ProjectRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            //Get map of the projects in datasnapshot
                            collectMembers((Map<String, Object>) dataSnapshot.getValue());
                            String [] dateParts = UncompletedTasksDates.get(i).split("/");
                            String day = dateParts[0];
                            String month = dateParts[1];
                            String year = dateParts[2];

                            nameUpdate.setText(UncompletedTasksNames.get(i));
                            descUpdate.setText(UncompletedTasksDescs.get(i));
                            DueDate.updateDate(Integer.parseInt(year),Integer.parseInt(month)-1,Integer.parseInt(day));

                            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String name = nameUpdate.getText().toString();
                                    final String desc = descUpdate.getText().toString();
                                    final String dueDate = DueDate.getDayOfMonth() + "/" +
                                            (DueDate.getMonth()+1) + "/" +
                                            DueDate.getYear();
                                    final String member = memberUpdate.getSelectedItem().toString();

                                    if( name.isEmpty() ){
                                        Toast.makeText(Project_page.this, "Please enter task name",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    } if( desc.isEmpty() ){
                                        Toast.makeText(Project_page.this, "Please enter task description",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    } if(member.equals("Choose A Group Member")){
                                        Toast.makeText(Project_page.this, "Please Choose member to assign task to",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    FirebaseDatabase.getInstance()
                                            .getReference().child("ProjectMembers")
                                            .child(ProjectID).orderByChild("UserName").equalTo(member).addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String uid = getUID((Map<String, Object>) dataSnapshot.getValue());
                                                    Map<String, Object> userJoin = new HashMap<>();
                                                    userJoin.put("TaskName", name);
                                                    userJoin.put("TaskDescription", desc);
                                                    userJoin.put("TaskDueDate", dueDate);
                                                    userJoin.put("UserAssignedID", uid);
                                                    userJoin.put("AssignedMemberName", member);

                                                    DatabaseReference refProject = FirebaseDatabase.getInstance()
                                                            .getReference("ProjectTasks")
                                                            .child(UncompletedTasksIDs.get(i));
                                                    refProject.updateChildren(userJoin)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Intent intent = getIntent();
                                                                    finish();
                                                                    startActivity(intent);
                                                                    Toast.makeText(context, "Updated", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    Toast.makeText(Project_page.this,
                                                            "Error Encountered", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                            // Cancel:
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.setView(dialogView);
                            builder.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Project_page.this,
                                "Error encountered, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String getUID(Map<String,Object> members){
        Map singleMember = null;
        for (Map.Entry<String, Object> entry : members.entrySet()){
            //Get Projects map
            singleMember = (Map) entry.getValue();
        }
        return (String) singleMember.get("UID");
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
        memberUpdate.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.shareWhatsApp) {
            // Here Share ProjectID to WhatsApp:
            Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
            whatsAppIntent.setType("text/plain");
            whatsAppIntent.setPackage("com.whatsapp");
            whatsAppIntent.putExtra(Intent.EXTRA_TEXT, ProjectID);
            try {
                startActivity(whatsAppIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(Project_page.this,
                        "Unable to share to WhatsApp", Toast.LENGTH_LONG).show();
            }
        } else if(item.getItemId()==android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        setResult(1);
        finish();
    }
}
