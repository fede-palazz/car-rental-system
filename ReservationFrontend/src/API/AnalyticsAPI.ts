import { DamageOrDirtinessAnalytics } from "@/models/analytics/DamageOrDirtinessAnalytics";
import { VehicleStatusesAnalytics } from "@/models/analytics/VehicleStatusesAnalytics";

const baseURL = "http://localhost:8083/api/v1/analytics-service/";

async function getVehiclesStatus(
  desiredDate: Date = new Date()
): Promise<VehicleStatusesAnalytics> {
  const queryParams = `desiredDate=${desiredDate?.toISOString()}`;

  const response = await fetch(
    baseURL + `analytics/vehicles/status/date?${queryParams}`,
    {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  if (response.ok) {
    const res = await response.json();
    return res;
  } else {
    const errDetail = await response.json();
    if (Array.isArray(errDetail.errors)) {
      throw new Error(
        errDetail.errors[0].msg ||
          "Something went wrong, please reload the page"
      );
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function getDamageOrDirtinessAnalytics(
  desiredStartDate: Date = new Date(
    new Date().setMonth(new Date().getMonth() - 1)
  ),
  desiredEndDate: Date = new Date(),
  dirtiness: boolean = false
): Promise<DamageOrDirtinessAnalytics> {
  const queryParams = `desiredStart=${desiredStartDate?.toISOString()}&desiredEnd=${desiredEndDate?.toISOString()}&dirtiness=${dirtiness}`;

  const response = await fetch(
    baseURL + `analytics/reservations/level?${queryParams}`,
    {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  if (response.ok) {
    const res = await response.json();
    return res;
  } else {
    const errDetail = await response.json();
    if (Array.isArray(errDetail.errors)) {
      throw new Error(
        errDetail.errors[0].msg ||
          "Something went wrong, please reload the page"
      );
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

const AnalyticsAPI = {
  getVehiclesStatus,
  getDamageOrDirtinessAnalytics,
};

export default AnalyticsAPI;
