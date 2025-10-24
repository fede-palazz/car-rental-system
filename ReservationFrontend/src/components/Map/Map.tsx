import "leaflet/dist/leaflet.css";
import "projektpro-leaflet-smoothwheelzoom";
import L from "leaflet";
import { MapContainer, TileLayer } from "react-leaflet";

const TurinBounds = L.latLngBounds(
  [44.95, 7.55], // Southwest coordinates
  [45.15, 7.8] // Northeast coordinates
);

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
    </MapContainer>
  );
}

export default Map;
