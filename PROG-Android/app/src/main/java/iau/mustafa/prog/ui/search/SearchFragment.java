/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog.ui.search;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import iau.mustafa.prog.MainMenu;
import iau.mustafa.prog.NetworkCheck;
import iau.mustafa.prog.Project_page;
import iau.mustafa.prog.R;

public class SearchFragment extends Fragment {

    private EditText searchField;
    private Button searchBtn;
    private Switch searchSwitch;
    private LinearLayout searchList;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);

        searchField = (EditText) view.findViewById(R.id.searchEditText);
        searchBtn = (Button) view.findViewById(R.id.searchBtn);
        searchSwitch = (Switch) view.findViewById(R.id.searchSwitch);
        searchList = (LinearLayout) view.findViewById(R.id.search);

        if(NetworkCheck.IsNetworkConnected(getContext())) {
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkCheck.IsNetworkConnected(getContext())) {
                        if (searchList.getChildCount() > 0) {
                            searchList.removeAllViews();
                        }
                        if (!searchField.getText().toString().isEmpty()) {
                            searchResults();
                        } else {
                            Toast.makeText(getContext(), "No Results", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "No Connection", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    public void searchResults(){
        String project = searchField.getText().toString();
        Query byName = FirebaseDatabase.getInstance()
                .getReference("Projects").orderByChild("projectName")
                .startAt(project).endAt(project + "\uf8ff");
        Query byID = FirebaseDatabase.getInstance()
                .getReference("Projects").child(project);

        if(searchSwitch.isChecked()){
            // By ID:
            byID.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Get map of the projects in datasnapshot
                        collectProjects((Map<String, Object>) dataSnapshot.getValue());
                    } else {
                        Toast.makeText(getContext(),
                                "Unable to find, project may not be available",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Unable to retrieve data",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // By Name:
            byName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //Get map of the projects in datasnapshot
                        collectProjects((Map<String, Object>) dataSnapshot.getValue());
                    } else {
                        Toast.makeText(getContext(),
                                "Unable to find, project may not be available",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Unable to retrieve data",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    private void collectProjects(Map<String,Object> projects) {
        final ArrayList<String> ProjectNames = new ArrayList<>();
        final ArrayList<String> ProjectDescs = new ArrayList<>();
        final ArrayList<String> ProjectIDs = new ArrayList<>();

        for (Map.Entry<String, Object> entry : projects.entrySet()){
            //Get Projects map
            Map singleProject = (Map) entry.getValue();
            ProjectNames.add((String) singleProject.get("projectName"));
            ProjectDescs.add((String) singleProject.get("description"));
            ProjectIDs.add(entry.getKey());
        }

        for(int i=0; i<ProjectNames.size();i++) {
            LinearLayout ll = new LinearLayout(getContext());
            TextView title = new TextView(getContext());
            TextView desc = new TextView(getContext());

            ll.setOrientation(LinearLayout.VERTICAL);
            title.setText(ProjectNames.get(i));
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(14);
            title.setPadding(0,20,0,0);
            title.setTextColor(getResources().getColor(R.color.colorAccent));
            desc.setText(ProjectDescs.get(i));
            desc.setTypeface(null, Typeface.ITALIC);
            desc.setTextSize(13);
            desc.setPadding(0,0,0,20);
            ll.addView(title);
            ll.addView(desc);
            View viewDivider = new View(getContext());
            int dividerHeight = (int) (getResources().getDisplayMetrics().density * 1); // 1dp to pixels
            viewDivider.setLayoutParams(new RelativeLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));
            viewDivider.setBackgroundColor(Color.parseColor("#CECECE"));
            ll.addView(viewDivider);
            final int final_i = i;
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), Project_page.class);
                    intent.putExtra("ProjectID", ProjectIDs.get(final_i));
                    getActivity().setResult(1);
                    startActivity(intent);
                }
            });
            searchList.addView(ll);

        }
    }
}