import ReservationsAPI from "@/API/ReservationsAPI";
import { Button } from "@/components/ui/button";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { MaintenanceType } from "@/models/enums/MaintenanceType";
import { Reservation } from "@/models/Reservation";
import { useEffect, useState } from "react";
import { Control, useWatch } from "react-hook-form";
import { Link } from "react-router-dom";

export default function AddOrEditMaintenanceForm({
  control,
  vehicleId,
}: {
  control: Control;
  vehicleId?: number;
}) {
  const maintenanceTypes = Object.entries(MaintenanceType).map(
    ([key, value]) => ({
      label: key,
      value,
    })
  );

  const [overlappingReservations, setOverlappingReservations] = useState<
    Reservation[]
  >([]);

  const startDate = useWatch({ control, name: "startDate" });
  const endDate = useWatch({ control, name: "plannedEndDate" });

  useEffect(() => {
    if (!startDate || !endDate || vehicleId == undefined) return;

    ReservationsAPI.getOverlappingReservations(
      vehicleId,
      startDate,
      endDate,
      true
    )
      .then((res: PagedResDTO<Reservation>) => {
        console.log(res.content);
        setOverlappingReservations(res.content);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [vehicleId, startDate, endDate]);

  return (
    <>
      <FormField
        control={control}
        name="startDate"
        render={({ field }) => (
          <FormItem className="flex flex-col">
            <FormLabel>Start Date</FormLabel>
            <DateTimePicker
              modalPopover
              displayFormat={{ hour24: "yyyy-MM-dd", hour12: "yyyy-MM-dd" }}
              className="!bg-background overflow-hidden"
              calendarDisabled={(val) => val < new Date()}
              defaultPopupValue={field.value ? field.value : new Date()}
              placeholder="Planned PickUp Date"
              value={field.value}
              onChange={field.onChange}
              granularity="day"
            />
            <FormMessage />
          </FormItem>
        )}
      />
      <FormField
        control={control}
        name="plannedEndDate"
        render={({ field }) => (
          <FormItem className="flex flex-col">
            <FormLabel>Planned End Date</FormLabel>
            <DateTimePicker
              modalPopover
              displayFormat={{ hour24: "yyyy-MM-dd", hour12: "yyyy-MM-dd" }}
              className="!bg-background overflow-hidden"
              calendarDisabled={(val) =>
                val < new Date() ||
                (control._formValues?.startDate &&
                  new Date(control._formValues.startDate) > val)
              }
              defaultPopupValue={field.value ? field.value : new Date()}
              placeholder="Planned End Date"
              value={field.value}
              onChange={field.onChange}
              granularity="day"
            />
            <FormDescription></FormDescription>
            <FormMessage />
          </FormItem>
        )}
      />
      <FormItem className="flex flex-col col-span-full w-full gap-2 items-center text-center">
        {overlappingReservations.length != 0 && (
          <>
            <FormLabel className="text-warning w-full">
              You cannot add a maintenance for this vehicle in this period.
              There{" "}
              {overlappingReservations.length != 1
                ? "are these reservations"
                : "is this reservation"}{" "}
              in the same period:{" "}
              {overlappingReservations.map((r, idx) => (
                <span key={r.id}>
                  <Link
                    to={`/reservations/change-vehicle/${r.id}`}
                    className="underline text-warning">
                    {r.id}
                  </Link>
                  {idx < overlappingReservations.length - 1 ? ", " : ""}
                </span>
              ))}
            </FormLabel>
            <FormLabel className="text-warning w-full"></FormLabel>
            <FormLabel className="text-warning w-full">
              Change {overlappingReservations.length == 1 ? "its" : "their"}{" "}
              vehicle or delete{" "}
              {overlappingReservations.length == 1 ? "it" : "them"} to add the
              maintenance. Click the reservation number to manage it.
            </FormLabel>
          </>
        )}
      </FormItem>
      <FormField
        disabled={overlappingReservations.length != 0}
        control={control}
        name="type"
        render={({ field }) => (
          <FormItem className="col-span-full">
            <FormLabel>Type</FormLabel>
            <Popover modal>
              <PopoverTrigger
                asChild
                disabled={overlappingReservations.length != 0}>
                <FormControl>
                  <Button
                    disabled={overlappingReservations.length != 0}
                    size="lg"
                    variant="ghost"
                    role="combobox"
                    className={cn(
                      " text-foreground col-span-full border border-input font-normal justify-between flex px-1.5",
                      !field.value && " text-muted-foreground"
                    )}>
                    <span className="flex items-center gap-2">
                      <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                        build
                      </span>
                      {field.value
                        ? (() => {
                            const selectedType = maintenanceTypes.find(
                              (type) => type.value === field.value
                            );
                            if (!selectedType) return "Select type";
                            // Format: capitalize first letter, replace underscores with spaces, rest lowercase
                            const formatted =
                              selectedType.label.charAt(0).toUpperCase() +
                              selectedType.label
                                .slice(1)
                                .replace(/_/g, " ")
                                .toLowerCase();
                            return formatted;
                          })()
                        : "Select type"}
                    </span>
                    <span className="material-symbols-outlined items-center md-18">
                      expand_all
                    </span>
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent className="p-0 bg-input">
                <Command>
                  <CommandInput placeholder="Search segment" className="h-9" />
                  <CommandList>
                    <CommandEmpty>No types found.</CommandEmpty>
                    <CommandGroup>
                      {maintenanceTypes.map((type) => (
                        <CommandItem
                          value={type.label}
                          key={type.value}
                          onSelect={() => {
                            field.onChange(type.value);
                          }}>
                          {type.label.charAt(0).toUpperCase() +
                            type.label
                              .slice(1)
                              .replace(/_/g, " ")
                              .toLowerCase()}
                          <span
                            className={cn(
                              "ml-auto",
                              type.value === field.value
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
            <FormMessage />
          </FormItem>
        )}
      />
      <FormField
        disabled={overlappingReservations.length != 0}
        control={control}
        name="defects"
        render={({ field }) => (
          <FormItem className="col-span-full">
            <FormLabel>Defects*</FormLabel>
            <FormControl>
              <Input
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    report_problem
                  </span>
                }
                placeholder={"Defects"}
                {...field}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
      <FormField
        disabled={overlappingReservations.length != 0}
        control={control}
        name="upcomingServiceNeeds"
        render={({ field }) => (
          <FormItem className="col-span-full">
            <FormLabel>Upcoming service Needs*</FormLabel>
            <FormControl>
              <Input
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    build_circle
                  </span>
                }
                placeholder={"Upcoming service needs"}
                {...field}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </>
  );
}
