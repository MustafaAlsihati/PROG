document.getElementById("LogoutBtn").addEventListener("click", function(){
    firebase.auth().signOut().then(function() {
        console.log('Signed Out');
    }, function(error) {
        console.error('Sign Out Error', error);
    });
});