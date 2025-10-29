import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect } from "react";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import MaintenancesAPI from "@/API/MaintenancesAPI";
import { toast } from "sonner";

export default function FinalizeMaintenanceDialog() {
  const navigate = useNavigate();
  const { vehicleId, maintenanceId } = useParams<{
    vehicleId: string;
    maintenanceId: string;
  }>();

  /*useEffect(() => {
    if (!vehicleId || !maintenanceId) {
      navigate("/vehicles");
    }
  }, [vehicleId, maintenanceId]);*/

  const finalizeMaintenanceSchema = z.object({
    actualEndDate: z
      .date({
        required_error: "Actual End Date is required",
        invalid_type_error: "Actual End Date must be a valid date",
      })
      .refine((date) => date <= new Date(), {
        message: "Actual End Date cannot be in the future",
      }),
  });

  const form = useForm({
    resolver: zodResolver(finalizeMaintenanceSchema),
    defaultValues: {
      actualEndDate: new Date(),
    },
  });

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    handleSubmit(form.getValues("actualEndDate"));
  }

  const handleSubmit = (actualEndDate: Date) => {
    MaintenancesAPI.finalizeMaintenanceByMaintenanceId(
      Number(vehicleId),
      Number(maintenanceId),
      actualEndDate
    )
      .then(() => {
        toast.success("Maintenance finalized successfully");
      })
      .catch((err) => {
        toast.error(err.message);
      })
      .finally(() => {
        navigate(-1);
      });
  };

  return (
    <Dialog
      open={true}
      onOpenChange={(isOpen) => {
        if (!isOpen) navigate(-1);
      }}>
      <DialogDescription></DialogDescription>
      <DialogContent
        className="sm:max-w-[425px] md:min-w-1/2 overflow-auto max-h-11/12"
        style={{
          scrollbarWidth: "none", // For Firefox
          msOverflowStyle: "none", // For IE and Edge
        }}>
        <DialogHeader>
          <DialogTitle>Finalize Maintenance</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-2 md:grid-cols-3 gap-x-8 gap-y-4">
            <FormField
              control={form.control}
              name="actualEndDate"
              render={({ field }) => (
                <FormItem className="flex flex-col col-span-full my-3 items-center">
                  <FormLabel>Actual End Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    displayFormat={{
                      hour24: "yyyy-MM-dd",
                      hour12: "yyyy-MM-dd",
                    }}
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => val > new Date()}
                    defaultPopupValue={field.value ? field.value : new Date()}
                    placeholder="Actual End Date"
                    value={field.value}
                    onChange={field.onChange}
                    granularity="day"
                  />
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter className="col-span-full ">
              <div className="flex items-center justify-between w-full">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(-1);
                  }}>
                  <span className="material-symbols-outlined items-center md-18">
                    close
                  </span>
                  Cancel
                </Button>
                <Button
                  variant="default"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    onSubmit();
                  }}>
                  Finalize
                  <span className="material-symbols-outlined  md-18">
                    handshake
                  </span>
                </Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
