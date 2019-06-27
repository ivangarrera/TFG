const firebase = require('firebase')

const config = {
    // API KEY
};

firebase.initializeApp(config);

const userController = {};
const connectedUsers = [];

//Controller function which will be called when a user signs in the application
userController.Signin = (req, res) => {
  console.log(req.body.username);
  if (!connectedUsers.includes(req.body.username)) {
    firebase.auth().signInWithEmailAndPassword(req.body.username, req.body.password)
    .then(result => {
      res.json({
        message: 'Success',
        data: req.body.username
      });
    })
    .catch(error => {
      res.json({
        message: 'Failure',
        data: null
      });
    });
  } else {
    res.json({
      message: 'Success',
      data: req.body.username
    });
  }
};

// Controller function which will be called when a user signs out from the application
userController.SignOut = (req, res) => {
  connectedUsers.splice(connectedUsers.indexOf(firebase.auth().currentUser), 1 );
  firebase.auth().signOut().then(() => {
    res.json({
      message: 'Success',
      data: null
    });
  })
  .catch(error => {
    res.json({
      message: 'Failure',
      data: null
    });
  });
};

userController.GetUser = (req, res) => {
  if (firebase.auth().currentUser == null) {
    res.json({
      message: 'Success',
      data: null
    });
  } else {
    res.json({
      message: 'Success',
      data: firebase.auth().currentUser.email
    });
  }
};

// Exports the controller
module.exports = userController;
