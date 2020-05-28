var projectName;
var projectDesc;
var projectDueDate;

$(document).ready(function(){
    $('.datepicker').datepicker();
    $('.datepicker').datepicker({ 
        format: 'dd/mm/yyyy'
    });
});

function createProject(){
    firebase.auth().onAuthStateChanged(function(user) {
        var currentUser = firebase.auth().currentUser;
        var uid = currentUser.uid;
        if (user) {
            projectName = document.getElementById("Project_Name").value;
            projectDesc = document.getElementById("Project_Desc").value;
            projectDueDate = document.getElementById("Project_DueDate").value;
            
            var projectAddRef = firebase.database().ref('Projects');
            projectAddRef.push().set({
                projectName: projectName,
                description: projectDesc,
                DueDate: projectDueDate,
                creator: uid
            }).then(success => {
                window.alert("Project Created");
                window.location.replace("myprojects.html");
            });
        }
    });
}