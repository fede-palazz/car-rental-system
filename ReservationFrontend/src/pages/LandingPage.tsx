import { DateTimePicker } from "@/components/ui/date-time-picker";
import landingImage from "../assets/landing.svg";
import { Button } from "@/components/ui/button";
import { DateRange } from "react-day-picker";
import { useNavigate } from "react-router-dom";
import UserContext from "@/contexts/UserContext";
import { useContext } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ChevronsUpDown } from "lucide-react";
import { UserRole } from "@/models/enums/UserRole";

export function LandingPage({
  date,
  setDate,
}: {
  date: DateRange | undefined;
  setDate: (value: DateRange | undefined) => void;
}) {
  const navigate = useNavigate();
  const user = useContext(UserContext);
  return (
    <div className="flex w-screen h-screen text-center justify-center">
      <img src={landingImage} className="w-1/2 h-auto" alt="boh" />
      <div className="flex w-1/2 flex-col h-screen mt-8">
        <div className="flex justify-end pr-4">
          {user ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button size="lg" variant="ghost">
                  <Avatar className="h-8 w-8 rounded-lg">
                    <AvatarImage src={undefined} alt={user.username} />
                    <AvatarFallback className="rounded-lg">
                      {user.firstName.charAt(0).toUpperCase() +
                        user.lastName.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <div className="grid flex-1 text-left text-sm leading-tight">
                    <span className="truncate font-semibold">
                      {user.username}
                    </span>
                    <span className="truncate text-xs">
                      {user.role.charAt(0).toUpperCase() +
                        user.role.slice(1).toLowerCase()}
                    </span>
                  </div>
                  <ChevronsUpDown className="ml-auto size-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg"
                side={"bottom"}
                align="end"
                sideOffset={4}>
                <DropdownMenuLabel className="p-0 font-normal">
                  <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                    <Avatar className="h-8 w-8 rounded-lg">
                      <AvatarImage src={undefined} alt={user.username} />
                      <AvatarFallback className="rounded-lg">
                        {user.firstName.charAt(0).toUpperCase() +
                          user.lastName.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                    <div className="grid flex-1 text-left text-sm leading-tight">
                      <span className="truncate font-semibold">
                        {user.username}
                      </span>
                      <span className="truncate text-xs">
                        {user.role.charAt(0).toUpperCase() +
                          user.role.slice(1).toLowerCase()}
                      </span>
                    </div>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuGroup>
                  <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                    <span className="material-symbols-outlined md-18">
                      id_card
                    </span>
                    {user.firstName} {user.lastName}
                  </DropdownMenuLabel>
                  <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                    <span className="material-symbols-outlined md-18">
                      mail
                    </span>
                    {user.email}
                  </DropdownMenuLabel>
                  <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                    <span className="material-symbols-outlined md-18">
                      home_pin
                    </span>
                    {user.address}
                  </DropdownMenuLabel>
                  <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                    <span className="material-symbols-outlined md-18">
                      call
                    </span>
                    {user.phoneNumber}
                  </DropdownMenuLabel>
                  {user.role == UserRole.CUSTOMER && (
                    <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                      <span className="material-symbols-outlined md-18">
                        license
                      </span>
                      Score: {user.eligibilityScore}
                    </DropdownMenuLabel>
                  )}
                </DropdownMenuGroup>
                <DropdownMenuSeparator />

                <form action="/logout" method="post">
                  <input
                    type="hidden"
                    name="_csrf"
                    value={user.csrf as string}
                  />
                  <DropdownMenuItem asChild variant="destructive">
                    <button type="submit" className="w-full">
                      <span className="material-symbols-outlined md-18">
                        logout
                      </span>
                      Logout
                    </button>
                  </DropdownMenuItem>
                </form>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button
              size="lg"
              className="justify-self-start self-end"
              onClick={() => {
                window.location.href = "http://localhost:8083/gateway-client";
              }}>
              <span className="material-symbols-outlined items-center md-18">
                login
              </span>
              Login
            </Button>
          )}
        </div>
        <div className="flex flex-col flex-1 justify-center gap-5">
          <h1 className="text-6xl font-extrabold">Welcome</h1>
          <h2 className="text-3xl font-bold">
            Search for available car models
          </h2>
          <div className="w-full flex justify-center gap-8">
            <div className="w-2/5">
              <h3 className="text-lg font-semibold mb-2">From</h3>
              <DateTimePicker
                calendarDisabled={(val) => {
                  return date?.to ? val > date.to : val < new Date();
                }}
                defaultPopupValue={date?.from ? date.from : new Date()}
                placeholder="From"
                value={date?.from}
                onChange={(value) => {
                  setDate({ from: value, to: date?.to });
                }}
                granularity="minute"
              />
            </div>
            <div className="w-2/5">
              <h3 className="text-lg font-semibold mb-2">To</h3>
              <DateTimePicker
                calendarDisabled={(val) => {
                  return date?.from ? val < date.from : val < new Date();
                }}
                defaultPopupValue={
                  date?.to ? date.to : date?.from ? date.from : undefined
                }
                placeholder="To"
                value={date?.to}
                onChange={(value) => {
                  setDate({ from: date?.from, to: value });
                }}
                granularity="minute"
              />
            </div>
          </div>
          <div>
            <div>
              <Button
                size="lg"
                disabled={!date?.from || !date?.to}
                onClick={() => navigate("models")}>
                <span className="material-symbols-outlined ">search</span>
                Search
              </Button>
            </div>
            <span className="font-light text-sm">or</span>
            <div>
              <Button
                className="p-0 underline"
                size="sm"
                variant="link"
                onClick={() => {
                  setDate(undefined);
                  navigate("models");
                }}>
                Browse all models
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
