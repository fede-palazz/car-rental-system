import { Pie, PieChart } from "recharts";

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
import { PieChartData } from "@/models/analytics/PieChartData";
import { Spinner } from "../../ui/spinner";
import { DateTimePicker } from "../../ui/date-time-picker";
import { DateRange } from "react-day-picker";

function PieChartCard({
  title,
  subtitle = undefined,
  chartConfig,
  chartData,
  dateRange,
  setDateRange,
}: {
  title: string;
  subtitle?: string | undefined;
  chartConfig: ChartConfig;
  chartData: PieChartData[];
  dateRange?: DateRange | undefined;
  setDateRange?: (value: DateRange) => void | undefined;
}) {
  return (
    <Card className="flex flex-col">
      <CardHeader className="flex items-center justify-between pb-0">
        <div>
          <CardTitle>{title}</CardTitle>
          {subtitle && <CardDescription>{subtitle}</CardDescription>}
        </div>
        <div className="text-sm text-muted-foreground">
          Total: {chartData.reduce((acc, item) => acc + item.count, 0)}
        </div>
      </CardHeader>
      <CardContent className="flex-1 pb-0">
        {dateRange != undefined && setDateRange != undefined && (
          <div className="w-full flex gap-5">
            <div className="w-1/2 flex-col">
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
            <div className="w-1/2 flex-col">
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
        )}
        {chartData.length > 0 ? (
          <ChartContainer
            config={chartConfig}
            className="mx-auto aspect-square max-h-[300px]">
            <PieChart>
              <Pie
                data={chartData}
                dataKey="count"
                nameKey={"label"}
                fill="fill"
              />
              <ChartTooltip
                cursor={false}
                content={<ChartTooltipContent nameKey="label" hideLabel />}
              />
              <ChartLegend
                content={
                  <ChartLegendContent nameKey="label" payload={undefined} />
                }
                className="-translate-y-2 flex-wrap gap-2 *:basis-1/4 *:justify-center"
              />
            </PieChart>
          </ChartContainer>
        ) : (
          <Spinner></Spinner>
        )}
      </CardContent>
    </Card>
  );
}

export default PieChartCard;
