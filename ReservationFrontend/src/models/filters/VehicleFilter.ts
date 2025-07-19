import { CarStatus } from "../Vehicle";

export interface VehicleFilter {
  licensePlate?: string;
  vin?: string;
  brand?: string;
  model?: string;
  year?: number;
  status?: CarStatus;
  minKmTravelled?: number;
  maxKmTravelled?: number;
  pendingCleaning?: boolean;
  pendingRepair?: boolean;
}
