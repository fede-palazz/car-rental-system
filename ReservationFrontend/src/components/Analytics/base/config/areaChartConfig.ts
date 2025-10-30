import { ChartConfig } from "@/components/ui/chart";

const sumAndAverageChartConfig = {
  count: {
    label: "Km",
  },
  average: {
    label: "Average",
    color: "var(--chart-1)",
  },
  sum: {
    label: "Total",
    color: "var(--chart-2)",
  },
} satisfies ChartConfig;

const AreaChartConfigs = { sumAndAverageChartConfig };

export default AreaChartConfigs;
