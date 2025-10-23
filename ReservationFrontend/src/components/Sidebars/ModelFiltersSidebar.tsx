import { CarModelFilter } from "@/models/filters/CarModelFilter";
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
import { Popover, PopoverContent, PopoverTrigger } from "../ui/popover";
import { cn } from "@/lib/utils";
import { CarSegment } from "@/models/enums/CarSegment";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "../ui/command";
import { Drivetrain } from "@/models/enums/Drivetrain";
import { TransmissionType } from "@/models/enums/TransmissionType";
import { CarCategory } from "@/models/enums/CarCategory";

function ModelFiltersSidebar({
  mobileOpen,
  setMobileOpen,
  setFilter,
  filter,
  fetchModels,
}: {
  mobileOpen: boolean;
  setMobileOpen: (val: boolean) => void;
  setFilter: (filter: CarModelFilter) => void;
  filter: CarModelFilter;
  fetchModels: (
    filter: CarModelFilter,
    orderParam?: string,
    sortparam?: string
  ) => void;
}) {
  const carSegments = Object.entries(CarSegment).map(([key, value]) => ({
    label: key,
    value,
  }));
  const carCategories = Object.entries(CarCategory).map(([key, value]) => ({
    label: key,
    value,
  }));

  const drivetrains = Object.entries(Drivetrain).map(([key, value]) => ({
    label: key,
    value,
  }));
  const transmissionTypes = Object.entries(TransmissionType).map(
    ([key, value]) => ({
      label: key,
      value,
    })
  );

  return (
    <Sidebar
      collapsible="none"
      side="right"
      variant="inset"
      mobileNoneCollapsibleOpen={mobileOpen}
      setMobileNoneCollapsibleOpen={setMobileOpen}
      className="pt-3 ">
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
            <span>Price</span>
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <div className="grid grid-cols-2 w-full max-w-md items-center gap-2">
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
                      euro_symbol
                    </span>
                  }
                  placeholder={"Min"}
                  value={filter.minRentalPrice || ""}
                  onChange={(event) => {
                    const value = event.target.value;
                    setFilter({ ...filter, minRentalPrice: Number(value) });
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
                      euro_symbol
                    </span>
                  }
                  placeholder={"Max"}
                  value={filter.maxRentalPrice || ""}
                  onChange={(event) => {
                    const value = event.target.value;
                    setFilter({ ...filter, maxRentalPrice: Number(value) });
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
                    htmlFor="category"
                    className="text-sm text-sidebar-foreground/70">
                    Category
                  </Label>
                  <Popover modal>
                    <PopoverTrigger asChild>
                      <Button
                        variant="ghost"
                        role="combobox"
                        id="category"
                        className={cn(
                          "text-foreground bg-background border border-input font-normal justify-between flex px-1.5 h-8",
                          !filter.category && " text-muted-foreground"
                        )}>
                        <span className="flex items-center gap-2">
                          <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                            category
                          </span>
                          {filter.category
                            ? carCategories
                                .find(
                                  (category) =>
                                    category.value === filter.category
                                )
                                ?.label?.charAt(0)
                                .toUpperCase() +
                              (carCategories
                                .find(
                                  (category) =>
                                    category.value === filter.category
                                )
                                ?.label?.slice(1)
                                .toLowerCase() || "")
                            : "Select category"}
                        </span>
                        <span className="material-symbols-outlined items-center md-18">
                          expand_all
                        </span>
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search category"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>No category found.</CommandEmpty>
                          <CommandGroup>
                            {carCategories.map((category) => (
                              <CommandItem
                                value={category.label}
                                key={category.value}
                                onSelect={() => {
                                  setFilter({
                                    ...filter,
                                    category: category.value,
                                  });
                                }}>
                                {category.label.charAt(0).toUpperCase() +
                                  category.label.slice(1).toLowerCase()}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    category.value === filter.category
                                      ? "opacity-100"
                                      : "opacity-0",
                                    "material-symbols-outlined md-18"
                                  )}>
                                  check
                                </span>
                              </CommandItem>
                            ))}
                          </CommandGroup>
                        </CommandList>
                      </Command>
                    </PopoverContent>
                  </Popover>
                  <Label
                    htmlFor="segment"
                    className="text-sm text-sidebar-foreground/70">
                    Segment
                  </Label>
                  <Popover modal>
                    <PopoverTrigger asChild>
                      <Button
                        variant="ghost"
                        role="combobox"
                        id="segment"
                        className={cn(
                          " text-foreground bg-background border border-input font-normal justify-between flex px-1.5 h-8",
                          !filter.segment && " text-muted-foreground"
                        )}>
                        <span className="flex items-center gap-2">
                          <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                            commute
                          </span>
                          {filter.segment
                            ? carSegments
                                .find(
                                  (segment) => segment.value === filter.segment
                                )
                                ?.label?.charAt(0)
                                .toUpperCase() +
                              (carSegments
                                .find(
                                  (segment) => segment.value === filter.segment
                                )
                                ?.label?.slice(1)
                                .toLowerCase() || "")
                            : "Select segment"}
                        </span>
                        <span className="material-symbols-outlined items-center md-18">
                          expand_all
                        </span>
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search segment"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>No segment found.</CommandEmpty>
                          <CommandGroup>
                            {carSegments.map((segment) => (
                              <CommandItem
                                value={segment.label}
                                key={segment.value}
                                onSelect={() => {
                                  setFilter({
                                    ...filter,
                                    segment: segment.value,
                                  });
                                }}>
                                {segment.label.charAt(0).toUpperCase() +
                                  segment.label.slice(1).toLowerCase()}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    segment.value === filter.segment
                                      ? "opacity-100"
                                      : "opacity-0",
                                    "material-symbols-outlined md-18"
                                  )}>
                                  check
                                </span>
                              </CommandItem>
                            ))}
                          </CommandGroup>
                        </CommandList>
                      </Command>
                    </PopoverContent>
                  </Popover>
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
                Engine Info
                <span className="material-symbols-outlined md-18 ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180">
                  arrow_drop_down
                </span>
              </CollapsibleTrigger>
            </SidebarGroupLabel>
            <CollapsibleContent>
              <SidebarGroupContent className="my-2">
                <div className="grid w-full max-w-sm items-center gap-1.5">
                  <Label
                    htmlFor="transmissionType"
                    className="text-sm text-sidebar-foreground/70">
                    Transmission Type
                  </Label>
                  <Popover modal>
                    <PopoverTrigger asChild>
                      <Button
                        variant="ghost"
                        id="transmissionType"
                        role="combobox"
                        className={cn(
                          " text-foreground bg-background border border-input font-normal justify-between flex px-1.5 h-8",
                          !filter.transmissionType && " text-muted-foreground"
                        )}>
                        <span className="flex items-center gap-2">
                          <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                            auto_transmission
                          </span>
                          {filter.transmissionType
                            ? transmissionTypes
                                .find(
                                  (type) =>
                                    type.value === filter.transmissionType
                                )
                                ?.label?.charAt(0)
                                .toUpperCase() +
                              (transmissionTypes
                                .find(
                                  (type) =>
                                    type.value === filter.transmissionType
                                )
                                ?.label?.slice(1)
                                .toLowerCase() || "")
                            : "Select transmission type"}
                        </span>
                        <span className="material-symbols-outlined items-center md-18">
                          expand_all
                        </span>
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search transmission type"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>
                            No transmission type found.
                          </CommandEmpty>
                          <CommandGroup>
                            {transmissionTypes.map((type) => (
                              <CommandItem
                                value={type.label}
                                key={type.value}
                                onSelect={() => {
                                  setFilter({
                                    ...filter,
                                    transmissionType: type.value,
                                  });
                                }}>
                                {type.label.charAt(0).toUpperCase() +
                                  type.label.slice(1).toLowerCase()}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    type.value === filter.transmissionType
                                      ? "opacity-100"
                                      : "opacity-0",
                                    "material-symbols-outlined md-18"
                                  )}>
                                  check
                                </span>
                              </CommandItem>
                            ))}
                          </CommandGroup>
                        </CommandList>
                      </Command>
                    </PopoverContent>
                  </Popover>
                  <Label
                    htmlFor="drivetrain"
                    className="text-sm text-sidebar-foreground/70">
                    Drivetrain
                  </Label>
                  <Popover modal>
                    <PopoverTrigger asChild>
                      <Button
                        id="drivetrain"
                        variant="ghost"
                        role="combobox"
                        className={cn(
                          " text-foreground bg-background border border-input font-normal justify-between flex px-1.5 h-8",
                          !filter.drivetrain && " text-muted-foreground"
                        )}>
                        <span className="flex items-center gap-2">
                          <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                            component_exchange
                          </span>
                          {filter.drivetrain
                            ? drivetrains
                                .find(
                                  (drivetrain) =>
                                    drivetrain.value === filter.drivetrain
                                )
                                ?.label?.charAt(0)
                                .toUpperCase() +
                              (drivetrains
                                .find(
                                  (drivetrain) =>
                                    drivetrain.value === filter.drivetrain
                                )
                                ?.label?.slice(1)
                                .toLowerCase() || "")
                            : "Select drivetrain"}
                        </span>
                        <span className="material-symbols-outlined items-center md-18">
                          expand_all
                        </span>
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search drivetrain"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>No drivetrain found.</CommandEmpty>
                          <CommandGroup>
                            {drivetrains.map((drivetrain) => (
                              <CommandItem
                                value={drivetrain.label}
                                key={drivetrain.value}
                                onSelect={() => {
                                  setFilter({
                                    ...filter,
                                    drivetrain: drivetrain.value,
                                  });
                                }}>
                                {drivetrain.label.charAt(0).toUpperCase() +
                                  drivetrain.label.slice(1).toLowerCase()}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    drivetrain.value === filter.drivetrain
                                      ? "opacity-100"
                                      : "opacity-0",
                                    "material-symbols-outlined md-18"
                                  )}>
                                  check
                                </span>
                              </CommandItem>
                            ))}
                          </CommandGroup>
                        </CommandList>
                      </Command>
                    </PopoverContent>
                  </Popover>
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
              brand: undefined,
              model: undefined,
              year: undefined,
              segment: undefined,
              category: undefined,
              engineType: undefined,
              transmissionType: undefined,
              drivetrain: undefined,
              minRentalPrice: undefined,
              maxRentalPrice: undefined,
            };
            setFilter(newFilter);
            fetchModels(newFilter);
          }}>
          Clear
        </Button>
        <Button
          variant="default"
          onClick={() => {
            fetchModels(filter);
          }}>
          Apply
        </Button>
      </SidebarFooter>
    </Sidebar>
  );
}

export default ModelFiltersSidebar;
