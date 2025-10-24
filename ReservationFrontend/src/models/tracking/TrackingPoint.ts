export interface TrackingPoint {
  lat: number;
  lng: number;
  timestamp: Date;
  bearing?: number;
  angle?: number;
  distanceIncremental?: number;
}
