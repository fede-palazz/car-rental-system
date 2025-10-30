"use client";

import { ChevronsUpDown } from "lucide-react";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";
import { User } from "@/models/User";
import { UserRole } from "@/models/enums/UserRole";
import { Button } from "../ui/button";

export function NavUser({
  user,
  setUser,
}: {
  user: User | undefined;
  setUser: (user: User) => void;
}) {
  const { isMobile, state } = useSidebar();

  return user ? (
    <SidebarMenu className="z-100000">
      <SidebarMenuItem className="z-100000">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton
              size="lg"
              className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground">
              <Avatar className="h-8 w-8 rounded-lg">
                <AvatarImage src={undefined} alt={user.username} />
                <AvatarFallback className="rounded-lg">
                  {user.firstName.charAt(0).toUpperCase() +
                    user.lastName.charAt(0).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div className="grid flex-1 text-left text-sm leading-tight">
                <span className="truncate font-semibold">{user.username}</span>
                <span className="truncate text-xs">
                  {user.role.charAt(0).toUpperCase() +
                    user.role.slice(1).toLowerCase()}
                </span>
              </div>
              <ChevronsUpDown className="ml-auto size-4" />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            className="w-[--radix-dropdown-menu-trigger-width] min-w-56 rounded-lg z-100000"
            side={isMobile ? "bottom" : "right"}
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
                <span className="material-symbols-outlined md-18">id_card</span>
                {user.firstName} {user.lastName}
              </DropdownMenuLabel>
              <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                <span className="material-symbols-outlined md-18">mail</span>
                {user.email}
              </DropdownMenuLabel>
              <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                <span className="material-symbols-outlined md-18">
                  home_pin
                </span>
                {user.address}
              </DropdownMenuLabel>
              <DropdownMenuLabel className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal">
                <span className="material-symbols-outlined md-18">call</span>
                {user.phone}
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
              <input type="hidden" name="_csrf" value={user.csrf as string} />
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
      </SidebarMenuItem>
    </SidebarMenu>
  ) : (
    <Button
      className="w-1/2 self-center"
      onClick={() => {
        window.location.href = "http://localhost:8083/gateway-client";
      }}>
      <span className="material-symbols-outlined items-center md-18">
        login
      </span>
      {state == "expanded" && "Login"}
    </Button>
  );
}
