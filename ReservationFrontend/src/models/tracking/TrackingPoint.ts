export interface TrackingPoint {
  lat: number;
  lng: number;
  timestamp: Date;
  bearing: number;
  distanceIncremental?: number;
}
