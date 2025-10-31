export enum ReservationStatus {
  PENDING = "PENDING", //Customer created reservation,but not paid yet
  EXPIRED = "EXPIRED", //Customer created reservation,but not paid yet
  CANCELLED = "CANCELLED",
  CONFIRMED = "CONFIRMED", // Customer reserved a vehicle
  PICKED_UP = "PICKED_UP", // Customer picked up the vehicle
  DELIVERED = "DELIVERED", // Customer returned the vehicle
}
