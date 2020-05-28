var currentUser;

window.onload = function() {
  init();
};

function init() {
  firebase.auth().onAuthStateChanged(function(user) {
    if (user) {
      currentUser = firebase.auth().currentUser;
      document.getElementById("editEmail").value = currentUser.email;

      var userRef = firebase.database().ref("Users/" + currentUser.uid);
      userRef.once("value").then(function(snapshot) {
        var userBio = snapshot.val().bio;
        document.getElementById("editBio").value = userBio;
      });
    }
  });
}

function updateEmail() {
  var email = document.getElementById("editEmail").value;
  var pass = document.getElementById("confirm_pass").value;

  firebase
    .auth()
    .signInWithEmailAndPassword(currentUser.email, pass)
    .then(function(userCredential) {
      userCredential.user
        .updateEmail(email)
        .then(function() {
          alert("Email Updated To: " + email);
        })
        .catch(function(error) {
          var errorCode = error.code;
          var errorMessage = error.message;
          if (errorCode === "auth/invalid-email") {
            alert("Invalid Email");
          } else {
            alert(errorMessage);
          }
          console.log(error);
        });
    })
    .catch(function(error) {
      var errorCode = error.code;
      var errorMessage = error.message;
      if (errorCode === "auth/wrong-password") {
        alert("Wrong password.");
      } else {
        alert(errorMessage);
      }
      console.log(error);
    });
}

function updateBio() {
  var editBio = document.getElementById("editBio").value;

  var bioUpdate = firebase.database().ref("Users/" + currentUser.uid + "/bio");
  bioUpdate.set(editBio).then(function() {
    alert("Bio Updated To: " + editBio);
  });
}
