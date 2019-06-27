import React from 'react';
import axios from 'axios';

class Header extends React.Component {
  constructor() {
    super();
    this.onLogout = this.onLogout.bind(this);
  }

  render() {
    if (this.props.isLogged === 'false') {
      return (
        <div className='row'>
        <div className='col-12'>
        <nav className="navbar navbar-expand-lg" style={{ backgroundColor: '#143449' }}>
        <img src='./../../GVIDI_White.png' alt='LOGO' height='100%' width='50px' />
        </nav>
        </div>
        </div >
      );
    }

    let images_url = '.' + '/..'.repeat(window.location.href.split('/').length - 4);

    return (
      <div className='row'>
      <div className='col-12'>
      <nav className="navbar navbar-expand-lg" style={{ backgroundColor: '#143449' }}>
      <a href='/'>
      <img src={images_url + '/GVIDI_White.png'} alt='LOGO' height='100%' width='50px' />
      </a>
      <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
      <span className="navbar-toggler-icon"></span>
      </button>

      <div className="collapse navbar-collapse" id="navbarSupportedContent">
      <ul className="navbar-nav mr-auto">

      </ul>

      <a className="nav-link dropdown-toggle" href="/" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
      <img src={images_url + '/user_icon.png'} alt='LOGO' height='100%' width='40px'></img> </a>
      <div className="dropdown-menu dropdown-menu-right" aria-labelledby="navbarDropdown">
      <form onSubmit={this.onLogout}>
      <button href='#' className='btn btn-link'>
      <i className="fas fa-sign-out-alt" /> Cerrar Sesión
      </button>
      </form>
      </div>
      </div>
      </nav>
      </div>
      </div>
    );
  }

  onLogout(val) {
    val.preventDefault();

    var self = this;
    axios.post('api/users/SignOut', {})
    .then(function (response) {
      // Successfully logged in
      if (response.data.message === "Success" && response.data.data === null) {
        self.props.history.push('/signin');
      }
    })
    .catch(function (error) {
      console.log(error);
    });
  }
}

export default Header;
