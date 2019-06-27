import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter as Router } from 'react-router-dom';
import App from './client/App';
import * as serviceWorker from './client/serviceWorker';

ReactDOM.render((<Router><App /></Router>), document.getElementById('root'));

serviceWorker.register();
