import AnalyticsAPI from "@/API/AnalyticsAPI";
import { SumAndAverageAreaChartData } from "@/models/analytics/SumAndAverageAreaChartData";
import { VehiclesKmTravelledAnalytics } from "@/models/analytics/VehicleKmTravelledAnalytics";
import { useEffect, useState } from "react";
import { DateRange } from "react-day-picker";
import { AreaChartCard } from "./base/AreaChartCard";
import AreaChartConfigs from "./base/config/areaChartConfig";
import { toast } from "sonner";
import { Spinner } from "../ui/spinner";
import VehicleAPI from "@/API/VehiclesAPI";
import { VehicleVinsResDTO } from "@/models/dtos/response/VehicleVinsResDTO";

function VehiclesKmTravelledChartCard() {
  const [granularity, setGranularity] = useState<"DAY" | "MONTH" | "YEAR">(
    "DAY"
  );

  const [sumAndAvgKmTravelled, setSumAndAvgKmTravelled] = useState<
    SumAndAverageAreaChartData[]
  >([]);

  const [selectedVin, setSelectedVin] = useState<string | undefined>(undefined);
  const [vinFilter, setVinFilter] = useState<string | undefined>(undefined);

  const [availableVins, setAvailableVins] = useState<string[]>([]);

  const [dateRange, setDateRange] = useState<DateRange>({
    from: new Date(new Date().setMonth(new Date().getMonth() - 1)),
    to: new Date(),
  });

  const fetchKm = async () => {
    if (!dateRange.from || !dateRange.to || !selectedVin) return;
    try {
      const [sum, avg] = await Promise.all([
        AnalyticsAPI.getVehicleKmTravelled(
          selectedVin,
          dateRange.from,
          dateRange.to,
          false,
          granularity
        ),
        AnalyticsAPI.getVehicleKmTravelled(
          selectedVin,
          dateRange.from,
          dateRange.to,
          true,
          granularity
        ),
      ]);

      const sumAndAvgData: SumAndAverageAreaChartData[] = sum.map(
        (sumEntry) => {
          const avgEntry = avg?.find(
            (a) => a.elementStart === sumEntry.elementStart
          );
          return {
            date: sumEntry.elementStart,
            sum: sumEntry.vehicleKmTravelled,
            average: avgEntry ? avgEntry.vehicleKmTravelled : 0,
          };
        }
      );
      setSumAndAvgKmTravelled(sumAndAvgData);
    } catch (err: unknown) {
      if (err instanceof Error) {
        toast.error(err.message);
      } else {
        toast.error("An unknown error occurred.");
      }
    }
  };
  useEffect(() => {
    VehicleAPI.getAllVins(vinFilter)
      .then((vins: VehicleVinsResDTO[]) => {
        const newVins = vins.map((v) => v.vin);
        setAvailableVins(newVins);
        if (selectedVin == undefined) {
          setSelectedVin(newVins[0]);
        }
      })
      .catch((err: Error) => {
        toast.error("Error fetching Vehicles");
      });
  }, [vinFilter]);

  useEffect(() => {
    if (dateRange.from && dateRange.to) {
      fetchKm();
    }
  }, [dateRange.from, dateRange.to, granularity, selectedVin]);

  return !selectedVin ? (
    <Spinner></Spinner>
  ) : (
    <AreaChartCard
      title={"Km Travelled"}
      subtitle="Km Travelled average and total by vehicle"
      chartConfig={AreaChartConfigs.sumAndAverageChartConfig}
      chartData={sumAndAvgKmTravelled}
      granularity={granularity}
      setGranularity={setGranularity}
      dateRange={dateRange}
      setDateRange={setDateRange}
      yAxeUnit="COUNT"
      availableDropdownAlternatives={availableVins}
      selectedAlternative={selectedVin}
      dropdownFilter={vinFilter}
      setDropdownFilter={setVinFilter}
      setSelectedAlternative={setSelectedVin}></AreaChartCard>
  );
}

export default VehiclesKmTravelledChartCard;
