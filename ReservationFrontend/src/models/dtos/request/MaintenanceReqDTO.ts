import { MaintenanceType } from "@/models/enums/MaintenanceType";

export interface MaintenanceReqDTO {
  startDate: Date;
  plannedEndDate: Date;
  defects: string;
  type: MaintenanceType;
  upcomingServiceNeeds: string;
}
