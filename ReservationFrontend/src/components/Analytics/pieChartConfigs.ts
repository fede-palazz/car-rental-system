import { ChartConfig } from "../ui/chart";

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

const chartData = [
  { browser: "chrome", visitors: 275, fill: "var(--color-chrome)" },
  { browser: "safari", visitors: 200, fill: "var(--color-safari)" },
  { browser: "firefox", visitors: 187, fill: "var(--color-firefox)" },
  { browser: "edge", visitors: 173, fill: "var(--color-edge)" },
  { browser: "other", visitors: 90, fill: "var(--color-other)" },
];

const chartConfig = {
  visitors: {
    label: "Visitors",
  },
  chrome: {
    label: "Chrome",
    color: "var(--chart-1)",
  },
  safari: {
    label: "Safari",
    color: "var(--chart-2)",
  },
  firefox: {
    label: "Firefox",
    color: "var(--chart-3)",
  },
  edge: {
    label: "Edge",
    color: "var(--chart-4)",
  },
  other: {
    label: "Other",
    color: "var(--chart-5)",
  },
} satisfies ChartConfig;

const PieChartConfigs = { chartConfig, vehiclesStatusConfig, chartData };

export default PieChartConfigs;
