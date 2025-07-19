import { ReservationStatus } from "@/models/enums/ReservationStatus";

export interface CustomerReservationResDTO {
  id: number;
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
}
