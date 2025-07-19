export interface FinalizeReservationDTO {
  actualDropOffDate: Date;
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasVehicleDamaged?: boolean;
  wasInvolvedInAccident?: boolean;
}
