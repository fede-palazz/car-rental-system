"use client";

import * as React from "react";
import { NavMain } from "./nav-main";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar";
import { NavUser } from "./nav-user";
import UserContext from "@/contexts/UserContext";
import { useContext } from "react";
import { UserRole } from "@/models/enums/UserRole";
import { User } from "@/models/User";

interface AppSidebarProps extends React.ComponentProps<typeof Sidebar> {
  setUser: (user: User) => void;
}

export function AppSidebar({ setUser, ...props }: AppSidebarProps) {
  const user = useContext(UserContext);

  const data = {
    user: user,
    navMain: [
      {
        title: user && user.role != UserRole.CUSTOMER ? "Car Models" : "Home",
        url: "/models",
        icon: (
          <span className="material-symbols-outlined items-center md-18">
            directions_car
          </span>
        ),
        isActive: true,
      },
      ...(user && user.role != UserRole.CUSTOMER
        ? [
            {
              title: "Vehicles",
              url: "/vehicles",
              icon: (
                <span className="material-symbols-outlined items-center md-18">
                  warehouse
                </span>
              ),
            },
          ]
        : []),
      ...(user
        ? [
            {
              title: "Reservations",
              url: "/reservations",
              icon: (
                <span className="material-symbols-outlined items-center md-18">
                  event_upcoming
                </span>
              ),
            },
          ]
        : []),
      ...(user && user.role != UserRole.CUSTOMER
        ? [
            {
              title: "Tracking",
              url: "/tracking",
              icon: (
                <span className="material-symbols-outlined items-center md-18">
                  globe_location_pin
                </span>
              ),
            },
          ]
        : []),
      ...(user && user.role != UserRole.CUSTOMER
        ? [
            {
              title: "Analytics",
              url: "/analytics",
              icon: (
                <span className="material-symbols-outlined items-center md-18">
                  analytics
                </span>
              ),
            },
          ]
        : []),
    ],
  };

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild>
              <div>
                <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                  <span className="material-symbols-outlined ">car_rental</span>
                </div>
                <div className="grid flex-1 text-left text-sm leading-tight">
                  <span className="truncate font-semibold">Car Rental</span>
                  <span className="truncate text-xs">Agency</span>
                </div>
              </div>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={data.navMain} />
      </SidebarContent>
      <SidebarFooter>
        {<NavUser user={data.user} setUser={setUser} />}
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  );
}
