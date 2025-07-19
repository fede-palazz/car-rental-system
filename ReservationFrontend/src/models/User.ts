import { UserRole } from "@/models/enums/UserRole.ts";

export interface User {
  id: number;
  csrf: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  address: string;
  phoneNumber: string;
  eligibilityScore?: number;
}
