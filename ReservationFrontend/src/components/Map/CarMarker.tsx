import { useEffect, useMemo, useRef, useState } from "react";
import { LeafletTrackingMarker } from "react-leaflet-tracking-marker";
import L, { LatLngTuple } from "leaflet";
import { carMarkers } from "@/utils/carMarkers";
import { useMap } from "react-leaflet";
import { renderToString } from "react-dom/server";
import { useNavigate, useParams } from "react-router-dom";

const markerIcon = function (index: number) {
  return new L.Icon({
    className: "!pointer-events-auto",
    iconUrl: carMarkers[index],
    iconSize: [36, 36],
  });
};

function CarMarker({
  latitude,
  longitude,
  bearing,
  iconIndex,
  vehicleId,
}: {
  latitude: number;
  longitude: number;
  bearing: number;
  iconIndex: number;
  vehicleId: number;
}) {
  const [prevPos, setPrevPos] = useState<LatLngTuple>([latitude, longitude]);
  const navigate = useNavigate();
  const map = useMap();
  const selectedVehicleId = Number(useParams().vehicleId);
  const vehicleIdRef = useRef(selectedVehicleId);

  console.log(selectedVehicleId);

  if (!Number.isNaN(selectedVehicleId)) {
    map.dragging.disable();
    map.doubleClickZoom.disable();
  } else {
    map.dragging.enable();
    map.doubleClickZoom.enable();
  }

  const handleClick = () => {
    navigate(`${vehicleId}`);
  };

  const myDivIcon = function (index: number) {
    return L.divIcon({
      className: "custom-marker !size-fit !pointer-events-auto",
      html: renderToString(
        <img
          src={carMarkers[index]}
          className="!pointer-events-auto"
          style={{
            width: "36px",
            height: "36px",
          }}
        />
      ),
    });
  };

  useEffect(() => {
    if (selectedVehicleId !== vehicleId) {
      return;
    }
    const currentZoom = map.getZoom();
    const offsetLatLng = map.latLngToContainerPoint([latitude, longitude]);
    offsetLatLng.x -= 300;
    const adjustedLatLng = map.containerPointToLatLng(offsetLatLng);

    /*if (vehicleIdRef.current == selectedVehicleId) {
      map.panTo(adjustedLatLng);
    } else {*/
    map.flyTo(adjustedLatLng, currentZoom, {
      duration: 0.7,
      easeLinearity: 0.3,
    });
    //}
    vehicleIdRef.current = selectedVehicleId;
  }, [selectedVehicleId]);

  useEffect(() => {
    if (prevPos[1] !== longitude && prevPos[0] !== latitude)
      setPrevPos([latitude, longitude]);
    if (vehicleIdRef.current == selectedVehicleId) {
      const offsetLatLng = map.latLngToContainerPoint([latitude, longitude]);
      offsetLatLng.x -= 300;
      const adjustedLatLng = map.containerPointToLatLng(offsetLatLng);
      map.panTo(adjustedLatLng);
    }
  }, [latitude, longitude, prevPos]);

  const icon = useMemo(() => myDivIcon(iconIndex), [iconIndex]);
  return (
    <LeafletTrackingMarker
      bubblingMouseEvents={false}
      interactive={true}
      icon={icon}
      position={[latitude, longitude]}
      previousPosition={prevPos}
      duration={500}
      //keepAtCenter={selectedVehicleId == vehicleId}
      rotationAngle={bearing}
      eventHandlers={{ click: handleClick }}></LeafletTrackingMarker>
  );
}

export default CarMarker;
