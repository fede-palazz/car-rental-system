import { ChartConfig } from "@/components/ui/chart";
import { damageAndDirtinessLevelLabels } from "@/utils/damageAndDirtinessLevelLabels";

const vehiclesStatusConfig = {
  count: {
    label: "Vehicles",
  },
  available: {
    label: "Available",
    color: "var(--chart-1)",
  },
  rented: {
    label: "Rented",
    color: "var(--chart-2)",
  },
  inMaintenance: {
    label: "In Maintenance",
    color: "var(--chart-3)",
  },
} satisfies ChartConfig;

const damageOrDirtinessLevelConfig = {
  count: {
    label: "Level",
  },
  zero: {
    label: damageAndDirtinessLevelLabels[0],
    color: "var(--chart-1)",
  },
  one: {
    label: damageAndDirtinessLevelLabels[1],
    color: "var(--chart-2)",
  },
  two: {
    label: damageAndDirtinessLevelLabels[2],
    color: "var(--chart-3)",
  },
  three: {
    label: damageAndDirtinessLevelLabels[3],
    color: "var(--chart-4)",
  },
  four: {
    label: damageAndDirtinessLevelLabels[4],
    color: "var(--chart-5)",
  },
  five: {
    label: damageAndDirtinessLevelLabels[5],
    color: "var(--chart-6)",
  },
} satisfies ChartConfig;

const PieChartConfigs = { vehiclesStatusConfig, damageOrDirtinessLevelConfig };

export default PieChartConfigs;
