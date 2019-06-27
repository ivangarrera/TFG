import React from 'react';
import CardClickOnPolygon from './CardClickOnPolygon';
import CardClickOnMarker from './CardClickOnMarker';
import googleMapsAPI from 'load-google-maps-api';
import axios from 'axios';

const API_CONFIG = {
  // API_KEY
}

class GoogleMap extends React.Component {
  constructor() {
    super();
    this.state = {
      marker_alert: null,
      poly_alert: null,
      locations: null,
      alerts: null
    }
  }

  componentDidMount() {
    let self = this;
    // Get vector with all the locations
    if (this.state.locations === null) {
      axios.get('api/expeditions/locations', {
        params: {
          expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6])))
        }
      })
      .then(data => {
        self.setState({ locations: data.data }, function() {
          // Get a vector with all the alerts
          if (self.state.alerts === null) {
            axios.get('api/expeditions/alerts', {
              params: {
                expName: decodeURIComponent(escape(window.atob(window.location.href.split("/")[6])))
              }
            })
            .then(data => {
              self.setState({ alerts: data.data }, function() {
                // Create the map and the path, using the vector of locations obtained
                if (self.state.locations !== null) {
                  googleMapsAPI(API_CONFIG).then(googleMaps => {
                    let map = new googleMaps.Map(self.refs.map, { center: { lat: self.state.locations[0].lat, lng: self.state.locations[0].lng }, zoom: 16 });

                    // Each alert will be a marker in the map
                    self.state.alerts.forEach(alert => {
                      let myLatlng = new googleMaps.LatLng(alert.Latitude, alert.Longitude);
                      let image = undefined;
                      if (alert.AlertType === 'STOP') {
                        image = { url: window.location.origin + '/marker_stop_32.png', size: new googleMaps.Size(32, 64), origin: new googleMaps.Point(0, 0) }
                      } else if (alert.AlertType === 'SOS') {
                        image = { url: window.location.origin + '/marker_sos_32.png', size: new googleMaps.Size(32, 64), origin: new googleMaps.Point(0, 0) }
                      }
                      let marker = new googleMaps.Marker({ position: myLatlng, animation: googleMaps.Animation.DROP, title: 'Alerta', icon: image });
                      marker.addListener('click', args => {
                        self.setState({ marker_alert: null });
                        self.setState({ marker_alert: alert, poly_alert: null});
                      });
                      marker.setMap(map);
                    });

                    let poli = new googleMaps.Polyline({
                      path: self.state.locations,
                      geodesic: true,
                      strokeColor: '#FF0000',
                      strokeOpacity: 1.0,
                      strokeWeight: 2
                    });

                    poli.setMap(map);

                    // Set click listener on the polygon
                    poli.addListener('click', args => {
                      self.setState({ marker_alert: null, poly_alert: args.latLng});
                      self.render();
                    });

                  }).catch(err => {
                    console.log("Error in google maps: ", err);
                  });
                }
              });
            })
            .catch(err => {
              console.log(err);
            });
          }
        })
        .catch(err => {
          console.log(err);
        });
      });
    }
  }

  render() {
    return (
      <div className='row'>
      <div className='col-md-2' />
      <div className='col-md-8'>
      <div ref='map' style={{ height: '450px', width: '100%' }} />
      <br/> <br />
      <CardClickOnPolygon alert={this.state.poly_alert} />
      <CardClickOnMarker alert={this.state.marker_alert} />
      </div>
      <div className='col-md-2' />
      </div>
    )
  }
}

export default GoogleMap;
