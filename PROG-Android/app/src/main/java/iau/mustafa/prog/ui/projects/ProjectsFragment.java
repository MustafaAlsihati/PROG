/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog.ui.projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import iau.mustafa.prog.AddProject;
import iau.mustafa.prog.MainMenu;
import iau.mustafa.prog.NetworkCheck;
import iau.mustafa.prog.Project;
import iau.mustafa.prog.Project_page;
import iau.mustafa.prog.R;
import iau.mustafa.prog.User;

public class ProjectsFragment extends Fragment {

    private View view;
    TabHost tabHost;
    private FirebaseAuth firebaseAuth;
    String myUid;
    int REQUEST_CODE;
    private ProgressBar allProjects_PG, myProjects_PG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_projects, container, false);

        allProjects_PG = (ProgressBar)view.findViewById(R.id.allProjectsProgressBar);
        myProjects_PG = (ProgressBar)view.findViewById(R.id.myProjectsProgressBar);

        allProjects_PG.setVisibility(View.VISIBLE);
        myProjects_PG.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getCurrentUser().getUid();

        tabHost = (TabHost) view.findViewById(R.id.tabHost_projects);
        tabHost.setup();

        //Tab 1: All Projects
        TabHost.TabSpec spec = tabHost.newTabSpec("All Projects");
        spec.setContent(R.id.tab1);
        spec.setIndicator("All Projects");
        tabHost.addTab(spec);

        //Tab 2: My Projects
        spec = tabHost.newTabSpec("My Projects");
        spec.setContent(R.id.tab2);
        spec.setIndicator("My Projects");
        tabHost.addTab(spec);

        System.out.println("My ID: " + myUid);

        if(NetworkCheck.IsNetworkConnected(getContext())) {
            Query allProjectRef = FirebaseDatabase.getInstance()
                    .getReference("Membership").child(myUid);

            Query myProjectRef = FirebaseDatabase.getInstance()
                    .getReference("Projects").orderByChild("creator").equalTo(myUid);

            // All Projects:
            allProjectRef.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                collectAllProjects((Map<String, Object>) dataSnapshot.getValue());
                                allProjects_PG.setVisibility(View.GONE);
                            } else {
                                allProjects_PG.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });

            // My Projects
            myProjectRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //Get map of the projects in datasnapshot
                                collectMyProjects((Map<String, Object>) dataSnapshot.getValue());
                                myProjects_PG.setVisibility(View.GONE);
                            } else {
                                myProjects_PG.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Unable to retrieve data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            //End of Projects Lists

        } else {
            Toast.makeText(getContext(), "No Connection", Toast.LENGTH_SHORT).show();
            allProjects_PG.setVisibility(View.GONE);
            myProjects_PG.setVisibility(View.GONE);
        }
        // Add Project Button:
        FloatingActionButton fab = view.findViewById(R.id.addProject);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddProject.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        return view;
    }

    private void collectAllProjects(Map<String,Object> projects) {
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
                            final ListView lv = (ListView) view.findViewById(R.id.all_projects_list);
                            final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                                    (view.getContext(), android.R.layout.simple_list_item_1, ProjectNames);
                            lv.setAdapter(arrayAdapter1);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getContext(), Project_page.class);
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
                        Toast.makeText(getContext(), "Error Encountered",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void collectMyProjects(Map<String,Object> projects) {
        final ArrayList<String> ProjectNames = new ArrayList<>();
        final ArrayList<String> ProjectIDs = new ArrayList<>();

        for (Map.Entry<String, Object> entry : projects.entrySet()){
            //Get Projects map
            Map singleProject = (Map) entry.getValue();
            ProjectNames.add((String) singleProject.get("projectName"));
            ProjectIDs.add(entry.getKey());
        }

        final ListView lv = (ListView) view.findViewById(R.id.myprojects_list);
        final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>
                (view.getContext(), android.R.layout.simple_list_item_1, ProjectNames);
        lv.setAdapter(arrayAdapter1);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), Project_page.class);
                intent.putExtra("ProjectID", ProjectIDs.get(position));
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            Intent intent = new Intent(getContext(), MainMenu.class);
            getActivity().finish();
            startActivity(intent);
        }
    }


}