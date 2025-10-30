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
import { Link, useNavigate, useParams } from "react-router-dom";
import { Reservation } from "@/models/Reservation";
import { useEffect, useState } from "react";
import ReservationsAPI from "@/API/ReservationsAPI";
import { FinalizeReservationDTO } from "@/models/dtos/request/FinalizeReservationDTO";
import { Switch } from "@/components/ui/switch";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import { addDays, format } from "date-fns";
import { Spinner } from "@/components/ui/spinner";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { damageAndDirtinessLevelLabels } from "@/utils/damageAndDirtinessLevelLabels";
import { toast } from "sonner";

export default function FinalizeReservationDialog() {
  const navigate = useNavigate();
  const { reservationId } = useParams<{
    reservationId: string;
  }>();

  const [reservation, setReservation] = useState<Reservation | undefined>(
    undefined
  );

  const reservationSchema = z
    .object({
      actualDropOffDate: z
        .date({
          required_error: "Actual Dropoff Date is required",
          invalid_type_error: "Actual Dropoff Date must be a valid date",
        })
        .refine((date) => date <= new Date(), {
          message: "Actual Dropoff Date cannot be in the future",
        }),
      bufferedDropOffDate: z
        .date({
          required_error: "Vehicle availability Date is required",
          invalid_type_error: "Vehicle availability Date must be a valid date",
        })
        .refine((date) => date <= new Date() || date > new Date(), {
          message: "Vehicle availability Date cannot be in the past",
        }),
      wasDeliveryLate: z.boolean().optional(),
      wasChargedFee: z.boolean().optional(),
      wasInvolvedInAccident: z.boolean().optional(),
      damageLevel: z.coerce
        .number()
        .min(0, { message: "Damage level must be at least 0" })
        .max(5, { message: "Damage level must be at most 5" }),
      dirtinessLevel: z.coerce
        .number()
        .min(0, { message: "Dirtiness level must be at least 0" })
        .max(5, { message: "Dirtiness level must be at most 5" }),
    })
    .refine(
      (data) => {
        if (!reservation || !reservation.actualPickUpDate) return true;
        return data.actualDropOffDate > reservation.actualPickUpDate!;
      },
      {
        path: ["actualDropOffDate"],
        message: "Drop-off date must be after pick-up date",
      }
    );

  const [overlappingReservations, setOverlappingReservations] = useState<
    Reservation[]
  >([]);

  useEffect(() => {
    ReservationsAPI.getReservationById(Number(reservationId))
      .then((reservation: Reservation) => {
        setReservation(reservation);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }, [reservationId]);

  const form = useForm<FinalizeReservationDTO>({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      bufferedDropOffDate: addDays(new Date(), 2),
      actualDropOffDate: new Date(),
      wasDeliveryLate: false,
      wasChargedFee: false,
      wasInvolvedInAccident: false,
      damageLevel: 0,
      dirtinessLevel: 0,
    },
  });

  const bufferedDropOffDate = form.watch("bufferedDropOffDate");

  useEffect(() => {
    if (bufferedDropOffDate == undefined) return;

    ReservationsAPI.getOverlappingReservationsByReservationId(
      Number(reservationId),
      bufferedDropOffDate,
      true
    )
      .then((res: PagedResDTO<Reservation>) => {
        console.log(res.content);
        setOverlappingReservations(res.content);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }, [reservationId, bufferedDropOffDate]);

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    handleSubmit(form.getValues());
  }

  const handleSubmit = (values: FinalizeReservationDTO) => {
    ReservationsAPI.finalizeReservation(Number(reservationId), values)
      .then(() => {
        toast.success("Reservation finalized successfully");
        navigate(-1);
      })
      .catch((err: Error) => {
        toast.error(err.message);
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
        {!reservation ? (
          <div className="flex items-center justify-center w-full min-h-[300px]">
            <Spinner />
          </div>
        ) : (
          <Form {...form}>
            <form className="grid grid-cols-2 md:grid-cols-3 gap-x-8 gap-y-4">
              <FormField
                control={form.control}
                name="bufferedDropOffDate"
                render={({ field }) => (
                  <FormItem className="flex flex-col col-span-full my-3 items-center">
                    <FormLabel>Vehicle Availability Date</FormLabel>
                    <DateTimePicker
                      modalPopover
                      className="!bg-background overflow-hidden"
                      calendarDisabled={(val) =>
                        val == new Date() || val < reservation.actualPickUpDate!
                      }
                      defaultPopupValue={field.value ? field.value : new Date()}
                      placeholder="Vehicle Availability Date"
                      value={field.value}
                      onChange={field.onChange}
                      granularity="minute"
                    />
                    <FormDescription>
                      Set this field to the date when the vehicle will be
                      available again
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormItem className="flex flex-col col-span-full w-full gap-2 items-center text-center">
                {overlappingReservations.length != 0 && (
                  <>
                    <FormLabel className="text-warning w-full">
                      You cannot set this new availability date for this
                      vehicle. There{" "}
                      {overlappingReservations.length != 1
                        ? "are these reservations"
                        : "is this reservation"}{" "}
                      in the same period:{" "}
                      {overlappingReservations.map((r, idx) => (
                        <span key={r.id}>
                          <Link
                            to={`/reservations/change-vehicle/${r.id}`}
                            className="underline text-warning">
                            {format(r.plannedPickUpDate, "dd/MM/yyyy HH:mm") +
                              " - " +
                              format(r.plannedPickUpDate, "dd/MM/yyyy HH:mm")}
                          </Link>
                          {idx < overlappingReservations.length - 1 ? ", " : ""}
                        </span>
                      ))}
                    </FormLabel>
                    <FormLabel className="text-warning w-full"></FormLabel>
                    <FormLabel className="text-warning w-full">
                      Change{" "}
                      {overlappingReservations.length == 1 ? "its" : "their"}{" "}
                      vehicle or delete{" "}
                      {overlappingReservations.length == 1 ? "it" : "them"} to
                      set this availability and finalize the reservation. Click
                      the reservation number to manage it.
                    </FormLabel>
                  </>
                )}
              </FormItem>
              <FormField
                disabled={overlappingReservations.length != 0}
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
                disabled={overlappingReservations.length != 0}
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
                disabled={overlappingReservations.length != 0}
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
                disabled={overlappingReservations.length != 0}
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
              <div className="col-span-full flex justify-around items-center gap-6 py-4">
                <FormField
                  disabled={overlappingReservations.length != 0}
                  control={form.control}
                  name="damageLevel"
                  render={({ field, fieldState }) => (
                    <FormItem className="flex flex-col items-center">
                      <FormLabel>Damage Level</FormLabel>
                      <FormControl>
                        <Select
                          name={field.name}
                          value={field.value.toString()}
                          onValueChange={field.onChange}>
                          <SelectTrigger
                            id="form-rhf-select-language"
                            aria-invalid={fieldState.invalid}
                            className="min-w-[120px]">
                            <SelectValue placeholder="Select" />
                          </SelectTrigger>
                          <SelectContent position="item-aligned">
                            {damageAndDirtinessLevelLabels.map(
                              (value, index) => (
                                <SelectItem
                                  key={index}
                                  value={index.toString()}>
                                  {value}
                                </SelectItem>
                              )
                            )}
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  disabled={overlappingReservations.length != 0}
                  control={form.control}
                  name="dirtinessLevel"
                  render={({ field, fieldState }) => (
                    <FormItem className="flex flex-col items-center">
                      <FormLabel>Dirtiness Level</FormLabel>
                      <FormControl>
                        <Select
                          name={field.name}
                          value={field.value.toString()}
                          onValueChange={field.onChange}>
                          <SelectTrigger
                            id="form-rhf-select-language"
                            aria-invalid={fieldState.invalid}
                            className="min-w-[120px]">
                            <SelectValue placeholder="Select" />
                          </SelectTrigger>
                          <SelectContent position="item-aligned">
                            {damageAndDirtinessLevelLabels.map(
                              (value, index) => (
                                <SelectItem
                                  key={index}
                                  value={index.toString()}>
                                  {value}
                                </SelectItem>
                              )
                            )}
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
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
        )}
      </DialogContent>
    </Dialog>
  );
}
