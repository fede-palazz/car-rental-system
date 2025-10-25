import { useEffect, useState } from "react";
import { LeafletTrackingMarker } from "react-leaflet-tracking-marker";

import L, { LatLngTuple } from "leaflet";
import { carMarkers } from "@/utils/carMarkers";

const markerIcon = function (index: number) {
  return new L.Icon({
    iconUrl: carMarkers[index],
    iconSize: [36, 36],
  });
};

function CarMarker({
  latitude,
  longitude,
  bearing,
  iconIndex,
}: {
  latitude: number;
  longitude: number;
  bearing: number;
  iconIndex: number;
}) {
  const [prevPos, setPrevPos] = useState<LatLngTuple>([latitude, longitude]);

  useEffect(() => {
    if (prevPos[1] !== longitude && prevPos[0] !== latitude)
      setPrevPos([latitude, longitude]);
  }, [latitude, longitude, prevPos]);

  return (
    <LeafletTrackingMarker
      icon={markerIcon(iconIndex)}
      position={[latitude, longitude]}
      previousPosition={prevPos}
      duration={5000}
      rotationAngle={bearing}
    />
  );
}

export default CarMarker;
