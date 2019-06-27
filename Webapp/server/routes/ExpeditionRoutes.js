const express = require('express');
const expeditionRouter = express.Router();

const expeditionController = require('../controllers/ExpeditionController');

expeditionRouter.get('/allexpeditions', expeditionController.GetExpeditionsFromUser);
expeditionRouter.get('/expedition', expeditionController.GetExpedition);
expeditionRouter.get('/locations', expeditionController.GetLocations);
expeditionRouter.get('/alerts', expeditionController.GetAlerts);
expeditionRouter.get('/minLocation', expeditionController.GetMinLocation);
expeditionRouter.get('/temphumi', expeditionController.getTempAndHumidityFromTimestamp);
expeditionRouter.get('/pace', expeditionController.getPaceFromTimestamp);

module.exports = expeditionRouter;
