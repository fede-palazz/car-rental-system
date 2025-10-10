export interface MaintenanceReqDTO {
  startDate: Date;
  plannedEndDate: Date;
  defects: string;
  type: string;
  upcomingServiceNeeds: string;
}
