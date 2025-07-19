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
import { Resolver, useForm } from "react-hook-form";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { CarModel } from "@/models/CarModel.ts";
import { useNavigate, useOutletContext } from "react-router-dom";
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
  kmTravelled: z.coerce
    .number({
      invalid_type_error: "Km travelled must be a number",
    })
    .optional(),
  pendingCleaning: z.boolean().optional(),
  pendingRepair: z.boolean().optional(),
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
      if (!val) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: "Status is required",
        });
      }
      if (val && !Object.values(CarStatus).includes(val)) {
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
  pendingRepair: z.boolean(),
});

export default function AddOrEditVehicleDialog() {
  const navigate = useNavigate();
  const vehicle: Vehicle | undefined = useOutletContext();

  const [models, setModels] = useState<CarModel[]>([]);

  const carStatuses = Object.entries(CarStatus).map(([key, value]) => ({
    label: key,
    value,
  }));

  const form = useForm<VehicleCreateDTO | VehicleUpdateDTO>({
    resolver: zodResolver(
      (vehicle
        ? vehicleUpdateSchema
        : vehicleCreateSchema) as unknown as ZodType<
        VehicleCreateDTO | VehicleUpdateDTO
      >
    ),
    defaultValues: {
      licensePlate: vehicle && vehicle.licensePlate ? vehicle.licensePlate : "",
      vin: vehicle && vehicle.vin ? vehicle.vin : "",
      status: vehicle && vehicle.status ? vehicle.status : CarStatus.AVAILABLE,
      kmTravelled:
        vehicle && vehicle.kmTravelled ? vehicle.kmTravelled : undefined,
      pendingCleaning:
        vehicle && vehicle.pendingCleaning ? vehicle.pendingCleaning : false,
      pendingRepair:
        vehicle && vehicle.pendingRepair ? vehicle.pendingRepair : false,
      carModelId:
        vehicle && vehicle.carModelId ? vehicle.carModelId : undefined,
    },
  });

  const fetchModels = () => {
    CarModelAPI.getAllModels(
      undefined,
      undefined,
      undefined,
      0,
      2147483647 - 1 //Max Kotlin int
    )
      .then((models: PagedResDTO<CarModel>) => {
        setModels(models.content);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    if (vehicle) {
      handleEdit(form.getValues() as VehicleUpdateDTO);
    } else {
      handleCreate(form.getValues() as VehicleCreateDTO);
    }
  }

  const handleEdit = (values: VehicleUpdateDTO) => {
    VehiclesAPI.editVehicleById(values, Number(vehicle!.id))
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleCreate = (values: VehicleCreateDTO) => {
    VehiclesAPI.createVehicle(values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
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
            {vehicle ? "Edit Vehicle" : "Create Vehicle"}
          </DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            {!vehicle && (
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
            <FormField
              control={form.control}
              name="licensePlate"
              render={({ field }) => (
                <FormItem className={vehicle && "col-span-full"}>
                  <FormLabel>License Plate{vehicle ? "" : "*"}</FormLabel>
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
            {!vehicle && (
              <FormField
                control={form.control}
                name="vin"
                render={({ field }) => (
                  <FormItem>
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
              name="kmTravelled"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Km travelled{!vehicle ? "" : "*"}</FormLabel>
                  <FormControl>
                    <Input
                      type="number"
                      startIcon={
                        <span className="material-symbols-outlined items-center md-18">
                          distance
                        </span>
                      }
                      placeholder={"Km travelled"}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="status"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Status{!vehicle ? "" : "*"}</FormLabel>
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
                              adjust
                            </span>
                            {field.value
                              ? carStatuses
                                  .find(
                                    (status) => status.value === field.value
                                  )
                                  ?.label?.charAt(0)
                                  .toUpperCase() +
                                (carStatuses
                                  .find(
                                    (status) => status.value === field.value
                                  )
                                  ?.label?.slice(1)
                                  .toLowerCase() || "")
                              : "Select status"}
                          </span>
                          <span className="material-symbols-outlined items-center md-18">
                            expand_all
                          </span>
                        </Button>
                      </FormControl>
                    </PopoverTrigger>
                    <PopoverContent modal className="p-0 bg-input">
                      <Command>
                        <CommandInput
                          placeholder="Search status"
                          className="h-9"
                        />
                        <CommandList>
                          <CommandEmpty>No status found.</CommandEmpty>
                          <CommandGroup>
                            {carStatuses.map((status) => (
                              <CommandItem
                                value={status.label}
                                key={status.value}
                                onSelect={() => {
                                  field.onChange(status.value);
                                }}>
                                {status.label.charAt(0).toUpperCase() +
                                  status.label.slice(1).toLowerCase()}
                                <span
                                  className={cn(
                                    "ml-auto",
                                    status.value === field.value
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
              control={form.control}
              name="pendingRepair"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Pending Repair{!vehicle ? "" : "*"}</FormLabel>
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
              name="pendingCleaning"
              render={({ field }) => (
                <FormItem className="flex flex-col items-center">
                  <FormLabel>Pending Cleaning{!vehicle ? "" : "*"}</FormLabel>
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
                  {vehicle ? "Edit" : "Create"}
                  <span className="material-symbols-outlined  md-18">
                    {vehicle ? "edit" : "add"}
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
