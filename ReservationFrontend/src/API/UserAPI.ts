import { User } from "@/models/User";

const baseURL = "http://localhost:8083";

async function getLoggedUserInfo(): Promise<User> {
  const response = await fetch(baseURL + `/me`, {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (response.ok) {
    const res = await response.json();
    if (res.error) {
      throw new Error(res.error);
    } else {
      const user = await getUserByUsername(res.name);
      return { ...user, csrf: res.csrf };
    }
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

async function getUserByUsername(username: string): Promise<User> {
  const response = await fetch(
    baseURL + `/api/v1/user-service/users/username/${username}`,
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
    return { ...res, csrf: "" };
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

const UserAPI = { getLoggedUserInfo };

export default UserAPI;
