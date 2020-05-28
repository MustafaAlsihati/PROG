var pid = window.location.search.substr(1);
var currentUser;
var userName;

$(document).ready(function () {
    $('.fixed-action-btn').floatingActionButton();
});

var btn = document.getElementById("addTask");
btn.addEventListener('click', function () {
    console.log("Float Button Clicked");
    window.location.href = "addtask.html?" + pid;
});

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        currentUser = firebase.auth().currentUser;
        getUserName();

        // Get Project Details:
        var projectRef = firebase.database().ref('Projects/' + pid);
        projectRef.once('value', (snapshot) => {
            var name = snapshot.val().projectName;
            var desc = '<i>' + snapshot.val().description + '</i>';
            var date = 'Due Date in: (<i>' + snapshot.val().DueDate + ')</i>';
            document.getElementById("project-name").innerHTML = name;
            document.getElementById("project-date").innerHTML = date;
            document.getElementById("project-desc").innerHTML = desc;
            document.title = name + " - PROG";
            if (snapshot.val().creator == currentUser.uid) {
                var btnText = 'Delete Project';
                var btn = document.getElementById("project-btn");
                btn.innerHTML = btnText;
                btn.addEventListener('click', function () {
                    var choice = confirm("Confirm deletion? Project Can't be restored!");
                    if (choice == true) {
                        var projectRef = firebase.database().ref('Projects/' + pid);
                        projectRef.remove();
                        var membersRef = firebase.database().ref('ProjectMembers/' + pid);
                        membersRef.remove();
                        var membersRef2 = firebase.database().ref('Membership/' + currentUser.uid + '/' + pid);
                        membersRef2.remove();
                        var deleteTasksRef = firebase.database().ref('ProjectTasks').orderByChild('ProjectID').equalTo(pid);
                        deleteTasksRef.remove();
                    }
                }, false);
            } else {
                //Check if Joined/Not:
                var projectRef = firebase.database().ref('ProjectMembers/' + pid + '/' + currentUser.uid + '/UID');
                projectRef.on('value', (snapshot) => {
                    console.log(snapshot.val());
                    if (snapshot.exists()) {
                        var btnText = 'Leave';
                        var btn = document.getElementById("project-btn");
                        btn.innerHTML = btnText;
                        btn.addEventListener('click', function () {
                            var choice = confirm("Confirm leaving?");
                            if (choice == true) {
                                var leaveRef = firebase.database().ref('ProjectMembers/' + pid + '/' + currentUser.uid);
                                leaveRef.remove();
                                var leave2Ref = firebase.database().ref('Membership/' + currentUser.uid + '/' + pid);
                                leave2Ref.remove();
                            }
                        }, false);
                    } else {
                        var btnText = 'Join';
                        var btn = document.getElementById("project-btn");
                        btn.innerHTML = btnText;
                        btn.addEventListener('click', function () {
                            var joinRef = firebase.database().ref('ProjectMembers/' + pid + '/' + currentUser.uid)
                            joinRef.set({
                                UID: currentUser.uid,
                                PID: pid,
                                UserName: userName
                            });
                            var join2Ref = firebase.database().ref('Membership/' + currentUser.uid + '/' + pid);
                            join2Ref.set({
                                UID: currentUser.uid,
                                PID: pid
                            });
                        }, false);
                    }
                });
            }
        });

        // Get Project Tasks:
        var tasksRef = firebase.database().ref('ProjectTasks').orderByChild('ProjectID').equalTo(pid);
        tasksRef.once('value', (snapshot) => {
            if (!snapshot.exists()) {
                var htmlcode = '<h5 class="grey-text center">No Tasks Available</h5>';
                document.getElementById("ProjectTasksList").insertAdjacentHTML('beforeend', htmlcode);
                return;
            }
            snapshot.forEach((child) => {
                if (child.val().Status == 'completed') {
                    var htmlcode = '<a href="#" class="collection-item completed"><b>' + child.val().TaskName + ': </b> ' +
                        child.val().TaskDescription + '<br><i> (Due: ' + child.val().TaskDueDate + ' Assigned To <b>' + child.val().AssignedMemberName + '</b>)</i></a>';
                    document.getElementById("ProjectTasksList").insertAdjacentHTML('beforeend', htmlcode);
                } else {
                    var htmlcode = '<a href="#" class="collection-item"><b>' + child.val().TaskName + ': </b> ' +
                        child.val().TaskDescription + '<br><i> (Due: ' + child.val().TaskDueDate + ' Assigned To <b>' + child.val().AssignedMemberName + '</b>)</i></a>';
                    document.getElementById("ProjectTasksList").insertAdjacentHTML('beforeend', htmlcode);
                }
            });
        });
    }
});

function getUserName() {
    var userRef = firebase.database().ref('Users/' + currentUser.uid);
    userRef.once('value').then(function (snapshot) {
        userName = snapshot.val().username;
    });
}