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
import { toast } from "sonner";

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
          <FormItem>
            <FormLabel>Motor Displacement</FormLabel>
            <FormControl>
              <Input
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    speed
                  </span>
                }
                placeholder={"Motor Displacement"}
                {...field}
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

      {/*
      <FormField
        control={control}
        name="featureIds"
        render={({ field }) => (
          <FormItem className="col-span-full">
            <FormLabel>Features</FormLabel>
            <Popover modal={true}>
              <PopoverTrigger asChild>
                <FormControl>
                  <Button
                    variant="outline"
                    role="combobox"
                    className={cn(
                      "justify-between overflow-hidden",
                      !field?.value?.length && "text-muted-foreground"
                    )}>
                    {field?.value?.length > 0
                      ? field.value
                          .map(
                            (id) =>
                              carFeatures.find((feature) => feature.id === id)
                                ?.description
                          )
                          .join(", ")
                      : "Select features"}
                    <ChevronsUpDown className="opacity-50" />
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent modal sideOffset={0} className=" p-0">
                <Command className="w-full">
                  <CommandInput placeholder="Search features" className="h-9" />
                  <CommandList
                    className="overflow-y-auto"
                    style={{
                      scrollbarWidth: "none", // For Firefox
                      msOverflowStyle: "none", // For IE and Edge
                    }}>
                    <CommandEmpty>No features found.</CommandEmpty>
                    <CommandGroup>
                      {carFeatures.map((feature) => (
                        <CommandItem
                          value={feature.description}
                          key={feature.id}
                          onSelect={(e) => {
                            if (field.value.includes(feature.id)) {
                              field.onChange(
                                field.value.filter(
                                  (value) => value !== feature.id
                                )
                              );
                            } else {
                              if (field?.value?.includes(feature.id)) {
                                field.onChange(
                                  field.value?.filter(
                                    (value) => value !== feature.id
                                  )
                                );
                              } else {
                                field.onChange([...field.value, feature.id]);
                              }
                            }
                          }}>
                          {feature.description}
                          <Check
                            className={cn(
                              "ml-auto",
                              field?.value?.includes(feature.id)
                                ? "opacity-100"
                                : "opacity-0"
                            )}
                          />
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
      /> */}
    </>
  );
}

export default EngineInfoForm;
