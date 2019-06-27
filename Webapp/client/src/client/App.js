import React, { Component } from 'react';
import { BrowserRouter, Switch, Route } from 'react-router-dom';
import ExpeditionGrid from './components/ExpeditionGrid';
import Signup from './components/Signup';
import Signin from './components/Signin'
import ExpeditionDetailedInfo from './components/ExpeditionDetailedInfo';

class App extends Component {
  render() {
    return (
      <div>
        <BrowserRouter>
          <Switch>
            <Route path='/profile/:id/expeditions' component={ExpeditionGrid} />
            <Route path='/profile/:id/expedition/:expid' component={ExpeditionDetailedInfo} />
            <Route exact path='/' component={Signin} />
            <Route exact path='/signup' component={Signup} />
            <Route exact path='/signin' component={Signin} />
          </Switch>
        </BrowserRouter>
      </div>
    );
  }
}

export default App;
