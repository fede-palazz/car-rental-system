import { ReservationStatus } from "./enums/ReservationStatus";

export interface Reservation {
  id: number;
  customerUsername?: string;
  vehicleId: number;
  licensePlate: string;
  vin: string;
  brand: string;
  model: string;
  year: string;
  carModelId: number;
  creationDate: Date;
  plannedPickUpDate: Date;
  actualPickUpDate?: Date;
  plannedDropOffDate: Date;
  actualDropOffDate?: Date;
  status: ReservationStatus;
  totalAmount: number;
  // Boolean attributes used to compute eligibility score
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasVehicleDamaged?: boolean;
  wasInvolvedInAccident?: boolean;
}
