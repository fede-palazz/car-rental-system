export function localizeDates(obj: any): any {
  if (obj === null || obj === undefined) return obj;

  if (
    typeof obj === "string" &&
    /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(obj)
  ) {
    // Append 'Z' if the server sends UTC without Z
    return new Date(obj.endsWith("Z") ? obj : obj + "Z");
  }

  if (Array.isArray(obj)) {
    return obj.map(localizeDates);
  }

  if (typeof obj === "object") {
    const result: any = {};
    for (const key in obj) {
      result[key] = localizeDates(obj[key]);
    }
    return result;
  }

  return obj;
}
