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
import { MaintenanceType } from "@/models/enums/MaintenanceType";
import { Control } from "react-hook-form";

export default function AddOrEditMaintenanceForm({
  control,
}: {
  control: Control;
}) {
  const maintenanceTypes = Object.entries(MaintenanceType).map(
    ([key, value]) => ({
      label: key,
      value,
    })
  );

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
      <FormField
        control={control}
        name="type"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Type</FormLabel>
            <Popover>
              <PopoverTrigger asChild>
                <FormControl>
                  <Button
                    size="lg"
                    variant="ghost"
                    role="combobox"
                    className={cn(
                      " text-foreground border border-input font-normal justify-between flex px-1.5",
                      !field.value && " text-muted-foreground"
                    )}>
                    <span className="flex items-center gap-2">
                      <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                        build
                      </span>
                      {field.value
                        ? maintenanceTypes
                            .find((type) => type.value === type.value)
                            ?.label?.charAt(0)
                            .toUpperCase() +
                          (maintenanceTypes
                            .find((type) => type.value === field.value)
                            ?.label?.slice(1)
                            .toLowerCase() || "")
                        : "Select type"}
                    </span>
                    <span className="material-symbols-outlined items-center md-18">
                      expand_all
                    </span>
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent modal className="p-0 bg-input">
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
                            type.label.slice(1).toLowerCase()}
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
