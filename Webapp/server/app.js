const express = require('express');
const http = require('http');
const path = require('path');
const cors = require('cors');
const bodyParser = require('body-parser')

const app = express();

app.set('port', process.env.PORT || 4000);          // Set the port

app.use(express.static('build'));
app.use(cors());

app.use(bodyParser.json())
app.use('/api/users', require('./routes/UserRoutes'));
app.use('/api/expeditions', require('./routes/ExpeditionRoutes'));

var server_created = http.createServer(app).listen(app.settings.port, function() {
	console.log ('Server listening on port ' + app.settings.port);
});

module.exports = app;
