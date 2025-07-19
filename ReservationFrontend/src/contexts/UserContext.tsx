import React from "react";
import { User } from "../models/User";

const UserContext = React.createContext<User | undefined>(undefined);

export default UserContext;
