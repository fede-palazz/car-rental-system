export interface MaintenanceFilter {
  defects?: string;
  completed?: boolean;
  type?: string;
  upcomingServiceNeeds?: string;
  minDate?: Date;
  maxDate?: Date;
}
