import { CarStatus } from "../../Vehicle";

export interface VehicleCreateDTO {
  licensePlate: string;
  vin: string;
  status?: CarStatus;
  kmTravelled?: number;
  pendingCleaning?: boolean;
  pendingRepair?: boolean;
  carModelId: number;
}
