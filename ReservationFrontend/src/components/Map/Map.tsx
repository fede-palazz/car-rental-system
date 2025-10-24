import "leaflet/dist/leaflet.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.Default.css";
import "projektpro-leaflet-smoothwheelzoom";
import L from "leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import { MapContainer, Marker, TileLayer } from "react-leaflet";
import { carMarkers } from "@/utils/carMarkers";

const TurinBounds = L.latLngBounds(
  [44.95, 7.55], // Southwest coordinates
  [45.15, 7.8] // Northeast coordinates
);

const markerIcon = function (index: number) {
  return new L.Icon({
    iconUrl: carMarkers[index],
    iconSize: [26.4, 32],
  });
};

function Map() {
  return (
    <MapContainer
      center={TurinBounds.getCenter()}
      className="w-full h-full rounded-xl"
      minZoom={8}
      zoom={13}
      maxZoom={15}
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
        spiderfyOnEveryZoom={true}
        spiderfyOnMaxZoom={true}
        //iconCreateFunction={municipalityClusterIcon}
        spiderfyDistanceMultiplier={2}
        showCoverageOnHover={false}>
        <Marker icon={markerIcon(0)} position={[45.07, 7.68]} />
        <Marker icon={markerIcon(1)} position={[45.1, 7.7]} />
        <Marker icon={markerIcon(2)} position={[45.06, 7.7]} />
      </MarkerClusterGroup>
    </MapContainer>
  );
}

export default Map;
