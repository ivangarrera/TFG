import React from "react";
import Header from './Header';
import axios from 'axios';

class Signin extends React.Component {
  constructor() {
    super();
    this.state = {
      authenticated: false,
      user: null,
      password: null
    };
    this.onSubmit = this.onSubmit.bind(this);
  }

  render() {
    return (
      <div>
        <Header isLogged='false' />
        <br /><br />
        <div className='row'>
          <div className='col-md-5 col-3' />
          <div className='col-md-2 col-6' style={{ textAlign: 'center' }}>
            <img src='./GVIDI_Trans.png' alt='LOGO' width='100%' />
          </div>
          <div className='col-md-5 col-3' />
        </div>

        <br /><br /><br />

        <div className='row'>
          <div className='col-md-2 col-1' />
          <div className='col-md-8 col-10'>
            <div className="form-group">
              <label htmlFor="exampleInputEmail1">Email</label>
              <input ref="email" type="email" className="form-control" id="exampleInputEmail1" aria-describedby="emailHelp" placeholder="Introduzca su email" />
            </div>
            <form onSubmit={this.onSubmit}>
              <div className="form-group">
                <label htmlFor="exampleInputPassword1">Contraseña</label>
                <input ref="password" type="password" className="form-control" id="exampleInputPassword1" placeholder="Introduzca su contraseña" />
              </div>
              <div style={{ textAlign: 'center' }}>
                <button className='btn btn-default' type="submit"><i className="fas fa-sign-in-alt" style={{ paddingRight: '10px' }} />Entrar</button>
              </div>
            </form>
          </div>
          <div className='col-md-2 col-1' />
        </div>
      </div>
    );
  }

  onSubmit(val) {
    val.preventDefault();
    const formData = {};
    for (const field in this.refs) {
      formData[field] = this.refs[field].value;
    }

    var self = this;
    axios.post('api/users/Signin', {
      username: formData.email,
      password: formData.password
    })
    .then(function (response) {
      // Successfully logged in
      if (response.data.message === "Success" && response.data.data !== null) {
        self.props.history.push('/profile/' + new Buffer(response.data.data).toString('base64') + '/expeditions');
      }
    })
    .catch(function (error) {
      console.log(error);
    });
  }
}

export default Signin;
