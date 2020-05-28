var pid;
var taskName;
var taskDesc;
var taskDueDate;
var taskMember;
var members = [];

window.onload = function() {
    pid = window.location.search.substr(1);

    $(document).ready(function(){
        $('.datepicker').datepicker();
        $('.datepicker').datepicker({ 
            format: 'dd/mm/yyyy'
        });
    });

    init();

    $(document).ready(function(){
        $('select').formSelect();
    });
}

function init(){
    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
            getMembers();
        }
    });
}

function createTask(){
    taskName = document.getElementById("Task_Name").value;
    taskDesc = document.getElementById("Task_Desc").value;
    taskDueDate = document.getElementById("Task_DueDate").value;
    taskMember = document.getElementById("membersListSelect").value;

    if(taskName == ''){
        alert("Please Enter Task Name");
        return;
    }
    if(taskDesc == ''){
        alert("Please Enter Task Description");
        return;
    }
    if(taskDueDate == ''){
        alert("Please Enter Task Due Date");
        return;
    }
    if(taskMember == -1){
        alert("Please select which member to assign task to");
        return;
    }
    console.log(taskName);
    console.log(taskDesc);
    console.log(taskDueDate);
    console.log(members[taskMember].uid+': ' +members[taskMember].name);
    
    var taskAddRef = firebase.database().ref('ProjectTasks');
    taskAddRef.push().set({
        TaskName: taskName,
        TaskDescription: taskDesc,
        TaskDueDate: taskDueDate,
        UserAssignedID: members[taskMember].uid,
        AssignedMemberName: members[taskMember].name,
        ProjectID: pid,
        Status: 'uncompleted'
    }).then(success => {
        window.alert("Task Added To: " + members[taskMember].name + ", Due in: " + taskDueDate);
        window.location.replace("project.html?" + pid);
    });
}

function getMembers(){
    var projectMembersRef = firebase.database().ref('ProjectMembers/'+pid);
    projectMembersRef.once('value', (snapshot) => {
        snapshot.forEach((child) => {
            members.push({
                name: child.val().UserName,
                uid: child.val().UID
            });
        });
        //Get members in Select input:
        for (let i = 0; i < members.length; i++) {
            $("select").append(
                $("<option></option>").attr("value",i).text(members[i].name)
            );
            $('select').trigger('contentChanged');
        }
    });
}