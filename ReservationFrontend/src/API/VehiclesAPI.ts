import { VehicleCreateDTO } from "@/models/dtos/request/VehicleCreateDTO";
import { VehicleUpdateDTO } from "@/models/dtos/request/VehicleUpdateDTO";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { VehicleFilter } from "@/models/filters/VehicleFilter";
import { Vehicle } from "@/models/Vehicle.ts";
import { getCsrfToken } from "./csrfToken";

const baseURL = "http://localhost:8083/api/v1/reservation-service/";

async function getAllVehicles(
  filter?: VehicleFilter,
  order: string = "asc",
  sort: string = "vin",
  page: number = 0,
  size: number = 9
): Promise<PagedResDTO<Vehicle>> {
  const queryParams =
    (filter
      ? Object.entries(filter)
          .filter(([, value]) => value !== undefined)
          .map(([key, value]) => {
            if (value instanceof Date) {
              value = value.toISOString();
            }
            return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
          })
          .join("&")
      : "") +
    `&order=${encodeURIComponent(order)}&sort=${encodeURIComponent(
      sort
    )}&page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;

  const response = await fetch(baseURL + `vehicles?${queryParams}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
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

async function getVehicleById(id: number): Promise<Vehicle> {
  const response = await fetch(baseURL + `vehicles/${id}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
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

async function deleteVehicleById(id: number): Promise<null> {
  const response = await fetch(baseURL + `vehicles/${id}`, {
    method: "DELETE",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
  });
  if (response.ok) {
    return null;
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

async function createVehicle(vehicleDTO: VehicleCreateDTO): Promise<Vehicle> {
  const response = await fetch(baseURL + "vehicles", {
    method: "POST",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(vehicleDTO),
  });
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

async function editVehicleById(
  vehicleDTO: VehicleUpdateDTO,
  id: number
): Promise<Vehicle> {
  const response = await fetch(baseURL + `vehicles/${id}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(vehicleDTO),
  });
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

async function getAvailableVehicles(
  carModelId: number,
  desiredStartDate: Date,
  desiredEndDate: Date,
  order: string = "asc",
  sort: string = "vin",
  page: number = 0,
  size: number = 9
): Promise<PagedResDTO<Vehicle>> {
  const queryParams = `carModelId=${carModelId}&desiredStart=${desiredStartDate.toISOString()}&desiredEnd=${desiredEndDate.toISOString()}&order=${encodeURIComponent(
    order
  )}&sort=${encodeURIComponent(sort)}&page=${encodeURIComponent(
    page
  )}&size=${encodeURIComponent(size)}`;

  const response = await fetch(baseURL + `vehicles/available?${queryParams}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
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

const VehicleAPI = {
  getAllVehicles,
  getVehicleById,
  deleteVehicleById,
  createVehicle,
  editVehicleById,
  getAvailableVehicles,
};

export default VehicleAPI;
