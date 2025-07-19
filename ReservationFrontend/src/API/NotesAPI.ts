import { NoteReqDTO } from "@/models/dtos/request/NoteReqDTO";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { NoteFilter } from "@/models/filters/NoteFilter";
import { Note } from "@/models/Note";
import { getCsrfToken } from "./csrfToken";

const baseURL = "http://localhost:8083/api/v1/reservation-service/";

async function getNotesByVehicleId(
  vehicleId: number,
  filter?: NoteFilter,
  order: string = "asc",
  sort: string = "date",
  page: number = 0,
  size: number = 9
): Promise<PagedResDTO<Note>> {
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

  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/notes?${queryParams}`,
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

async function getNoteById(vehicleId: number, noteId: number): Promise<Note> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/notes/${noteId}`,
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

async function createNote(
  vehicleId: number,
  noteDTO: NoteReqDTO
): Promise<Note> {
  const response = await fetch(baseURL + `vehicles/${vehicleId}/notes`, {
    method: "POST",
    credentials: "include",
    headers: {
      "X-CSRF-TOKEN": getCsrfToken(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(noteDTO),
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

async function editNoteById(
  vehicleId: number,
  noteDTO: NoteReqDTO,
  noteId: number
): Promise<Note> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/notes/${noteId}`,
    {
      method: "PUT",
      credentials: "include",
      headers: {
        "X-CSRF-TOKEN": getCsrfToken(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(noteDTO),
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

async function deleteNoteById(
  vehicleId: number,
  noteId: number
): Promise<null> {
  const response = await fetch(
    baseURL + `vehicles/${vehicleId}/notes/${noteId}`,
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

const NotesAPI = {
  getNotesByVehicleId,
  getNoteById,
  createNote,
  editNoteById,
  deleteNoteById,
};

export default NotesAPI;
