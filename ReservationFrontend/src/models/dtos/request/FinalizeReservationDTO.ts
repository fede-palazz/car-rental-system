export interface FinalizeReservationDTO {
  actualDropOffDate: Date;
  bufferedDropOffDate: Date;
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasVehicleDamaged?: boolean;
  wasInvolvedInAccident?: boolean;
}
