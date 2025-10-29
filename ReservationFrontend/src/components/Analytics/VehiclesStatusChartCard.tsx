import { toast } from "sonner";
import PieChartCard from "./base/PieChartCard";
import PieChartConfigs from "./base/config/pieChartConfigs";
import AnalyticsAPI from "@/API/AnalyticsAPI";
import { VehicleStatusesAnalytics } from "@/models/analytics/VehicleStatusesAnalytics";
import { PieChartData } from "@/models/analytics/PieChartData";
import { useEffect, useState } from "react";

function VehiclesStatusChartCard() {
  const [vehiclesStatusData, setVehiclesStatusData] = useState<PieChartData[]>(
    []
  );

  useEffect(() => {
    AnalyticsAPI.getVehiclesStatus(new Date())
      .then((vehicleStatusesAnalytics: VehicleStatusesAnalytics) => {
        setVehiclesStatusData([
          {
            label: "available",
            count: vehicleStatusesAnalytics.availableCount,
            fill: "var(--color-available)",
          },
          {
            label: "rented",
            count: vehicleStatusesAnalytics.rentedCount,
            fill: "var(--color-rented)",
          },
          {
            label: "inMaintenance",
            count: vehicleStatusesAnalytics.inMaintenanceCount,
            fill: "var(--color-inMaintenance)",
          },
        ]);
      })
      .catch((err) => {
        toast.error(err);
      });
  }, []);

  return (
    <PieChartCard
      title={"Current Vehicles status"}
      chartConfig={PieChartConfigs.vehiclesStatusConfig}
      chartData={vehiclesStatusData}
    />
  );
}

export default VehiclesStatusChartCard;
