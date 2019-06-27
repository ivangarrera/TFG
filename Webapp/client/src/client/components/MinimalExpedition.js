import React from 'react';

class MinimalExpedition extends React.Component {
  render() {
    let url = window.location.href.split('/');
    url = '/profile/' + url[4] + '/expedition/' + new Buffer(this.props.expName).toString('base64');

    return (
      <div className="col-md-4 card testimonial-card">
      <div className="card-up" style={{ backgroundColor: this.props.tintColor, marginLeft: '-5%', width: 'auto' }}>
      <a href={url}>
      <i className="fas fa-2x fa-align-justify" style={{ marginLeft: '85%', marginTop: '5%', color: '#ffffff' }}></i>
      </a>
      </div>

      <div className="avatar" style={{ marginLeft: 30 + '%' }}>
      <img src="/favicon.ico" className="rounded-circle" alt="woman avatar" />
      </div>

      <div className="card-body">
      <h4 className="card-title" style={{ textAlign: 'center' }}>{this.props.expName}</h4>
      <h6 className="card-title" style={{ textAlign: 'center' }}>{this.timestampToDate()}</h6>
      <hr />
      </div>
      <div className="card-body" style={{ backgroundColor: this.props.tintColor, marginLeft: '-5%', width: 'auto' }}>
      <div className='row'>
      <div className='col-md-4'>
      <p style={{ color: '#ffffff' }}>Guía:</p>
      <p style={{ color: '#ffffff' }}>{this.props.guideName.split("@")[0]}</p>
      </div>
      <div className='col-md-4'>
      <p style={{ color: '#ffffff' }}>Duración:</p>
      <p style={{ color: '#ffffff' }}>{this.secondsToHms()}</p>
      </div>
      <div className='col-md-4'>
      <p style={{ color: '#ffffff' }}>Distancia:</p>
      <p style={{ color: '#ffffff' }}>{this.parseDistance()}</p>
      </div>
      </div>
      </div>
      </div>
    );
  }

  parseDistance() {
    let distance = this.props.totalDistance;
    return (
      parseFloat(distance).toFixed(3) + ' kms'
    );
  }

  timestampToDate() {
    let a = new Date(this.props.initTime * 1000);
    let months = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    let year = a.getFullYear();
    let month = months[a.getMonth()];
    let date = a.getDate();

    return date + '/' + month + '/' + year;
  }

  secondsToHms() {
    let duration = Number(this.props.duration);
    let h = Math.floor(duration / 3600);
    let m = Math.floor(duration % 3600 / 60);
    let s = Math.floor(duration % 3600 % 60);

    let hDisplay = h > 0 ? h + (h === 1 ? " hora, " : " horas, ") : "";
    let mDisplay = m > 0 ? m + (m === 1 ? " minuto, " : " minutos, ") : "";
    let sDisplay = s > 0 ? s + (s === 1 ? " segundo" : " segundos") : "";
    return hDisplay + mDisplay + sDisplay;
  }
}

export default MinimalExpedition;
