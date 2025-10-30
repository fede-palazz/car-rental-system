import { toast } from "sonner";
import PieChartCard from "./base/PieChartCard";
import PieChartConfigs from "./base/config/pieChartConfigs";
import AnalyticsAPI from "@/API/AnalyticsAPI";
import { PieChartData } from "@/models/analytics/PieChartData";
import { useEffect, useState } from "react";
import { DamageOrDirtinessAnalytics } from "@/models/analytics/DamageOrDirtinessAnalytics";
import { DateRange } from "react-day-picker";

function DamageOrDirtinessLevelChartCard({
  dirtiness = false,
}: {
  dirtiness?: boolean;
}) {
  const [damageOrDirtinessLevelData, setDamageOrDirtinessLevelData] = useState<
    PieChartData[]
  >([]);
  const [dateRange, setDateRange] = useState<DateRange>({
    from: new Date(new Date().setMonth(new Date().getMonth() - 1)),
    to: new Date(),
  });

  const fetchLevels = () => {
    AnalyticsAPI.getDamageOrDirtinessAnalytics(
      dateRange.from,
      dateRange.to,
      dirtiness
    )
      .then((damageOrDirtinessAnalytics: DamageOrDirtinessAnalytics) => {
        console.log(damageOrDirtinessAnalytics);
        setDamageOrDirtinessLevelData([
          {
            label: "zero", //damageAndDirtinessLevelLabels[0],
            count: damageOrDirtinessAnalytics.levelZeroCount,
            fill: "var(--color-zero)",
          },
          {
            label: "one", //damageAndDirtinessLevelLabels[1],
            count: damageOrDirtinessAnalytics.levelOneCount,
            fill: "var(--color-one)",
          },
          {
            label: "two", //damageAndDirtinessLevelLabels[2],
            count: damageOrDirtinessAnalytics.levelTwoCount,
            fill: "var(--color-two)",
          },
          {
            label: "three", //damageAndDirtinessLevelLabels[3],
            count: damageOrDirtinessAnalytics.levelThreeCount,
            fill: "var(--color-three)",
          },
          {
            label: "four", //damageAndDirtinessLevelLabels[4],
            count: damageOrDirtinessAnalytics.levelFourCount,
            fill: "var(--color-four)",
          },
          {
            label: "five", //damageAndDirtinessLevelLabels[5],
            count: damageOrDirtinessAnalytics.levelFiveCount,
            fill: "var(--color-five)",
          },
        ]);
      })
      .catch((err) => {
        toast.error(err);
      });
  };

  useEffect(() => {
    if (dateRange.from && dateRange.to) {
      fetchLevels();
    }
  }, [dateRange.from, dateRange.to]);

  return (
    <PieChartCard
      title={dirtiness ? "Dirtiness Level" : "Damage level"}
      chartConfig={PieChartConfigs.damageOrDirtinessLevelConfig}
      chartData={damageOrDirtinessLevelData}
      dateRange={dateRange}
      setDateRange={setDateRange}
    />
  );
}

export default DamageOrDirtinessLevelChartCard;
