import React from 'react';
import axios from 'axios';

class GeneralInfoExpeditionCard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      first_field_name: "",
      first_field_value: "",
      second_field_name: "",
      second_field_value: "",
      third_field_name: "",
      third_field_value: "",
      data: null
    }
  }

  componentDidMount() {
    var self = this;
    if (this.state.data === null) {
      axios.get('api/expeditions/expedition', {
        params: {
          expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6])))
        }
      })
      .then(data => {
        self.setState({ data: data.data }, function() {
          self.renderCorrectInfo(self.props.card_identifier);
        });
      })
      .catch(err => {
        console.log(err);
      });
    }
  }

  renderCorrectInfo(identifier) {
    var self = this;

    switch (identifier) {
      case '1':
      this.setState({
        first_field_name: "Guía",
        first_field_value: self.state.data.GuideName.split("@")[0],
        second_field_name: "Fecha de inicio",
        second_field_value: self.getDateFromTimestamp(self.state.data.InitTime),
        third_field_name: "Fecha de fin",
        third_field_value: self.state.data.EndTime === undefined ? "En curso" : self.getDateFromTimestamp(self.state.data.EndTime)
      });
      break;
      case '2':
      this.setState({
        first_field_name: "Duración de la expedición",
        first_field_value: self.state.data.Duration === undefined ? "En curso" : self.secondsToHms(self.state.data.Duration),
        second_field_name: "Distancia total recorrida",
        second_field_value: self.state.data.User.TotalDistance.toFixed(2) + " kms",
        third_field_name: "Ritmo medio",
        third_field_value: self.getParticipantAvgPace().toFixed(2) + " min/km"
      });
      break;
      case '3':
      this.setState({
        first_field_name: "Temperatura media",
        first_field_value: self.getArrayMean(self.state.data.User.Temperature) === null ? "No hay datos" : self.getArrayMean(self.state.data.User.Temperature).toFixed(2) + " ºC",
        second_field_name: "Temperatura máxima",
        second_field_value: self.getArrayMax(self.state.data.User.Temperature) === null ? "No hay datos" : self.getArrayMax(self.state.data.User.Temperature).toFixed(2) + " ºC",
        third_field_name: "Temperatura mínima",
        third_field_value: self.getArrayMin(self.state.data.User.Temperature) === null ? "No hay datos" : self.getArrayMin(self.state.data.User.Temperature).toFixed(2) + " ºC"
      });
      break;
      case '4':
      this.setState({
        first_field_name: "Humedad media",
        first_field_value: self.getArrayMean(self.state.data.User.Humidity) === null ? "No hay datos" : self.getArrayMean(self.state.data.User.Humidity).toFixed(2) + "%",
        second_field_name: "Humedad máxima",
        second_field_value: self.getArrayMax(self.state.data.User.Humidity) === null ? "No hay datos" : self.getArrayMax(self.state.data.User.Humidity).toFixed(2) + "%",
        third_field_name: "Humedad mínima",
        third_field_value: self.getArrayMin(self.state.data.User.Humidity) === null ? "No hay datos" : self.getArrayMin(self.state.data.User.Humidity).toFixed(2) + "%"
      });
      break;
      case '5':
      this.setState({
        first_field_name: "Alertas de socorro",
        first_field_value: self.getNumberAlertsOfParticipant("SOS"),
        second_field_name: "Alertas de separación máxima",
        second_field_value: self.getNumberAlertsOfParticipant("Distance"),
        third_field_name: "Alertas de parada",
        third_field_value: self.getNumberAlertsOfParticipant("STOP")
      });
      break;
      default:
      break;
    }
  }

  getArrayMean(array) {
    let other_array = [];
    if (array[0] !== undefined) {
      array.forEach(element => {
        other_array.push(element.Data);
      });
      let size = other_array.length;
      let sum = other_array.reduce((previous, current) => current += previous);
      return sum / size;
    }
    return null;
  }

  getArrayMax(array) {
    let other_array = [];
    if (array[0] !== undefined) {
      array.forEach(element => {
        other_array.push(element.Data);
      });
      return Math.max(...other_array);
    }
    return null;
  }

  getArrayMin(array) {
    let other_array = [];
    if (array[0] !== undefined) {
      array.forEach(element => {
        other_array.push(element.Data);
      });
      return Math.min(...other_array);
    }
    return null;
  }

  getNumberAlertsOfParticipant(alertType) {
    let participant = this.state.data.User;
    let SOS_counter = 0;
    participant.Alerts.forEach(alert => {
      if (alert.AlertType === alertType) {
        SOS_counter++;
      }
    });
    return SOS_counter;
  }

  getParticipantAvgPace() {
    let participant = this.state.data.User;
    let total_time_in_seconds = parseInt(this.state.data.EndTime) - parseInt(this.state.data.InitTime);
    let total_time_in_minutes = total_time_in_seconds / 60;
    // Return the average pace
    let avg_time = total_time_in_minutes / parseFloat(participant.TotalDistance);
    return isNaN(avg_time) ? Infinity : avg_time;
  }

  secondsToHms(duration) {
    duration = Number(duration);
    let h = Math.floor(duration / 3600);
    let m = Math.floor(duration % 3600 / 60);
    let s = Math.floor(duration % 3600 % 60);

    let hDisplay = h > 0 ? h + (h === 1 ? " hora, " : " horas, ") : "";
    let mDisplay = m > 0 ? m + (m === 1 ? " minuto, " : " minutos, ") : "";
    let sDisplay = s > 0 ? s + (s === 1 ? " segundo" : " segundos") : "";
    return hDisplay + mDisplay + sDisplay;
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

  render() {
    return (
      <div className="card bg-warning">
      <div className="card-body text-center">
      <h5 style={{ color: '#ffffff' }}>{this.state.first_field_name}: {this.state.first_field_value}</h5>
      <h5 style={{ color: '#ffffff' }}>{this.state.second_field_name}: {this.state.second_field_value}</h5>
      <h5 style={{ color: '#ffffff' }}>{this.state.third_field_name}: {this.state.third_field_value}</h5>
      </div>
      </div>
    );
  }
}

export default GeneralInfoExpeditionCard;
