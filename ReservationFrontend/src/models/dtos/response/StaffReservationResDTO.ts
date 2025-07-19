import { CustomerReservationResDTO } from "./CustomerReservationResDTO";

export interface StaffReservationResDTO {
  commonInfo: CustomerReservationResDTO;
  customerUsername: string;
  wasDeliveryLate?: boolean;
  wasChargedFee?: boolean;
  wasVehicleDamaged?: boolean;
  wasInvolvedInAccident?: boolean;
}
