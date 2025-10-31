import { DamageOrDirtinessAnalytics } from "@/models/analytics/DamageOrDirtinessAnalytics";
import { ReservationsCountAnalytics } from "@/models/analytics/ReservationsCountAnalytics";
import { ReservationsTotalAmountAnalytics } from "@/models/analytics/ReservationsTotalAmountAnalytics";
import { VehiclesKmTravelledAnalytics } from "@/models/analytics/VehicleKmTravelledAnalytics";
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

async function getVehicleKmTravelled(
  vin: string,
  desiredStartDate: Date = new Date(
    new Date().setMonth(new Date().getMonth() - 1)
  ),
  desiredEndDate: Date = new Date(),
  average: boolean = false,
  granularity: "DAY" | "MONTH" | "YEAR"
): Promise<VehiclesKmTravelledAnalytics> {
  const queryParams = `desiredStart=${desiredStartDate?.toISOString()}&desiredEnd=${desiredEndDate?.toISOString()}&average=${average}&granularity=${granularity}`;

  const response = await fetch(
    baseURL + `analytics/vehicles/km-travelled/vin/${vin}?${queryParams}`,
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

async function getReservationsAmount(
  desiredStartDate: Date = new Date(
    new Date().setMonth(new Date().getMonth() - 1)
  ),
  desiredEndDate: Date = new Date(),
  average: boolean = false,
  granularity: "DAY" | "MONTH" | "YEAR"
): Promise<ReservationsTotalAmountAnalytics[]> {
  const queryParams = `desiredStart=${desiredStartDate?.toISOString()}&desiredEnd=${desiredEndDate?.toISOString()}&average=${average}&granularity=${granularity}`;

  const response = await fetch(
    baseURL + `analytics/reservations/total-amount?${queryParams}`,
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

async function getReservationsCount(
  desiredStartDate: Date = new Date(
    new Date().setMonth(new Date().getMonth() - 1)
  ),
  desiredEndDate: Date = new Date(),
  average: boolean = false,
  granularity: "DAY" | "MONTH" | "YEAR"
): Promise<ReservationsCountAnalytics[]> {
  const queryParams = `desiredStart=${desiredStartDate?.toISOString()}&desiredEnd=${desiredEndDate?.toISOString()}&average=${average}&granularity=${granularity}`;

  const response = await fetch(
    baseURL + `analytics/reservations/count?${queryParams}`,
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

const AnalyticsAPI = {
  getVehiclesStatus,
  getDamageOrDirtinessAnalytics,
  getVehicleKmTravelled,
  getReservationsAmount,
  getReservationsCount,
};

export default AnalyticsAPI;
