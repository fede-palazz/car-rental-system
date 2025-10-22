import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormDescription,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { CarSegment } from "@/models/enums/CarSegment";
import { Control } from "react-hook-form";
import DefaultCar from "@/assets/defaultCarModel.png";

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
import { CarCategory } from "@/models/enums/CarCategory";

function ModelInfoForm({ control }: { control: Control }) {
  const carSegments = Object.entries(CarSegment).map(([key, value]) => ({
    label: key,
    value,
  }));

  const carCategories = Object.entries(CarCategory).map(([key, value]) => ({
    label: key,
    value,
  }));

  return (
    <>
      <div className="flex-grow flex items-center border-input border-6 rounded-md justify-center">
        <img src={DefaultCar} className="py-4" />
      </div>
      <div className="gap-y-2 flex flex-col">
        <FormField
          control={control}
          name="brand"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Brand*</FormLabel>
              <FormControl>
                <Input
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      brand_family
                    </span>
                  }
                  placeholder={"Brand"}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={control}
          name="model"
          render={({ field }) => (
            <FormItem className="col-span-full">
              <FormLabel>Model*</FormLabel>
              <FormControl>
                <Input
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      directions_car
                    </span>
                  }
                  placeholder={"Model"}
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={control}
          name="year"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Year*</FormLabel>
              <FormControl>
                <Input
                  min={1950}
                  max={new Date().getFullYear()}
                  inputMode="numeric"
                  placeholder={"Year"}
                  pattern="\d{4}"
                  maxLength={4}
                  className="[&::-webkit-inner-spin-button]:appearance-none"
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      calendar_today
                    </span>
                  }
                  {...field}
                  onKeyDown={(e) => {
                    // Allow only digits, navigation, and editing keys
                    if (
                      !/[0-9]/.test(e.key) &&
                      e.key !== "Backspace" &&
                      e.key !== "Tab" &&
                      e.key !== "ArrowLeft" &&
                      e.key !== "ArrowRight"
                    ) {
                      e.preventDefault();
                    }
                    // Prevent entering more than 4 digits
                    if (
                      /[0-9]/.test(e.key) &&
                      e.currentTarget.value.length >= 4 &&
                      !e.currentTarget.selectionStart &&
                      !e.currentTarget.selectionEnd
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
      </div>
      <FormField
        control={control}
        name="doorsNumber"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Doors Number*</FormLabel>
            <FormControl>
              <Input
                className="[&::-webkit-inner-spin-button]:appearance-none"
                min={1}
                inputMode="numeric"
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    door_open
                  </span>
                }
                placeholder={"Doors Number"}
                {...field}
                onKeyDown={(e) => {
                  // Allow only digits, navigation, and editing keys
                  if (
                    !/[0-9]/.test(e.key) &&
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
        name="seatingCapacity"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Seating Capacity*</FormLabel>
            <FormControl>
              <Input
                className="[&::-webkit-inner-spin-button]:appearance-none"
                min={1}
                inputMode="numeric"
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    chair
                  </span>
                }
                placeholder={"Seating Capacity"}
                {...field}
                onKeyDown={(e) => {
                  // Allow only digits, navigation, and editing keys
                  if (
                    !/[0-9]/.test(e.key) &&
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
        name="luggageCapacity"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Luggage Capacity*</FormLabel>
            <FormControl>
              <Input
                className="[&::-webkit-inner-spin-button]:appearance-none"
                min={1}
                inputMode="numeric"
                startIcon={
                  <span className="material-symbols-outlined items-center md-18">
                    luggage
                  </span>
                }
                placeholder={"Luggage Capacity"}
                {...field}
                onKeyDown={(e) => {
                  // Allow only digits, navigation, and editing keys
                  if (
                    !/[0-9]/.test(e.key) &&
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
        name="category"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Category</FormLabel>
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
                        category
                      </span>
                      {field.value
                        ? carCategories
                            .find((category) => category.value === field.value)
                            ?.label?.charAt(0)
                            .toUpperCase() +
                          (carCategories
                            .find((category) => category.value === field.value)
                            ?.label?.slice(1)
                            .toLowerCase() || "")
                        : "Select category"}
                    </span>
                    <span className="material-symbols-outlined items-center md-18">
                      expand_all
                    </span>
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent modal className="p-0 bg-input">
                <Command>
                  <CommandInput placeholder="Search segment" className="h-9" />
                  <CommandList>
                    <CommandEmpty>No category found.</CommandEmpty>
                    <CommandGroup>
                      {carCategories.map((category) => (
                        <CommandItem
                          value={category.label}
                          key={category.value}
                          onSelect={() => {
                            field.onChange(category.value);
                          }}>
                          {category.label.charAt(0).toUpperCase() +
                            category.label.slice(1).toLowerCase()}
                          <span
                            className={cn(
                              "ml-auto",
                              category.value === field.value
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
        name="segment"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Segment</FormLabel>
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
                      <span className="material-symbols-outlined items-center text-muted-foreground md-18">
                        commute
                      </span>
                      {field.value
                        ? carSegments
                            .find((segment) => segment.value === field.value)
                            ?.label?.charAt(0)
                            .toUpperCase() +
                          (carSegments
                            .find((segment) => segment.value === field.value)
                            ?.label?.slice(1)
                            .toLowerCase() || "")
                        : "Select segment"}
                    </span>
                    <span className="material-symbols-outlined items-center md-18">
                      expand_all
                    </span>
                  </Button>
                </FormControl>
              </PopoverTrigger>
              <PopoverContent modal className="p-0 bg-input">
                <Command>
                  <CommandInput placeholder="Search segment" className="h-9" />
                  <CommandList>
                    <CommandEmpty>No segment found.</CommandEmpty>
                    <CommandGroup>
                      {carSegments.map((segment) => (
                        <CommandItem
                          value={segment.label}
                          key={segment.value}
                          onSelect={() => {
                            field.onChange(segment.value);
                          }}>
                          {segment.label.charAt(0).toUpperCase() +
                            segment.label.slice(1).toLowerCase()}
                          <span
                            className={cn(
                              "ml-auto",
                              segment.value === field.value
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
            <FormDescription className="text-background">""</FormDescription>
            <FormMessage />
          </FormItem>
        )}
      />
      {
        <FormField
          control={control}
          name="rentalPrice"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Rental Price*</FormLabel>
              <FormControl>
                <Input
                  min={0}
                  inputMode="decimal"
                  pattern="[0-9]*\.?[0-9]*"
                  className="[&::-webkit-inner-spin-button]:appearance-none"
                  startIcon={
                    <span className="material-symbols-outlined items-center md-18">
                      payments
                    </span>
                  }
                  placeholder={"Rental Price"}
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
              <FormDescription>Per day</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
      }
    </>
  );
}

export default ModelInfoForm;
