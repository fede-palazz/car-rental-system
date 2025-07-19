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
import { useContext } from "react";
import UserContext from "@/contexts/UserContext";
import ReservationsAPI from "@/API/ReservationsAPI";
import { FinalizeReservationDTO } from "@/models/dtos/request/FinalizeReservationDTO";
import { Switch } from "@/components/ui/switch";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import { toast } from "sonner";

export default function FinalizeReservationDialog() {
  const user = useContext(UserContext);
  const navigate = useNavigate();
  const reservation: Reservation = useOutletContext();

  const reservationSchema = z
    .object({
      actualDropOffDate: z
        .date({
          required_error: "Actual Dropoff Date is required",
          invalid_type_error: "Actual Dropoff Date must be a valid date",
        })
        .refine((date) => date <= new Date() || date > new Date(), {
          message: "Actual Dropoff Date cannot be in the past",
        }),
      wasDeliveryLate: z.boolean().optional(),
      wasChargedFee: z.boolean().optional(),
      wasVehicleDamaged: z.boolean().optional(),
      wasInvolvedInAccident: z.boolean().optional(),
    })
    .refine(
      (data) =>
        data.actualDropOffDate > reservation.actualPickUpDate! && {
          //data.actualDropOffDate <= new Date(),
          path: ["actualDropOffDate"],
          message: "Drop-off date must be after pick-up date",
        }
    );

  const form = useForm<FinalizeReservationDTO>({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      actualDropOffDate: new Date(),
      wasDeliveryLate: false,
      wasChargedFee: false,
      wasVehicleDamaged: false,
      wasInvolvedInAccident: false,
    },
  });

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    handleSubmit(form.getValues());
  }

  const handleSubmit = (values: FinalizeReservationDTO) => {
    ReservationsAPI.finalizeReservation(reservation.id, values)
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
          <DialogTitle>Finalize Reservation</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <FormField
              control={form.control}
              name="actualDropOffDate"
              render={({ field }) => (
                <FormItem className="flex flex-col col-span-full my-3 items-center">
                  <FormLabel>Actual Dropoff Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) =>
                      val == new Date() || val < reservation.actualPickUpDate!
                    }
                    defaultPopupValue={field.value ? field.value : new Date()}
                    placeholder="Actual DropOff Date"
                    value={field.value}
                    onChange={field.onChange}
                    granularity="minute"
                  />
                  <FormDescription>
                    The pickup date is{" "}
                    {reservation.actualPickUpDate?.toLocaleDateString()}
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="wasDeliveryLate"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Late Delivery</FormLabel>
                  <FormControl>
                    <Switch
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="wasChargedFee"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Charged Fee</FormLabel>
                  <FormControl>
                    <Switch
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="wasVehicleDamaged"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Damaged Vehicle</FormLabel>
                  <FormControl>
                    <Switch
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="wasInvolvedInAccident"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Accident</FormLabel>
                  <FormControl>
                    <Switch
                      checked={field.value}
                      onCheckedChange={field.onChange}
                    />
                  </FormControl>
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
