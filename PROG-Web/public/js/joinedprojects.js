let projectDetals = [];

firebase.auth().onAuthStateChanged(function (user) {
    if (user) {
        var currentUser = firebase.auth().currentUser;
        var projectIdsRef = firebase.database().ref('Membership/' + currentUser.uid);

        //First Search:
        projectIdsRef.once('value', (snapshot) => {
            if (!snapshot.exists()) {
                var htmlcode = '<h5 class="grey-text center">No Projects Joined</h5>';
                document.getElementById("joinedProjectsResult")
                    .insertAdjacentHTML('beforeend', htmlcode);
                return;
            }
            snapshot.forEach((child) => {
                // Second Search:
                var projectDetailssRef = firebase.database().ref('Projects/' + child.val().PID);
                projectDetailssRef.once('value', (childSnapshot) => {
                    if (childSnapshot.exists()) {
                        var htmlcode = '<a href="project.html?' + childSnapshot.key +
                            '" class="collection-item">' + childSnapshot.val().projectName +
                            '<p style="font-size: 8pt; margin: 0px; color: gray;"><i>' +
                            childSnapshot.val().description + '</i></p></a>';
                        document.getElementById("joinedProjectsResult")
                            .insertAdjacentHTML('beforeend', htmlcode);
                    }
                });
            });
        });
    }
});