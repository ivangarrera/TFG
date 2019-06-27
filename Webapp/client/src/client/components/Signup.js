import React from "react";
import Header from './Header';

class Signup extends React.Component {
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
            <form>
              <div className="form-group">
                <label for="exampleInputEmail1">Nombre</label>
                <input type="text" className="form-control" id="exampleInputName1" placeholder="Introduzca su nombre" />
              </div>
              <div className="form-group">
                <label for="exampleInputEmail1">Email</label>
                <input type="email" className="form-control" id="exampleInputEmail1" aria-describedby="emailHelp" placeholder="Introduzca su email" />
              </div>
              <div className="form-group">
                <label for="exampleInputPassword1">Contraseña</label>
                <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Introduzca su contraseña" />
              </div>
              <div className="form-group">
                <label for="exampleInputPassword2">Repetir contraseña</label>
                <input type="password" className="form-control" id="exampleInputPassword1" placeholder="Repetia su Contraseña" />
              </div>
              <div style={{ textAlign: 'center' }}>
                <button style={{ textAlign: 'center', backgroundColor: '#143449' }} type="submit" className="btn">Registrarse</button>
              </div>
            </form>
          </div>
          <div className='col-md-2 col-1' />
        </div>
      </div>
    );
  }

}

export default Signup;
