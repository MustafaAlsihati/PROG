let TaskDetails = [];

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        var currentUser = firebase.auth().currentUser;
        var tasksRef = firebase.database().ref('ProjectTasks')
            .orderByChild('UserAssignedID').equalTo(currentUser.uid);

        tasksRef.once('value', (snapshot) => {
            if (!snapshot.exists()) {
                var htmlcode = '<h5 class="grey-text center">No Tasks Available</h5>';
                document.getElementById("TasksList").insertAdjacentHTML('beforeend', htmlcode);
                return;
            }
            snapshot.forEach((child) => {
                TaskDetails.push({
                    "tid": child.key,
                    "taskname": child.val().TaskName,
                    "taskdescription": child.val().TaskDescription,
                    "taskduedate": child.val().TaskDueDate,
                    "status": child.val().Status
                });
            });
            // Add the Checkboxes:
            for (i = 0; i < TaskDetails.length; i++) {
                var htmlcode;
                if (TaskDetails[i].status == 'uncompleted') {
                    htmlcode = '<p><label><input onchange=onChange(' + i + ') id=' + TaskDetails[i].tid + ' type="checkbox"/><span id="taskStatus' + TaskDetails[i].tid + '"><b>' + TaskDetails[i].taskname + ':</b> ' + TaskDetails[i].taskdescription + ' <i>(Due: ' + TaskDetails[i].taskduedate + ')</i></span><div class="divider"></div></label></p>';
                } else {
                    htmlcode = '<p><label><input checked="checked" onchange=onChange(' + i + ') id=' + TaskDetails[i].tid + ' type="checkbox"/><span id="taskStatus' + TaskDetails[i].tid + '" class="completed"><b>' + TaskDetails[i].taskname + ':</b> ' + TaskDetails[i].taskdescription + ' <i>(Due: ' + TaskDetails[i].taskduedate + ')</i></span><div class="divider"></div></label></p>';
                }
                document.getElementById("TasksList").insertAdjacentHTML('beforeend', htmlcode);
            }
        });
    }
});

function onChange(i) {
    var cb = document.getElementById(TaskDetails[i].tid);
    var task = document.getElementById("taskStatus" + TaskDetails[i].tid);
    if (cb.checked == true) {
        console.log(TaskDetails[i].tid + ": is completed");
        task.classList.add("completed");
        var tasksRef = firebase.database().ref('ProjectTasks/' + TaskDetails[i].tid + '/Status');
        tasksRef.set('completed')
    } else {
        console.log(TaskDetails[i].tid + ": is uncompleted");
        task.classList.remove("completed");
        var tasksRef = firebase.database().ref('ProjectTasks/' + TaskDetails[i].tid + '/Status');
        tasksRef.set('uncompleted')
    }
}