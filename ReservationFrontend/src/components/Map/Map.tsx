import "leaflet/dist/leaflet.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.css";
import "react-leaflet-cluster/dist/assets/MarkerCluster.Default.css";
import "projektpro-leaflet-smoothwheelzoom";
import L from "leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import { MapContainer, TileLayer } from "react-leaflet";
import MarkersList from "./MarkersList";

const TurinBounds = L.latLngBounds(
  [44.5, 7.0], // Much further southwest
  [45.7, 8.5] // Much further northeast
);

function Map() {
  return (
    <>
      <MapContainer
        center={TurinBounds.getCenter()}
        className="w-full h-full rounded-xl"
        minZoom={8}
        zoom={13}
        //maxZoom={15}
        //bounds={TurinBounds}
        //maxBounds={TurinBounds}
        //maxBoundsViscosity={1.0}
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
          <MarkersList></MarkersList>
        </MarkerClusterGroup>
        {/*<Outlet></Outlet>*/}
      </MapContainer>
    </>
  );
}

export default Map;
