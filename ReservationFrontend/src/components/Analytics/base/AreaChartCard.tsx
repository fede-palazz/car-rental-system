import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { SumAndAverageAreaChartData } from "@/models/analytics/SumAndAverageAreaChartData";
import { DateRange } from "react-day-picker";
import { DateTimePicker } from "@/components/ui/date-time-picker";

export function AreaChartCard({
  title,
  subtitle = undefined,
  chartConfig,
  chartData,
  granularity,
  setGranularity,
  dateRange,
  setDateRange,
  yAxeUnit,
}: {
  title: string;
  subtitle?: string | undefined;
  chartConfig: ChartConfig;
  chartData: SumAndAverageAreaChartData[];
  granularity: "DAY" | "MONTH" | "YEAR";
  setGranularity: (value: "DAY" | "MONTH" | "YEAR") => void;
  dateRange: DateRange;
  setDateRange: (value: DateRange) => void;
  yAxeUnit: "EURO" | "COUNT";
}) {
  const dateTickerFormatter = (value: any) => {
    const date = new Date(value);
    if (granularity === "DAY") {
      return date.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
      });
    } else if (granularity === "MONTH") {
      return date.toLocaleDateString("en-US", {
        month: "short",
        year: "numeric",
      });
    } else if (granularity === "YEAR") {
      return date.getFullYear().toString();
    }
    return new Date(value).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
  };

  return (
    <Card className="pt-0">
      <CardHeader className="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
        <div className="grid flex-1 gap-1">
          <CardTitle>{title}</CardTitle>
          <CardDescription>{subtitle && subtitle}</CardDescription>
        </div>
        <div className="flex items-end gap-5">
          <div className="flex items-end gap-5">
            <div className="flex flex-col">
              <label className="block text-sm font-medium text-muted-foreground mb-2">
                From
              </label>
              <DateTimePicker
                displayFormat={{ hour24: "yyyy-MM-dd", hour12: "yyyy-MM-dd" }}
                calendarDisabled={(val) => {
                  return (
                    val > new Date() ||
                    (dateRange?.to ? val > dateRange.to : false)
                  );
                }}
                defaultPopupValue={
                  dateRange?.from ? dateRange.from : new Date()
                }
                placeholder="From"
                value={dateRange?.from}
                onChange={(value) => {
                  setDateRange({ from: value, to: dateRange?.to });
                }}
                granularity="day"
              />
            </div>
            <div className="flex flex-col">
              <label className="block text-sm font-medium text-muted-foreground mb-2">
                To
              </label>
              <DateTimePicker
                displayFormat={{ hour24: "yyyy-MM-dd", hour12: "yyyy-MM-dd" }}
                calendarDisabled={(val) => {
                  return (
                    val > new Date() ||
                    (dateRange?.from ? val < dateRange.from : false)
                  );
                }}
                defaultPopupValue={
                  dateRange?.to
                    ? dateRange.to
                    : dateRange?.from
                    ? dateRange.from
                    : undefined
                }
                placeholder="To"
                value={dateRange?.to}
                onChange={(value) => {
                  setDateRange({ from: dateRange?.from, to: value });
                }}
                granularity="day"
              />
            </div>
          </div>
          <div className="flex flex-col">
            <label className="block text-sm font-medium text-muted-foreground mb-2">
              Granularity
            </label>
            <Select value={granularity} onValueChange={setGranularity}>
              <SelectTrigger
                className="hidden w-[160px] rounded-lg sm:ml-auto sm:flex"
                aria-label="Select a value">
                <SelectValue placeholder="Day" />
              </SelectTrigger>
              <SelectContent className="rounded-xl">
                <SelectItem value="DAY" className="rounded-lg">
                  Day
                </SelectItem>
                <SelectItem value="MONTH" className="rounded-lg">
                  Month
                </SelectItem>
                <SelectItem value="YEAR" className="rounded-lg">
                  Year
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </CardHeader>
      <CardContent className="pe-2 pt-4 sm:px-6 sm:pt-6">
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[250px] w-full">
          <AreaChart data={chartData}>
            <defs>
              <linearGradient id="fillSum" x1="0" y1="0" x2="0" y2="1">
                <stop
                  offset="5%"
                  stopColor="var(--color-sum)"
                  stopOpacity={0.8}
                />
                <stop
                  offset="95%"
                  stopColor="var(--color-sum)"
                  stopOpacity={0.1}
                />
              </linearGradient>
              <linearGradient id="fillAverage" x1="0" y1="0" x2="0" y2="1">
                <stop
                  offset="5%"
                  stopColor="var(--color-average)"
                  stopOpacity={0.8}
                />
                <stop
                  offset="95%"
                  stopColor="var(--color-average)"
                  stopOpacity={0.1}
                />
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="date"
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tickCount={
                granularity === "DAY" ? 32 : granularity === "MONTH" ? 12 : 5
              }
              tickFormatter={(value) => {
                return dateTickerFormatter(value);
              }}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tickCount={5}
              domain={["auto", "datamax+1"]}
              tickFormatter={(value) => {
                if (yAxeUnit === "EURO") {
                  return `€${Number(value).toFixed(2)}`;
                } else if (yAxeUnit === "COUNT") {
                  return Math.round(value).toString();
                }
                return value;
              }}
              allowDataOverflow={false}
              allowDecimals={yAxeUnit !== "COUNT"}
            />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  labelFormatter={(value) => {
                    return dateTickerFormatter(value);
                  }}
                  indicator="dot"
                />
              }
            />
            <Area
              dataKey="sum"
              type="linear"
              fill="url(#fillSum)"
              stroke="var(--color-sum)"
            />
            <Area
              dataKey="average"
              type="monotone"
              fill="url(#fillAverage)"
              stroke="var(--color-average)"
            />
            <ChartLegend content={<ChartLegendContent payload={undefined} />} />
          </AreaChart>
        </ChartContainer>
      </CardContent>
    </Card>
  );
}
