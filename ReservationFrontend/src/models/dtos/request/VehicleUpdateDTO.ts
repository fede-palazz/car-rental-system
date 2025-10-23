import { CarStatus } from "@/models/Vehicle";

export interface VehicleUpdateDTO {
  licensePlate?: string;
  status: CarStatus;
  kmTravelled: number;
  pendingCleaning: boolean;
}
