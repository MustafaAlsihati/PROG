function resetPassword() {
    var email = document.getElementById("resetPassEmail").value;
    firebase.auth().sendPasswordResetEmail(email).then(function () {
        alert('Password Reset Email Has Been Sent!');
    }).catch(function (error) {
        var errorCode = error.code;
        if (errorCode == 'auth/invalid-email') {
            alert("Invalid Email");
        } else if (errorCode == 'auth/user-not-found') {
            alert("User Not Found.\nPlease Check the Entered Email Again");
        }
    });
}