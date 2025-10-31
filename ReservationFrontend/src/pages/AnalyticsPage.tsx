import DamageOrDirtinessLevelChartCard from "@/components/Analytics/DamageOrDirtinessLevelChartCard";
import VehicleStatusChartCard from "../components/Analytics/VehiclesStatusChartCard";
import { ThemeToggler } from "@/components/ThemeToggler";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import ReservationsAmountChartCard from "@/components/Analytics/ReservationsAmountChartCard";
import ReservationsCountChartCard from "@/components/Analytics/ReservationsCountChartCard";
import VehiclesKmTravelledChartCard from "@/components/Analytics/VehiclesKmTravelledChartCard";

function AnalyticsPage() {
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
        <div className="grid grid-cols-3 gap-4 w-full px-2">
          <VehicleStatusChartCard></VehicleStatusChartCard>
          <DamageOrDirtinessLevelChartCard></DamageOrDirtinessLevelChartCard>
          <DamageOrDirtinessLevelChartCard
            dirtiness={true}></DamageOrDirtinessLevelChartCard>
          <div className="col-span-full">
            <ReservationsAmountChartCard></ReservationsAmountChartCard>
          </div>
          <div className="col-span-full">
            <ReservationsCountChartCard></ReservationsCountChartCard>
          </div>
          <div className="col-span-full">
            <VehiclesKmTravelledChartCard></VehiclesKmTravelledChartCard>
          </div>
        </div>
      </div>
      ;
    </SidebarInset>
  );
}

export default AnalyticsPage;
