import AnalyticsAPI from "@/API/AnalyticsAPI";
import { SumAndAverageAreaChartData } from "@/models/analytics/SumAndAverageAreaChartData";
import { useEffect, useState } from "react";
import { DateRange } from "react-day-picker";
import { AreaChartCard } from "./base/AreaChartCard";
import AreaChartConfigs from "./base/config/areaChartConfig";

function ReservationsCountChartCard() {
  const [granularity, setGranularity] = useState<"DAY" | "MONTH" | "YEAR">(
    "DAY"
  );

  const [sumAndAvgAmountData, setSumAndAvgAmountData] = useState<
    SumAndAverageAreaChartData[]
  >([]);

  const [dateRange, setDateRange] = useState<DateRange>({
    from: new Date(new Date().setMonth(new Date().getMonth() - 1)),
    to: new Date(),
  });

  const fetchAmount = async () => {
    if (!dateRange.from || !dateRange.to) return;
    try {
      const [sum, avg] = await Promise.all([
        AnalyticsAPI.getReservationsCount(
          dateRange.from,
          dateRange.to,
          false,
          granularity
        ),
        AnalyticsAPI.getReservationsCount(
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
            sum: sumEntry.reservationsCount,
            average: avgEntry ? avgEntry.reservationsCount : 0,
          };
        }
      );
      setSumAndAvgAmountData(sumAndAvgData);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (dateRange.from && dateRange.to) {
      fetchAmount();
    }
  }, [dateRange.from, dateRange.to, granularity]);

  return (
    <AreaChartCard
      title={"Reservations"}
      subtitle="Average and total number of reservations"
      chartConfig={AreaChartConfigs.sumAndAverageChartConfig}
      chartData={sumAndAvgAmountData}
      granularity={granularity}
      setGranularity={setGranularity}
      dateRange={dateRange}
      setDateRange={setDateRange}
      yAxeUnit="COUNT"></AreaChartCard>
  );
}

export default ReservationsCountChartCard;
