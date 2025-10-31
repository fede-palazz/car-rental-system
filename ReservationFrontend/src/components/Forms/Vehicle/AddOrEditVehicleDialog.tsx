import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { z, ZodType } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { CarModel } from "@/models/CarModel.ts";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { CarStatus, Vehicle } from "@/models/Vehicle";
import { useEffect, useState } from "react";
import { VehicleCreateDTO } from "@/models/dtos/request/VehicleCreateDTO";
import CarModelAPI from "@/API/CarModelsAPI";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import VehiclesAPI from "@/API/VehiclesAPI";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import { VehicleUpdateDTO } from "@/models/dtos/request/VehicleUpdateDTO";
import { toast } from "sonner";

const vehicleCreateSchema = z.object({
  licensePlate: z.string().min(7, "License plate must be 7 chars").max(7),
  vin: z.string().min(17, "Vin must be 17 chars").max(17),
  kmTravelled: z.coerce
    .number({
      invalid_type_error: "Km travelled must be a number",
    })
    .optional(),
  pendingCleaning: z.boolean().optional(),
  carModelId: z.coerce
    .number({
      required_error: "Car model is required",
      invalid_type_error: "Car model must be valid",
    })
    .min(0, "Car model must be greater than zero"),
});

const vehicleUpdateSchema = z.object({
  licensePlate: z
    .string()
    .min(7, "License plate must be 7 chars")
    .max(7)
    .optional(),
  status: z
    .nativeEnum(CarStatus)
    .optional()
    .superRefine((val, ctx) => {
      if (val === undefined) return; // valid, since it's optional
      if (!Object.values(CarStatus).includes(val)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: "Invalid status selected",
        });
      }
    }),
  kmTravelled: z.coerce.number({
    invalid_type_error: "Km travelled must be a number",
  }),
  pendingCleaning: z.boolean(),
});

export default function AddOrEditVehicleDialog() {
  const navigate = useNavigate();
  const location = useLocation();
  const { vehicleId } = useParams<{
    vehicleId: string;
  }>();
  const isEdit = location.pathname.includes("edit");

  const [models, setModels] = useState<CarModel[]>([]);
  const carStatuses = Object.entries(CarStatus).map(([key, value]) => ({
    label: key,
    value,
  }));

  const form = useForm<VehicleCreateDTO | VehicleUpdateDTO>({
    resolver: zodResolver(
      (isEdit
        ? vehicleUpdateSchema
        : vehicleCreateSchema) as unknown as ZodType<
        VehicleCreateDTO | VehicleUpdateDTO
      >
    ),
    defaultValues: {
      licensePlate: "",
      vin: "",
      kmTravelled: undefined,
      pendingCleaning: false,
      carModelId: undefined,
    },
  });

  useEffect(() => {
    //if (location.pathname !== `/reservations/${vehicleId}`) return;
    if (!isEdit) return;
    VehiclesAPI.getVehicleById(Number(vehicleId))
      .then((vehicle: Vehicle) => {
        //setVehicle(vehicle);
        form.reset(
          {
            licensePlate: vehicle.licensePlate,
            kmTravelled: vehicle.kmTravelled,
            pendingCleaning: vehicle.pendingCleaning,
          },
          { keepDefaultValues: true, keepDirtyValues: true }
        );
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }, [vehicleId, location.pathname]);

  const fetchModels = () => {
    CarModelAPI.getAllModels(undefined, undefined, undefined, 0, 1, true)
      .then((models: PagedResDTO<CarModel>) => {
        setModels(models.content);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  };

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    if (isEdit) {
      handleEdit(form.getValues() as VehicleUpdateDTO);
    } else {
      handleCreate(form.getValues() as VehicleCreateDTO);
    }
  }

  const handleEdit = (values: VehicleUpdateDTO) => {
    VehiclesAPI.editVehicleById(values, Number(vehicleId))
      .then(() => {
        toast.success("Vehicle modified successfully");
        navigate(-1);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  };

  const handleCreate = (values: VehicleCreateDTO) => {
    VehiclesAPI.createVehicle(values)
      .then(() => {
        toast.success("Vehicle created successfully");
        navigate(-1);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  };

  useEffect(() => {
    fetchModels();
  }, []);

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
          <DialogTitle>
            {isEdit ? "Edit Vehicle" : "Create Vehicle"}
          </DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            {!isEdit && (
              <FormField
                control={form.control}
                name="carModelId"
                render={({ field }) => (
                  <FormItem className="col-span-full">
                    <FormLabel>Model</FormLabel>
                    <Popover modal>
                      <PopoverTrigger asChild>
                        <FormControl>
                          <Button
                            size="lg"
                            variant="ghost"
                            role="combobox"
                            className={cn(
                              "bg-background text-foreground border border-input font-normal justify-between flex px-1.5",
                              !field.value && " text-muted-foreground"
                            )}>
                            <span className="flex items-center gap-2">
                              <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                                directions_car
                              </span>
                              {field.value && models != undefined
                                ? (models.find(
                                    (model) => model.id == field.value
                                  )?.brand || "") +
                                  " " +
                                  (models.find(
                                    (model) => model.id == field.value
                                  )?.model || "") +
                                  " " +
                                  (models.find(
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
                              {models.map((model) => (
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

                    <FormMessage></FormMessage>
                  </FormItem>
                )}
              />
            )}
            {!isEdit && (
              <FormField
                control={form.control}
                name="vin"
                render={({ field }) => (
                  <FormItem className="col-span-full">
                    <FormLabel>Vin*</FormLabel>
                    <FormControl>
                      <Input
                        startIcon={
                          <span className="material-symbols-outlined items-center md-18">
                            car_tag
                          </span>
                        }
                        placeholder={"Vin"}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}
            <FormField
              control={form.control}
              name="licensePlate"
              render={({ field }) => (
                <FormItem className={isEdit ? "col-span-full" : undefined}>
                  <FormLabel>License Plate{isEdit ? "" : "*"}</FormLabel>
                  <FormControl>
                    <Input
                      startIcon={
                        <span className="material-symbols-outlined items-center md-18">
                          tag
                        </span>
                      }
                      placeholder={"License Plate"}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="kmTravelled"
              render={({ field }) => (
                <FormItem className={isEdit ? "col-span-full" : undefined}>
                  <FormLabel>Km travelled{!isEdit ? "" : "*"}</FormLabel>
                  <FormControl>
                    <Input
                      min={0}
                      inputMode="decimal"
                      pattern="[0-9]*\.?[0-9]*"
                      className="[&::-webkit-inner-spin-button]:appearance-none"
                      startIcon={
                        <span className="material-symbols-outlined items-center md-18">
                          distance
                        </span>
                      }
                      placeholder={"Km travelled"}
                      {...field}
                      onKeyDown={(e) => {
                        // Allow only digits and dot
                        if (
                          !/[0-9.]/.test(e.key) &&
                          e.key !== "Backspace" &&
                          e.key !== "Tab" &&
                          e.key !== "ArrowLeft" &&
                          e.key !== "ArrowRight"
                        ) {
                          e.preventDefault();
                        }
                      }}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="pendingCleaning"
              render={({ field }) => (
                <FormItem className="flex flex-col col-span-full items-center">
                  <FormLabel>Pending Cleaning{!isEdit ? "" : "*"}</FormLabel>
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
                  type="button"
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
                  {isEdit ? "Edit" : "Create"}
                  <span className="material-symbols-outlined  md-18">
                    {isEdit ? "edit" : "add"}
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
