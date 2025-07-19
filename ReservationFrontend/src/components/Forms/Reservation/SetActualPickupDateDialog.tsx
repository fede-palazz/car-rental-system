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
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { useNavigate, useOutletContext } from "react-router-dom";
import { Reservation } from "@/models/Reservation";

import ReservationsAPI from "@/API/ReservationsAPI";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import { toast } from "sonner";

export default function SetActualPickupDateDialog() {
  const navigate = useNavigate();
  const reservation: Reservation = useOutletContext();

  const reservationSchema = z
    .object({
      actualPickUpDate: z
        .date({
          required_error: "Actual Pick-Up Date is required",
          invalid_type_error: "Actual  Pick-Up Date must be a valid date",
        })
        .refine((date) => date <= new Date(), {
          message: "Actual Pick-Up Date cannot be in the past",
        }),
    })
    .refine(
      (data) =>
        data.actualPickUpDate > reservation.plannedPickUpDate &&
        data.actualPickUpDate <= new Date(),
      {
        path: ["actualPickUpDate"],
        message: "Actual pick-up date must be after the planned one",
      }
    );

  type formType = z.infer<typeof reservationSchema>;

  const form = useForm<formType>({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      actualPickUpDate: new Date(),
    },
  });

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    handleSubmit(form.getValues().actualPickUpDate);
  }

  const handleSubmit = (values: Date) => {
    ReservationsAPI.setActualPickUpDate(reservation.id, values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
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
          <DialogTitle>Set Pickup Date</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <FormField
              control={form.control}
              name="actualPickUpDate"
              render={({ field }) => (
                <FormItem className="flex flex-col col-span-full my-3 items-center">
                  <FormLabel>Actual PickUp Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) =>
                      val > new Date() || val < reservation.plannedPickUpDate
                    }
                    defaultPopupValue={field.value ? field.value : new Date()}
                    placeholder="Actual PickUp Date"
                    value={field.value}
                    onChange={field.onChange}
                    granularity="minute"
                  />
                  <FormDescription>
                    The planned date is{" "}
                    {reservation.plannedPickUpDate
                      .toLocaleString()
                      .slice(0, -3)}
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter className="col-span-full ">
              <div className="flex items-center justify-between w-full">
                <Button
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
                  Set
                  <span className="material-symbols-outlined  md-18">
                    event_available
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
