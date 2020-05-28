let projectDetals = [];

$(document).ready(function(){
    $('.fixed-action-btn').floatingActionButton();
});

var btn = document.getElementById("addProject");
btn.addEventListener('click', function() {
  console.log("Float Button Clicked");
  window.location.href = "addproject.html";
});

firebase.auth().onAuthStateChanged(function(user) {
    if (user) {
        var currentUser = firebase.auth().currentUser;
        // Search:
        var projectDetailssRef = firebase.database().ref('Projects').orderByChild('creator').equalTo(currentUser.uid);
        projectDetailssRef.once('value', (snapshot) => {
            if (snapshot.exists()) {
                snapshot.forEach((child) => {
                    var htmlcode = '<a href="project.html?'+ child.key + '" class="collection-item">'+ child.val().projectName +'<p style="font-size: 8pt; margin: 0px; color: gray;"><i>'+ child.val().description +'</i></p></a>';
                    document.getElementById("myProjectsResult").insertAdjacentHTML('beforeend', htmlcode);
                });
            } else {
                var htmlcode = '<h5 class="grey-text center">You didnt create any project, try creating one :)</h5>';
                document.getElementById("myProjectsResult").insertAdjacentHTML('beforeend', htmlcode);
            }
        });
    }
});