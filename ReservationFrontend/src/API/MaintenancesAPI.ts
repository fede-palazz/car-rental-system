import { MaintenanceReqDTO } from "@/models/dtos/request/MaintenanceReqDTO";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { MaintenanceFilter } from "@/models/filters/MaintenanceFilter";
import { Maintenance } from "@/models/Maintenance";
import { getCsrfToken } from "./csrfToken";

const baseURL = "http://localhost:8083/api/v1/reservation-service/";

async function getMaintenancesByVehicleId(
  vehicleId: number,
  filter?: MaintenanceFilter,
  order: string = "asc",
  sort: string = "startDate",
  page: number = 0,
  size: number = 9
): Promise<PagedResDTO<Maintenance>> {
  const queryParams =
    (filter != undefined
      ? Object.entries(filter)
          .filter(([, value]) => value !== undefined)
          .map(
            ([key, value]) =>
              `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
          )
          .join("&")
      : "") +
    `&order=${encodeURIComponent(order)}&sort=${encodeURIComponent(
      sort
    )}&page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;

  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/maintenances?${queryParams}`,
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
    console.log(errDetail);
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

async function getMaintenanceById(
  vehicleId: number,
  maintenanceId: number
): Promise<Maintenance> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/maintenances/${maintenanceId}`,
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

async function createMaintenance(
  vehicleId: number,
  maintenanceDTO: MaintenanceReqDTO
): Promise<Maintenance> {
  console.log(maintenanceDTO);
  const response = await fetch(baseURL + `vehicles/${vehicleId}/maintenances`, {
    method: "POST",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      ...maintenanceDTO,
      startDate: maintenanceDTO.startDate.toISOString(),
      plannedEndDate: maintenanceDTO.plannedEndDate.toISOString(),
    }),
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function editMaintenanceById(
  vehicleId: number,
  maintenanceDTO: MaintenanceReqDTO,
  maintenanceId: number
): Promise<Maintenance> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/maintenances/${maintenanceId}`,
    {
      method: "PUT",
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": getCsrfToken(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(maintenanceDTO),
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

async function finalizeMaintenanceByMaintenanceId(
  vehicleId: number,
  maintenanceId: number,
  actualEndDate: Date
): Promise<Maintenance> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/maintenances/${maintenanceId}/finalize`,
    {
      method: "PUT",
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": getCsrfToken(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(actualEndDate),
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

async function deleteMaintenanceById(
  vehicleId: number,
  maintenanceId: number
): Promise<null> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/maintenances/${maintenanceId}`,
    {
      method: "DELETE",
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": getCsrfToken(),
        "Content-Type": "application/json",
      },
    }
  );
  if (response.ok) {
    return null;
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

const MaintenancesAPI = {
  getMaintenancesByVehicleId,
  getMaintenanceById,
  deleteMaintenanceById,
  createMaintenance,
  editMaintenanceById,
  finalizeMaintenanceByMaintenanceId,
};

export default MaintenancesAPI;
