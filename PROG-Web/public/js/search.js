function getResults() {
    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {
            document.getElementById("searchResult").innerHTML = "";
            var name = document.getElementById("searchField").value;

            console.log('input: ' + name);

            if (name == '') {
                return;
            }

            var SearchRef = firebase.database().ref('Projects')
                .orderByChild("projectName")
                .startAt(name).endAt(name + "\uf8ff").limitToFirst(10);
            // Get Results Start:
            SearchRef.once('value', (snapshot) => {
                if (snapshot.exists()) {
                    snapshot.forEach((child) => {
                        var htmlcode = '<a href="project.html?' +
                            child.key + '" class="collection-item">' +
                            child.val().projectName +
                            '<p style="font-size: 8pt; margin: 0px; color: gray;"><i>' +
                            child.val().description + '</i></p></a>';
                        document.getElementById("searchResult").innerHTML += htmlcode;
                    });
                } else {
                    var htmlcode = '<h5 class="grey-text center">Project Not Found :(</h5>';
                    document.getElementById("searchResult").innerHTML += htmlcode;
                }
            });
            // Get Results End;
        }
    });
}