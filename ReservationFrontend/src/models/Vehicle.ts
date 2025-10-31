export interface Vehicle {
  id: number;
  licensePlate: string;
  vin: string;
  carModelId: number;
  brand: string;
  model: string;
  year: number;
  status: CarStatus;
  kmTravelled: number;
  pendingCleaning: boolean;
}

export enum CarStatus {
  AVAILABLE = "AVAILABLE",
  RENTED = "RENTED",
  IN_MAINTENANCE = "IN_MAINTENANCE",
}
