import { VehicleFilter } from "@/models/filters/VehicleFilter";
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
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { Checkbox } from "../ui/checkbox";

function VehicleFiltersSidebar({
  mobileOpen,
  setMobileOpen,
  setFilter,
  filter,
  fetchVehicles,
}: {
  mobileOpen: boolean;
  setMobileOpen: (val: boolean) => void;
  setFilter: (filter: VehicleFilter) => void;
  filter: VehicleFilter;
  fetchVehicles: (filter: VehicleFilter, order?: string, sort?: string) => void;
}) {
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
        <SidebarGroup>
          <SidebarGroupLabel
            className="text-md pl-0 text-sidebar-foreground"
            asChild>
            <span>Km Travelled</span>
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <div className="grid grid-cols-2 w-full max-w-sm items-center gap-2">
              <div className="flex flex-col gap-1.5">
                <Label
                  htmlFor="minPrice"
                  className="text-sm text-sidebar-foreground/70">
                  Min
                </Label>
                <Input
                  id="minPrice"
                  type="number"
                  className="h-8"
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      distance
                    </span>
                  }
                  placeholder={"Min"}
                  value={filter.minKmTravelled || ""}
                  onChange={(event) => {
                    const value = event.target.value;
                    setFilter({ ...filter, minKmTravelled: Number(value) });
                  }}
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label
                  htmlFor="maxPrice"
                  className="text-sm text-sidebar-foreground/70">
                  Max
                </Label>
                <Input
                  id="maxPrice"
                  type="number"
                  className="h-8"
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      distance
                    </span>
                  }
                  placeholder={"Max"}
                  value={filter.maxKmTravelled || ""}
                  onChange={(event) => {
                    const value = event.target.value;
                    setFilter({ ...filter, maxKmTravelled: Number(value) });
                  }}
                />
              </div>
            </div>
          </SidebarGroupContent>
        </SidebarGroup>
        <Collapsible defaultOpen className="group/collapsible">
          <SidebarGroup>
            <SidebarGroupLabel
              className="text-md pl-0 text-sidebar-foreground"
              asChild>
              <CollapsibleTrigger>
                General Info
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="brand"
                    className="text-sm text-sidebar-foreground/70">
                    Brand
                  </Label>
                  <Input
                    id="brand"
                    className="h-8"
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        brand_family
                      </span>
                    }
                    placeholder={"Brand"}
                    value={filter.brand || ""}
                    onChange={(event) => {
                      const value = event.target.value;
                      setFilter({ ...filter, brand: value });
                    }}
                  />
                  <Label
                    htmlFor="model"
                    className="text-sm text-sidebar-foreground/70">
                    Model
                  </Label>
                  <Input
                    id="model"
                    className="h-8"
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        directions_car
                      </span>
                    }
                    placeholder={"Model"}
                    value={filter.model || ""}
                    onChange={(event) => {
                      const value = event.target.value;
                      setFilter({ ...filter, model: value });
                    }}
                  />
                  <Label
                    htmlFor="year"
                    className="text-sm text-sidebar-foreground/70">
                    Year
                  </Label>
                  <Input
                    id="year"
                    type="number"
                    className="h-8"
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        calendar_today
                      </span>
                    }
                    placeholder={"Year"}
                    value={filter.year || ""}
                    onChange={(event) => {
                      const value = event.target.value;
                      setFilter({ ...filter, year: Number(value) });
                    }}
                  />
                  <Label
                    htmlFor="vin"
                    className="text-sm text-sidebar-foreground/70">
                    Vin
                  </Label>
                  <Input
                    id="vin"
                    className="h-8"
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        car_tag
                      </span>
                    }
                    placeholder={"Vin"}
                    value={filter.vin || ""}
                    onChange={(event) => {
                      const value = event.target.value;
                      setFilter({ ...filter, vin: value });
                    }}
                  />
                  <Label
                    htmlFor="licensePlate"
                    className="text-sm text-sidebar-foreground/70">
                    License Plate
                  </Label>
                  <Input
                    id="licensePlate"
                    className="h-8"
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        tag
                      </span>
                    }
                    placeholder={"License Plate"}
                    value={filter.licensePlate || ""}
                    onChange={(event) => {
                      const value = event.target.value;
                      setFilter({ ...filter, licensePlate: value });
                    }}
                  />
                </div>
                <div className="flex flex-col mt-6 mb-3 gap-2">
                  <div className="flex items-center space-x-2 w-full">
                    <Checkbox
                      id="pendingCleaning"
                      checked={filter.pendingCleaning || false}
                      onCheckedChange={(checked) => {
                        setFilter({
                          ...filter,
                          pendingCleaning: checked === true,
                        });
                      }}
                    />
                    <Label htmlFor="pendingCleaning">Pending Cleaning</Label>
                  </div>
                </div>
              </SidebarGroupContent>
            </CollapsibleContent>
          </SidebarGroup>
        </Collapsible>
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
              status: undefined,
              minKmTravelled: undefined,
              maxKmTravelled: undefined,
              pendingCleaning: undefined,
            };
            setFilter(newFilter);
            fetchVehicles(newFilter);
          }}>
          Clear
        </Button>
        <Button
          variant="default"
          onClick={() => {
            fetchVehicles(filter);
          }}>
          Apply
        </Button>
      </SidebarFooter>
    </Sidebar>
  );
}

export default VehicleFiltersSidebar;
