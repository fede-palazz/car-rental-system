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
import { Control } from "react-hook-form";

export default function AddOrEditMaintenanceForm({
  control,
}: {
  control: Control;
}) {
  return (
    <>
      //TODO
      {true ? (
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
              <FormItem className="col-span-full">
                <FormLabel>Type*</FormLabel>
                <FormControl>
                  <Input
                    startIcon={
                      <span className="material-symbols-outlined items-center md-18">
                        build
                      </span>
                    }
                    placeholder={"Type"}
                    {...field}
                  />
                </FormControl>
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
      ) : (
        <></>
      )}
    </>
  );
}
