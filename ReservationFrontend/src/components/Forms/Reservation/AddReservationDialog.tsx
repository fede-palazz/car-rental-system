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
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import { ReservationCreateDTO } from "@/models/dtos/request/ReservationCreateDTO";
import ReservationsAPI from "@/API/ReservationsAPI";
import CarModelAPI from "@/API/CarModelsAPI";
import { CarModel } from "@/models/CarModel";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { DateTimePicker } from "@/components/ui/date-time-picker";
import { toast } from "sonner";
import UserContext from "@/contexts/UserContext";
import { UserRole } from "@/models/enums/UserRole";
import { Reservation } from "@/models/Reservation";

const reservationSchema = z
  .object({
    carModelId: z.coerce
      .number({
        required_error: "Model Id is required",
        invalid_type_error: "Model Id must be a number",
      })
      .min(1, "Model Id must be greater than zero"),
    plannedPickUpDate: z
      .date({
        required_error: "Planned Pick-Up Date is required",
        invalid_type_error: "Planned Pick-Up Date must be a valid date",
      })
      .refine((date) => date >= new Date(), {
        message: "Planned Pick-Up Date cannot be in the past",
      }),
    plannedDropOffDate: z
      .date({
        required_error: "Planned Dropoff Date is required",
        invalid_type_error: "Planned Dropoff Date must be a valid date",
      })
      .refine((date) => date >= new Date(), {
        message: "Planned DropOff Date cannot be in the past",
      }),
  })
  .refine((data) => data.plannedDropOffDate > data.plannedPickUpDate, {
    path: ["plannedDropOffDate"],
    message: "Drop-off date must be after pick-up date",
  });

export default function AddReservationDialog({
  plannedPickUpDate,
  plannedDropOffDate,
}: {
  plannedPickUpDate: Date | undefined;
  plannedDropOffDate: Date | undefined;
}) {
  const user = useContext(UserContext);
  const navigate = useNavigate();
  const { carModelId } = useParams<{
    carModelId: string;
  }>();

  const [availableModels, setAvailableModels] = useState<
    CarModel[] | undefined
  >(undefined);

  const [hasPendingReservation, setHasPendingReservation] = useState<
    boolean | undefined
  >(undefined);

  const form = useForm<ReservationCreateDTO>({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      carModelId: carModelId ? Number(carModelId) : undefined,
      plannedPickUpDate: plannedPickUpDate ? plannedPickUpDate : undefined,
      plannedDropOffDate: plannedDropOffDate ? plannedDropOffDate : undefined,
    },
  });

  const fetchAvailableModels = () => {
    if (
      !form.getValues("plannedPickUpDate") ||
      !form.getValues("plannedDropOffDate")
    ) {
      return;
    } else {
      CarModelAPI.getAvailableModels(
        {
          from: form.getValues("plannedPickUpDate"),
          to: form.getValues("plannedDropOffDate"),
        },
        undefined,
        undefined,
        undefined,
        0,
        2147483647 - 1, //Max Kotlin int
        true
      )
        .then((models: PagedResDTO<CarModel>) => {
          setAvailableModels(models.content);
        })
        .catch((err: Error) => {
          toast.error(err.message);
        });
    }
  };

  //TODO, does it return always the pending one?
  function checkPendingReservation() {
    if (user != undefined && user.role == UserRole.CUSTOMER) {
      ReservationsAPI.getPendingReservation()
        .then((res: PagedResDTO<Reservation>) => {
          const pending = res.content[0];
          setHasPendingReservation(pending !== undefined);
        })
        .catch((err: Error) => {
          toast.error(err.message);
        });
    }
  }

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    handleCreate(form.getValues());
  }

  const handleCreate = (values: ReservationCreateDTO) => {
    ReservationsAPI.createReservation(values)
      .then(() => {
        toast.success("Reservation successfull");
        navigate("/reservations");
      })
      .catch((err: Error) => {
        toast.error(err.message);
        navigate(-1);
      });
  };

  useEffect(() => {
    checkPendingReservation();
    fetchAvailableModels();
    const subscription = form.watch((values) => {
      const { plannedPickUpDate, plannedDropOffDate } = values;

      if (plannedPickUpDate && plannedDropOffDate) {
        fetchAvailableModels();
      }
    });

    // Cleanup
    return () => subscription.unsubscribe();
  }, []);

  return (
    <Dialog
      open={true}
      onOpenChange={(isOpen) => {
        if (!isOpen) navigate(-1);
      }}>
      <DialogDescription></DialogDescription>
      <DialogContent
        className="sm:max-w-[425px] top-1/3 md:min-w-1/2 overflow-auto max-h-11/12"
        style={{
          scrollbarWidth: "none", // For Firefox
          msOverflowStyle: "none", // For IE and Edge
        }}>
        <DialogHeader>
          <DialogTitle>Create Reservation</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <FormItem className="flex flex-col col-span-full w-full gap-2 items-center text-center">
              {hasPendingReservation && (
                <FormLabel className="text-warning w-full">
                  You cannot do another reservation while you have a pending
                  one.
                  <span>
                    <Link
                      to={`/reservations`}
                      className="underline text-warning">
                      {" "}
                      Pay or cancel{" "}
                    </Link>
                    the pending one before
                  </span>
                </FormLabel>
              )}
            </FormItem>
            <FormField
              control={form.control}
              disabled={hasPendingReservation !== false}
              name="plannedPickUpDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>Planned PickUp Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      const plannedDropOffDate =
                        form.getValues("plannedDropOffDate");
                      // Disable dates before today and after plannedDropOffDate (if set)
                      return (
                        val < new Date() ||
                        (plannedDropOffDate ? val > plannedDropOffDate : false)
                      );
                    }}
                    defaultPopupValue={field.value ? field.value : new Date()}
                    placeholder="Planned PickUp Date"
                    value={field.value}
                    onChange={field.onChange}
                    granularity="minute"
                  />
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              disabled={hasPendingReservation !== false}
              name="plannedDropOffDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>Planned Dropoff Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => {
                      const plannedPickUp = form.getValues("plannedPickUpDate");
                      return (
                        val < new Date() ||
                        (plannedPickUp ? val < plannedPickUp : false)
                      );
                    }}
                    defaultPopupValue={field.value ? field.value : new Date()}
                    placeholder="Planned DropOff Date"
                    value={field.value}
                    onChange={field.onChange}
                    granularity="minute"
                  />
                  <FormDescription></FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <span className="text-destructive text-sm">
              {availableModels &&
                !availableModels.find(
                  (model) => model.id == form.getValues("carModelId")
                ) &&
                "The selected model is not available during these dates"}
            </span>
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
                  disabled={
                    !availableModels?.find(
                      (model) => model.id == form.getValues("carModelId")
                    ) || hasPendingReservation !== false
                  }
                  onClick={(event) => {
                    event.stopPropagation();
                    onSubmit();
                  }}>
                  {"Reserve"}
                  <span className="material-symbols-outlined  md-18">
                    {"add"}
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
