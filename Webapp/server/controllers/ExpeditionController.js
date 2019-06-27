const firebase = require('firebase');

const expeditionController = {};

// Controller function which will provide all the expeditons which a user has been
// part of
expeditionController.GetExpeditionsFromUser = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;
  let expeditions_array = [];

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();

        // If the user is the guide of the expedition
        if (data.Guide.id === current_user.email) {
          expeditions_array.push({
            "ExpName":data.ExpName,
            "InitTime":data.InitTime,
            "GuideName":data.Guide.id,
            "Duration":data.Duration,
            "TotalDistance":data.Guide.TotalDistance
          });
        } else { // Is it a participant?
          data.Participants.forEach(participant => {
            if (participant.id === current_user.email) {
              expeditions_array.push({
                "ExpName":data.ExpName,
                "InitTime":data.InitTime,
                "GuideName":data.Guide.id,
                "Duration":data.Duration,
                "TotalDistance":participant.TotalDistance
              });
            }
          });
        }
      });
      res.json(expeditions_array);
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
};

// Controller function which will provide detailed information about a certain expedition
expeditionController.GetExpedition = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;
  let information = {};

  let expeditionName = req.query.expName;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            information = {
              "EndTime":data.EndTime,
              "InitTime":data.InitTime,
              "GuideName":data.Guide.id,
              "Duration":data.Duration,
              "User": data.Guide
            };
          } else { // Is it a participant?
            data.Participants.forEach(participant => {
              if (participant.id === current_user.email) {
                information = {
                  "EndTime":data.ExpName,
                  "InitTime":data.InitTime,
                  "GuideName":data.Guide.id,
                  "Duration":data.Duration,
                  "User": participant
                };
              }
            });
          }
        }
      });
      res.json(information);
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
};

// Controller function which will provide all the locations of the authenticated user
// during a certain expedition
expeditionController.GetLocations = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;
  let locations = [];

  let expeditionName = req.query.expName;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            data.Guide.PreviousLocations.forEach(location => {
              if (location.Lat !== '')
              locations.push({ lat: parseFloat(location.Lat), lng: parseFloat(location.Lon) });
            });
          } else { // Is it a participant?
            data.Participants.forEach(participant => {
              if (participant.id === current_user.email) {
                participant.PreviousLocations.forEach(location => {
                  if (location.Lat !== '')
                  locations.push({ lat: parseFloat(location.Lat), lng: parseFloat(location.Lon) });
                });
              }
            });
          }
        }
      });
      res.json(locations);
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
};

// Controller function which will provide all the alerts of the authenticated user
// during a certain expedition
expeditionController.GetAlerts = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;
  let alerts = [];

  let expeditionName = req.query.expName;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            data.Guide.Alerts.forEach(alert => {
              alerts.push(alert);
            });
          } else { // Is it a participant?
            data.Participants.forEach(participant => {
              if (participant.id === current_user.email) {
                data.Guide.Alerts.forEach(alert => {
                  alerts.push(alert);
                });
              }
            });
          }
        }
      });
      res.json(alerts);
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
};

// Controller function which will provide the closer location to a given one, during
// a certain expedition
expeditionController.GetMinLocation = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;

  let expeditionName = req.query.expName;
  let lat = req.query.lat;
  let lon = req.query.lon;
  let min_distance = Infinity;
  let min_location = null;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            data.Guide.PreviousLocations.forEach(location => {
              let distance = getDistanceBetweenTwoPoints(lat, lon, parseFloat(location.Lat), parseFloat(location.Lon));
              if (distance < min_distance) {
                min_distance = distance;
                min_location = location;
              }
            });
          } else { // Is it a participant?
            data.Participants.forEach(participant => {
              if (participant.id === current_user.email) {
                participant.PreviousLocations.forEach(location => {
                  let distance = getDistanceBetweenTwoPoints(lat, lon, parseFloat(location.Lat), parseFloat(location.Lon));
                  if (distance < min_distance) {
                    min_distance = distance;
                    min_location = location;
                  }
                });
              }
            });
          }
        }
      });
      res.json(min_location);
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
};

