import AnalyticsAPI from "@/API/AnalyticsAPI";
import PieChartCard from "@/components/Analytics/PieChartCard";
import PieChartConfigs from "@/components/Analytics/pieChartConfigs";
import { ThemeToggler } from "@/components/ThemeToggler";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { PieChartData } from "@/models/analytics/PieChartData";
import { VehicleStatusesAnalytics } from "@/models/analytics/VehicleStatusesAnalytics";
import { useEffect, useState } from "react";
import { toast } from "sonner";

function AnalyticsPage() {
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
    <SidebarInset
      id="sidebar-inset"
      className="p-2 flex flex-col w-full overflow-x-auto">
      <div className=" flex items-center justify-between border-b mb-2">
        <SidebarTrigger />
        {
          <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">{`Analytics`}</h1>
        }
        <ThemeToggler></ThemeToggler>
      </div>
      <div className="grow flex flex-col">
        <PieChartCard
          title={"Current Vehicles status"}
          chartConfig={PieChartConfigs.vehiclesStatusConfig}
          chartData={vehiclesStatusData}></PieChartCard>
      </div>
      ;
    </SidebarInset>
  );
}

export default AnalyticsPage;
