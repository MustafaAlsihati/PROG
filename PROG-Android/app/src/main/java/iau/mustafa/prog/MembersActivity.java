/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class MembersActivity extends AppCompatActivity {

    Context context;
    String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        context=this;

        setTitle("Members");

        if(!NetworkCheck.IsNetworkConnected(context)){
            Toast.makeText(context, "No Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pid = getIntent().getStringExtra("PID");
            Query ProjectRef = FirebaseDatabase.getInstance().getReference("ProjectMembers")
                    .child(pid).orderByChild("PID").equalTo(pid);
            ProjectRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //Get map of the projects in datasnapshot
                                collectUsers((Map<String, Object>) dataSnapshot.getValue());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Error Loading Page", Toast.LENGTH_SHORT).show();
        }

    }

    private void collectUsers(Map<String,Object> users) {
        final ArrayList<String> UserNames = new ArrayList<>();
        final ArrayList<String> Uids = new ArrayList<>();

        for (Map.Entry<String, Object> entry : users.entrySet()){
            //Get Projects map
            Map singleProject = (Map) entry.getValue();
            UserNames.add((String) singleProject.get("UserName"));
            Uids.add((String) singleProject.get("UID"));
        }

        final ListView lv = (ListView) findViewById(R.id.memberListView);
        final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                (context, android.R.layout.simple_list_item_1, UserNames);
        lv.setAdapter(arrayAdapter1);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, UsersProfile.class);
                intent.putExtra("UID", Uids.get(position));
                startActivity(intent);
            }
        });
    }
}
