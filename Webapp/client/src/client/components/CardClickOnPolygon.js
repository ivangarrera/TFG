import React from 'react'
import axios from 'axios';

class CardClickOnPolygon extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      location: null,
      temperature: undefined,
      humidity: undefined,
      pace: undefined
    }
  }

  componentDidUpdate() {
    var self = this;

    if (this.props.alert !== null && this.props.alert.location !== this.state.location) {
      axios.get('api/expeditions/minLocation', {
        params: {
          expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6]))),
          lat: self.props.alert.lat(),
          lon: self.props.alert.lng()
        }
      })
      .then(data => {
        if (self.state.location === null) {
          self.setState({ location: data.data, temperature: undefined, humidity: undefined, pace: undefined });
        } else {
          if (self.state.location.Lat !== data.data.Lat ||
            self.state.location.Lon !== data.data.Lon) {
              self.setState({ location: data.data, temperature: undefined, humidity: undefined, pace: undefined });
            }
          }
        })
        .catch(err => {
          console.log(err);
        });
      }
    }

    render() {
      var self = this;
      let humi = null;
      let temp = null;
      let pace = null;
      if (this.props.alert !== null && this.state.location !== null) {
        if (this.state.temperature === undefined && this.state.humidity === undefined && this.state.pace === undefined) {
          axios.get('api/expeditions/temphumi', {
            params: {
              expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6]))),
              timestamp: self.state.location.Timestamp
            }
          })
          .then(data => {
            humi = data.data.humidity.Data;
            temp = data.data.temperature.Data;

            axios.get('api/expeditions/pace', {
              params: {
                expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6]))),
                timestamp: self.state.location.Timestamp
              }
            })
            .then(data => {
              pace = data.data.pace;
              self.setState({location: self.state.location, temperature: temp, humidity: humi, pace: pace});
            })
            .catch(err => {
              console.log(err);
            });
          })
          .catch(err => {
            console.log(err);
          });

          return (<div></div>);
        } else {
          return (
            <div id='polycard' style={{ visibility: 'visible' }}>
            <button type="button" className="close" data-dismiss="alert" aria-label="Close" onClick={this.closeClick}>
            <span aria-hidden="true">&times;</span>
            </button>
            <div className="card bg-warning">
            <div className="card-body text-center">
            <h5 style={{ color: '#ffffff' }}>Ritmo inmediato: {this.state.pace === null ? "No hay datos" : this.state.pace + " min/km"}</h5>
            <h5 style={{ color: '#ffffff' }}>Temperatura: {this.state.temperature === null ? "No hay datos" : this.state.temperature + "ºC"}</h5>
            <h5 style={{ color: '#ffffff' }}>Humedad: {this.state.humidity === null ? "No hay datos" : this.state.humidity + "%"}</h5>
            <h5 style={{ color: '#ffffff' }}>Coordenadas: Lat: {self.state.location.Lat}, Lon: {self.state.location.Lon}</h5>
            <h5 style={{ color: '#ffffff' }}>Fecha y hora: {self.getDateFromTimestamp(self.state.location.Timestamp)}</h5>
            </div>
            </div>
            </div>
          );
        }
      } else {
        return (<div></div>);
      }
    }

    closeClick() {
      document.getElementById('polycard').style.visibility = 'hidden';
    }

    getDateFromTimestamp(timestamp) {
      let a = new Date(timestamp * 1000);
      let months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
      let year = a.getFullYear();
      let month = months[a.getMonth()];
      let date = '0' + a.getDate();
      let hour = '0' + a.getHours();
      let min = '0' + a.getMinutes();
      let sec = '0' + a.getSeconds();

      return date.substr(-2) + ' ' + month + ' ' + year + ' - ' + hour.substr(-2) + ':' + min.substr(-2) + ':' + sec.substr(-2);
    }
  }

  export default CardClickOnPolygon;
