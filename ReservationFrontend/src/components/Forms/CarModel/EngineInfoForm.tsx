import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Control } from "react-hook-form";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { EngineType } from "@/models/enums/EngineType";
import { Drivetrain } from "@/models/enums/Drivetrain";
import { TransmissionType } from "@/models/enums/TransmissionType";
import { useEffect, useState } from "react";
import { CarFeature } from "@/models/CarFeature";
import CarModelAPI from "@/API/CarModelsAPI";
import { MultiSelect } from "@/components/ui/multi-select";

function EngineInfoForm({ control }: { control: Control }) {
  const engineTypes = Object.entries(EngineType).map(([key, value]) => ({
    label: key,
    value,
  }));
  const drivetrains = Object.entries(Drivetrain).map(([key, value]) => ({
    label: key,
    value,
  }));
  const transmissionTypes = Object.entries(TransmissionType).map(
    ([key, value]) => ({
      label: key,
      value,
    })
  );
  const [carFeatures, setCarFeatures] = useState<CarFeature[]>([]);

  useEffect(() => {
    CarModelAPI.getCarFeatures()
      .then((features) => {
        setCarFeatures(features);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);

  return (
    <>
      <FormField
        control={control}
        name="engineType"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Engine Type*</FormLabel>
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
                    <span className={cn("flex items-center gap-2")}>
                      <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                        car_gear
                      </span>
                      {field.value
                        ? engineTypes
                            .find((type) => type.value === field.value)
                            ?.label?.charAt(0)
                            .toUpperCase() +
                          (engineTypes
                            .find((type) => type.value === field.value)
                            ?.label?.slice(1)
                            .toLowerCase() || "")
                        : "Select engine type"}
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
                    placeholder="Search engine type"
                    className="h-9"
                  />
                  <CommandList>
                    <CommandEmpty>No types found.</CommandEmpty>
                    <CommandGroup>
                      {engineTypes.map((type) => (
                        <CommandItem
                          value={type.label}
                          key={type.value}
                          onSelect={() => {
                            field.onChange(type.value);
                          }}>
                          {type.label.charAt(0).toUpperCase() +
                            type.label.slice(1).toLowerCase()}
                          <span
                            className={cn(
                              "ml-auto",
                              type.value === field.value
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
        control={control}
        name="transmissionType"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Transmission Type*</FormLabel>
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
                        auto_transmission
                      </span>
                      {field.value
                        ? transmissionTypes
                            .find((type) => type.value === field.value)
                            ?.label?.charAt(0)
                            .toUpperCase() +
                          (transmissionTypes
                            .find((type) => type.value === field.value)
                            ?.label?.slice(1)
                            .toLowerCase() || "")
                        : "Select transmission type"}
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
                    placeholder="Search transmission type"
                    className="h-9"
                  />
                  <CommandList>
                    <CommandEmpty>No types found.</CommandEmpty>
                    <CommandGroup>
                      {transmissionTypes.map((type) => (
                        <CommandItem
                          value={type.label}
                          key={type.value}
                          onSelect={() => {
                            field.onChange(type.value);
                          }}>
                          {type.label.charAt(0).toUpperCase() +
                            type.label.slice(1).toLowerCase()}
                          <span
                            className={cn(
                              "ml-auto",
                              type.value === field.value
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
        control={control}
        name="drivetrain"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Drivetrain*</FormLabel>
            <Popover>
              <PopoverTrigger asChild>
                <FormControl>
                  <Button
                    size="lg"
                    variant="ghost"
                    role="combobox"
                    className={cn(
                      "text-foreground border border-input font-normal justify-between flex px-1.5",
                      !field.value && " text-muted-foreground"
                    )}>
                    <span className="flex items-center gap-2">
                      <span className="material-symbols-outlined items-center md-18 text-muted-foreground">
                        component_exchange
                      </span>
                      {field.value
                        ? drivetrains.find(
                            (drivetrain) => drivetrain.value === field.value
                          )?.label
                        : "Select drivetrain"}
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
                    placeholder="Search drivetrain"
                    className="h-9"
                  />
                  <CommandList>
                    <CommandEmpty>No drivetrain found.</CommandEmpty>
                    <CommandGroup>
                      {drivetrains.map((drivetrain) => (
                        <CommandItem
                          value={drivetrain.label}
                          key={drivetrain.value}
                          onSelect={() => {
                            field.onChange(drivetrain.value);
                          }}>
                          {drivetrain.label}
                          <span
                            className={cn(
                              "ml-auto",
                              drivetrain.value === field.value
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
        control={control}
        name="motorDisplacement"
        render={({ field }) => (
          <FormItem key={`motorDisplacement-${"edit"}`}>
            <FormLabel>Motor Displacement</FormLabel>
            <FormControl key={`motorDisplacement-${"edit"}`}>
              <Input
                key={`motorDisplacement-${"edit"}`}
                inputMode="decimal"
                pattern="[0-9]*\.?[0-9]*"
                className="[&::-webkit-inner-spin-button]:appearance-none"
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    speed
                  </span>
                }
                placeholder={"Motor Displacement"}
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
        control={control}
        name="featureIds"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Features</FormLabel>
            <FormControl>
              <MultiSelect
                options={carFeatures.map((feature) => {
                  return {
                    label: feature.description,
                    value: feature.id.toString(),
                  };
                })}
                defaultValue={
                  field.value?.map((id: number) => id.toString()) || []
                }
                startIcon="extension"
                modalPopover
                onValueChange={(values) => {
                  field.onChange(values.map((value) => parseInt(value)));
                }}
                placeholder="Select features"
                variant="inverted"
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </>
  );
}

export default EngineInfoForm;
