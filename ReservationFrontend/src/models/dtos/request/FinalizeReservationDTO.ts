export interface FinalizeReservationDTO {
  actualDropOffDate: Date;
  bufferedDropOffDate: Date;
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasInvolvedInAccident?: boolean;
  damageLevel: number;
  dirtinessLevel: number;
}
