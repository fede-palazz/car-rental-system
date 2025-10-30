import { FinalizeReservationDTO } from "@/models/dtos/request/FinalizeReservationDTO";
import { ReservationCreateDTO } from "@/models/dtos/request/ReservationCreateDTO";
import { CustomerReservationResDTO } from "@/models/dtos/response/CustomerReservationResDTO";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { StaffReservationResDTO } from "@/models/dtos/response/StaffReservationResDTO";
import { ReservationFilter } from "@/models/filters/ReservationFilter";
import { Reservation } from "@/models/Reservation";
import { getCsrfToken } from "./csrfToken";
import { localizeDates } from "@/utils/dateUtils";

const baseURL = "http://localhost:8083/api/v1/reservation-service/";

async function getAllReservations(
  filter?: ReservationFilter,
  order: string = "asc",
  sort: string = "brand",
  page: number = 0,
  size: number = 9,
  isCustomer = true
): Promise<PagedResDTO<Reservation>> {
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
    )}&page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
  const response = await fetch(baseURL + `reservations?${queryParams}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (response.ok) {
    const res = await response.json();
    if (!isCustomer) {
      res.content = localizeDates(res.content).map(
        (reservation: StaffReservationResDTO) => {
          const { commonInfo, ...otherProperties } =
            reservation as StaffReservationResDTO;
          return {
            ...commonInfo,
            creationDate: new Date(commonInfo.creationDate),
            plannedPickUpDate: new Date(commonInfo.plannedPickUpDate),
            plannedDropOffDate: new Date(commonInfo.plannedDropOffDate),
            actualDropOffDate: commonInfo.actualDropOffDate
              ? new Date(commonInfo.actualDropOffDate)
              : undefined,
            actualPickUpDate: commonInfo.actualPickUpDate
              ? new Date(commonInfo.actualPickUpDate)
              : undefined,
            ...otherProperties,
          } as Reservation;
        }
      );
    } else {
      res.content = localizeDates(res.content).map(
        (reservation: CustomerReservationResDTO) => {
          return {
            ...reservation,
            creationDate: new Date(reservation.creationDate),
            plannedPickUpDate: new Date(reservation.plannedPickUpDate),
            plannedDropOffDate: new Date(reservation.plannedDropOffDate),
            actualDropOffDate: reservation.actualDropOffDate
              ? new Date(reservation.actualDropOffDate)
              : undefined,
            actualPickUpDate: reservation.actualPickUpDate
              ? new Date(reservation.actualPickUpDate)
              : undefined,
          } as Reservation;
        }
      );
    }

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

async function getPendingReservation(): Promise<PagedResDTO<Reservation>> {
  const queryParams = `status=PENDING&order=${encodeURIComponent(
    "asc"
  )}&sort=${encodeURIComponent("brand")}&page=${encodeURIComponent(
    0
  )}&size=${encodeURIComponent(1)}`;
  const response = await fetch(baseURL + `reservations?${queryParams}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (response.ok) {
    const res = await response.json();

    res.content = localizeDates(res.content).map(
      (reservation: CustomerReservationResDTO) => {
        return {
          ...reservation,
          creationDate: new Date(reservation.creationDate),
          plannedPickUpDate: new Date(reservation.plannedPickUpDate),
          plannedDropOffDate: new Date(reservation.plannedDropOffDate),
          actualDropOffDate: reservation.actualDropOffDate
            ? new Date(reservation.actualDropOffDate)
            : undefined,
          actualPickUpDate: reservation.actualPickUpDate
            ? new Date(reservation.actualPickUpDate)
            : undefined,
        } as Reservation;
      }
    );

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

async function getReservationById(id: number): Promise<Reservation> {
  const response = await fetch(baseURL + `reservations/${id}`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (response.ok) {
    const res = await response.json();

    const reservation = localizeDates(res) as StaffReservationResDTO;

    const { commonInfo, ...otherProperties } = reservation;

    const returningReservation = {
      ...commonInfo,
      creationDate: new Date(commonInfo.creationDate),
      plannedPickUpDate: new Date(commonInfo.plannedPickUpDate),
      plannedDropOffDate: new Date(commonInfo.plannedDropOffDate),
      actualDropOffDate: commonInfo.actualDropOffDate
        ? new Date(commonInfo.actualDropOffDate)
        : undefined,
      actualPickUpDate: commonInfo.actualPickUpDate
        ? new Date(commonInfo.actualPickUpDate)
        : undefined,
      ...otherProperties,
    } as Reservation;

    return returningReservation;
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

async function deleteReservationById(id: number): Promise<null> {
  const response = await fetch(baseURL + `reservations/${id}`, {
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

async function editReservationById(
  reservationDTO: ReservationCreateDTO,
  id: number
): Promise<Reservation> {
  const response = await fetch(baseURL + `reservations/${id}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(reservationDTO),
  });
  if (response.ok) {
    const res = await response.json();
    return localizeDates(res);
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

async function createReservation(
  reservationDTO: ReservationCreateDTO
): Promise<Reservation> {
  const response = await fetch(baseURL + "reservations", {
    method: "POST",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(reservationDTO),
  });
  if (response.ok) {
    const res = await response.json();
    return localizeDates(res);
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

async function setActualPickUpDate(
  id: number,
  actualPickUpDate: Date
): Promise<Reservation> {
  const response = await fetch(baseURL + `reservations/${id}/pick-up-date`, {
    method: "PUT",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ actualPickUpDate: actualPickUpDate.toISOString() }),
  });
  if (response.ok) {
    const res = await response.json();
    return localizeDates(res);
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

async function finalizeReservation(
  id: number,
  finalizeReservation: FinalizeReservationDTO
): Promise<Reservation> {
  const response = await fetch(baseURL + `reservations/${id}/finalize`, {
    method: "PUT",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(finalizeReservation),
  });
  if (response.ok) {
    const res = await response.json();
    return localizeDates(res);
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

async function payReservation(id: number) {
  const response = await fetch(baseURL + `reservations/${id}/payment-request`, {
    method: "POST",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      reservationId: id,
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

async function updateReservationVehicle(
  reservationId: number,
  newVehicleId: number
) {
  const response = await fetch(
    baseURL + `reservations/${reservationId}/vehicle/${newVehicleId}`,
    {
      method: "PUT",
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
    } else {
      throw new Error(
        errDetail.detail ?? "Something went wrong, please reload the page"
      );
    }
  }
}

async function getOverlappingReservations(
  vehicleId: number,
  desiredStartDate: Date,
  desiredEndDate: Date,
  singlePage: boolean = true,
  order: string = "asc",
  sort: string = "brand",
  page: number = 0,
  size: number = 10
): Promise<PagedResDTO<Reservation>> {
  const queryParams = `vehicleId=${vehicleId}&desiredStart=${desiredStartDate.toISOString()}&desiredEnd=${desiredEndDate.toISOString()}&singlePage=${singlePage}&order=${encodeURIComponent(
    order
  )}&sort=${encodeURIComponent(sort)}&page=${encodeURIComponent(
    page
  )}&size=${encodeURIComponent(size)}`;
  const response = await fetch(
    baseURL + `reservations/overlapping?${queryParams}`,
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
    res.content = localizeDates(res.content).map(
      (reservation: StaffReservationResDTO) => {
        const { commonInfo, ...otherProperties } =
          reservation as StaffReservationResDTO;
        return {
          ...commonInfo,
          creationDate: new Date(commonInfo.creationDate),
          plannedPickUpDate: new Date(commonInfo.plannedPickUpDate),
          plannedDropOffDate: new Date(commonInfo.plannedDropOffDate),
          actualDropOffDate: commonInfo.actualDropOffDate
            ? new Date(commonInfo.actualDropOffDate)
            : undefined,
          actualPickUpDate: commonInfo.actualPickUpDate
            ? new Date(commonInfo.actualPickUpDate)
            : undefined,
          ...otherProperties,
        } as Reservation;
      }
    );
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

async function getOverlappingReservationsByReservationId(
  reservationId: number,
  bufferedDropOffDate: Date,
  singlePage: boolean = true,
  order: string = "asc",
  sort: string = "brand",
  page: number = 0,
  size: number = 10
): Promise<PagedResDTO<Reservation>> {
  const queryParams = `bufferedDropOffDate=${bufferedDropOffDate.toISOString()}&singlePage=${singlePage}&order=${encodeURIComponent(
    order
  )}&sort=${encodeURIComponent(sort)}&page=${encodeURIComponent(
    page
  )}&size=${encodeURIComponent(size)}`;
  const response = await fetch(
    baseURL + `reservations/${reservationId}/overlapping?${queryParams}`,
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
    res.content = localizeDates(res.content).map(
      (reservation: StaffReservationResDTO) => {
        const { commonInfo, ...otherProperties } =
          reservation as StaffReservationResDTO;
        return {
          ...commonInfo,
          creationDate: new Date(commonInfo.creationDate),
          plannedPickUpDate: new Date(commonInfo.plannedPickUpDate),
          plannedDropOffDate: new Date(commonInfo.plannedDropOffDate),
          actualDropOffDate: commonInfo.actualDropOffDate
            ? new Date(commonInfo.actualDropOffDate)
            : undefined,
          actualPickUpDate: commonInfo.actualPickUpDate
            ? new Date(commonInfo.actualPickUpDate)
            : undefined,
          ...otherProperties,
        } as Reservation;
      }
    );
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

const ReservationsAPI = {
  getAllReservations,
  getPendingReservation,
  deleteReservationById,
  editReservationById,
  setActualPickUpDate,
  finalizeReservation,
  createReservation,
  payReservation,
  updateReservationVehicle,
  getOverlappingReservations,
  getReservationById,
  getOverlappingReservationsByReservationId,
};

export default ReservationsAPI;
