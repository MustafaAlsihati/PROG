var userName = localStorage.getItem('userName');

firebase.auth().onAuthStateChanged(function (user) {
  if (user) {
    if (userName != null) {
      document.getElementById("userName").innerHTML = userName + "<i class='material-icons right'>arrow_drop_down</i>";
    } else {
      getUserName();
    }
  } else {
    window.location.replace("/login.html");
  }
});

function getUserName() {
  // Get UserName:
  var currentUser = firebase.auth().currentUser;
  var userRef = firebase.database().ref('Users/' + currentUser.uid);
  userRef.once('value').then(function (snapshot) {
    userName = snapshot.val().username;
    var saveUserName = userName;
    localStorage.setItem('userName', saveUserName);
    document.getElementById("userName").innerHTML = userName + "<i class='material-icons right'>arrow_drop_down</i>";
  });
}