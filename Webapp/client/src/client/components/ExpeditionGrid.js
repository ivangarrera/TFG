import React from 'react';
import Header from './Header'
import MinimalExpedition from './MinimalExpedition'
import axios from 'axios';

class ExpeditionGrid extends React.Component {
  constructor() {
    super();
    this.state = {
      expeditions: []
    };
  }

  componentDidMount() {
    var self = this;
    axios({
      method: 'GET',
      url: 'api/users/user'
    })
    .then(data => {
      if (data.data.data === null) {
        self.props.history.push('/signin');
      }
      // Check if the url is correct. If it is not correct, redirect the user to the correct one
      let value_to_compare = new Buffer(data.data.data).toString('base64');
      if (window.location.href.split('/')[4] !== value_to_compare.toString()) {
        window.open('/', '_self');
      }
    })
    .catch(err => {
      console.log(err);
    });

    // Get all the expeditions of the current user
    axios({
      method: 'GET',
      url: 'api/expeditions/allexpeditions'
    })
    .then(data => {
      data.data.forEach(expedition => {
        let expeditions = this.state.expeditions;
        expeditions.push(expedition);
        this.setState({
          expeditions: expeditions
        });
      });
    })
    .catch(err => {
      console.log(err);
    });
  }

  render() {
    let my_expeditions = [];
    for (let i = 0; i < this.state.expeditions.length; i++) {
      let exp = this.state.expeditions[i];
      if (i % 2 === 0) {
        my_expeditions.push(<MinimalExpedition tintColor={'#143449'} expName={exp.ExpName}
        initTime={exp.InitTime} guideName={exp.GuideName} duration={exp.Duration}
        totalDistance={exp.TotalDistance} key={i + 1}/>);
      } else {
        my_expeditions.push(<MinimalExpedition tintColor={'#8EBF2A'} expName={exp.ExpName}
        initTime={exp.InitTime} guideName={exp.GuideName} duration={exp.Duration}
        totalDistance={exp.TotalDistance} key={i + 1}/>);
      }
    }

    return (
      <div>
      <Header isLogged='true' history={this.props.history} />
      <br /> <br />
      <div className='row'>
      <div className='col-md-12'>
      <h1 style={{ textAlign: 'center' }}>Rutas en las que has participado</h1>
      </div>
      </div>
      <div className='row'>
      <div className='col-md-1'> </div>
      <div className='col-md-10'>
      <div className='row'>
      {my_expeditions}
      </div>
      </div>
      <div className='col-md-1'> </div>
      </div>
      </div>
    );
  }
}

export default ExpeditionGrid;
