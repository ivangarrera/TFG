import React from 'react'

class CardClickOnMarker extends React.Component {
    render() {
        if (this.props.alert !== null) {
            return (
                <div id='markercard' style={{ visibility: 'visible' }}>
                    <button type="button" className="close" data-dismiss="alert" aria-label="Close" onClick={this.closeClick}>
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <div className="card bg-warning">
                        <div className="card-body text-center">
                            <h5 style={{ color: '#ffffff' }}>Tipo de alerta: {this.props.alert.AlertType}</h5>
                            <h5 style={{ color: '#ffffff' }}>Coordenadas: Lat: {this.props.alert.Latitude}, Lon: {this.props.alert.Longitude}</h5>
                            <h5 style={{ color: '#ffffff' }}>Fecha y hora: {this.getDateFromTimestamp(this.props.alert.Timestamp)}</h5>
                        </div>
                    </div>
                </div>
            );
        } else {
            return (<div></div>);
        }
    }

    closeClick() {
        document.getElementById('markercard').style.visibility = 'hidden';
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

export default CardClickOnMarker;
