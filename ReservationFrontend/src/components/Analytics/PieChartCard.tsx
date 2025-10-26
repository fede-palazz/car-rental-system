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
import { Spinner } from "../ui/spinner";

function PieChartCard({
  title,
  subtitle = undefined,
  chartConfig,
  chartData,
}: {
  title: string;
  subtitle?: string | undefined;
  chartConfig: ChartConfig;
  chartData: PieChartData[];
}) {
  return (
    <Card className="flex flex-col">
      <CardHeader className="items-center pb-0">
        <CardTitle>{title}</CardTitle>
        {subtitle && <CardDescription>{subtitle}</CardDescription>}
      </CardHeader>
      <CardContent className="flex-1 pb-0">
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
