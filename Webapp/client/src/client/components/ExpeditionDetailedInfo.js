import React from 'react';
import axios from 'axios';
import Header from './Header'
import Carousel from './CarouselShowCards'
import GoogleMap from './GoogleMap'

class ExpeditionDetailedInfo extends React.Component {
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
  }

  render() {
    return (
      <div>
      <Header isLogged='true' history={this.props.history}/>
      <br /> <br />
      <div className='row'>
      <div className='col-md-12'>
      <h1 style={{ textAlign: 'center' }}>Información general:</h1>
      </div>
      </div>

      <br /> <br />

      <div className='row' style={{textAlign:"center"}}>
      <div className='col-md-12'>
      <Carousel />
      </div>
      </div>


      <br /> <hr /> <br />

      <div className='row'>
      <div className='col-md-12'>
      <h1 style={{ textAlign: 'center' }}>Recorrido realizado:</h1>
      </div>
      </div>

      <br /> <br />

      <div className='row' style={{textAlign:"center"}}>
      <div className='col-md-12'>
      <GoogleMap />
      </div>
      </div>
      </div>
    );
  }
}

export default ExpeditionDetailedInfo;
