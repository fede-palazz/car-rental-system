const baseURL = "http://localhost:8083/api/v1/tracking-service/";

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
