export interface VehicleCreateDTO {
  licensePlate: string;
  vin: string;
  kmTravelled?: number;
  pendingCleaning?: boolean;
  carModelId: number;
}
