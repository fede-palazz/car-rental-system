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
import { useNavigate, useOutletContext } from "react-router-dom";
import { Reservation } from "@/models/Reservation";
import { useEffect, useState } from "react";
import { ReservationCreateDTO } from "@/models/dtos/request/ReservationCreateDTO";
import ReservationsAPI from "@/API/ReservationsAPI";
import CarModelAPI from "@/API/CarModelsAPI";
import { CarModel } from "@/models/CarModel";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { DateTimePicker } from "@/components/ui/date-time-picker";

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

export default function AddOrEditReservationDialog() {
  const navigate = useNavigate();

  const reservation:
    | Reservation
    | {
        plannedPickUpDate: Date;
        plannedDropOffDate: Date;
        carModelId: number;
      }
    | undefined = useOutletContext();

  const [availableModels, setAvailableModels] = useState<
    CarModel[] | undefined
  >(undefined);

  const form = useForm<ReservationCreateDTO>({
    resolver: zodResolver(reservationSchema),
    defaultValues: {
      carModelId:
        reservation && reservation.carModelId
          ? reservation.carModelId
          : undefined,
      plannedPickUpDate:
        reservation && reservation.plannedPickUpDate
          ? reservation.plannedPickUpDate
          : undefined,
      plannedDropOffDate:
        reservation && reservation.plannedDropOffDate
          ? reservation.plannedDropOffDate
          : undefined,
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
        .catch((err) => {
          console.log(err);
        });
    }
  };

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
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  useEffect(() => {
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
            <FormField
              control={form.control}
              name="plannedPickUpDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>Planned PickUp Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) => val < new Date()}
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
              name="plannedDropOffDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>Planned Dropoff Date</FormLabel>
                  <DateTimePicker
                    modalPopover
                    className="!bg-background overflow-hidden"
                    calendarDisabled={(val) =>
                      val < new Date() ||
                      form.getValues("plannedPickUpDate") > val
                    }
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
            {/* <FormField
              control={form.control}
              name="carModelId"
              render={({ field }) => (
                <FormItem className="col-span-full">
                  <FormLabel>Model</FormLabel>
                  <Popover modal>
                    <PopoverTrigger asChild>
                      <FormControl>
                        <Button
                          disabled={
                            !form.getValues("plannedPickUpDate") ||
                            !form.getValues("plannedDropOffDate")
                          }
                          size="lg"
                          variant="ghost"
                          role="combobox"
                          className={cn(
                            "bg-background text-muted-foreground border border-input font-normal justify-between flex px-1.5",
                            !field.value && " text-muted-foreground"
                          )}>
                          <span className="flex items-center text-foreground gap-2">
                            <span className="material-symbols-outlined items-center md-18">
                              directions_car
                            </span>
                            {field.value && availableModels != undefined
                              ? (availableModels.find(
                                  (model) => model.id == field.value
                                )?.brand || "") +
                                " " +
                                (availableModels.find(
                                  (model) => model.id == field.value
                                )?.model || "") +
                                " " +
                                (availableModels.find(
                                  (model) => model.id == field.value
                                )?.year || "")
                              : "Select model"}
                          </span>
                          <span className="material-symbols-outlined items-center md-18">
                            expand_all
                          </span>
                        </Button>
                      </FormControl>
                    </PopoverTrigger>
                    <PopoverContent className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search model"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>No model found.</CommandEmpty>
                          <CommandGroup>
                            {availableModels.map((model) => (
                              <CommandItem
                                value={model.id.toString()}
                                key={model.id}
                                onSelect={() => {
                                  field.onChange(model.id);
                                }}>
                                {model.brand +
                                  " " +
                                  model.model +
                                  " " +
                                  model.year}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    model.id === field.value
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
                  <FormDescription>
                    {!form.getValues("plannedPickUpDate") ||
                    !form.getValues("plannedDropOffDate")
                      ? "Select pickup and dropoff dates before"
                      : ""}
                  </FormDescription>
                  <FormMessage>
                   
                  </FormMessage>
                </FormItem>
              )}
            />*/}
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
                    )
                  }
                  onClick={(event) => {
                    event.stopPropagation();
                    onSubmit();
                  }}>
                  {"Create"}
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
