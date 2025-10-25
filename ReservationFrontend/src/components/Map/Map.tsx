import "leaflet/dist/leaflet.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.Default.css";
import "projektpro-leaflet-smoothwheelzoom";
import L from "leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import { MapContainer, TileLayer } from "react-leaflet";
import TrackingAPI from "@/API/TrackingAPI";
import { TrackingSession } from "@/models/tracking/TrackingSession";
import { useEffect, useState } from "react";
import CarMarker from "./CarMarker";
import { TrackingPoint } from "@/models/tracking/TrackingPoint";

const TurinBounds = L.latLngBounds(
  [44.95, 7.55], // Southwest coordinates
  [45.15, 7.8] // Northeast coordinates
);

function Map() {
  const [sessions, setSessions] = useState<TrackingSession[]>([]);

  const fetchSessions = () => {
    TrackingAPI.getTrackingSessions().then(
      (trackingSessions: TrackingSession[]) => {
        setSessions(trackingSessions);
      }
    );
  };

  useEffect(() => {
    fetchSessions();
    const interval = setInterval(fetchSessions, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <MapContainer
      center={TurinBounds.getCenter()}
      className="w-full h-full rounded-xl"
      minZoom={8}
      zoom={13}
      //maxZoom={15}
      bounds={TurinBounds}
      maxBounds={TurinBounds}
      maxBoundsViscosity={1.0}
      touchZoom
      doubleClickZoom
      attributionControl={true}
      zoomControl={false}
      scrollWheelZoom={false} //TODO FIX ON FIREFOX Needed to enable smooth zoom
      markerZoomAnimation>
      <TileLayer
        keepBuffer={100}
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MarkerClusterGroup
        spiderfyOnEveryZoom={false}
        spiderfyOnMaxZoom={true}
        //iconCreateFunction={municipalityClusterIcon}
        spiderfyDistanceMultiplier={2}
        showCoverageOnHover={false}>
        {Array.isArray(sessions) &&
          sessions.map((session: TrackingSession, index) => {
            const lastPoint: TrackingPoint = session.lastTrackingPoint;
            return (
              <CarMarker
                latitude={lastPoint.lat}
                longitude={lastPoint.lng}
                bearing={lastPoint.bearing}
                iconIndex={index % 8}
              />
            );
          })}
      </MarkerClusterGroup>
    </MapContainer>
  );
}

export default Map;
