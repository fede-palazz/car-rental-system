import { CarModelCreateDTO } from "@/models/dtos/request/CarModelCreateDTO.ts";
import { CarModel } from "../models/CarModel.ts";
import { CarFeature } from "@/models/CarFeature.ts";
import { CarModelFilter } from "@/models/filters/CarModelFilter.ts";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO.ts";
import { DateRange } from "react-day-picker";
import { getCsrfToken } from "./csrfToken.ts";

const baseURL = "http://localhost:8083/api/v1/reservation-service/";

async function getAllModels(
  filter?: CarModelFilter,
  order: string = "asc",
  sort: string = "brand",
  page: number = 0,
  size: number = 9
): Promise<PagedResDTO<CarModel>> {
  const queryParams =
    (filter
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

  const response = await fetch(baseURL + `models?${queryParams}`, {
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function getAvailableModels(
  dateRange: DateRange | undefined,
  filter?: CarModelFilter,
  order: string = "asc",
  sort: string = "brand",
  page: number = 0,
  size: number = 9,
  singlePage: boolean = false
): Promise<PagedResDTO<CarModel>> {
  const queryParams =
    (filter != undefined
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
    )}&page=${encodeURIComponent(page)}&size=${encodeURIComponent(
      size
    )}&singlePage=${encodeURIComponent(singlePage)}` +
    (dateRange
      ? `&desiredPickUpDate=${encodeURIComponent(
          dateRange.from!.toISOString()
        )}&desiredDropOffDate=${encodeURIComponent(
          dateRange.to!.toISOString()
        )}`
      : "");

  const response = await fetch(
    baseURL + `reservations/search-availability?${queryParams}`,
    {
      method: "GET",
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": getCsrfToken(),
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

async function getModelById(id: number): Promise<CarModel> {
  const response = await fetch(baseURL + `models/${id}`, {
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function deleteModelById(id: number): Promise<null> {
  const response = await fetch(baseURL + `models/${id}`, {
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function getCarFeatures(): Promise<CarFeature[]> {
  const response = await fetch(baseURL + "models/features", {
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function getCarFeatureById(id: number): Promise<CarFeature> {
  const response = await fetch(baseURL + `models/features/${id}`, {
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
    }
    throw new Error(
      errDetail.error || "Something went wrong, please reload the page"
    );
  }
}

async function createModel(modelDTO: CarModelCreateDTO): Promise<CarModel> {
  if (modelDTO.motorDisplacement === undefined) {
    delete modelDTO.motorDisplacement;
  }
  const response = await fetch(baseURL + "models", {
    method: "POST",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(modelDTO),
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

async function editModelById(
  modelDTO: CarModelCreateDTO,
  id: number
): Promise<CarModel> {
  const response = await fetch(baseURL + `models/${id}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(modelDTO),
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

const CarModelAPI = {
  getAllModels,
  getAvailableModels,
  getModelById,
  deleteModelById,
  getCarFeatures,
  getCarFeatureById,
  createModel,
  editModelById,
};

export default CarModelAPI;
