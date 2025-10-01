import { Checkbox } from "@/components/ui/checkbox";
import {
  FormControl,
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
      <FormField
        control={control}
        name="completed"
        render={({ field }) => (
          <FormItem className="flex items-center mb-2">
            <FormLabel>Completed</FormLabel>
            <FormControl>
              <Checkbox
                checked={field.value}
                onCheckedChange={field.onChange}
              />
            </FormControl>
          </FormItem>
        )}
      />
    </>
  );
}
