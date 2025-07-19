export interface MaintenanceReqDTO {
  defects: string;
  completed: boolean;
  type: string;
  upcomingServiceNeeds?: string;
}
