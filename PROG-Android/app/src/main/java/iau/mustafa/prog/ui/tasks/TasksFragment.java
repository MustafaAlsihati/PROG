/*
 * Copyright (c) PROG's Team (Mustafa AlSihati Team).
 * This Project is currently an academic project for educational purposes.
 * This Project May be used for benefits for the working team.
 * Fully owned by the application developers.
 */

package iau.mustafa.prog.ui.tasks;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
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
import iau.mustafa.prog.NetworkCheck;
import iau.mustafa.prog.R;

public class TasksFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    LinearLayout myTasksList;
    private CheckBox tasksListBox;
    private ProgressBar pg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        myTasksList = (LinearLayout) view.findViewById(R.id.myTasksList);
        pg = (ProgressBar)view.findViewById(R.id.TasksProgressBar) ;

        pg.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        String myUid = firebaseAuth.getCurrentUser().getUid();

        if(NetworkCheck.IsNetworkConnected(getContext())) {
            Query ProjectRef = FirebaseDatabase.getInstance()
                    .getReference("ProjectTasks").orderByChild("UserAssignedID").equalTo(myUid);
            ProjectRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //Get map of the user tasks in datasnapshot
                                collectTasks((Map<String, Object>) dataSnapshot.getValue());
                                pg.setVisibility(View.GONE);
                            } else {
                                pg.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No Connection", Toast.LENGTH_SHORT).show();
            pg.setVisibility(View.GONE);
        }

        return view;
    }

    private void collectTasks(Map<String,Object> tasks) {
        final ArrayList<String> tasksNames = new ArrayList<>();
        final ArrayList<String> taskDescs = new ArrayList<>();
        final ArrayList<String> ProjectsJoined = new ArrayList<>();
        final ArrayList<String> TasksID = new ArrayList<>();
        final ArrayList<String> TaskStatus = new ArrayList<>();
        final ArrayList<String> TasksDueDates = new ArrayList<>();

        for (Map.Entry<String, Object> entry : tasks.entrySet()){
            //Get Projects map
            Map singleTask = (Map) entry.getValue();
            tasksNames.add((String) singleTask.get("TaskName"));
            taskDescs.add((String) singleTask.get("TaskDescription"));
            ProjectsJoined.add((String) singleTask.get("ProjectID"));
            TaskStatus.add((String) singleTask.get("Status"));
            TasksDueDates.add((String) singleTask.get("TaskDueDate"));
            TasksID.add(entry.getKey());
        }

        for(int i=0; i<tasksNames.size();i++){
            DatabaseReference rootRef = FirebaseDatabase.getInstance()
                    .getReference("ProjectMembers");
            final int final_i = i;
            try {
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild(ProjectsJoined.get(final_i))) {
                            tasksListBox = new CheckBox(getContext());
                            tasksListBox.setId(final_i);
                            tasksListBox.setText(tasksNames.get(final_i));
                            tasksListBox.setTextSize(20);
                            myTasksList.addView(tasksListBox);
                            // Highlight Completed Tasks:
                            if (TaskStatus.get(final_i).equals("completed")) {
                                tasksListBox.setChecked(true);
                                tasksListBox.setPaintFlags(tasksListBox.getPaintFlags()
                                        | Paint.STRIKE_THRU_TEXT_FLAG);
                                tasksListBox.setTextColor(Color.parseColor("#00e676"));
                            }
                            // Notification About Uncompleted Tasks:
                            if(TaskStatus.get(final_i).equals("uncompleted")){
                                String title = tasksNames.get(final_i);
                                String body = "Due date at: " + TasksDueDates.get(final_i);
                                showNotification(getContext(), title, body, getActivity().getIntent());
                            }

                            tasksListBox.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    String message = taskDescs.get(final_i) + "\n\nDue Date: " + TasksDueDates.get(final_i);
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                    alertDialog.setTitle(tasksNames.get(final_i));
                                    alertDialog.setMessage(message);
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                    return false;
                                }
                            });
                            tasksListBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        // Update the value to completed Here
                                        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                                                .getReference("ProjectTasks").child(TasksID.get(final_i));
                                        Map<String, Object> updates = new HashMap<String, Object>();

                                        updates.put("Status", "completed");
                                        rootRef.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Change Text style based on status
                                                buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                                buttonView.setTextColor(Color.parseColor("#00e676"));
                                            }
                                        });
                                    } else {
                                        // Update the value to uncompleted Here
                                        DatabaseReference rootRef = FirebaseDatabase.getInstance()
                                                .getReference("ProjectTasks").child(TasksID.get(final_i));
                                        Map<String, Object> updates = new HashMap<String, Object>();

                                        updates.put("Status", "uncompleted");
                                        rootRef.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Change Text style based on status
                                                buttonView.setPaintFlags(buttonView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                                buttonView.setTextColor(Color.parseColor("#000000"));
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Unable to retrieve data", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e){
                //
            }
        }
    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_LOW;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo_prog)
                .setContentTitle(title)
                .setSound(null)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }
}
