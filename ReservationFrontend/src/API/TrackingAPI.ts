import { TrackingSession } from "@/models/tracking/TrackingSession";

const baseURL = "http://localhost:8083/api/v1/tracking-service/";

async function getTrackingSessions(): Promise<TrackingSession[]> {
  const response = await fetch(baseURL + `tracking/sessions`, {
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

const TrackingAPI = {
  getTrackingSessions,
};

export default TrackingAPI;
