import AnalyticsAPI from "@/API/AnalyticsAPI";
import { VehiclesKmTravelledAnalytics } from "@/models/analytics/VehicleKmTravelledAnalytics";
import { useEffect, useState } from "react";

function VehiclesKmTravelledChartCard() {
  const [averageKmTravelled, setAvgKmTravelled] =
    useState<VehiclesKmTravelledAnalytics>();

  const [sumKmTravelled, setSumKmTravelled] =
    useState<VehiclesKmTravelledAnalytics>();

  useEffect(() => {}, []);
}

export default VehiclesKmTravelledChartCard;
