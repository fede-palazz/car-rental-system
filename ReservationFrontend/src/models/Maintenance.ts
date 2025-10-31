import { MaintenanceType } from "./enums/MaintenanceType";

export interface Maintenance {
  id: number;
  defects: string;
  type: MaintenanceType;
  upcomingServiceNeeds: string;
  startDate: Date;
  plannedEndDate: Date;
  actualEndDate?: Date;
}
