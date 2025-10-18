let csrfToken: string = "";

export function setCsrfToken(token: string) {
  csrfToken = token;
}

export function getCsrfToken(): string {
  console.log("ECCHECCAZZO");
  console.log(csrfToken);
  return csrfToken as string;
}
