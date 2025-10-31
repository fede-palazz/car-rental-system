import TrackingAPI from "@/API/TrackingAPI";
import { TrackingPoint } from "@/models/tracking/TrackingPoint";
import { TrackingSession } from "@/models/tracking/TrackingSession";
import { useState, useEffect } from "react";
import CarMarker from "./CarMarker";

function MarkersList() {
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
    const interval = setInterval(fetchSessions, 2000);
    return () => clearInterval(interval);
  }, []);

  return (
    <>
      {Array.isArray(sessions) &&
        sessions.map((session: TrackingSession, index) => {
          const lastPoint: TrackingPoint = session.lastTrackingPoint;
          return (
            <CarMarker
              latitude={lastPoint.lat}
              longitude={lastPoint.lng}
              bearing={lastPoint.bearing}
              iconIndex={index % 8}
              vehicleId={session.vehicleId}
            />
          );
        })}
    </>
  );
}

export default MarkersList;
