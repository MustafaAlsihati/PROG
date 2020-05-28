function isEmpty(str) {
    return (!str || 0 === str.length);
}

function init() {
    document.getElementById('Login').addEventListener('click', toggleSignIn, false);
    document.getElementById('Register').addEventListener('click', handleSignUp, false);
}

function toggleSignIn() {
    if (firebase.auth().currentUser) {
        console.log("Signed In already");
        alert("Signed In already");
    } else {
        var email = document.getElementById('LogEmail').value;
        var password = document.getElementById('LogPass').value;
        if (isEmpty(email)) {
            alert('Please enter an email address.');
            return;
        }
        if (password.length < 6) {
            alert('Please enter a valid password.');
            return;
        }

        firebase.auth().signInWithEmailAndPassword(email, password)
            .then(function (firebaseUser) {
                console.log("Success");
                window.location.replace("tasks.html");
            })
            .catch(function (error) {
                var errorCode = error.code;
                var errorMessage = error.message;
                if (errorCode === 'auth/wrong-password') {
                    alert('Wrong password.');
                } else {
                    alert(errorMessage);
                }
                console.log(error);
            });
    }
}

function handleSignUp() {
    var username = document.getElementById('RegName').value;
    var password = document.getElementById('RegPass').value;
    var email = document.getElementById('RegEmail').value;

    if (isEmpty(email)) {
        alert('Please enter an email address.');
        return;
    }
    if (password.length < 6) {
        alert('Please enter a valid password.');
        return;
    }
    if (password.includes(" ")) {
        alert('Spaces in password are not allowed.');
        return;
    }

    firebase.auth().createUserWithEmailAndPassword(email, password)
        .then(userData => {
            writeUserData(userData.user.uid, username);
        }).catch(function (error) {
            var errorCode = error.code;
            var errorMessage = error.message;
            console.log("Error : " + errorMessage);
        });
}

function writeUserData(userId, name) {
    firebase.database().ref('Users/' + userId).set({
        username: name
    }).then(success => {
        console.log(userId);
        window.alert("Registered");
        window.location.replace("tasks.html");
    });
}

window.onload = function () {
    init();
}