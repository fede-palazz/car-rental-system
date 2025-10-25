import { TrackingPoint } from "./TrackingPoint";

export interface TrackingSession {
  id: number;
  reservationId: number;
  vehicleId: number;
  customerUsername: string;
  startDate: Date;
  endDate: Date;
  lastTrackingPoint: TrackingPoint;
}
