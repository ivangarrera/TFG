const express = require('express');
const userRouter = express.Router();

const userController = require('../controllers/UserController');

userRouter.post('/signin', userController.Signin);

userRouter.post('/signout', userController.SignOut);

userRouter.get('/user', userController.GetUser);

module.exports = userRouter;
