import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarSeparator,
} from "../ui/sidebar";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "../ui/collapsible";
import { Label } from "../ui/label";
import { Button } from "../ui/button";
import { ReservationFilter } from "@/models/filters/ReservationFilter";
import UserContext from "@/contexts/UserContext";
import { useContext } from "react";
import { UserRole } from "@/models/enums/UserRole";
import { DateTimePicker } from "../ui/date-time-picker";
import { Checkbox } from "../ui/checkbox";

function ReservationFiltersSidebar({
  mobileOpen,
  setMobileOpen,
  setFilter,
  filter,
  fetchReservations,
}: {
  mobileOpen: boolean;
  setMobileOpen: (val: boolean) => void;
  setFilter: (filter: ReservationFilter) => void;
  filter: ReservationFilter;
  fetchReservations: (
    filter: ReservationFilter,
    orderParam?: string,
    sortparam?: string
  ) => void;
}) {
  const user = useContext(UserContext);

  return (
    <Sidebar
      collapsible="none"
      side="right"
      variant="inset"
      mobileNoneCollapsibleOpen={mobileOpen}
      setMobileNoneCollapsibleOpen={setMobileOpen}
      className="pt-3">
      <SidebarHeader className="flex flex-row items-center justify-center">
        <span className="material-symbols-outlined ">filter_alt</span>
        <span className="text-lg font-semibold">Filters</span>
      </SidebarHeader>
      <SidebarSeparator className="mx-0" />
      <SidebarContent
        style={{
          overflowY: "auto",
          scrollbarWidth: "none",
          msOverflowStyle: "none",
        }}
        className="scrollbar-hide">
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                Creation Date
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="minCreationDate"
                    className="text-sm text-sidebar-foreground/70">
                    From
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.maxCreationDate && val > filter.maxCreationDate
                      );
                    }}
                    defaultPopupValue={
                      filter.minCreationDate
                        ? filter.minCreationDate
                        : new Date()
                    }
                    placeholder="From"
                    value={filter.minCreationDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        minCreationDate: value,
                      });
                    }}
                    granularity="minute"
                  />

                  <Label
                    htmlFor="maxCreationDate"
                    className="text-sm text-sidebar-foreground/70">
                    To
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        (!!filter.minCreationDate &&
                          val < filter.minCreationDate) ||
                        val > new Date()
                      );
                    }}
                    defaultPopupValue={
                      filter.maxCreationDate
                        ? filter.maxCreationDate
                        : new Date()
                    }
                    placeholder="To"
                    value={filter.maxCreationDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        maxCreationDate: value,
                      });
                    }}
                    granularity="minute"
                  />
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                Planned PickUp Date
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="minPlannedPickUpDate"
                    className="text-sm text-sidebar-foreground/70">
                    From
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.maxPlannedPickUpDate &&
                        val > filter.maxPlannedPickUpDate
                      );
                    }}
                    defaultPopupValue={
                      filter.minPlannedPickUpDate
                        ? filter.minPlannedPickUpDate
                        : new Date()
                    }
                    placeholder="From"
                    value={filter.minPlannedPickUpDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        minPlannedPickUpDate: value,
                      });
                    }}
                    granularity="minute"
                  />

                  <Label
                    htmlFor="maxPlannedPickUpDate"
                    className="text-sm text-sidebar-foreground/70">
                    To
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.minPlannedPickUpDate &&
                        val < filter.minPlannedPickUpDate
                      );
                    }}
                    defaultPopupValue={
                      filter.maxPlannedPickUpDate
                        ? filter.maxPlannedPickUpDate
                        : new Date()
                    }
                    placeholder="To"
                    value={filter.maxPlannedPickUpDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        maxPlannedPickUpDate: value,
                      });
                    }}
                    granularity="minute"
                  />
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                Actual PickUp Date
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="minActualPickUpDate"
                    className="text-sm text-sidebar-foreground/70">
                    From
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.maxActualPickUpDate &&
                        val > filter.maxActualPickUpDate
                      );
                    }}
                    defaultPopupValue={
                      filter.minActualPickUpDate
                        ? filter.minActualPickUpDate
                        : new Date()
                    }
                    placeholder="From"
                    value={filter.minActualPickUpDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        minActualPickUpDate: value,
                      });
                    }}
                    granularity="minute"
                  />

                  <Label
                    htmlFor="maxActualPickUpDate"
                    className="text-sm text-sidebar-foreground/70">
                    To
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.minActualPickUpDate &&
                        val < filter.minActualPickUpDate
                      );
                    }}
                    defaultPopupValue={
                      filter.maxActualPickUpDate
                        ? filter.maxActualPickUpDate
                        : new Date()
                    }
                    placeholder="To"
                    value={filter.maxActualPickUpDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        maxActualPickUpDate: value,
                      });
                    }}
                    granularity="minute"
                  />
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                Planned DropOff Date
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="minPlannedDropOffDate"
                    className="text-sm text-sidebar-foreground/70">
                    From
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.maxPlannedDropOffDate &&
                        val > filter.maxPlannedDropOffDate
                      );
                    }}
                    defaultPopupValue={
                      filter.minPlannedDropOffDate
                        ? filter.minPlannedDropOffDate
                        : new Date()
                    }
                    placeholder="From"
                    value={filter.minPlannedDropOffDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        minPlannedDropOffDate: value,
                      });
                    }}
                    granularity="minute"
                  />

                  <Label
                    htmlFor="maxPlannedDropOffDate"
                    className="text-sm text-sidebar-foreground/70">
                    To
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.minPlannedDropOffDate &&
                        val < filter.minPlannedDropOffDate
                      );
                    }}
                    defaultPopupValue={
                      filter.maxPlannedDropOffDate
                        ? filter.maxPlannedDropOffDate
                        : new Date()
                    }
                    placeholder="To"
                    value={filter.maxPlannedDropOffDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        maxPlannedDropOffDate: value,
                      });
                    }}
                    granularity="minute"
                  />
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                Actual DropOff Date
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="minActualDropOffDate"
                    className="text-sm text-sidebar-foreground/70">
                    From
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.maxActualDropOffDate &&
                        val > filter.maxActualDropOffDate
                      );
                    }}
                    defaultPopupValue={
                      filter.minActualDropOffDate
                        ? filter.minActualDropOffDate
                        : new Date()
                    }
                    placeholder="From"
                    value={filter.minActualDropOffDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        minActualDropOffDate: value,
                      });
                    }}
                    granularity="minute"
                  />

                  <Label
                    htmlFor="maxActualDropOffDate"
                    className="text-sm text-sidebar-foreground/70">
                    To
                  </Label>
                  <DateTimePicker
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      return (
                        !!filter.minActualDropOffDate &&
                        val < filter.minActualDropOffDate
                      );
                    }}
                    defaultPopupValue={
                      filter.maxActualDropOffDate
                        ? filter.maxActualDropOffDate
                        : new Date()
                    }
                    placeholder="To"
                    value={filter.maxActualDropOffDate}
                    onChange={(value) => {
                      setFilter({
                        ...filter,
                        maxActualDropOffDate: value,
                      });
                    }}
                    granularity="minute"
                  />
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
        {user && user.role !== UserRole.CUSTOMER && (
          <Collapsible defaultOpen className="group/collapsible">
            <SidebarGroup>
              <SidebarGroupLabel
                className="text-md pl-0 text-sidebar-foreground"
                asChild>
                <CollapsibleTrigger>
                  Dropoff Conditions
                  <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                    arrow_drop_down
                  </span>
                </CollapsibleTrigger>
              </SidebarGroupLabel>
              <CollapsibleContent>
                <SidebarGroupContent className="my-2 ">
                  <div className="flex flex-col my-3 gap-2">
                    <div className="flex items-center space-x-2 w-full">
                      <Checkbox
                        id="wasDeliveryLate"
                        checked={filter.wasDeliveryLate || false}
                        onCheckedChange={(checked) => {
                          setFilter({
                            ...filter,
                            wasDeliveryLate: checked === true,
                          });
                        }}
                      />
                      <Label htmlFor="wasDeliveryLate">Late Delivery</Label>
                    </div>
                    <div className="flex items-center space-x-2 w-full">
                      <Checkbox
                        id="wasChargedFee"
                        checked={filter.wasChargedFee || false}
                        onCheckedChange={(checked) => {
                          setFilter({
                            ...filter,
                            wasChargedFee: checked === true,
                          });
                        }}
                      />
                      <Label htmlFor="wasChargedFee">Charged Fee</Label>
                    </div>
                    <div className="flex items-center space-x-2 w-full">
                      <Checkbox
                        id="wasVehicleDamaged"
                        checked={filter.wasVehicleDamaged || false}
                        onCheckedChange={(checked) => {
                          setFilter({
                            ...filter,
                            wasVehicleDamaged: checked === true,
                          });
                        }}
                      />
                      <Label htmlFor="wasVehicleDamaged">Damage</Label>
                    </div>
                    <div className="flex items-center space-x-2 w-full">
                      <Checkbox
                        id="wasInvolvedInAccident"
                        checked={filter.wasInvolvedInAccident || false}
                        onCheckedChange={(checked) => {
                          setFilter({
                            ...filter,
                            wasInvolvedInAccident: checked === true,
                          });
                        }}
                      />
                      <Label htmlFor="wasInvolvedInAccident">Accident</Label>
                    </div>
                  </div>
                </SidebarGroupContent>
              </CollapsibleContent>
            </SidebarGroup>
          </Collapsible>
        )}
      </SidebarContent>
      <SidebarSeparator className="mx-0 my-2" />
      <SidebarFooter className="flex flex-row mb-2 justify-between">
        <Button
          variant="outline"
          onClick={() => {
            const newFilter = {
              licensePlate: undefined,
              vin: undefined,
              brand: undefined,
              model: undefined,
              year: undefined,
              minCreationDate: undefined,
              maxCreationDate: undefined,
              minPlannedPickUpDate: undefined,
              maxPlannedPickUpDate: undefined,
              minActualPickUpDate: undefined,
              maxActualPickUpDate: undefined,
              minPlannedDropOffDate: undefined,
              maxPlannedDropOffDate: undefined,
              minActualDropOffDate: undefined,
              maxActualDropOffDate: undefined,
              status: undefined,
              wasDeliveryLate: undefined,
              wasChargedFee: undefined,
              wasVehicleDamaged: undefined,
              wasInvolvedInAccident: undefined,
            };
            setFilter(newFilter);
            fetchReservations(newFilter);
          }}>
          Clear
        </Button>
        <Button
          variant="default"
          onClick={() => {
            fetchReservations(filter);
          }}>
          Apply
        </Button>
      </SidebarFooter>
    </Sidebar>
  );
}

export default ReservationFiltersSidebar;
