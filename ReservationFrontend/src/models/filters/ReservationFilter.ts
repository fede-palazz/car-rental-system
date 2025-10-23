import { ReservationStatus } from "../enums/ReservationStatus";

export interface ReservationFilter {
  licensePlate?: string;
  vin?: string;
  brand?: string;
  model?: string;
  year?: number;
  minCreationDate?: Date;
  maxCreationDate?: Date;
  minPlannedPickUpDate?: Date;
  maxPlannedPickUpDate?: Date;
  minActualPickUpDate?: Date;
  maxActualPickUpDate?: Date;
  minPlannedDropOffDate?: Date;
  maxPlannedDropOffDate?: Date;
  minActualDropOffDate?: Date;
  maxActualDropOffDate?: Date;
  status?: ReservationStatus;
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasInvolvedInAccident?: boolean;
}