// Controller function which will return the temperature and humidity which existed
// during the expedition at a given time
expeditionController.getTempAndHumidityFromTimestamp = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;

  let expeditionName = req.query.expName;
  let timestamp = req.query.timestamp;
  let minTime = Infinity;
  let tempValue = null;
  let humiValue = null;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            data.Guide.Humidity.forEach(humidity_val => {
              if (Math.abs(humidity_val.Timestamp - timestamp) < minTime) {
                minTime = Math.abs(humidity_val.Timestamp - timestamp);
                humiValue = humidity_val;
              }
            });
            minTime = Infinity;
            data.Guide.Temperature.forEach(temp_val => {
              if (Math.abs(temp_val.Timestamp - timestamp) < minTime) {
                minTime = Math.abs(temp_val.Timestamp - timestamp);
                tempValue = temp_val;
              }
            });
          } else {
            data.Participants.forEach(participant => {
              if (participant.id === current_user.email) {
                participant.Humidity.forEach(humidity_val => {
                  if (Math.abs(humidity_val.Timestamp - timestamp) < minTime) {
                    minTime = Math.abs(humidity_val.Timestamp - timestamp);
                    humiValue = humidity_val;
                  }
                });
                minTime = Infinity;
                participant.Temperature.forEach(temp_val => {
                  if (Math.abs(temp_val.Timestamp - timestamp) < minTime) {
                    minTime = Math.abs(temp_val.Timestamp - timestamp);
                    tempValue = temp_val;
                  }
                });
              }
            });
          }
        }
      });
      res.json({ humidity: humiValue, temperature: tempValue });
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
}

// Controller function which will return the participant's pace at a given time
expeditionController.getPaceFromTimestamp = (req, res) => {
  const db = firebase.firestore();
  const expeditionsRef = db.collection('Expeditions');
  const current_user = firebase.auth().currentUser;

  let expeditionName = req.query.expName;
  let timestamp = Number(req.query.timestamp);

  let prev_loc = null;
  let pace = null;

  if (current_user) {
    expeditionsRef.get().then(querySnapshot => {
      querySnapshot.forEach(doc => {
        const data = doc.data();
        if (data.ExpName === expeditionName) {
          // If the user is the guide of the expedition
          if (data.Guide.id === current_user.email) {
            data.Guide.PreviousLocations.forEach(location => {
              if (location.Timestamp === timestamp && prev_loc !== null) {
                let distance = getDistanceBetweenTwoPoints(parseFloat(prev_loc.Lat), parseFloat(prev_loc.Lon),
                parseFloat(location.Lat), parseFloat(location.Lon));
                let time = (location.Timestamp - prev_loc.Timestamp) / 60;
                pace = (time / distance).toFixed(2);
                if (pace > 50) {
                  pace = "Parado";
                }
              } else {
                prev_loc = location;
              }
            });
          } else {
            data.Participants.forEach(participant => {
              if (current_user.email === participant.id) {

              }
            });
          }
        }
      });
      res.json({ pace: pace });
    }).catch(function (error) {
      console.log("Error getting document -> ", error);
    });
  }
}

// Get the distance between to points using the Harvesine formula
function getDistanceBetweenTwoPoints(lat_first, lon_first, lat_second, lon_second) {
  lat_first = toRadians(Math.abs(lat_first));
  lon_first = toRadians(Math.abs(lon_first));
  lat_second = toRadians(Math.abs(lat_second));
  lon_second = toRadians(Math.abs(lon_second));

  let lat_distance = lat_first - lat_second;
  let lon_distance = lon_first - lon_second;

  let a = Math.pow(Math.sin(lat_distance / 2), 2) + Math.cos(lat_second) * Math.cos(lat_first) * Math.pow(Math.sin(lon_distance / 2), 2);
  let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  // The 6373.0 value is the radius of the earth
  return 6373.0 * c;
}

function toRadians(degrees) {
  return degrees * (Math.PI / 180);
}

// Exports the controller
module.exports = expeditionController;
